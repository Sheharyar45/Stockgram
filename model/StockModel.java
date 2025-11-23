package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.db.DBConnection;

public class StockModel{
    public Map<String, Double> stockPrices;
    private int userId;

    public StockModel(int userId) {
        this.userId = userId;
        stockPrices = new HashMap<>();
        loadStockPrices();
    }
    

    private void loadStockPrices() {
        stockPrices.clear();
        String query = "SELECT DISTINCT ON (stock_symbol) stock_symbol, close " +
                       "FROM (" +
                       "    SELECT stock_symbol, timestamp, close FROM historicdata " +
                       "    UNION ALL " +
                       "    SELECT stock_symbol, timestamp, close FROM newstockdata WHERE user_id = ?" +
                       ") AS all_data " +
                       "ORDER BY stock_symbol, timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, this.userId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String symbol = rs.getString("stock_symbol");
                    double closePrice = rs.getDouble("close");
                    stockPrices.put(symbol, closePrice);
                }
            }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getStockPrice(String symbol) {
        return stockPrices.getOrDefault(symbol, -1.0);
    }

    public Map<String, Double> getAllPrices() {
        return stockPrices;
    }

    public void getHistory(String symbol, int days) {
        String query = "SELECT timestamp, open, high, low, close, volume " +
                       "FROM (" +
                       "    SELECT stock_symbol, timestamp, open, high, low, close, volume FROM historicdata " +
                       "    UNION ALL " +
                       "    SELECT stock_symbol, timestamp, open, high, low, close, volume FROM newstockdata WHERE user_id = ?" +
                       ") AS all_data " + "WHERE stock_symbol = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, this.userId);
            stmt.setString(2, symbol);
            stmt.setInt(3, days);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No historical data found for stock: " + symbol);
                    return;
                }
                System.out.printf("\n--- Historical Data for %s ---\n", symbol);
                System.out.printf("%-20s %-10s %-10s %-10s %-10s %-10s%n", 
                                  "Timestamp", "Open", "High", "Low", "Close", "Volume");
                while (rs.next()) {
                    System.out.printf("%-20s $%-9.2f $%-9.2f $%-9.2f $%-9.2f %-10d%n",
                                      rs.getDate("timestamp").toString(),
                                      rs.getDouble("open"),
                                      rs.getDouble("high"),
                                      rs.getDouble("low"),
                                      rs.getDouble("close"),
                                      rs.getLong("volume"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addStockData(String symbol, Date date, double open, double high, double low, double close, int volume) {
        String stockExistsQuery = "SELECT 1 FROM historicdata WHERE stock_symbol = ?";
        String query = "INSERT INTO newstockdata (stock_symbol, timestamp, open, high, low, close, volume, user_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT (stock_symbol, timestamp, user_id) DO UPDATE SET open = ?, high = ?, " +
                       "low = ?, close = ?, volume = ?";
        String checkQuery = "SELECT 1 FROM historicdata WHERE stock_symbol = ? AND timestamp = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            PreparedStatement stockCheckStmt = conn.prepareStatement(stockExistsQuery)) {
            // Check if stock exists in stocks table
            stockCheckStmt.setString(1, symbol);
            ResultSet stockRs = stockCheckStmt.executeQuery();
            if (!stockRs.next()) {
                System.out.println("Stock symbol " + symbol + " does not exist in the system.");
                return false;
            }
            // Check for existing entry
            checkStmt.setString(1, symbol);
            checkStmt.setDate(2, date);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("An entry for " + symbol + " on " + date.toString() + " already exists.");
                return false;
            }
            stmt.setString(1, symbol);
            stmt.setDate(2, date);
            stmt.setDouble(3, open);
            stmt.setDouble(4, high);
            stmt.setDouble(5, low);
            stmt.setDouble(6, close);
            stmt.setInt(7, volume);
            stmt.setInt(8, this.userId);
            stmt.setDouble(9, open);
            stmt.setDouble(10, high);
            stmt.setDouble(11, low);
            stmt.setDouble(12, close);
            stmt.setInt(13, volume);   

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                loadStockPrices();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
}