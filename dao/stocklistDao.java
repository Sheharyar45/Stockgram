package cs.toronto.edu.dao;

import cs.toronto.edu.db.DBConnection;
import cs.toronto.edu.models.Stocklist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StocklistDAO {

    public Stocklist getStocklistById(int id) {
        String sql = "SELECT list_id, list_name, user_id FROM Stocklist WHERE list_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Stocklist(
                        rs.getInt("list_id"),
                        rs.getString("list_name"),
                        rs.getInt("user_id")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Stocklist> getAllStocklistsForUser(int userId) {
        String sql = "SELECT list_id, list_name, user_id FROM Stocklist WHERE user_id = ?";
        List<Stocklist> lists = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lists.add(new Stocklist(
                        rs.getInt("list_id"),
                        rs.getString("list_name"),
                        rs.getInt("user_id")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lists;
    }

    public void createStocklist(Stocklist stocklist) {
        String sql = "INSERT INTO Stocklist (list_name, user_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, stocklist.getListName());
            stmt.setInt(2, stocklist.getUserId());
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteStocklist(int id) {
        String sql = "DELETE FROM Stocklist WHERE list_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
