import chess.*;
import client.ServerFacade;
import client.AuthResponse;

import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {
        ServerFacade server = new ServerFacade("localhost", 8080);
        String authToken = null;
        boolean loggedIn = false;

        Scanner scanner = new Scanner(System.in);
        System.out.println("♕ Welcome to 240 Chess Client");

        boolean running = true;
        while (running) {
            if (!loggedIn) {
                System.out.println("Type 'help' to see available commands.");
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
                            AuthResponse response = server.register(username, password, email);
                            System.out.println("✅ Registered and logged in as " + response.username);
                            authToken = response.authToken;
                            loggedIn = true;
                        } catch (Exception e) {
                            System.out.println("❌ Registration failed: " + e.getMessage());
                        }
                    }
                    case "login" -> {
                        System.out.print("Username: ");
                        String username = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();
                        try {
                            AuthResponse response = server.login(username, password);
                            System.out.println("✅ Logged in as " + response.username);
                            authToken = response.authToken;
                            loggedIn = true;
                        } catch (Exception e) {
                            System.out.println("❌ Login failed: " + e.getMessage());
                        }
                    }
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            } else {
                authToken = runPostLoginREPL(scanner, server, authToken);
                if (authToken == null) {
                    loggedIn = false;
                }
            }
        }

        scanner.close();
    }

    private static void printHelp() {
        System.out.println("""
            Available commands (Prelogin):
              help     - Display this help message
              register - Create a new account
              login    - Log in with your username and password
              quit     - Exit the program
        """);
    }

    private static String runPostLoginREPL(Scanner scanner, ServerFacade server, String authToken) {
        boolean postLoginRunning = true;
        while (postLoginRunning) {
            System.out.print("@chess> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> System.out.println("""
                    Available commands (Postlogin):
                      help     - Show this help message
                      create   - Create a new game
                      logout   - Log out and return to prelogin menu
                """);

                case "create" -> {
                    System.out.print("Game name: ");
                    String gameName = scanner.nextLine();
                    try {
                        var response = server.createGame(authToken, gameName);
                        System.out.println("✅ Game '" + gameName + "' created with ID: " + response.gameID);
                    } catch (Exception e) {
                        System.out.println("❌ Failed to create game: " + e.getMessage());
                    }
                }

                case "logout" -> {
                    try {
                        server.logout(authToken);
                        System.out.println("✅ Logged out.");
                        return null; // this ends post-login loop and returns to pre-login
                    } catch (Exception e) {
                        System.out.println("❌ Logout failed: " + e.getMessage());
                    }
                }

                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }
        return authToken;
    }
}
