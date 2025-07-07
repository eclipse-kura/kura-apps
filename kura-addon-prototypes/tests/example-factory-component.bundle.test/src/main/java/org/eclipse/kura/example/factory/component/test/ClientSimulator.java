package org.eclipse.kura.example.factory.component.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSimulator {

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

            socket.setSoTimeout(10000);
            // Send message to server
            out.println(messageToSend);

            String receivedResponse = in.readLine();
            this.response = receivedResponse;

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
