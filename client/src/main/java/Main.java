import chess.*;
import client.ServerFacade;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ServerFacade server = new ServerFacade("localhost", 8080);
        String authToken = null;
        boolean loggedIn = false;

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
                        var response = server.register(username, password, email);
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
                        var response = server.login(username, password);
                        System.out.println("✅ Logged in as " + response.username);
                        authToken = response.authToken;
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("❌ Login failed: " + e.getMessage());
                    }
                }

                case "logout" -> {
                    if (!loggedIn || authToken == null) {
                        System.out.println("⚠️  You are not logged in.");
                        break;
                    }

                    try {
                        server.logout(authToken);
                        System.out.println("✅ Logged out.");
                        loggedIn = false;
                        authToken = null;
                    } catch (Exception e) {
                        System.out.println("❌ Logout failed: " + e.getMessage());
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
              register- Create a new account
              login   - Log in with your username and password
              quit    - Exit the program
        """);
    }
}
