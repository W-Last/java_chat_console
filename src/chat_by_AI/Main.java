package chat_by_AI;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Enumeration;

public class Main {

    private static final int PORT = 12345;
    private static String NICKNAME;

    public static void main(String[] args) {
        try {
            NICKNAME = getOrCreateNickname();
            System.out.println("Nickname: " + NICKNAME);

            System.out.println("Type 'server' to start as server or 'client' to start as client:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String choice = reader.readLine().trim();

            if (choice.equalsIgnoreCase("server")) {
                startServer();
            } else if (choice.equalsIgnoreCase("client")) {
                startClient();
            } else {
                System.out.println("Invalid choice. Please start the program again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getOrCreateNickname() throws IOException {
        String userHome = System.getProperty("user.home");
        Path chatDir = Paths.get(userHome, "AppData", "java_chat");
        Path nicknameFile = chatDir.resolve("nickname.log");

        if (Files.notExists(chatDir)) {
            Files.createDirectories(chatDir);
        }

        if (Files.exists(nicknameFile)) {
            return new String(Files.readAllBytes(nicknameFile)).trim();
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter your nickname: ");
            String nickname = reader.readLine().trim();
            Files.write(nicknameFile, nickname.getBytes());
            return nickname;
        }
    }

    private static void startServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients to connect...");

            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client connected!");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

                // Receiving messages thread
                Thread receiveThread = new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                receiveThread.start();

                // Sending messages
                String userInput;
                while ((userInput = stdIn.readLine()) != null) {
                    out.println(NICKNAME + ": " + userInput);
                }
            }
        }
    }

    private static void startClient() {
        try {
            String serverIPAddress = discoverServerIPAddress();
            if (serverIPAddress == null) {
                System.out.println("Server not found on the local network.");
                return;
            }

            System.out.println("Connecting to server at " + serverIPAddress + "...");
            try (Socket socket = new Socket(serverIPAddress, PORT)) {
                System.out.println("Connected to server!");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

                // Receiving messages thread
                Thread receiveThread = new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                receiveThread.start();

                // Sending messages
                String userInput;
                while ((userInput = stdIn.readLine()) != null) {
                    out.println(NICKNAME + ": " + userInput);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String discoverServerIPAddress() {
        try {
            String localIPPrefix = getLocalIPPrefix();
            for (int i = 1; i < 255; i++) {
                String ip = localIPPrefix + i;
                if (isPortOpen(ip, PORT, 50)) {
                    return ip;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getLocalIPPrefix() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) {
                    String localAddress = addr.getHostAddress();
                    return localAddress.substring(0, localAddress.lastIndexOf('.') + 1);
                }
            }
        }
        return null;
    }

    private static boolean isPortOpen(String ip, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}