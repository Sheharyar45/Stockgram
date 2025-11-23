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

    public StockModel() {
        stockPrices = new HashMap<>();
        loadStockPrices();
    }
    

    private void loadStockPrices() {
        stockPrices.clear();
        String query = "SELECT DISTINCT ON (stock_symbol) stock_symbol, close " +
                       "FROM historicdata ORDER BY stock_symbol DESC, timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                stockPrices.put(rs.getString("stock_symbol"), rs.getDouble("close"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getStockPrice(String symbol) {
        return stockPrices.getOrDefault(symbol, -1.0);
    }

    public Map<String, Double> getAllPrices() {
        return stockPrices;
    }

    public static void getHistory(String symbol, int days) {
        String query = "SELECT timestamp, open, high, low, close, volume " +
                       "FROM historicdata WHERE stock_symbol = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, symbol);
            stmt.setInt(2, days);
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
}