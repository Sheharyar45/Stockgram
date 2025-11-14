package cs.toronto.edu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        try {
            while (running) {
                System.out.println("\nEnter your selection:");
                System.out.println("1. Sign In");
                System.out.println("2. Sign Up");
                System.out.println("3. Exit");
                System.out.print("Your choice: ");

                String line = scanner.nextLine().trim();
                int choice;
                try {
                    choice = Integer.parseInt(line);
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid input. Please enter 1, 2 or 3.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        System.out.println("You selected: Sign In");
                        System.out.print("Username: ");
                        String u = scanner.nextLine();
                        System.out.print("Password: ");
                        String p = scanner.nextLine();

                        Integer userId = AuthService.signIn(u, p);

                        if (userId != null) {
                            System.out.println("Login successful!");
                            // Menu.show(userId);
                            // if you want to stop showing main menu after successful login, set running = false;
                        } else {
                            System.out.println("Invalid login. Try again.");
                        }
                        break;

                    case 2:
                        System.out.println("You selected: Sign Up");
                        System.out.print("Username: ");
                        String username = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();

                        boolean signUpSuccess = AuthService.signUp(username, password, email);
                        if (signUpSuccess) {
                            System.out.println("Sign up successful! You can now sign in.");
                        } else {
                            System.out.println("Sign up failed. Please try again.");
                        }
                        break;

                    case 3:
                        System.out.println("Exiting program...");
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid selection. Please enter 1, 2, or 3.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        } finally {
            scanner.close();
        }
    }
}