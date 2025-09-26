/*******************************************************************************
 * Copyright (c) 2011, 2025 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.raspberrypi.sensehat.example;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.raspberrypi.sensehat.SenseHat;
import org.eclipse.kura.raspberrypi.sensehat.joystick.Joystick;
import org.eclipse.kura.raspberrypi.sensehat.joystick.JoystickEvent;
import org.eclipse.kura.raspberrypi.sensehat.ledmatrix.Colors;
import org.eclipse.kura.raspberrypi.sensehat.ledmatrix.FrameBuffer;
import org.eclipse.kura.raspberrypi.sensehat.sensors.HTS221;
import org.eclipse.kura.raspberrypi.sensehat.sensors.LPS25H;
import org.eclipse.kura.raspberrypi.sensehat.sensors.LSM9DS1;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, //
        configurationPolicy = ConfigurationPolicy.REQUIRE, //
        service = { ConfigurableComponent.class }, //
        enabled = true, //
        name = "org.eclipse.kura.raspberrypi.sensehat.example.SenseHatExample" //
)
@Designate(ocd = SenseHatExampleOCD.class, factory = false)
public class SenseHatExample implements ConfigurableComponent {

    private static final Logger s_logger = LoggerFactory.getLogger(SenseHatExample.class);

    private static final int I2C_BUS = 1;
    private static final int I2C_ADDRESS_SIZE = 7;
    private static final int I2C_FREQUENCY = 400000;
    private static final int I2C_ACC_ADDRESS = 0x6A;
    private static final int I2C_MAG_ADDRESS = 0x1C;
    private static final int I2C_PRE_ADDRESS = 0x5C;
    private static final int I2C_HUM_ADDRESS = 0x5F;

    private Joystick senseHatJoystick;
    private JoystickEvent je;
    private boolean runThread;

    private FrameBuffer frameBuffer;

    private ScheduledExecutorService joystickworker;
    private Future<?> joystickhandle;

    private SenseHat senseHat;

    private LSM9DS1 imuSensor; // Inertial Measurement Unit (Accelerometer, Gyroscope, Magnetometer)
    private LPS25H pressureSensor; // Atmospheric Pressure
    private HTS221 humiditySensor; // Humidity

    private static ScheduledFuture<?> startUpdateThread;
    private ScheduledThreadPoolExecutor executor;

    // ----------------------------------------------------------------
    //
    // Dependencies
    //
    // ----------------------------------------------------------------

    public SenseHatExample() {
        super();
    }

    public void setSenseHatService(SenseHat senseHat) {
        this.senseHat = senseHat;
    }

    public void unsetSenseHatService(SenseHat senseHat) {
        this.senseHat = null;
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------

    @Activate
    protected void activate(SenseHatExampleOCD ocd) {
        s_logger.info("Activating Sense Hat Application...");

        this.executor = new ScheduledThreadPoolExecutor(1);
        this.executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        if (startUpdateThread != null) {
            startUpdateThread.cancel(true);
            startUpdateThread = null;
        }
        startUpdateThread = this.executor.schedule(new Runnable() {

            @Override
            public void run() {
                update(ocd);
            }
        }, 0, TimeUnit.MILLISECONDS);

        s_logger.info("Activating Sense Hat Application... Done.");
    }

    @Deactivate
    protected void deactivate() {
        s_logger.info("Deactivating Sense Hat Application...");

        LPS25H.closeDevice();
        HTS221.closeDevice();
        LSM9DS1.closeDevice();

        if (this.joystickhandle != null) {
            this.joystickhandle.cancel(true);
        }
        if (this.joystickworker != null) {
            this.joystickworker.shutdown();
        }
        if (this.senseHatJoystick != null) {
            Joystick.closeJoystick();
        }

        if (this.frameBuffer != null) {
            this.frameBuffer.clearFrameBuffer();
            FrameBuffer.closeFrameBuffer();
            this.frameBuffer = null;
        }

        if (startUpdateThread != null) {
            startUpdateThread.cancel(true);
            startUpdateThread = null;
        }
        this.executor = null;

        s_logger.info("Deactivating Sense Hat Application... Done.");
    }

    @Modified
    public void updated(SenseHatExampleOCD ocd) {
        s_logger.info("Updated Sense Hat Application...");

        if (startUpdateThread != null) {
            startUpdateThread.cancel(true);
            startUpdateThread = null;
        }
        startUpdateThread = this.executor.schedule(new Runnable() {

            @Override
            public void run() {
                update(ocd);
            }
        }, 0, TimeUnit.MILLISECONDS);

        s_logger.info("Updated Sense Hat Application... Done.");
    }

    // ----------------------------------------------------------------
    //
    // Private Methods
    //
    // ----------------------------------------------------------------

    private void update(SenseHatExampleOCD ocd) {
        if (ocd.imu_accelerometer_enable() || ocd.imu_gyroscope_enable() || ocd.imu_compass_enable()) {

            this.imuSensor = this.senseHat.getIMUSensor(I2C_BUS, I2C_ACC_ADDRESS, I2C_MAG_ADDRESS, I2C_ADDRESS_SIZE,
                    I2C_FREQUENCY);
            boolean status = this.imuSensor.initDevice(ocd.imu_accelerometer_enable(), ocd.imu_gyroscope_enable(),
                    ocd.imu_compass_enable());
            if (!status) {
                s_logger.error("Unable to initialize IMU sensor.");
            } else {
                if (ocd.imu_accelerometer_enable()) {
                    float[] acc = new float[3];
                    for (int i = 0; i < ocd.imu_sample_number(); i++) {
                        acc = this.imuSensor.getAccelerometerRaw();
                    }
                    s_logger.info("Acceleration X : " + acc[0] + " Y : " + acc[1] + " Z : " + acc[2]);
                }
                if (ocd.imu_gyroscope_enable()) {
                    float[] gyro = new float[3];
                    for (int i = 0; i < ocd.imu_sample_number(); i++) {
                        gyro = this.imuSensor.getGyroscopeRaw();
                    }
                    s_logger.info("Orientation X : " + gyro[0] + " Y : " + gyro[1] + " Z : " + gyro[2]);
                }
                if (ocd.imu_compass_enable()) {
                    float[] comp = new float[3];
                    for (int i = 0; i < ocd.imu_sample_number(); i++) {
                        comp = this.imuSensor.getCompassRaw();
                    }
                    s_logger.info("Compass X : " + comp[0] + " Y : " + comp[1] + " Z : " + comp[2]);
                }
            }
        } else {
            LSM9DS1.closeDevice();
        }

        if (ocd.pressure_enable()) {

            this.pressureSensor = this.senseHat.getPressureSensor(I2C_BUS, I2C_PRE_ADDRESS, I2C_ADDRESS_SIZE,
                    I2C_FREQUENCY);
            boolean status = this.pressureSensor.initDevice();
            if (!status) {
                s_logger.error("Unable to initialize pressure sensor.");
            } else {
                s_logger.info("Pressure : {}", this.pressureSensor.getPressure());
                s_logger.info("Temperature : {}", this.pressureSensor.getTemperature());
            }

        } else {
            LPS25H.closeDevice();
        }

        if (ocd.humidity_enable()) {

            this.humiditySensor = this.senseHat.getHumiditySensor(I2C_BUS, I2C_HUM_ADDRESS, I2C_ADDRESS_SIZE,
                    I2C_FREQUENCY);
            boolean status = this.humiditySensor.initDevice();
            if (!status) {
                s_logger.error("Unable to initialize humidity sensor.");
            } else {
                s_logger.info("Humidity : {}", this.humiditySensor.getHumidity());
                s_logger.info("Temperature : {}", this.humiditySensor.getTemperature());
            }

        } else {
            HTS221.closeDevice();
        }

        if (ocd.lcd_screen_enabled()) {

            this.frameBuffer = this.senseHat.getFrameBuffer();
            FrameBuffer.setRotation(ocd.screen_rotation());
            this.frameBuffer.showMessage(ocd.screen_message(), Colors.fromColorString(ocd.screen_text_color()),
                    Colors.BLACK);

        } else {
            if (this.frameBuffer != null) {
                this.frameBuffer.clearFrameBuffer();
                FrameBuffer.closeFrameBuffer();
                this.frameBuffer = null;
            }
        }

        if (ocd.stick_enable()) {

            this.senseHatJoystick = this.senseHat.getJoystick();
            this.runThread = true;

            this.joystickworker = Executors.newSingleThreadScheduledExecutor();
            this.joystickhandle = this.joystickworker.submit(new Runnable() {

                @Override
                public void run() {

                    while (SenseHatExample.this.runThread) {
                        SenseHatExample.this.je = SenseHatExample.this.senseHatJoystick.read();
                        logJoystick(SenseHatExample.this.je);
                    }

                }
            });

        } else {
            this.runThread = false;
            if (this.joystickhandle != null) {
                this.joystickhandle.cancel(true);
            }
            if (this.joystickworker != null) {
                this.joystickworker.shutdownNow();
            }
            if (this.senseHatJoystick != null) {
                Joystick.closeJoystick();
            }
        }
    }

    private void logJoystick(JoystickEvent je) {

        if (je.getCode() == Joystick.KEY_ENTER) {
            if (je.getValue() == Joystick.STATE_PRESS) {
                s_logger.info("Enter key pressed.");
            } else if (je.getValue() == Joystick.STATE_RELEASE) {
                s_logger.info("Enter key released.");
            } else if (je.getValue() == Joystick.STATE_HOLD) {
                s_logger.info("Enter key held.");
            }
        } else if (je.getCode() == Joystick.KEY_LEFT) {
            if (je.getValue() == Joystick.STATE_PRESS) {
                s_logger.info("Lef key pressed.");
            } else if (je.getValue() == Joystick.STATE_RELEASE) {
                s_logger.info("Left key released.");
            } else if (je.getValue() == Joystick.STATE_HOLD) {
                s_logger.info("Left key held.");
            }
        } else if (je.getCode() == Joystick.KEY_RIGHT) {
            if (je.getValue() == Joystick.STATE_PRESS) {
                s_logger.info("Right key pressed.");
            } else if (je.getValue() == Joystick.STATE_RELEASE) {
                s_logger.info("Right key released.");
            } else if (je.getValue() == Joystick.STATE_HOLD) {
                s_logger.info("Right key held.");
            }
        } else if (je.getCode() == Joystick.KEY_UP) {
            if (je.getValue() == Joystick.STATE_PRESS) {
                s_logger.info("Up key pressed.");
            } else if (je.getValue() == Joystick.STATE_RELEASE) {
                s_logger.info("Up key released.");
            } else if (je.getValue() == Joystick.STATE_HOLD) {
                s_logger.info("Up key held.");
            }
        }
        if (je.getCode() == Joystick.KEY_DOWN) {
            if (je.getValue() == Joystick.STATE_PRESS) {
                s_logger.info("Down key pressed.");
            } else if (je.getValue() == Joystick.STATE_RELEASE) {
                s_logger.info("Down key released.");
            } else if (je.getValue() == Joystick.STATE_HOLD) {
                s_logger.info("Down key held.");
            }
        }

    }

}
