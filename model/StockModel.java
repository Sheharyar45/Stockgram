package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.db.DBConnection;

public class StockModel{
    private Map<String, Double> stockPrices;

    public StockModel() {
        stockPrices = new HashMap<>();
        loadStockPrices();
    }
    

    private void loadStockPrices() {
        stockPrices.clear();
        String query = "SELECT DISTINCT ON (stock_symbol) stock_symbol, close " +
                       "FROM historicdata ORDER BY stock_symbol, timestamp DESC";

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
}