package cs.toronto.edu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        try {
            // Register the PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Connect to the database
            conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/mydb", "postgres", "postgres");
            System.out.println("Opened database successfully");

            // Create a statement object
            stmt = conn.createStatement();

            // Query the first 10 rows from HistoricData
            String sqlCheck = "SELECT timestamp, Stock_symbol, close FROM HistoricData ORDER BY timestamp DESC LIMIT 10;";
            ResultSet rsCheck = stmt.executeQuery(sqlCheck);

            System.out.println("First 10 rows of HistoricData:");
            System.out.println("Timestamp \tSymbol \tClose");
            while (rsCheck.next()) {
                String timestamp = rsCheck.getDate("timestamp").toString();
                String symbol = rsCheck.getString("Stock_symbol");
                float close = rsCheck.getFloat("close");
                System.out.println(timestamp + "\t" + symbol + "\t" + close);
            }
            rsCheck.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                System.out.println("Disconnected from the database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}