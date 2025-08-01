package org.eclipse.kura.example.factory.component.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSimulator {

    private static final Logger logger = LoggerFactory.getLogger(ClientSimulator.class);

    private final String host;
    private final int port;

    private String response;
    private Exception exception;

    public ClientSimulator(String hostName, int hostPort) {
        this.host = hostName;
        this.port = hostPort;
    }

    public void sendMessage(String messageToSend) {
        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            logger.info("\n\nClient Simulator -> Starting\n\n");

            socket.setSoTimeout(10000);
            // Send message to server
            out.println(messageToSend);

            logger.info("\n\nClient Simulator -> Message sent: {}\n\n", messageToSend);

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                logger.info("\n\nLine received: {}\n\n", line);
                sb.append(line).append("\n");
            }

            String receivedResponse = sb.toString();

            this.response = receivedResponse;

            logger.info("\n\nClient Simulator -> Ended: {}\n\n", this.response);

        } catch (Exception ex) {
            this.exception = ex;
        }

    }

    public String getResponseMessage() {
        return this.response;
    }

    public Exception getException() {
        return this.exception;
    }

}
