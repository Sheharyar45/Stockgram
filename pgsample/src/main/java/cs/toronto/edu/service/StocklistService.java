package cs.toronto.edu;

import java.util.Scanner;
import java.util.List;
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
            System.out.println("5. View public stocklists");
            System.out.println("6. View invited stocklists");
            System.out.println("7. Back");
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
                    viewPublicStocklists();
                    break;
                case "6":
                    List<Integer> invites = StocklistModel.getInvitedStocklists(userId);
                    if (invites.isEmpty()) {
                        System.out.println("No stocklists shared with you for review.");
                    } else {
                        System.out.println("--- Stocklists shared with you ---");
                        for (int sid : invites) {
                            System.out.println("Stocklist ID: " + sid);
                        }
                    }
                    break;
                case "7":
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

        boolean owner = StocklistModel.isOwner(userId, listId);
        boolean invited = StocklistModel.isInvitedReviewer(userId, listId);
        String visibility = StocklistModel.getVisibility(listId);

        if (visibility == null) {
            System.out.println("Stocklist not found.");
            return;
        }

        if (!owner) {
            // Public is always open to view/review
            if (visibility.equals("public")) {
                System.out.println("Opening public stocklist (read-only).");
                stocklistMenu(listId, userId, stockModel, sc, false);
                return;
            }

            // Private but user is invited
            if (invited) {
                System.out.println("You are invited to review this private stocklist (read-only).");
                stocklistMenu(listId, userId, stockModel, sc, false);
                return;
            }

            System.out.println("You do not have access to this private stocklist.");
            return;
        }

        stocklistMenu(listId, userId, stockModel, sc, true);
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

    private static void viewPublicStocklists() {
        List<String> publicLists = StocklistModel.getPublicStocklists();

        System.out.println("\n--- PUBLIC STOCKLISTS ---");

        if (publicLists.isEmpty()) {
            System.out.println("No public stocklists available.");
            return;
        }

        for (String entry : publicLists) {
            System.out.println(entry);
        }
    }


    // Stocklist Menu
    private static void stocklistMenu(int listId, int userId, StockModel stockModel, Scanner sc, boolean allowManage) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== STOCKLIST ACTIONS =====");
            System.out.println("1. View stocks in this list");
            if (allowManage) {
                System.out.println("2. Add stock");
                System.out.println("3. Remove stock");
            }
            System.out.println("4. Manage reviews");
            System.out.println("5. View statistics");
            System.out.println("6. View predictions");
            if (allowManage) {
                System.out.println("7. Send to friend for review");
            }
            System.out.println("8. Back");

            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    StocklistModel.viewStocksInList(listId);
                    break;

                case "2":
                    if (!allowManage) {
                        System.out.println("You cannot modify a stocklist you do not own.");
                        break;
                    }

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

                    if (StocklistModel.addStockToList(userId, listId, symbol, shares, stockModel)) {
                        System.out.println("Stock added successfully.");
                    } else {
                        System.out.println("Failed to add stock.");
                    }
                    break;

                case "3":
                    if (!allowManage) {
                        System.out.println("You cannot modify a stocklist you do not own.");
                        break;
                    }

                    System.out.print("Enter stock symbol to remove: ");
                    String symbolRemove = sc.nextLine().trim().toUpperCase();

                    if (StocklistModel.removeStockFromList(userId, listId, symbolRemove)) {
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
                    StocklistModel.predictPrices(listId, sc, stockModel);
                    break;

                case "7":
                    if (!allowManage) {
                        System.out.println("You cannot send a stocklist you do not own.");
                        break;
                    }

                    System.out.print("Enter friend's user ID: ");
                    int friendId;
                    try {
                        friendId = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid user ID.");
                        break;
                    }

                    if (StocklistModel.sendToFriend(userId, friendId, listId)) {
                        System.out.println("Stocklist sent successfully.");
                    } else {
                        System.out.println("Failed to send stocklist.");
                    }
                    break;
                
                case "8":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
