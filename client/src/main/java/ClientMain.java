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
        System.out.println("\u2655 Welcome to 240 Chess Client");

        boolean running = true;
        while (running) {
            if (!loggedIn) {
                System.out.println("Type 'help' to see available commands.");
                System.out.print("> ");
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.equals("help")) {
                    printPreloginHelp();
                } else if (input.equals("register")) {
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    try {
                        AuthResponse response = server.register(username, password, email);
                        System.out.println("\u2705 Registered and logged in as " + response.username);
                        authToken = response.authToken;
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("\u274C Registration failed: " + e.getMessage());
                    }
                } else if (input.equals("login")) {
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    try {
                        AuthResponse response = server.login(username, password);
                        System.out.println("\u2705 Logged in as " + response.username);
                        authToken = response.authToken;
                        loggedIn = true;
                    } catch (Exception e) {
                        System.out.println("\u274C Login failed: " + e.getMessage());
                    }
                } else if (input.equals("quit")) {
                    System.out.println("Goodbye!");
                    running = false;
                } else {
                    System.out.println("Unknown command. Type 'help' for a list of commands.");
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

    private static void printPreloginHelp() {
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
            System.out.print("@chess> type 'help' for available commands ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printPostloginHelp();
                case "list" -> handleListGames(server, authToken);
                case "play" -> handlePlay(scanner, server, authToken);
                case "observe" -> handleObserve(scanner, server, authToken);
                case "create" -> handleCreate(scanner, server, authToken);
                case "logout" -> {
                    if (handleLogout(server, authToken)) return null;
                }
                default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }
        return authToken;
    }

    private static void printPostloginHelp() {
        System.out.println("""
        Available commands (Postlogin):
          help     - Show this help message
          create   - Create a new game
          list     - View all available games
          play     - Join a game as a player
          observe  - Join a game as an observer
          logout   - Log out and return to prelogin menu
    """);
    }

    private static void handleListGames(ServerFacade server, String authToken) {
        try {
            var response = server.listGames(authToken);
            if (response.games == null || response.games.isEmpty()) {
                System.out.println("📝 No games available.");
                return;
            }
            System.out.println("🎮 Available Games:");
            for (var game : response.games) {
                System.out.printf("  [game ID: %d] \"%s\" | White: %s | Black: %s%n",
                        game.gameID,
                        game.gameName,
                        game.whiteUsername != null ? game.whiteUsername : "(empty)",
                        game.blackUsername != null ? game.blackUsername : "(empty)");
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to list games: " + e.getMessage());
        }
    }

    private static void handlePlay(Scanner scanner, ServerFacade server, String authToken) {
        try {
            System.out.print("Enter Game ID: ");
            int gameID = Integer.parseInt(scanner.nextLine());
            System.out.print("Choose color (WHITE or BLACK): ");
            String color = scanner.nextLine().trim().toUpperCase();

            server.joinGame(authToken, gameID, color);
            System.out.println("✅ Joined game " + gameID + " as " + color);
        } catch (Exception e) {
            System.out.println("❌ Failed to join game: " + e.getMessage());
        }
    }

    private static void handleObserve(Scanner scanner, ServerFacade server, String authToken) {
        try {
            System.out.print("Enter Game ID: ");
            int gameID = Integer.parseInt(scanner.nextLine());

            server.joinGame(authToken, gameID, null);
            System.out.println("👁️ Now observing game " + gameID);
            runGameREPL(scanner, server, authToken, gameID, false);
        } catch (Exception e) {
            System.out.println("❌ Failed to observe game: " + e.getMessage());
        }
    }

    private static void handleCreate(Scanner scanner, ServerFacade server, String authToken) {
        try {
            System.out.print("Game name: ");
            String gameName = scanner.nextLine();

            var response = server.createGame(authToken, gameName);
            System.out.println("✅ Game '" + gameName + "' created with ID: " + response.gameID);
        } catch (Exception e) {
            System.out.println("❌ Failed to create game: " + e.getMessage());
        }
    }

    private static boolean handleLogout(ServerFacade server, String authToken) {
        try {
            server.logout(authToken);
            System.out.println("✅ Logged out.");
            return true;
        } catch (Exception e) {
            System.out.println("❌ Logout failed: " + e.getMessage());
            return false;
        }
    }

    private static void runGameREPL(Scanner scanner, ServerFacade server, String authToken, int gameID, boolean isPlayer) {
        System.out.println("🎯 Entered game " + gameID + (isPlayer ? " as a PLAYER" : " as an OBSERVER"));

        boolean inGame = true;
        while (inGame) {
            System.out.print("@game> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("help")) {
                if (isPlayer) {
                    System.out.println("""
                    Game Commands (Player):
                      help   - Show this help message
                      leave  - Leave the game and return to lobby
                      move   - (not implemented)
                    """);
                } else {
                    System.out.println("""
                    Game Commands (Observer):
                      help   - Show this help message
                      leave  - Stop observing and return to lobby
                    """);
                }
            } else if (input.equals("leave")) {
                System.out.println("🚪 Leaving game " + gameID + "...");
                inGame = false;
            } else if (input.equals("move")) {
                if (!isPlayer) {
                    System.out.println("⚠️ Observers can't make moves.");
                } else {
                    System.out.println("⏳ Move functionality not implemented yet.");
                }
            } else {
                System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
        }
    }
}
