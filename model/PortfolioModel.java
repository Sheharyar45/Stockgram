package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cs.toronto.edu.db.DBConnection;

public class PortfolioModel {

    public static void viewPortfolios(int userId) {
        String query = "SELECT name, cash_amount FROM Portfolios WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n===== YOUR PORTFOLIOS =====");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("- %s (Cash: $%.2f)%n", rs.getString("name"), rs.getDouble("cash_amount"));
            }
            if (!found) {
                System.out.println("No portfolios found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean createPortfolio(int userId, String portfolioName) {
        // check if portfolio with same name exists for user
        String name = portfolioName.trim();
        if (name.isEmpty()) {
            System.out.println("Portfolio name cannot be empty.");
            return false;
        }
        String checkQuery = "SELECT COUNT(*) AS count FROM Portfolios WHERE user_id = ? AND name = ?";
        String insertQuery = "INSERT INTO Portfolios (user_id, name, cash_amount) VALUES (?, ?, 0)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, name);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Portfolio with this name already exists.");
                return false;
            }
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, name);
            insertStmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getPortfolioIdByName(int userId, String name) {
        String query = "SELECT portfolio_id FROM Portfolios WHERE user_id = ? AND name = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("portfolio_id");
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public static void viewPortfolioDetails(int portfolioId, String name) {
        String query = "SELECT cash_amount FROM Portfolios WHERE portfolio_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.printf("Portfolio: %s%n", name);
                System.out.printf("Cash Amount: $%.2f%n", rs.getDouble("cash_amount"));
            } else {
                System.out.println("Portfolio not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

}