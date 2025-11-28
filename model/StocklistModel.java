package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import cs.toronto.edu.db.DBConnection;
import cs.toronto.edu.model.StockModel;

public class StocklistModel {

    private int userId;

    public StocklistModel(int userId) {
        this.userId = userId;
    }

    public static Integer createStocklist(int userId, String visibility) {
        String sql = "INSERT INTO stocklist (user_id, visibility) VALUES (?, ?) RETURNING stocklist_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, visibility);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("stocklist_id");
            }

        } catch (Exception e) {
            System.out.println("Error creating stocklist: " + e.getMessage());
        }
        return null;
    }

    public static boolean isOwner(int userId, int stocklistId) {
        String sql = "SELECT 1 FROM stocklist WHERE stocklist_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stocklistId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void viewStocklists(int userId) {
        String sql = "SELECT stocklist_id, visibility FROM stocklist WHERE user_id = ? ORDER BY stocklist_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- YOUR STOCKLISTS ---");
            boolean hasAny = false;

            while (rs.next()) {
                hasAny = true;
                System.out.printf("ID: %d | Visibility: %s\n",
                                  rs.getInt("stocklist_id"),
                                  rs.getString("visibility"));
            }

            if (!hasAny) {
                System.out.println("You have no stocklists.");
            }

        } catch (Exception e) {
            System.out.println("Error viewing stocklists: " + e.getMessage());
        }
    }

    public static void viewStocksInList(int stocklistId) {
        String sql = "SELECT stock_symbol, shares FROM stocklistholdings WHERE stocklist_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stocklistId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- STOCKS IN LIST " + stocklistId + " ---");
            boolean empty = true;

            while (rs.next()) {
                empty = false;
                System.out.printf("%s | %.2f shares\n",
                                  rs.getString("stock_symbol"),
                                  rs.getDouble("shares"));
            }

            if (empty) {
                System.out.println("This list is empty.");
            }

        } catch (Exception e) {
            System.out.println("Error viewing stocks: " + e.getMessage());
        }
    }

    public static boolean addStockToList(int userId, int stocklistId, String symbol, double shares, StockModel stockModel) {
        if (!isOwner(userId, stocklistId)) {
            System.out.println("Only the owner can modify this stocklist.");
            return false;
        }

        symbol = symbol.trim().toUpperCase();
        if (symbol.isEmpty() || shares <= 0) {
            System.out.println("Invalid stock symbol or number of shares.");
            return false;
        }

        if (stockModel.getStockPrice(symbol) < 0) {
            System.out.println("Stock symbol not found.");
            return false;
        }

        String selectSql = "SELECT shares FROM stocklistholdings WHERE stocklist_id = ? AND stock_symbol = ?";
        String updateSql = "UPDATE stocklistholdings SET shares = shares + ? WHERE stocklist_id = ? AND stock_symbol = ?";
        String insertSql = "INSERT INTO stocklistholdings(stocklist_id, stock_symbol, shares) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setInt(1, stocklistId);
            selectStmt.setString(2, symbol);

            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                // Stock exists, update the shares
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, shares);
                    updateStmt.setInt(2, stocklistId);
                    updateStmt.setString(3, symbol);
                    updateStmt.executeUpdate();
                }
                System.out.println("Added " + shares + " shares to existing stock.");
            } else {
                // Stock does not exist, insert new
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, stocklistId);
                    insertStmt.setString(2, symbol);
                    insertStmt.setDouble(3, shares);
                    insertStmt.executeUpdate();
                }
                System.out.println("Stock added to the list.");
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean removeStockFromList(int userId, int stocklistId, String symbol) {
        if (!isOwner(userId, stocklistId)) {
            System.out.println("Only the owner can modify this stocklist.");
            return false;
        }

        symbol = symbol.trim().toUpperCase();
        String sql = "DELETE FROM stocklistholdings WHERE stocklist_id = ? AND stock_symbol = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stocklistId);
            stmt.setString(2, symbol);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Stock not found in this list.");
                return false;
            }

            System.out.println("Stock removed from the list.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void showStatistics(int stocklistId, Scanner sc, StockModel stockModel) {
        // Get stocks in the stocklist
        List<String> stocklistStocks = new ArrayList<>();
        String sql = "SELECT stock_symbol FROM stocklistholdings WHERE stocklist_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stocklistId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stocklistStocks.add(rs.getString("stock_symbol"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (stocklistStocks.isEmpty()) {
            System.out.println("No stocks in this stocklist to analyze.");
            return;
        }

        System.out.println("\n===== STOCKLIST STATISTICS =====");
        stockModel.showStatistics(stocklistStocks, sc);
    }

    public static boolean deleteStocklist(int userId, int stocklistId) {
        if (!isOwner(userId, stocklistId)) {
            System.out.println("Only the owner can modify this stocklist.");
            return false;
        }


        String sql = "DELETE FROM stocklist WHERE stocklist_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stocklistId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Stocklist deleted successfully.");
                return true;
            } else {
                System.out.println("Failed to delete stocklist.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getPublicStocklists() {
        List<String> results = new ArrayList<>();

        String sql =
            "SELECT s.stocklist_id, u.username " +
            "FROM stocklist s " +
            "JOIN users u ON s.user_id = u.user_id " +
            "WHERE s.visibility = 'public' " +
            "ORDER BY s.stocklist_id";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("stocklist_id");
                String username = rs.getString("username");

                results.add("ID: " + id + " | Owner: " + username);
            }

        } catch (Exception e) {
            System.out.println("Error fetching public stocklists: " + e.getMessage());
        }

        return results;
    }

    public static boolean sendToFriend(int senderId, int receiverId, int stocklistId) {
        String sql = "INSERT INTO requestreview (sender, receiver, stocklist_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, stocklistId);
            stmt.executeUpdate();
            System.out.println("Stocklist sent to friend successfully.");
            return true;

        } catch (Exception e) { // catch all checked exceptions
            if (e instanceof SQLException sqlEx && sqlEx.getSQLState().equals("23505")) {
                System.out.println("You have already sent this stocklist to this friend.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }


    public static List<Integer> getInvitedStocklists(int userId) {
        List<Integer> stocklists = new ArrayList<>();
        String sql = "SELECT stocklist_id FROM requestreview WHERE receiver = ? ORDER BY stocklist_id";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                stocklists.add(rs.getInt("stocklist_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stocklists;
    }

    public static boolean isInvitedReviewer(int userId, int stocklistId) {
        String sql = "SELECT 1 FROM requestreview WHERE receiver = ? AND stocklist_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, stocklistId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getVisibility(int stocklistId) {
        String sql = "SELECT visibility FROM stocklist WHERE stocklist_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stocklistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
                return rs.getString("visibility");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void predictPrices(int stocklistId, Scanner sc, StockModel stockModel) {
        viewStocksInList(stocklistId);
        System.out.print("Enter stock symbol to view predictions: ");
        String symbol = sc.nextLine().trim().toUpperCase();
        
        System.out.println("Enter the interval (e.g., 30d, 4w, 6m, 1y): ");
        String interval = sc.nextLine().trim().toLowerCase();
        if (!interval.matches("\\d+[dwmy]")) {
            System.out.println("Invalid interval format.");
            return;
        }
        int amount = Integer.parseInt(interval.substring(0, interval.length() - 1));
        char unit = interval.charAt(interval.length() - 1);
        int days = unit == 'd' ? amount :
                unit == 'w' ? amount * 7 :
                unit == 'm' ? amount * 30 :
                unit == 'y' ? amount * 365 : amount;
        stockModel.predictPrices(symbol, days);
    }
}
