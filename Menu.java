package cs.toronto.edu;

import java.util.Scanner;
import cs.toronto.edu.model.StockModel;


public class Menu {

    public static void show(int userId) {
        if (userId == -1) {
            Main.main(new String[0]);
            return;
        }

        Scanner scanner = new Scanner(System.in);

        boolean loggedIn = true;
        StockModel stockModel = new StockModel(userId);

        while (loggedIn) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Friends");
            System.out.println("2. Stocks");
            System.out.println("3. Portfolios");
            System.out.println("4. Transactions");
            System.out.println("5. Stocklists");
            System.out.println("6. Reviews");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();

            switch (input) {

                case "1":
                    System.out.println("Opening Friends...");
                    FriendsService.menu(userId); 
                    break;
                case "2":
                    System.out.println("Opening Stocks...");
                    StockService.menu(userId, stockModel);
                    break;

                case "3":
                    System.out.println("Opening Portfolios...");
                    PortfolioService.menu(userId, stockModel);
                    break;

                case "4":
                    System.out.println("Opening Transactions...");
                    TransactionsService.menu(userId);
                    break;

                case "5":
                    System.out.println("Opening Stocklists...");
                    StocklistService.menu(userId);
                    break;

                case "6":
                    System.out.println("Opening Reviews...");
                    // ReviewService.menu(userId);
                    break;

                case "7":
                    System.out.println("Logging out...");
                    loggedIn = false;
                    break;

                default:
                    System.out.println("Invalid option. Please enter a number 1-7.");
            }
        }
    }
}
