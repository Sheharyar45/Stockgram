package cs.toronto.edu;
import cs.toronto.edu.model.PortfolioModel;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class PortfolioService {

    public static void menu(int userId) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== PORTFOLIO MENU =====");
            System.out.println("1. View my portfolios");
            System.out.println("2. Create new portfolio");
            System.out.println("3. Select portfolio");
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
                    selectPortfolio(userId, sc);
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // create portfolio
    private static void createPortfolio(int userId, Scanner sc) {
        System.out.print("Enter new portfolio name: ");
        String portfolioName = sc.nextLine();
        try {
            if(PortfolioModel.createPortfolio(userId, portfolioName)) {
                System.out.println("Portfolio created successfully.");
                // ask if they want to deposit initial cash
                System.out.print("Do you want to deposit initial cash? (y/n): ");
                String choice = sc.nextLine();
                if (choice.equalsIgnoreCase("y")) {
                    // PortfolioModel.depositInitialCash(userId, portfolioName, sc);
                    System.out.println("Initial cash deposited.");
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






    private static void selectPortfolio(int userId, Scanner sc) {
        System.out.print("Enter Portfolio name to select or 1 to go back: ");
        String portfolioName = sc.nextLine();
        if (portfolioName.equals("1")) {
            return;
        }
        try {
            int portfolioId = PortfolioModel.getPortfolioIdByName(userId, portfolioName);
            if (portfolioId != -1) {
                PortfolioModel.viewPortfolioDetails(portfolioId, portfolioName);
                portfolioMenu(portfolioId);
            } else {
                System.out.println("You do not own a portfolio with that name.");
            }
        } catch (Exception e) {
            System.out.println("Error selecting portfolio: " + e.getMessage());
        }
    }

    private static void portfolioMenu(int portfolioId) {
        // int portfolioId = PortfolioModel.getPortfolioIdByName(portfolioName);
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== PORTFOLIO ACTIONS =====");
            System.out.println("1. Deposit cash");
            System.out.println("2. Withdraw cash");
            System.out.println("3. Buy stock");
            System.out.println("4. Sell stock");
            System.out.println("5. View holdings");
            System.out.println("6. View market value");
            System.out.println("7. View historical performance");
            System.out.println("8. View predictions");
            System.out.println("9. Portfolio statistics");
            System.out.println("10. Back");
            System.out.print("Choose: ");

            switch (sc.nextLine()) {
                case "1":
                    // PortfolioModel.deposit(portfolioId, sc);
                    break;
                case "2":
                    // PortfolioModel.withdraw(portfolioId, sc);
                    break;
                case "3":
                    // PortfolioModel.buyStock(portfolioId, sc);
                    break;
                case "4":
                    // PortfolioModel.sellStock(portfolioId, sc);
                    break;
                case "5":
                    // PortfolioModel.viewHoldings(portfolioId);
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