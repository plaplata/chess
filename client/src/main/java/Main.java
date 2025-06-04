import chess.*;
import client.ServerFacade;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("♕ Welcome to 240 Chess Client");
        System.out.println("Type 'help' to see available commands.");

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();

                case "register" -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();

                    try {
                        var server = new ServerFacade("localhost", 8080); // Change port if needed
                        var response = server.register(username, password, email);
                        System.out.println("✅ Registered and logged in as " + response.username);
                        // TODO: Transition to post-login UI
                    } catch (Exception e) {
                        System.out.println("❌ Registration failed: " + e.getMessage());
                    }
                }

                case "quit" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }

                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }

        scanner.close();
    }

    private static void printHelp() {
        System.out.println("""
            Available commands (Prelogin):
              help    - Display this help message
              quit    - Exit the program
              login   - Log in with your username and password
              register- Create a new account
        """);
    }
}
