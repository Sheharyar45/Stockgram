package cs.toronto.edu;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.model.StockModel;

public class StockService {

    public static void menu(int userId) {
        Scanner sc = new Scanner(System.in);
        StockModel stockModel = new StockModel();
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
                    searchStock(sc);
                    break;
                case "3":
                    addStockData(sc);
                    // Refresh model to reflect potential new latest prices
                    stockModel = new StockModel();
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

    private static void searchStock(Scanner sc) {
        System.out.print("Enter stock symbol: ");
        String symbol = sc.nextLine().trim().toUpperCase();
        StockModel.getHistory(symbol);
    }

    private static void addStockData(Scanner sc) {
    //     try {
    //         System.out.print("Symbol: ");
    //         String symbol = sc.nextLine().trim().toUpperCase();
    //         System.out.print("Date (YYYY-MM-DD): ");
    //         String dateStr = sc.nextLine();
    //         Date date = Date.valueOf(dateStr);

    //         System.out.print("Open: ");
    //         double open = Double.parseDouble(sc.nextLine());
    //         System.out.print("High: ");
    //         double high = Double.parseDouble(sc.nextLine());
    //         System.out.print("Low: ");
    //         double low = Double.parseDouble(sc.nextLine());
    //         System.out.print("Close: ");
    //         double close = Double.parseDouble(sc.nextLine());
    //         System.out.print("Volume: ");
    //         int volume = Integer.parseInt(sc.nextLine());

    //         if (StockModel.addStockData(symbol, date, open, high, low, close, volume)) {
    //             System.out.println("Stock data added successfully.");
    //         } else {
    //             System.out.println("Failed to add stock data.");
    //         }
    //     } catch (IllegalArgumentException e) {
    //         System.out.println("Invalid date format or number format.");
    //     } catch (Exception e) {
    //         System.out.println("Error adding data: " + e.getMessage());
    //     }
    }
}