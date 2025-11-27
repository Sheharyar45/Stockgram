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
    Connection conn;

    public StockModel(int userId) {
        try {
            this.conn = DBConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.userId = userId;
        stockPrices = new HashMap<>();
        makeStatsViews();
        loadStockPrices();
    }

    private void makeStatsViews() {
        String views = 
        "CREATE OR REPLACE TEMP VIEW combined_data AS " +
            "SELECT stock_symbol, timestamp, open, high, low, close, volume " +
            "FROM historicdata " +
            "UNION ALL " +
            "SELECT stock_symbol, timestamp, open, high, low, close, volume " +
            "FROM newstockdata WHERE user_id = " + this.userId + "; " +

        "CREATE OR REPLACE TEMP VIEW stock_returns AS " +
            "SELECT stock_symbol, timestamp, " +
            "       (close / NULLIF(LAG(close) OVER (PARTITION BY stock_symbol ORDER BY timestamp), 0) - 1.0) AS daily_return " +
            "FROM combined_data; " + 
        "" +
        "CREATE OR REPLACE TEMP VIEW market_returns AS " +
            "SELECT timestamp, AVG(daily_return) AS market_return " +
            "FROM stock_returns " +
            "WHERE daily_return IS NOT NULL " +
            "GROUP BY timestamp; ";
        try (Statement stmt = this.conn.createStatement()) {
            stmt.execute(views);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private void loadStockPrices() {
        stockPrices.clear();
        String query = "SELECT DISTINCT ON (stock_symbol) stock_symbol, close " +
                       "FROM combined_data " +
                       "ORDER BY stock_symbol, timestamp DESC";

        try (PreparedStatement stmt = this.conn.prepareStatement(query)) {
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
                       "FROM combined_data WHERE stock_symbol = ? ORDER BY timestamp DESC LIMIT ?";

        try (PreparedStatement stmt = this.conn.prepareStatement(query)) {
            
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

    public boolean addStockData(String symbol, Date date, double open, double high, double low, double close, int volume) {
        String stockExistsQuery = "SELECT 1 FROM historicdata WHERE stock_symbol = ?";
        String query = "INSERT INTO newstockdata (stock_symbol, timestamp, open, high, low, close, volume, user_id) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT (stock_symbol, timestamp, user_id) DO UPDATE SET open = ?, high = ?, " +
                       "low = ?, close = ?, volume = ?";
        String checkQuery = "SELECT 1 FROM historicdata WHERE stock_symbol = ? AND timestamp = ?";

        try (PreparedStatement stmt = this.conn.prepareStatement(query);
            PreparedStatement checkStmt = this.conn.prepareStatement(checkQuery);
            PreparedStatement stockCheckStmt = this.conn.prepareStatement(stockExistsQuery)) {
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
                makeStatsViews();
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
                       "FROM combined_data WHERE stock_symbol = ? ORDER BY timestamp DESC LIMIT ?";

        try (PreparedStatement stmt = this.conn.prepareStatement(query)) {
            
            stmt.setString(1, symbol);
            stmt.setInt(2, days);
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
        String sqlInterval = switch (unit) {
            case 'd' -> amount + " DAYS";
            case 'w' -> (amount * 7) + " DAYS";
            case 'm' -> amount + " MONTHS";
            case 'y' -> amount + " YEARS";
            default -> amount + " DAYS";
        };

        System.out.println("\n---- STOCK STATISTICS ----");
        System.out.println("Analysis period: " + interval + "\n");

        showCovAndBeta(symbols, sqlInterval);

        showCovarianceMatrix(symbols, sqlInterval);
    }


    private void showCovAndBeta(List<String> symbols, String Interval) {
        System.out.println("----- COEFFICIENT OF VARIATION & BETA -----");
        System.out.printf("%-10s %15s %15s%n", "Symbol", "COV", "Beta");
        System.out.println("------------------------------------------");

        String covBetaQuery = 
            "WITH " +
            "latest_date AS (" +
            "    SELECT MAX(timestamp) AS max_date FROM combined_data" +
            "), " +
            "updated_stock_returns AS (" +
            "    SELECT * FROM stock_returns, latest_date l " +
            "    WHERE timestamp >= l.max_date - ?::INTERVAL" +
            "), " +
            "updated_market_returns AS (" +
            "    SELECT * FROM market_returns, latest_date l " +
            "    WHERE timestamp >= l.max_date - ?::INTERVAL" +
            "), " +
            "stock_stats AS (" +
            "    SELECT " +
            "        sr.stock_symbol, " +
            "        AVG(sr.daily_return) AS mean_return, " +
            "        STDDEV(sr.daily_return) AS stddev_return, " +
            "        COVAR_POP(sr.daily_return, mr.market_return) AS cov_with_market " +
            "    FROM updated_stock_returns sr " +
            "    JOIN updated_market_returns mr ON sr.timestamp = mr.timestamp " +
            "    WHERE sr.daily_return IS NOT NULL " +
            "    GROUP BY sr.stock_symbol" +
            "), " +
            "market_variance AS (" +
            "    SELECT VAR_POP(market_return) AS var_market " +
            "    FROM updated_market_returns" +
            ") " +
            "SELECT " +
            "    ss.stock_symbol, " +
            "    CASE WHEN ss.mean_return <> 0 THEN ss.stddev_return / ss.mean_return ELSE NULL END AS cov, " +
            "    CASE WHEN mv.var_market <> 0 THEN ss.cov_with_market / mv.var_market ELSE NULL END AS beta " +
            "FROM stock_stats ss " +
            "CROSS JOIN market_variance mv " +
            "WHERE ss.stock_symbol = ?";

        try{
            double totalBeta = 0;
            double totalCov = 0;
            int count = 0;

            for (String symbol : symbols) {
                try (PreparedStatement stmt = this.conn.prepareStatement(covBetaQuery)) {
                    stmt.setString(1, Interval);
                    stmt.setString(2, Interval);
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

    private void showCovarianceMatrix(List<String> symbols, String Interval) {
        if (symbols.size() < 2) {
            System.out.println("Need at least 2 stocks for covariance matrix.");
            return;
        }

        System.out.println("----- COVARIANCE/CORRELATION MATRIX -----");

        String matrixQuery = 
            "WITH " +
            "latest_date AS (" +
            "    SELECT MAX(timestamp) AS max_date FROM combined_data" +
            "), " +
            "updated_stock_returns AS (" +
            "    SELECT * FROM stock_returns, latest_date l " +
            "    WHERE timestamp >= l.max_date - ?::INTERVAL" +
            "    AND stock_symbol = ANY(?)" +
            "), " +
            "pairs AS (" +
            "    SELECT a.stock_symbol AS s1, b.stock_symbol AS s2, a.daily_return AS r1, b.daily_return AS r2 " +
            "    FROM updated_stock_returns a JOIN updated_stock_returns b USING (timestamp) " +
            "    WHERE a.stock_symbol <= b.stock_symbol " +
            "    AND a.daily_return IS NOT NULL AND b.daily_return IS NOT NULL" +
            ") " +
            "SELECT s1, s2, " +
            "       COUNT(*) AS num, " +
            "       COVAR_SAMP(r1, r2) AS covar, " +
            "       CORR(r1, r2) AS corr " +
            "FROM pairs " +
            "GROUP BY s1, s2 " +
            "ORDER BY s1, s2";

        try (PreparedStatement stmt = this.conn.prepareStatement(matrixQuery)) {
            
            stmt.setString(1, Interval);
            stmt.setArray(2, this.conn.createArrayOf("VARCHAR", symbols.toArray()));
            
            ResultSet rs = stmt.executeQuery();
            
            int size = symbols.size();
            double[][] covariance = new double[size][size];
            double[][] correlation = new double[size][size];
            Map<String, Integer> symbolIndex = new HashMap<>();
            for (int i = 0; i < size; i++) {
                symbolIndex.put(symbols.get(i), i);
            }
            
            while (rs.next()) {
                String s1 = rs.getString("s1");
                String s2 = rs.getString("s2");
                double covar = rs.getDouble("covar");
                double corr = rs.getDouble("corr");
                
                Integer i = symbolIndex.get(s1);
                Integer j = symbolIndex.get(s2);
                
                if (i != null && j != null) {
                    covariance[i][j] = covariance[j][i] = covar;
                    correlation[i][j] = correlation[j][i] = corr;
                }
            }

            System.out.println("\nCovariance Matrix:");
            printMatrix(symbols, covariance);
            
            System.out.println("\nCorrelation Matrix:");
            printMatrix(symbols, correlation);
            
            System.out.println("\nInterpretation:");
            System.out.println("  Correlation near +1: Stocks move together");
            System.out.println("  Correlation near -1: Stocks move opposite");
            System.out.println("  Correlation near  0: Stocks move independently");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printMatrix(List<String> symbols, double[][] matrix) {
        System.out.printf("%10s", "");
        for (String s : symbols) {
            System.out.printf("%10s", s);
        }
        System.out.println();
        
        for (int i = 0; i < symbols.size(); i++) {
            System.out.printf("%10s", symbols.get(i));
            for (int j = 0; j < symbols.size(); j++) {
                System.out.printf("%10.4f", matrix[i][j]);
            }
            System.out.println();
        }
    }

    public void predictPrices(String symbol, int days) {
        String query = "SELECT timestamp, close " +
                       "FROM combined_data WHERE stock_symbol = ? ORDER BY timestamp ASC";
        String latestDate = "Select MAX(timestamp) as max_date from combined_data WHERE stock_symbol = ?";
        try (PreparedStatement stmt = this.conn.prepareStatement(query);
             PreparedStatement latestDateStmt = this.conn.prepareStatement(latestDate)) {
            stmt.setString(1, symbol);
            latestDateStmt.setString(1, symbol);
            try (ResultSet rs = stmt.executeQuery();
                 ResultSet latestRs = latestDateStmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No historical data found for stock: " + symbol);
                    return;
                }
                Date maxDate = null;
                if (latestRs.next()) {
                    maxDate = latestRs.getDate("max_date");
                }
                List<Double> prices = new ArrayList<>();
                while (rs.next()) {
                    prices.add(rs.getDouble("close"));
                }
                if (prices.size() < 2) {
                    System.out.println("Not enough data to make predictions for " + symbol);
                    return;
                }
                predict(prices, days, maxDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void predict(List<Double> prices, int days, Date maxDate) {
        int n = prices.size();

        // Compute linear regression (slope + intercept), I found this formula online
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = prices.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Step for sampling points (for large days), so that we dont get too many points if user chooses large days
        int maxPoints = 50;
        int step = Math.max(1, days / maxPoints);

        List<Double> predictions = new ArrayList<>();
        for (int i = n; i < n + days; i++) {
            if ((i - n) % step == 0 || i == n + days - 1) {
                predictions.add(slope * i + intercept);
            }
        }

        printAsciiGraph(predictions, maxDate, step);
    }

    private void printAsciiGraph(List<Double> predictions, Date maxDate, int step) {
        if (predictions.isEmpty()) {
            System.out.println("No predictions to display.");
            return;
        }
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Double val : predictions) {
            min = Math.min(min, val);
            max = Math.max(max, val);
        }

        int width = 50;
        System.out.println("\n--- Price Predictions ---");
        System.out.printf("Min: $%.2f, Max: $%.2f%n", min, max);

        int dayOffset = 1;
        for (Double val : predictions) {
            Date predictionDate = new Date(maxDate.getTime() + (long) dayOffset * 24 * 60 * 60 * 1000);
            
            int stars = (int) Math.round((val - min) / (max - min) * width);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < stars; i++) sb.append("*");

            System.out.printf("%s | %-50s $%.2f%n", predictionDate.toString(), sb.toString(), val);
            dayOffset += step;
        }

        System.out.print("\n\n\nSparkline: ");
        for (Double val : predictions) {
            int index = (int) Math.round((val - min) / (max - min) * 7);
            char[] blocks = {'▁','▂','▃','▄','▅','▆','▇','█'};
            System.out.print(blocks[Math.min(index, blocks.length - 1)]);
        }
        System.out.println();
    }
        



}