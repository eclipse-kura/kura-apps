/*******************************************************************************
 * Copyright (c) 2025 Eurotech and/or its affiliates and others
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

package org.eclipse.kura.example.factory.component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final int tcpPort;
    private final Map<String, String> requestAndAnswers;
    private final boolean isCaseSensitive;

    private String errorMessage;

    private ServerSocket serverSocket;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public ClientHandler(FactoryComponentExampleOptions options) {
        this.tcpPort = options.getTcpPort();
        this.requestAndAnswers = extractRequestAndAnswer(options.getAvailableMessages());
        this.isCaseSensitive = options.isCaseSensitiveEnabled();
    }

    private Map<String, String> extractRequestAndAnswer(String messageList) {

        List<String> messages = new ArrayList<>();
        Arrays.asList(messageList.split("\\R")).stream().forEach(line -> {
            messages.addAll(Arrays.asList(line.split(":")));
        });

        if (messages.size() % 2 != 0) {
            logger.error("Error during extraction of requests and answers. Service will respond with error code.");
            this.errorMessage = "Configuration Error: check the settings.";
            return Collections.emptyMap();
        }

        return IntStream.range(0, messages.size() / 2).boxed()
                .collect(Collectors.toMap(i -> messages.get(i * 2), i -> messages.get(i * 2 + 1)));
    }

    public void buildAndRunSocket() {

        logger.info("Server starting on port {}", this.tcpPort);

        try {
            if (!Objects.isNull(this.serverSocket) && !this.serverSocket.isClosed()) {
                this.serverSocket.close();
            }

            this.serverSocket = new ServerSocket(this.tcpPort);
            this.serverSocket.setSoTimeout(500);

            this.executor.submit(() -> handleClient(serverSocket));

        } catch (Exception ex) {
            logger.error("Server stopped due to: {}", ex.getMessage());
        }
    }

    private void handleClient(ServerSocket serverSocket) {
        while (!this.executor.isShutdown()) {

            try (Socket clientSocket = serverSocket.accept()) {
                logger.info("Client connected: {}", clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                if (this.requestAndAnswers.isEmpty()) {
                    out.write(errorMessage);
                    out.flush();
                    return;
                }

                String input = in.readLine();
                logger.info("Received: {}", input);

                if (checkRequest(input)) {
                    out.write(this.requestAndAnswers.get(input) + "\n");
                } else {
                    out.write(generateAvailableRequestMessage() + "\n");
                }
                out.flush();
            } catch (IOException e) {
                logger.debug("Timeout reached");
            }
        }
    }

    private boolean checkRequest(String request) {
        if (this.isCaseSensitive) {
            return this.requestAndAnswers.containsKey(request);
        } else {
            return this.requestAndAnswers.entrySet().stream()
                    .anyMatch(entry -> entry.getKey().equalsIgnoreCase(request));
        }
    }

    private String generateAvailableRequestMessage() {

        StringBuilder response = new StringBuilder("Unknown request. Available ones are:\n");

        this.requestAndAnswers.forEach((key, value) -> response.append("\t- " + key + "\n"));

        response.append(this.isCaseSensitive ? "Be careful, requests are case sensitive."
                : "Requests are not case sensitive, just ask something :)");

        return response.toString();

    }

    public void stopSocket() throws IOException {

        this.serverSocket.close();
        this.serverSocket = null;

        this.executor.shutdown();
    }

}
