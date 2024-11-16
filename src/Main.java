import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Main {

    private static String NICKNAME;
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try {
            NICKNAME = chatModules.Modules.getOrCreateNickname();
            System.out.println("Nickname: " + NICKNAME);

            System.out.println("Type 'server' to start as server or 'client' to start as client:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String choice = reader.readLine().trim();

            if (choice.equalsIgnoreCase("server")) {
                chatModules.Modules.startServer(PORT,NICKNAME);
            } else if (choice.equalsIgnoreCase("client")) {
                chatModules.Modules.startClient(PORT,NICKNAME);
            } else {
                System.out.println("Invalid choice. Please start the program again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
