package chatModules;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Enumeration;

public class Modules {

    public static String getOrCreateNickname() throws IOException {
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
            String nickname;
            while ((nickname = reader.readLine().trim()).isEmpty()) {
                System.out.println("Nickname cannot be empty. Please enter a valid nickname: ");
            }
            Files.write(nicknameFile, nickname.getBytes());
            return nickname;
        }
    }

    public static void startServer(int PORT, String NICKNAME) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients to connect...");

            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client connected!");

                handleCommunication(clientSocket, NICKNAME);
            }
        }
    }

    public static void startClient(int PORT, String NICKNAME) {
        try {
            String serverIPAddress = discoverServerIPAddress(PORT);
            if (serverIPAddress == null) {
                System.out.println("Server not found on the local network.");
                return;
            }

            System.out.println("Connecting to server at " + serverIPAddress + "...");
            try (Socket socket = new Socket(serverIPAddress, PORT)) {
                System.out.println("Connected to server!");

                handleCommunication(socket, NICKNAME);
            }
        } catch (IOException e) {
            System.err.println("Error while connecting to server: " + e.getMessage());
        }
    }

    // Method to handle communication (sending and receiving messages)
    private static void handleCommunication(Socket socket, String NICKNAME) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            // Receiving messages in a separate thread
            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading messages: " + e.getMessage());
                }
            });
            receiveThread.start();

            // Sending messages
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(NICKNAME + ": " + userInput);
            }
        } catch (IOException e) {
            System.err.println("Error during communication: " + e.getMessage());
        }
    }

    private static String discoverServerIPAddress(int PORT) {
        try {
            String localIPPrefix = getLocalIPPrefix();
            for (int i = 1; i < 255; i++) {
                String ip = localIPPrefix + i;
                if (isPortOpen(ip, PORT, 50)) {
                    return ip;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during network discovery: " + e.getMessage());
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
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address) {
                    String localAddress = address.getHostAddress();
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
