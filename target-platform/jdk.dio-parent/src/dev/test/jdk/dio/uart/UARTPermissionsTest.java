/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package dio.uart;

import com.sun.javatest.Status;
import com.sun.javatest.Test;
import static dio.shared.TestBase.STATUS_OK;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import jdk.dio.uart.UART;

/**
 *
 * @test
 * @sources UARTPermissionsTest.java
 * @executeClass dio.uart.UARTPermissionsTest
 *
 * @title UART permission verification
 *
 * @author stanislav.smirnov@oracle.com
 */
public class UARTPermissionsTest extends UARTTestBase implements Test {

    /**
     * Standard command-line entry point.
     *
     * @param args command line args (ignored)
     */
    public static void main(String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        Test t = new UARTPermissionsTest();
        Status s = t.run(args, null, err);
        s.exit();
    }

    /**
     * Main test method. The test consists of a series of test cases; the test
     * passes only if all the individual test cases pass.
     *
     * @param args ignored
     * @param out ignored
     * @param err a stream to which to write details about test failures
     * @return a Status object indicating if the test passed or failed
     */
    @Override
    public Status run(String[] args, PrintWriter out, PrintWriter err) {
        if(!decodeConfig(args)){
            return printFailedStatus("Error occured while decoding input arguments");
        }

        return (testPermissions().getType() == Status.FAILED
                ? printFailedStatus("some test cases failed") : Status.passed("OK"));
    }

    private Status testPermissions() {
        Status result = Status.passed(STATUS_OK);
        start("Trying to open device when do not have enough Security permissions.");
        Iterator<Config> devicesList = UARTDevices.iterator();
        while (devicesList.hasNext()) {
            Config uartConfig = devicesList.next();
            try (UART device = uartConfig.open()) {
                result = printFailedStatus("UART device was opened without required permissions");
            } catch (SecurityException ex) {
                System.out.println("Security exception was caught as expected.");
            } catch (IOException ex) {
                result = printFailedStatus("Unexpected IOException: " + ex.getClass().getName() + ":" + ex.getMessage());
            }
        }

        stop();
        return result;
    }

}
