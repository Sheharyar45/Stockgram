package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import cs.toronto.edu.db.DBConnection;

public class StocklistModel {

    private int userId;

    public StocklistModel(int userId) {
        this.userId = userId;
    }

    // ---------------- STOCKLIST MANAGEMENT ----------------
    public static void viewStocklists(int userId) {
        String query = "SELECT stocklist_id, visibility FROM stocklist WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n----- YOUR STOCKLISTS -----");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("- ID: %d | Visibility: %s%n", rs.getInt("stocklist_id"), rs.getString("visibility"));
            }
            if (!found) {
                System.out.println("No stocklists found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean createStocklist(int userId, String visibility) {
        if (visibility == null || (!visibility.equalsIgnoreCase("public") && !visibility.equalsIgnoreCase("private"))) {
            System.out.println("Visibility must be 'public' or 'private'.");
            return false;
        }

        String insertQuery = "INSERT INTO stocklist (user_id, visibility) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setInt(1, userId);
            stmt.setString(2, visibility.toLowerCase());
            stmt.executeUpdate();
            System.out.println("Stocklist created successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getStocklistIdByUserAndVisibility(int userId, String visibility) {
        String query = "SELECT stocklist_id FROM stocklist WHERE user_id = ? AND visibility = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setString(2, visibility.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("stocklist_id");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ---------------- STOCK MANAGEMENT ----------------
    public boolean addStockToList(int listId, String symbol, double shares) {
        String insert = "INSERT INTO stocklistholdings (stocklist_id, stock_symbol, shares) " +
                        "VALUES (?, ?, ?) " +
                        "ON CONFLICT (stocklist_id, stock_symbol) DO UPDATE SET shares = stocklistholdings.shares + ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insert)) {

            stmt.setInt(1, listId);
            stmt.setString(2, symbol.toUpperCase());
            stmt.setDouble(3, shares);
            stmt.setDouble(4, shares);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeStockFromList(int listId, String symbol) {
        String delete = "DELETE FROM stocklistholdings WHERE stocklist_id = ? AND stock_symbol = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(delete)) {

            stmt.setInt(1, listId);
            stmt.setString(2, symbol.toUpperCase());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void viewStocksInList(int listId) {
        String query = "SELECT stock_symbol, shares FROM stocklistholdings WHERE stocklist_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n----- STOCKS IN THIS LIST -----");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("- %s: %.2f shares%n", rs.getString("stock_symbol"), rs.getDouble("shares"));
            }
            if (!found) {
                System.out.println("No stocks in this list.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean stocklistExists(int userId, int listId) {
        String query = "SELECT 1 FROM stocklist WHERE stocklist_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
