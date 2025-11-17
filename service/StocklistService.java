package cs.toronto.edu;

import java.sql.*;
import java.util.Scanner;
import cs.toronto.edu.db.DBConnection;

public class StocklistService {

    public static void menu(int userId) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== STOCKLIST MENU =====");
            System.out.println("1. View my stocklists");
            System.out.println("2. Create stocklist");
            System.out.println("3. Delete stocklist");
            System.out.println("4. View stocks in a stocklist");
            System.out.println("5. Add stock to stocklist");
            System.out.println("6. Remove stock from stocklist");
            System.out.println("7. Change visibility");
            System.out.println("8. Back");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine()) {
                case "1": viewStocklists(userId); break;
                case "2": createStocklist(userId, scanner); break;
                case "3": deleteStocklist(userId, scanner); break;
                case "4": viewStocklistContents(userId, scanner); break;
                case "5": addStock(userId, scanner); break;
                case "6": removeStock(userId, scanner); break;
                case "7": changeVisibility(userId, scanner); break;
                case "8": running = false; break;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // =============================================
    // 1. View user stocklists
    // =============================================
    private static void viewStocklists(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT stocklist_id, visibility FROM Stocklist WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            System.out.println("\nYour Stocklists:");
            boolean empty = true;

            while (rs.next()) {
                empty = false;
                System.out.println("ID: " + rs.getInt("stocklist_id") +
                                   " | Visibility: " + rs.getString("visibility"));
            }

            if (empty) System.out.println("No stocklists found.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // 2. Create stocklist
    // =============================================
    private static void createStocklist(int userId, Scanner scanner) {
        System.out.print("Enter visibility (public/private): ");
        String visibility = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "INSERT INTO Stocklist (user_id, visibility) VALUES (?, ?) RETURNING stocklist_id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, visibility);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Created stocklist with ID: " + rs.getInt("stocklist_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // 3. Delete stocklist
    // =============================================
    private static void deleteStocklist(int userId, Scanner scanner) {
        System.out.print("Enter stocklist ID to delete: ");
        int listId = Integer.parseInt(scanner.nextLine());

        try (Connection conn = DBConnection.getConnection()) {

            // Check ownership  
            String check = "SELECT * FROM Stocklist WHERE stocklist_id=? AND user_id=?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setInt(1, listId);
            psCheck.setInt(2, userId);
            ResultSet rs = psCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("Stocklist not found or not owned by you.");
                return;
            }

            // Delete the stocklist (ON DELETE CASCADE handles holdings)
            String sql = "DELETE FROM Stocklist WHERE stocklist_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, listId);

            ps.executeUpdate();
            System.out.println("Stocklist deleted.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // 4. View holdings inside a stocklist
    // =============================================
    private static void viewStocklistContents(int userId, Scanner scanner) {
        System.out.print("Enter stocklist ID: ");
        int listId = Integer.parseInt(scanner.nextLine());

        try (Connection conn = DBConnection.getConnection()) {

            // Ownership check
            String check = "SELECT * FROM Stocklist WHERE stocklist_id=? AND user_id=?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setInt(1, listId);
            psCheck.setInt(2, userId);
            if (!psCheck.executeQuery().next()) {
                System.out.println("Stocklist not found or not yours.");
                return;
            }

            String sql = 
                "SELECT sh.stock_symbol, sh.shares " +
                "FROM StocklistHoldings sh " +
                "WHERE sh.stocklist_id=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, listId);

            ResultSet rs = ps.executeQuery();
            System.out.println("\nStocks in Stocklist:");
            boolean empty = true;

            while (rs.next()) {
                empty = false;
                System.out.println(rs.getString("stock_symbol") + " | Shares: " + rs.getInt("shares"));
            }

            if (empty) System.out.println("This stocklist is empty.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // 5. Add stock to stocklist
    // =============================================
    private static void addStock(int userId, Scanner scanner) {
        System.out.print("Stocklist ID: ");
        int listId = Integer.parseInt(scanner.nextLine());
        System.out.print("Stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Shares: ");
        int shares = Integer.parseInt(scanner.nextLine());

        try (Connection conn = DBConnection.getConnection()) {

            // Check ownership
            String check = "SELECT * FROM Stocklist WHERE stocklist_id=? AND user_id=?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setInt(1, listId);
            psCheck.setInt(2, userId);
            if (!psCheck.executeQuery().next()) {
                System.out.println("Not your stocklist.");
                return;
            }

            // Insert or update
            String sql =
                "INSERT INTO StocklistHoldings (stocklist_id, stock_symbol, shares) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (stocklist_id, stock_symbol) DO UPDATE SET shares = EXCLUDED.shares";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, listId);
            ps.setString(2, symbol);
            ps.setInt(3, shares);

            ps.executeUpdate();
            System.out.println("Stock added/updated.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // 6. Remove stock from stocklist
    // =============================================
    private static void removeStock(int userId, Scanner scanner) {
        System.out.print("Stocklist ID: ");
        int listId = Integer.parseInt(scanner.nextLine());
        System.out.print("Stock symbol: ");
        String symbol = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {

            String check = "SELECT * FROM Stocklist WHERE stocklist_id=? AND user_id=?";
            PreparedStatement psCheck = conn.prepareStatement(check);
            psCheck.setInt(1, listId);
            psCheck.setInt(2, userId);
            if (!psCheck.executeQuery().next()) {
                System.out.println("Not your stocklist.");
                return;
            }

            String sql =
                "DELETE FROM StocklistHoldings WHERE stocklist_id=? AND stock_symbol=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, listId);
            ps.setString(2, symbol);

            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("Stock removed.");
            else System.out.println("Stock not found in list.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // 7. Change visibility
    // =============================================
    private static void changeVisibility(int userId, Scanner scanner) {
        System.out.print("Stocklist ID: ");
        int listId = Integer.parseInt(scanner.nextLine());
        System.out.print("New visibility (public/private): ");
        String visibility = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {

            String sql =
                "UPDATE Stocklist SET visibility=? WHERE stocklist_id=? AND user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, visibility);
            ps.setInt(2, listId);
            ps.setInt(3, userId);

            int rows = ps.executeUpdate();

            if (rows > 0) System.out.println("Visibility updated.");
            else System.out.println("Stocklist not found or unauthorized.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
