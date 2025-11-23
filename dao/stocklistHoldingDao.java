package cs.toronto.edu.dao;

import cs.toronto.edu.db.DBConnection;
import cs.toronto.edu.models.StocklistHolding;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StocklistHoldingDAO {

    public List<StocklistHolding> getHoldingsForList(int listId) {
        String sql = "SELECT holding_id, list_id, stock_symbol FROM StocklistHoldings WHERE list_id = ?";
        List<StocklistHolding> holdings = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                holdings.add(new StocklistHolding(
                        rs.getInt("holding_id"),
                        rs.getInt("list_id"),
                        rs.getString("stock_symbol")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return holdings;
    }

    public void addStockToList(int listId, String symbol) {
        String sql = "INSERT INTO StocklistHoldings (list_id, stock_symbol) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listId);
            stmt.setString(2, symbol);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeStockFromList(int listId, String symbol) {
        String sql = "DELETE FROM StocklistHoldings WHERE list_id = ? AND stock_symbol = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, listId);
            stmt.setString(2, symbol);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
