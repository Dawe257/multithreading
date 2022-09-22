package com.dzhenetl.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.nio.charset.StandardCharsets;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class ServerThread implements Runnable {
    private final Socket socket;
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js"
    );

    private BufferedReader in;
    private BufferedOutputStream out;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedOutputStream(socket.getOutputStream());

            String[] parts = readRequest();

            if (parts.length != 3) {
                // just close socket
                return;
            }

            Request request = new Request(parts[1]);

            if (!validPaths.contains(request.getPath())) {
                sendError(out);
                return;
            }

            Path filePath = Path.of(".", "public", request.getPath());
            String mimeType = Files.probeContentType(filePath);

            if (request.getPath().equals("/classic.html")) {
                sendTimeTemplate(filePath, mimeType);
                return;
            }

            sendResponse(filePath, mimeType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] readRequest() throws IOException {
        String requestLine = in.readLine();

        return requestLine.split(" ");
    }

    private void sendError(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

    public void sendTimeTemplate(Path filePath, String type) throws IOException {
        String template = Files.readString(filePath);
        byte[] content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + type + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    public void sendResponse(Path filePath, String type) throws IOException {
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + type + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
