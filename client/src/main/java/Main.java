import chess.*;
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
