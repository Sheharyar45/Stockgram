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

    public List<Map<String, Object>> getStockGraphData(String symbol, int days) {
        List<Map<String, Object>> history = new ArrayList<>();
        String query = "SELECT timestamp, close " +
                       "FROM (" +
                       "    SELECT stock_symbol, timestamp, close FROM historicdata " +
                       "    UNION ALL " +
                       "    SELECT stock_symbol, timestamp, close FROM newstockdata WHERE user_id = ?" +
                       ") AS all_data " + "WHERE stock_symbol = ? ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, this.userId);
            stmt.setString(2, symbol);
            stmt.setInt(3, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("timestamp", rs.getDate("timestamp"));
                    point.put("close", rs.getDouble("close"));
                    history.add(point);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Reverse for making graph old to new
        List<Map<String, Object>> reversed = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0; i--) {
            reversed.add(history.get(i));
        }
        return reversed;
    }

    public void showStatistics(List<String> symbols, Scanner sc) {
        if (symbols == null || symbols.isEmpty()) {
            System.out.println("No stocks to analyze.");
            return;
        }

        // Get time interval from user
        System.out.print("Enter the interval for analysis (e.g., 30d, 4w, 6m, 1y): ");
        String interval = sc.nextLine().trim().toLowerCase();
        if (!interval.matches("\\d+[dwmy]")) {
            System.out.println("Invalid interval format. Use format like: 30d, 4w, 6m, 1y");
            return;
        }
        
        int amount = Integer.parseInt(interval.substring(0, interval.length() - 1));
        char unit = interval.charAt(interval.length() - 1);
        String pgInterval = switch (unit) {
            case 'd' -> amount + " DAYS";
            case 'w' -> (amount * 7) + " DAYS";
            case 'm' -> amount + " MONTHS";
            case 'y' -> amount + " YEARS";
            default -> amount + " DAYS";
        };

        System.out.println("\n===== STOCK STATISTICS =====");
        System.out.println("Analysis period: " + interval + "\n");

        // 1. Calculate COV and Beta for each stock
        showCovAndBeta(symbols, pgInterval);

        // 2. Calculate covariance matrix
        // showCovarianceMatrix(symbols, pgInterval);
    }

        private String getCombinedsql() {
        return "combined_data AS (" +
               "    SELECT stock_symbol, timestamp, close FROM historicdata " +
               "    UNION ALL " +
               "    SELECT stock_symbol, timestamp, close FROM newstockdata WHERE user_id = ?" +
               ")";
    }

    private void showCovAndBeta(List<String> symbols, String pgInterval) {
        System.out.println("----- COEFFICIENT OF VARIATION & BETA -----");
        System.out.printf("%-10s %15s %15s%n", "Symbol", "COV", "Beta");
        System.out.println("------------------------------------------");

        String covBetaQuery = 
            "WITH " + getCombinedsql() + ", " +
            "latest_date AS (" +
            "    SELECT MAX(timestamp) AS max_date FROM combined_data" +
            "), " +
            "stock_returns AS (" +
            "    SELECT " +
            "        stock_symbol, " +
            "        timestamp, " +
            "        (close - LAG(close) OVER (PARTITION BY stock_symbol ORDER BY timestamp)) " +
            "            / NULLIF(LAG(close) OVER (PARTITION BY stock_symbol ORDER BY timestamp), 0) AS daily_return " +
            "    FROM combined_data, latest_date " +
            "    WHERE timestamp >= latest_date.max_date - ?::INTERVAL" +
            "), " +
            "market_returns AS (" +
            "    SELECT " +
            "        timestamp, " +
            "        AVG(daily_return) AS market_return " +
            "    FROM stock_returns " +
            "    WHERE daily_return IS NOT NULL " +
            "    GROUP BY timestamp" +
            "), " +
            "stock_stats AS (" +
            "    SELECT " +
            "        sr.stock_symbol, " +
            "        AVG(sr.daily_return) AS mean_return, " +
            "        STDDEV(sr.daily_return) AS stddev_return, " +
            "        COVAR_POP(sr.daily_return, mr.market_return) AS cov_with_market " +
            "    FROM stock_returns sr " +
            "    JOIN market_returns mr ON sr.timestamp = mr.timestamp " +
            "    WHERE sr.daily_return IS NOT NULL " +
            "    GROUP BY sr.stock_symbol" +
            "), " +
            "market_variance AS (" +
            "    SELECT VAR_POP(market_return) AS var_market " +
            "    FROM market_returns" +
            ") " +
            "SELECT " +
            "    ss.stock_symbol, " +
            "    CASE WHEN ss.mean_return <> 0 THEN ss.stddev_return / ss.mean_return ELSE NULL END AS cov, " +
            "    CASE WHEN mv.var_market <> 0 THEN ss.cov_with_market / mv.var_market ELSE NULL END AS beta " +
            "FROM stock_stats ss " +
            "CROSS JOIN market_variance mv " +
            "WHERE ss.stock_symbol = ?";

        try (Connection conn = DBConnection.getConnection()) {
            double totalBeta = 0;
            double totalCov = 0;
            int count = 0;

            for (String symbol : symbols) {
                try (PreparedStatement stmt = conn.prepareStatement(covBetaQuery)) {
                    stmt.setInt(1, this.userId);
                    stmt.setString(2, pgInterval);
                    stmt.setString(3, symbol);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        double cov = rs.getDouble("cov");
                        boolean covNull = rs.wasNull();
                        double beta = rs.getDouble("beta");
                        boolean betaNull = rs.wasNull();
                        
                        if (!covNull && !betaNull) {
                            System.out.printf("%-10s %15.4f %15.4f%n", symbol, cov, beta);
                            totalCov += cov;
                            totalBeta += beta;
                            count++;
                        } else {
                            System.out.printf("%-10s %15s %15s%n", symbol, "N/A", "N/A");
                        }
                    } else {
                        System.out.printf("%-10s %15s %15s%n", symbol, "N/A", "N/A");
                    }
                }
            }

            if (count > 0) {
                System.out.println("------------------------------------------");
                System.out.printf("%-10s %15.4f %15.4f%n", "Average", totalCov / count, totalBeta / count);
                System.out.println("\nInterpretation:");
                System.out.println("  COV: Higher = more volatile/risky");
                System.out.println("  Beta > 1: More volatile than market");
                System.out.println("  Beta < 1: Less volatile than market");
                System.out.println("  Beta = 1: Same volatility as market");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }

}