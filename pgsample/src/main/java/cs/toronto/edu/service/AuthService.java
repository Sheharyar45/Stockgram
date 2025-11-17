package cs.toronto.edu;

import java.sql.*;
import cs.toronto.edu.db.DBConnection;

public class AuthService {

    public static boolean signUp(String username, String password, String email) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "INSERT INTO Users (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Integer signIn(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT user_id FROM Users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id"); // login success
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // login failed
    }
}