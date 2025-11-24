package cs.toronto.edu;
import cs.toronto.edu.model.PortfolioModel;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.model.TransactionsModel;


public class TransactionsService {

    public static void menu(int userId) {
        Scanner scanner = new Scanner(System.in);
        boolean viewing = true;
        TransactionsModel transactionsModel = new TransactionsModel(userId);

        while (viewing) {
            System.out.println("\n===== TRANSACTIONS MENU =====");
            System.out.println("View transactions from the last:");
            System.out.println("1. Hours");
            System.out.println("2. Days");
            System.out.println("3. Months");
            System.out.println("4. Years");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();
            String intervalType = "";

            switch (input) {
                case "1": intervalType = "HOURS"; break;
                case "2": intervalType = "DAYS"; break;
                case "3": intervalType = "MONTHS"; break;
                case "4": intervalType = "YEARS"; break;
                case "5": viewing = false; continue;
                default:
                    System.out.println("Invalid option.");
                    continue;
            }

            System.out.print("Enter number of " + intervalType.toLowerCase() + ": ");
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value <= 0) {
                    System.out.println("Please enter a positive number.");
                    continue;
                }
                transactionsModel.viewTransactions(intervalType, value);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter an integer.");
            }
        }
    }
}