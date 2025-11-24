package cs.toronto.edu;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import java.util.Scanner;
import cs.toronto.edu.model.StockModel;

public class StockService {

    public static void menu(int userId) {
        Scanner sc = new Scanner(System.in);
        StockModel stockModel = new StockModel(userId);
        boolean running = true;

        while (running) {
            System.out.println("\n----- STOCK MENU -----");
            System.out.println("1. View all stocks");
            System.out.println("2. Search for specific stock");
            System.out.println("3. Add stock data");
            System.out.println("4. Back");
            System.out.print("Choose an option: ");

            String input = sc.nextLine();

            switch (input) {
                case "1":
                    viewAllStocks(stockModel);
                    break;
                case "2":
                    searchStock(sc, stockModel);
                    break;
                case "3":
                    addStockData(sc, stockModel);
                    
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void viewAllStocks(StockModel model) {
        System.out.println("\n--- Market Prices ---");
        Map<String, Double> prices = model.getAllPrices();
        if (prices.isEmpty()) {
            System.out.println("No stock data available.");
        } else {
            for (Map.Entry<String, Double> entry : prices.entrySet()) {
                System.out.printf("%-10s $%.2f%n", entry.getKey(), entry.getValue());
            }
        }
    }

    private static void searchStock(Scanner sc, StockModel model) {
        System.out.print("Enter stock symbol: ");
        String symbol = sc.nextLine().trim().toUpperCase();
        // ask for interval x amount of: day , week , month , year
        System.out.println("Enter the interval for historical data in the format <amount> <unit> (e.g., 30d, 4w, 6m, 1y) = (30 days, 4 weeks, 6 months, 1 year): ");
        String interval = sc.nextLine().trim().toLowerCase();
        if (!interval.matches("\\d+[dwmy]")) {
            System.out.println("Invalid interval format.");
            return;
        }
        int amount = Integer.parseInt(interval.substring(0, interval.length() - 1));
        char unit = interval.charAt(interval.length() - 1);
        System.out.printf("Fetching last %d %s of data for %s...\n", amount,
                          unit == 'd' ? "days" : unit == 'w' ? "weeks" : unit == 'm' ? "months" : "years", symbol);
        int days = unit == 'd' ? amount :
                   unit == 'w' ? amount * 7 :
                   unit == 'm' ? amount * 30 :
                   unit == 'y' ? amount * 365 : amount;
        model.getHistory(symbol, days);
    }

    private static void addStockData(Scanner sc, StockModel model) {
        try {
            System.out.print("Symbol: ");
            String symbol = sc.nextLine().trim().toUpperCase();
            System.out.print("Date (YYYY-MM-DD): ");
            String dateStr = sc.nextLine();
            Date date = Date.valueOf(dateStr);
            System.out.print("Open: ");
            double open = Double.parseDouble(sc.nextLine());
            System.out.print("High: ");
            double high = Double.parseDouble(sc.nextLine());
            System.out.print("Low: ");
            double low = Double.parseDouble(sc.nextLine());
            System.out.print("Close: ");
            double close = Double.parseDouble(sc.nextLine());
            System.out.print("Volume: ");
            int volume = Integer.parseInt(sc.nextLine());
            if(open < 0 || high < 0 || low < 0 || close < 0 || volume < 0) {
                System.out.println("Stock prices and volume must be non-negative.");
                return;
            }

            if (model.addStockData(symbol, date, open, high, low, close, volume)) {
                System.out.println("Stock data added successfully.");
            } else {
                System.out.println("Failed to add stock data.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format or number format.");
        } catch (Exception e) {
            System.out.println("Error adding data: " + e.getMessage());
        }
    }
}