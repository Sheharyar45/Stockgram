package cs.toronto.edu;
import cs.toronto.edu.model.PortfolioModel;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.model.StockModel;



public class PortfolioService {

    public static void menu(int userId) {
        Scanner sc = new Scanner(System.in);
        boolean on = true;

        while (on) {
            System.out.println("\n----- PORTFOLIO MENU -----");
            System.out.println("1. View my portfolios");
            System.out.println("2. Create new portfolio");
            System.out.println("3. Open portfolio");
            System.out.println("4. Back");
            System.out.print("Choose an option: ");

            String input = sc.nextLine();

            switch (input) {
                case "1":
                    PortfolioModel.viewPortfolios(userId);
                    break;
                case "2":
                    createPortfolio(userId, sc);
                    break;
                case "3":
                    openPortfolio(userId, sc);
                    break;
                case "4":
                    on = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createPortfolio(int userId, Scanner sc) {
        System.out.print("Enter new portfolio name: ");
        String portfolioName = sc.nextLine();
        try {
            if(PortfolioModel.createPortfolio(userId, portfolioName)) {
                System.out.println("Portfolio created successfully.");
                System.out.print("Do you want to deposit initial cash? (y/n): ");
                String choice = sc.nextLine();
                if (choice.equalsIgnoreCase("y")) {
                    int portfolioId = PortfolioModel.getPortfolioIdByName(userId, portfolioName);
                    if(portfolioId == -1) {
                        System.out.println("Error getting portfolio ID.");
                        return;
                    }
                    if (deposit(portfolioId, sc, userId)) {
                        System.out.println("Initial cash deposited successfully.");
                    } else {
                        System.out.println("Failed to deposit initial cash.");
                    }
                    return;
                }
                else if (choice.equalsIgnoreCase("n")) {
                    System.out.println("No initial cash deposited.");
                }
                else {
                    System.out.println("Invalid choice. No initial cash deposited.");
                }
            }
            else {
                System.out.println("Failed to create portfolio.");
            }
            
        } catch (Exception e) {
            System.out.println("Error creating portfolio: " + e.getMessage());
        }
    }






    private static void openPortfolio(int userId, Scanner sc) {
        System.out.print("Enter Portfolio name to select or 1 to go back: ");
        String portfolioName = sc.nextLine();
        if (portfolioName.equals("1")) {
            return;
        }
        try {
            int portfolioId = PortfolioModel.getPortfolioIdByName(userId, portfolioName);
            if (portfolioId != -1) {
                PortfolioModel.viewPortfolioDetails(portfolioId, portfolioName);
                portfolioMenu(portfolioId, userId);
            } else {
                System.out.println("You do not own a portfolio with that name.");
            }
        } catch (Exception e) {
            System.out.println("Error selecting portfolio: " + e.getMessage());
        }
    }

    private static boolean deposit(int portfolioId, Scanner sc, int userId) {
        System.out.print("Amount to deposit: ");
        double amount = Double.parseDouble(sc.nextLine());
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return false;
        }
        System.out.print("Deposit from (1) Bank or (2) Another Portfolio? Enter 1 or 2: ");
        String choice = sc.nextLine();
        if (choice.equals("2")) {
            System.out.print("Enter source Portfolio name: ");
            String sourceName = sc.nextLine();
            int sourceId = PortfolioModel.getPortfolioIdByName(userId, sourceName);
            if(sourceId == -1) {
                System.out.println("Portfolio not found.");
                return false;
            }
            if (!PortfolioModel.withdraw(sourceId, amount) ||
                !PortfolioModel.deposit(portfolioId, amount)) {
                System.out.println("Failed to transfer funds from source portfolio.");
                return false;
            }
        } else if (choice.equals("1")) {
            if (!PortfolioModel.deposit(portfolioId, amount)) {
                System.out.println("Failed to deposit cash.");
                return false;
            }
        } else {
            System.out.println("Invalid choice.");
            return false;
        }
        return true; 
    }

    private static boolean withdraw(int portfolioId, Scanner sc, int userId) {
        System.out.print("Amount to withdraw: ");
        double amount = Double.parseDouble(sc.nextLine());
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return false;
        }
        System.out.print("Are you withdrawing to (1) Bank or (2) Another Portfolio? Enter 1 or 2: ");
        String choice = sc.nextLine();
        if (choice.equals("2")) {
            System.out.print("Enter destination Portfolio name: ");
            String destName = sc.nextLine();
            int destId = PortfolioModel.getPortfolioIdByName(userId, destName);
            if(destId == -1) {
                System.out.println("Portfolio not found.");
                return false;
            }
            if (!PortfolioModel.withdraw(portfolioId, amount) ||
                !PortfolioModel.deposit(destId, amount)) {
                System.out.println("Failed to transfer funds to destination portfolio.");
                return false;
            }
        } else if (choice.equals("1")) {
            if (!PortfolioModel.withdraw(portfolioId, amount)) {
                System.out.println("Failed to withdraw cash.");
                return false;
            }
        } else {
            System.out.println("Invalid choice.");
            return false;
        }
        return true; 
    }

    private static void portfolioMenu(int portfolioId, int userId) {
        // int portfolioId = PortfolioModel.getPortfolioIdByName(portfolioName);
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        StockModel stockModel = new StockModel();

        while (running) {
            System.out.println("\n===== PORTFOLIO ACTIONS =====");
            System.out.println("1. Deposit cash");
            System.out.println("2. Withdraw cash");
            System.out.println("3. Buy stock");
            System.out.println("4. Sell stock");
            System.out.println("5. View holdings and cash balance");
            System.out.println("6. View market value");
            System.out.println("7. View historical performance");
            System.out.println("8. View predictions");
            System.out.println("9. Portfolio statistics");
            System.out.println("10. Back");
            System.out.print("Choose: ");

            switch (sc.nextLine()) {
                case "1":
                    deposit(portfolioId, sc, userId);   
                    break;
                case "2":
                    withdraw(portfolioId, sc, userId);
                    break;
                case "3":
                    if (!PortfolioModel.buyStock(portfolioId, sc, stockModel)) {
                        System.out.println("Failed to buy stock.");
                    }
                    break;
                case "4":
                    if (!PortfolioModel.sellStock(portfolioId, sc, stockModel)) {
                        System.out.println("Failed to sell stock.");
                    }
                    break;
                case "5":
                    PortfolioModel.viewHoldings(portfolioId, stockModel);
                    break;
                case "6":
                    // PortfolioCalculations.showMarketValue(portfolioId);
                    break;
                case "7":
                    // PortfolioCalculations.showHistory(portfolioId, sc);
                    break;
                case "8":
                    // PortfolioCalculations.predictPrices(portfolioId, sc);
                    break;
                case "9":
                    // PortfolioCalculations.showStatistics(portfolioId);
                    break;
                case "10":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }
}