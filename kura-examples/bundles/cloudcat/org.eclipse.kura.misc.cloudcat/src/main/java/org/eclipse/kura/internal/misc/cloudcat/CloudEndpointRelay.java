/*******************************************************************************
 * Copyright (c) 2017, 2026 Eurotech and/or its affiliates and others
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
package org.eclipse.kura.internal.misc.cloudcat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloudconnection.CloudEndpoint;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.subscriber.listener.CloudSubscriberListener;
import org.eclipse.kura.message.KuraPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relays the messages received by a source {@link CloudEndpoint} on behalf of an application ID to a target
 * {@link CloudEndpoint} on behalf of another application ID, preserving the application topic.
 * <p>
 * The subscription and publishing properties used here are the ones understood by the Kapua cloud connection
 * (org.eclipse.kura.cloud.CloudService factory).
 */
public class CloudEndpointRelay {

    private static final Logger logger = LoggerFactory.getLogger(CloudEndpointRelay.class);

    // Keys of the subscription and message properties understood by the Kapua cloud connection.
    private static final String APP_ID = "APP_ID";
    private static final String APP_TOPIC = "APP_TOPIC";
    private static final String QOS = "QOS";
    private static final String RETAIN = "RETAIN";
    private static final String PRIORITY = "PRIORITY";
    private static final String CONTROL = "CONTROL";

    // Key of the application topic property set by the Kapua cloud connection on the received messages.
    private static final String RECEIVED_APP_TOPIC = "appTopic";

    private static final int DFLT_PRIORITY = 5;

    private static final String FORWARDED_MESSAGE_METRIC_NAME = "_fwd";

    private final CloudEndpoint sourceEndpoint;
    private final String sourceAppId;
    private final CloudEndpoint targetEndpoint;
    private final String targetAppId;
    private final List<CloudCatSubscription> dataSubscriptions;
    private final List<CloudCatSubscription> controlSubscriptions;

    private final List<CloudSubscriberListener> registeredListeners = new ArrayList<>();

    public CloudEndpointRelay(
            CloudEndpoint sourceEndpoint,
            String sourceAppId,
            CloudEndpoint targetEndpoint,
            String targetAppId,
            List<CloudCatSubscription> dataSubscriptions,
            List<CloudCatSubscription> controlSubscriptions) {
        this.sourceEndpoint = sourceEndpoint;
        this.sourceAppId = sourceAppId;
        this.targetEndpoint = targetEndpoint;
        this.targetAppId = targetAppId;
        this.dataSubscriptions = dataSubscriptions;
        this.controlSubscriptions = controlSubscriptions;
    }

    /**
     * Registers the configured subscriptions on the source endpoint. The cloud connection takes care of restoring
     * them after a reconnection.
     */
    public void start() {
        registerSubscribers(this.dataSubscriptions, false);
        registerSubscribers(this.controlSubscriptions, true);
    }

    /**
     * Removes the subscriptions registered on the source endpoint.
     */
    public void stop() {
        for (CloudSubscriberListener listener : this.registeredListeners) {
            this.sourceEndpoint.unregisterSubscriber(listener);
        }
        this.registeredListeners.clear();
    }

    private void registerSubscribers(List<CloudCatSubscription> subscriptions, boolean isControl) {
        for (CloudCatSubscription subscription : subscriptions) {
            String topic = subscription.getTopic();
            int qos = subscription.getQos();

            Map<String, Object> subscriptionProperties = new HashMap<>();
            subscriptionProperties.put(APP_ID, this.sourceAppId);
            subscriptionProperties.put(APP_TOPIC, topic);
            subscriptionProperties.put(QOS, qos);
            subscriptionProperties.put(CONTROL, isControl);

            CloudSubscriberListener listener = message -> relay(message, qos, isControl);
            this.sourceEndpoint.registerSubscriber(subscriptionProperties, listener);
            this.registeredListeners.add(listener);

            logger.info(
                    "Relaying {} topic: '{}' from: {} to: {}",
                    isControl ? "control" : "data",
                    topic,
                    this.sourceAppId,
                    this.targetAppId);
        }
    }

    private void relay(KuraMessage message, int qos, boolean isControl) {
        KuraPayload payload = message.getPayload();
        if (payload == null) {
            logger.debug("Received message without a valid payload, discarding");
            return;
        }

        if (isForwardedMessage(payload)) {
            logger.debug("Received already forwarded message, discarding");
            return;
        }

        String appTopic = (String) message.getProperties().get(RECEIVED_APP_TOPIC);

        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put(APP_ID, this.targetAppId);
        messageProperties.put(APP_TOPIC, appTopic);
        messageProperties.put(QOS, qos);
        messageProperties.put(RETAIN, false);
        messageProperties.put(PRIORITY, DFLT_PRIORITY);
        messageProperties.put(CONTROL, isControl);

        try {
            this.targetEndpoint.publish(new KuraMessage(toForwardedPayload(payload), messageProperties));
        } catch (KuraException e) {
            logger.warn(
                    "Failed to relay incoming {} message on topic: '{}' from: {} to: {}",
                    isControl ? "control" : "data",
                    appTopic,
                    this.sourceAppId,
                    this.targetAppId,
                    e);
        }
    }

    private static boolean isForwardedMessage(KuraPayload payload) {
        final Object isForwardedMessage = payload.getMetric(FORWARDED_MESSAGE_METRIC_NAME);
        return isForwardedMessage instanceof Boolean && (Boolean) isForwardedMessage;
    }

    /*
     * The received payload may be shared with other subscribers of the same endpoint, so a copy is marked as
     * forwarded instead of the original.
     */
    private static KuraPayload toForwardedPayload(KuraPayload payload) {
        KuraPayload forwarded = new KuraPayload();
        forwarded.setTimestamp(payload.getTimestamp());
        forwarded.setPosition(payload.getPosition());
        forwarded.setBody(payload.getBody());
        payload.metrics().forEach(forwarded::addMetric);
        forwarded.addMetric(FORWARDED_MESSAGE_METRIC_NAME, true);
        return forwarded;
    }
}
