package cs.toronto.edu;

import java.util.Scanner;
import cs.toronto.edu.model.StocklistModel;
import cs.toronto.edu.model.StockModel;
import cs.toronto.edu.service.ReviewService;

public class StocklistService {

    public static void menu(int userId, StockModel stockModel) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n----- STOCKLIST MENU -----");
            System.out.println("1. View my stocklists");
            System.out.println("2. Create new stocklist");
            System.out.println("3. Open stocklist");
            System.out.println("4. Delete stocklist");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    StocklistModel.viewStocklists(userId);
                    break;
                case "2":
                    createStocklist(userId, sc);
                    break;
                case "3":
                    openStocklist(userId, sc, stockModel);
                    break;
                case "4":
                    deleteStocklist(userId, sc);
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createStocklist(int userId, Scanner sc) {
        System.out.print("Enter stocklist visibility (public/private): ");
        String visibility = sc.nextLine().trim().toLowerCase();

        if (!visibility.equals("public") && !visibility.equals("private")) {
            System.out.println("Invalid visibility. Must be 'public' or 'private'.");
            return;
        }

        Integer stocklistId = StocklistModel.createStocklist(userId, visibility);

        if (stocklistId != null) {
            System.out.println("Stocklist created successfully. ID: " + stocklistId);
        } else {
            System.out.println("Failed to create stocklist.");
        }
    }

    private static void openStocklist(int userId, Scanner sc, StockModel stockModel) {
        System.out.print("Enter stocklist ID to open or 'back' to go back: ");
        String input = sc.nextLine().trim();

        if (input.equalsIgnoreCase("back")) return;

        int listId;
        try {
            listId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid stocklist ID.");
            return;
        }

        if (!StocklistModel.stocklistExistsForUser(userId, listId)) {
            System.out.println("Stocklist not found or does not belong to you.");
            return;
        }

        stocklistMenu(listId, userId, stockModel, sc);
    }

    private static void deleteStocklist(int userId, Scanner sc) {
        System.out.print("Enter the stocklist ID to delete or 'back' to cancel: ");
        String input = sc.nextLine().trim();
        if (input.equalsIgnoreCase("back")) return;

        int stocklistId;
        try {
            stocklistId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid stocklist ID.");
            return;
        }

        if (StocklistModel.deleteStocklist(userId, stocklistId)) {
            System.out.println("Stocklist deleted successfully.");
        } else {
            System.out.println("Failed to delete stocklist.");
        }
    }


    // Stocklist Menu
    private static void stocklistMenu(int listId, int userId, StockModel stockModel, Scanner sc) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== STOCKLIST ACTIONS =====");
            System.out.println("1. View stocks in this list");
            System.out.println("2. Add stock");
            System.out.println("3. Remove stock");
            System.out.println("4. Manage reviews");
            System.out.println("5. View predictions");
            System.out.println("6. Back");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    StocklistModel.viewStocksInList(listId);
                    break;

                case "2":
                    System.out.print("Enter stock symbol: ");
                    String symbol = sc.nextLine().trim().toUpperCase();

                    System.out.print("Enter number of shares: ");
                    double shares;
                    try {
                        shares = Double.parseDouble(sc.nextLine());
                        if (shares <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number of shares.");
                        break;
                    }

                    if (StocklistModel.addStockToList(listId, symbol, shares, stockModel)) {
                        System.out.println("Stock added successfully.");
                    } else {
                        System.out.println("Failed to add stock.");
                    }
                    break;

                case "3":
                    System.out.print("Enter stock symbol to remove: ");
                    symbol = sc.nextLine().trim().toUpperCase();

                    if (StocklistModel.removeStockFromList(listId, symbol)) {
                        System.out.println("Stock removed successfully.");
                    } else {
                        System.out.println("Failed to remove stock.");
                    }
                    break;

                case "4":
                    ReviewService.menu(userId, listId);
                    break;

                case "5":
                    StocklistModel.showStatistics(listId, sc, stockModel);
                    break;

                case "6":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
