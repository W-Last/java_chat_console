import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    private static String NICKNAME;
    private static final int PORT = 12345;
    private static final String SERVER_OPTION = "server";
    private static final String CLIENT_OPTION = "client";

    public static void main(String[] args) {
        try {
            NICKNAME = chatModules.Modules.getOrCreateNickname();
            System.out.println("Nickname: " + NICKNAME);

            String choice = getUserChoice();
            handleUserChoice(choice);
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }

    // Prompt the user for their choice of server or client
    private static String getUserChoice() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String choice;
        do {
            System.out.println("Type 'server' to start as server or 'client' to start as client:");
            choice = reader.readLine().trim();
            if (!choice.equalsIgnoreCase(SERVER_OPTION) && !choice.equalsIgnoreCase(CLIENT_OPTION)) {
                System.out.println("Invalid choice. Please type 'server' or 'client'.");
            }
        } while (!choice.equalsIgnoreCase(SERVER_OPTION) && !choice.equalsIgnoreCase(CLIENT_OPTION));
        return choice;
    }

    // Handle the user's choice
    private static void handleUserChoice(String choice) throws IOException {
        if (choice.equalsIgnoreCase(SERVER_OPTION)) {
            chatModules.Modules.startServer(PORT, NICKNAME);
        } else if (choice.equalsIgnoreCase(CLIENT_OPTION)) {
            chatModules.Modules.startClient(PORT, NICKNAME);
        }
    }
}
