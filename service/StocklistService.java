package cs.toronto.edu;

import java.util.Scanner;
import cs.toronto.edu.model.StocklistModel;

public class StocklistService {

    public static void menu(int userId) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n----- STOCKLIST MENU -----");
            System.out.println("1. View my stocklists");
            System.out.println("2. Create new stocklist");
            System.out.println("3. Open stocklist");
            System.out.println("4. Back");
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
                    openStocklist(userId, sc);
                    break;
                case "4":
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

        if (StocklistModel.createStocklist(userId, visibility)) {
            System.out.println("Stocklist created successfully.");
        } else {
            System.out.println("Failed to create stocklist.");
        }
    }

    private static void openStocklist(int userId, Scanner sc) {
        System.out.print("Enter stocklist ID to open or 'back' to go back: ");
        String input = sc.nextLine().trim();

        if (input.equals("back")) return;

        int listId;
        try {
            listId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid stocklist ID.");
            return;
        }

        if (!StocklistModel.stocklistExists(userId, listId)) {
            System.out.println("Stocklist not found.");
            return;
        }

        stocklistMenu(listId, userId);
    }

    private static void stocklistMenu(int listId, int userId) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        StocklistModel stocklistModel = new StocklistModel(userId);

        while (running) {
            System.out.println("\n===== STOCKLIST ACTIONS =====");
            System.out.println("1. View stocks in this list");
            System.out.println("2. Add stock");
            System.out.println("3. Remove stock");
            System.out.println("4. Back");
            System.out.print("Choose: ");

            switch (sc.nextLine().trim()) {
                case "1":
                    StocklistModel.viewStocksInList(listId);
                    break;
                case "2":
                    addStock(listId, sc, stocklistModel);
                    break;
                case "3":
                    removeStock(listId, sc, stocklistModel);
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void addStock(int listId, Scanner sc, StocklistModel stocklistModel) {
        System.out.print("Enter stock symbol to add: ");
        String symbol = sc.nextLine().trim().toUpperCase();

        System.out.print("Enter number of shares: ");
        double shares;
        try {
            shares = Double.parseDouble(sc.nextLine());
            if (shares <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of shares.");
            return;
        }

        if (stocklistModel.addStockToList(listId, symbol, shares)) {
            System.out.println("Stock added successfully.");
        } else {
            System.out.println("Failed to add stock.");
        }
    }

    private static void removeStock(int listId, Scanner sc, StocklistModel stocklistModel) {
        System.out.print("Enter stock symbol to remove: ");
        String symbol = sc.nextLine().trim().toUpperCase();

        if (stocklistModel.removeStockFromList(listId, symbol)) {
            System.out.println("Stock removed successfully.");
        } else {
            System.out.println("Stock not found in this list.");
        }
    }
}
