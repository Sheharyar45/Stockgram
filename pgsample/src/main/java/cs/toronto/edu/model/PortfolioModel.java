package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import cs.toronto.edu.db.DBConnection;
import cs.toronto.edu.model.StockModel;

public class PortfolioModel {

    public static void viewPortfolios(int userId) {
        String q = "SELECT name, cash_amount, investment FROM Portfolios WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(q)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n----- YOUR PORTFOLIOS -----");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("- %s (Cash: $%.2f, Investment: $%.2f)%n", rs.getString("name"), rs.getDouble("cash_amount"), rs.getDouble("investment"));
            }
            if (!found) {
                System.out.println("No portfolios found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean createPortfolio(int userId, String portfolioName) {
        // check if portfolio with same name owned by user
        String name = portfolioName.trim();
        if (name.isEmpty()) {
            System.out.println("Portfolio name cannot be empty.");
            return false;
        }
        String checkQuery = "SELECT COUNT(*) AS count FROM Portfolios WHERE user_id = ? AND name = ?";
        String insertQuery = "INSERT INTO Portfolios (user_id, name, cash_amount, investment) VALUES (?, ?, 0, 0)";
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

    public static double getCurrentInvestment(int portfolioId) {
        String query = "SELECT investment FROM Portfolios WHERE portfolio_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("investment");
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public static double getCashAmount(int portfolioId) {
        String query = "SELECT cash_amount FROM Portfolios WHERE portfolio_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("cash_amount");
            } else {
                return 0.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    public static void viewPortfolioDetails(int portfolioId, String name) {
        String query = "SELECT cash_amount, investment FROM Portfolios WHERE portfolio_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.printf("Portfolio: %s%n", name);
                System.out.printf("Cash Amount: $%.2f%n", rs.getDouble("cash_amount"));
                System.out.printf("Investment: $%.2f%n", rs.getDouble("investment"));
            } else {
                System.out.println("Portfolio not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deposit(int portfolioId, Scanner sc) {
        System.out.print("Amount to deposit: ");
        double amount = Double.parseDouble(sc.nextLine());
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return false;
        }

        String add = "UPDATE Portfolios SET cash_amount = cash_amount + ?, investment = investment + ? WHERE portfolio_id = ?";
        String tc = "INSERT INTO Transactions(portfolio_id, type, amount, timestamp) VALUES (?, 'Deposit', ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(add);
            PreparedStatement tcStmt = conn.prepareStatement(tc)) {

            stmt.setDouble(1, amount);
            stmt.setDouble(2, amount);
            stmt.setInt(3, portfolioId);
            stmt.executeUpdate();

            tcStmt.setInt(1, portfolioId);
            tcStmt.setDouble(2, amount);
            tcStmt.executeUpdate();

            System.out.println("Deposit successful.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean withdraw(int portfolioId, Scanner sc) {
        System.out.print("Amount to withdraw: ");
        double amount = Double.parseDouble(sc.nextLine());
        if (amount <= 0) {
            System.out.println("Withdrawal amount must be positive.");
            return false;
        }

        String remove = "UPDATE Portfolios SET cash_amount = cash_amount - ? WHERE portfolio_id = ? AND cash_amount >= ?";
        String tc = "INSERT INTO Transactions(portfolio_id, type, amount, timestamp) VALUES (?, 'Withdraw', ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(remove);
            PreparedStatement tcStmt = conn.prepareStatement(tc)) {

            stmt.setDouble(1, amount);
            stmt.setInt(2, portfolioId);
            stmt.setDouble(3, amount);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                System.out.println("Insufficient funds for withdrawal.");
                return false;
            }

            tcStmt.setInt(1, portfolioId);
            tcStmt.setDouble(2, amount);
            tcStmt.executeUpdate();

            System.out.println("Withdrawal successful.");
            double curInvestment = getCurrentInvestment(portfolioId);
            double newCashAmount = getCashAmount(portfolioId);
            // if new cash amount is less than investment, adjust investment
            if (curInvestment > newCashAmount) {
                String adjustInvestment = "UPDATE Portfolios SET investment = investment - ? WHERE portfolio_id = ?";    
                PreparedStatement adjustInvestmentStmt = conn.prepareStatement(adjustInvestment);
                adjustInvestmentStmt.setDouble(1, amount);
                adjustInvestmentStmt.setInt(2, portfolioId);
                adjustInvestmentStmt.executeUpdate();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean buyStock(int portfolioId, Scanner sc, StockModel stockModel) {
        System.out.print("Stock symbol to buy: ");
        String symbol = sc.nextLine().trim().toUpperCase();
        System.out.print("Number of shares to buy: ");
        double shares = Double.parseDouble(sc.nextLine());
        if (shares <= 0) {
            System.out.println("Number of shares must be positive.");
            return false;
        }

        double pricePerShare = stockModel.getStockPrice(symbol);
        if (pricePerShare < 0) {
            System.out.println("Stock symbol not found.");
            return false;
        }
        double totalCost = pricePerShare * shares;

        String deductCash = "UPDATE Portfolios SET cash_amount = cash_amount - ? WHERE portfolio_id = ? AND cash_amount >= ?";
        String addHolding = "INSERT INTO portfolioholdings (portfolio_id, stock_symbol, shares, cost) VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT (portfolio_id, stock_symbol) DO UPDATE SET " +
                            "shares = portfolioholdings.shares + ?, " +
                            "cost = portfolioholdings.cost + ?";
        String tc = "INSERT INTO Transactions(portfolio_id, type, stock_symbol, amount, shares, timestamp) " +
                    "VALUES (?, 'Buy', ?, ?, ?, NOW())";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement cashStmt = conn.prepareStatement(deductCash);
            PreparedStatement holdingStmt = conn.prepareStatement(addHolding);
            PreparedStatement tcStmt = conn.prepareStatement(tc)) {

            // Deduct cash
            cashStmt.setDouble(1, totalCost);
            cashStmt.setInt(2, portfolioId);
            cashStmt.setDouble(3, totalCost);
            int rows = cashStmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Insufficient funds to complete purchase.");
                return false;
            }

            // Add to holdings
            holdingStmt.setInt(1, portfolioId);
            holdingStmt.setString(2, symbol);
            holdingStmt.setDouble(3, shares);
            holdingStmt.setDouble(4, totalCost);
            holdingStmt.setDouble(5, shares);
            holdingStmt.setDouble(6, totalCost);
            holdingStmt.executeUpdate();

            // Record transaction
            tcStmt.setInt(1, portfolioId);
            tcStmt.setString(2, symbol);
            tcStmt.setDouble(3, totalCost);
            tcStmt.setDouble(4, shares);
            tcStmt.executeUpdate();

            System.out.println("Stock purchase successful. " + shares + " shares of " + symbol + " bought at $" + pricePerShare + " each.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sellStock(int portfolioId, Scanner sc, StockModel stockModel) {
        System.out.print("Stock symbol to sell: ");
        String symbol = sc.nextLine().trim().toUpperCase();
        System.out.print("Number of shares to sell: ");
        double shares = Double.parseDouble(sc.nextLine());
        if (shares <= 0) {
            System.out.println("Number of shares must be positive.");
            return false;
        }
        double pricePerShare = stockModel.getStockPrice(symbol);
        if (pricePerShare < 0) {
            System.out.println("Stock symbol not found.");
            return false;
        }
        double payout = pricePerShare * shares;
        double avgprice = 0.0;
        String getAvgPrice = "SELECT cost, shares FROM portfolioholdings WHERE portfolio_id = ? AND stock_symbol = ?";
        String removeHolding = "UPDATE portfolioholdings SET shares = shares - ?, cost = cost - ? WHERE portfolio_id = ? AND stock_symbol = ? AND shares >= ?";
        String deleteHolding = "DELETE FROM portfolioholdings WHERE portfolio_id = ? AND stock_symbol = ? AND shares = 0";
        String addCash = "UPDATE Portfolios SET cash_amount = cash_amount + ? WHERE portfolio_id = ?";
        String tc = "INSERT INTO Transactions(portfolio_id, type, stock_symbol, amount, shares, timestamp) " +
                    "VALUES (?, 'Sell', ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement holdingStmt = conn.prepareStatement(removeHolding);
            PreparedStatement cashStmt = conn.prepareStatement(addCash);
            PreparedStatement avgPriceStmt = conn.prepareStatement(getAvgPrice);
            PreparedStatement tcStmt = conn.prepareStatement(tc)) { 
            // Get average price
            avgPriceStmt.setInt(1, portfolioId);
            avgPriceStmt.setString(2, symbol);
            ResultSet rs = avgPriceStmt.executeQuery();
            if (rs.next()) {
                double totalCost = rs.getDouble("cost");
                double totalShares = rs.getDouble("shares");
                avgprice = totalCost / totalShares;
            } else {
                System.out.println("You do not own any shares of this stock.");
                return false;
            }    
            // Remove from holdings
            holdingStmt.setDouble(1, shares);
            holdingStmt.setDouble(2, avgprice * shares);
            holdingStmt.setInt(3, portfolioId);
            holdingStmt.setString(4, symbol);
            holdingStmt.setDouble(5, shares);
            int rows = holdingStmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Insufficient shares to complete sale.");
                return false;
            }
            // Delete holding if shares now zero
            PreparedStatement deleteHoldingStmt = conn.prepareStatement(deleteHolding);
            deleteHoldingStmt.setInt(1, portfolioId);
            deleteHoldingStmt.setString(2, symbol);
            deleteHoldingStmt.executeUpdate();
            // Add to cash
            cashStmt.setDouble(1, payout);
            cashStmt.setInt(2, portfolioId);
            cashStmt.executeUpdate();

            // Record transaction
            tcStmt.setInt(1, portfolioId);
            tcStmt.setString(2, symbol);
            tcStmt.setDouble(3, payout);
            tcStmt.setDouble(4, shares);
            tcStmt.executeUpdate();

            System.out.println("Stock sale successful. " + shares + " shares of " + symbol + " sold at $" + pricePerShare + " each.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }     
    }

    public static void viewHoldings(int portfolioId, StockModel stockModel) {
        String query = "SELECT stock_symbol, shares, cost FROM portfolioholdings WHERE portfolio_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, portfolioId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n----- PORTFOLIO HOLDINGS -----");
            boolean found = false;
            while (rs.next()) {
                found = true;
                double currValue = stockModel.getStockPrice(rs.getString("stock_symbol")) * rs.getDouble("shares");
                System.out.printf("- %s: %.2f shares (Total Cost: $%.2f, Current Value: $%.2f)%n", rs.getString("stock_symbol"), rs.getDouble("shares"), rs.getDouble("cost"), currValue);
            }
            if (!found) {
                System.out.println("No holdings found in this portfolio.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}