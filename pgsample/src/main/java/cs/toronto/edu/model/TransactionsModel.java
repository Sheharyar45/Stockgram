package cs.toronto.edu.model;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.db.DBConnection;


public class TransactionsModel {
    private int userId;

    public TransactionsModel(int userId) {
        this.userId = userId;
    }

    public void viewTransactions(String intervalType, int value) {
        String sql = "SELECT t.type, t.amount, t.shares, t.stock_symbol, t.timestamp, p.name " +
                     "FROM transactions t " +
                     "JOIN portfolios p ON t.portfolio_id = p.portfolio_id " +
                     "WHERE p.user_id = ? " +
                     "AND t.timestamp >= NOW() - (? || ' " + intervalType.toLowerCase() + "')::INTERVAL " +
                     "ORDER BY t.timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, value);

            ResultSet rs = pstmt.executeQuery();

            // dont show transaction id
            System.out.printf("%-15s %-10s %-15s %-10s %-15s %-25s%n",
                              "Portfolio Name", "Type", "Amount", "Shares", "Stock Symbol", "Transaction Time");
            System.out.println("---------------------------------------------------------------------------------------");

            while (rs.next()) {
                String stockSymbol = rs.getString("stock_symbol") != null ? rs.getString("stock_symbol") : "N/A";
                String transactionType = rs.getString("type");
                int amount = rs.getInt("amount");
                int shares = rs.getInt("shares");
                Timestamp transactionTime = rs.getTimestamp("timestamp");

                System.out.printf("%-15s %-10s %-15d %-10d %-15s %-25s%n",
                                  rs.getString("name"), transactionType, amount, shares, stockSymbol, transactionTime.toString());
            }

        } catch (Exception e) {
            System.out.println("Error retrieving transactions: " + e.getMessage());
        }
    }   
}