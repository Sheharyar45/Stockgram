package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cs.toronto.edu.db.DBConnection;

public class FriendsModel {

    public static List<String> getFriends(int userId) {
        List<String> friends = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = 
                "SELECT u.username " +
                "FROM Friendship f " +
                "JOIN Users u ON (u.user_id = CASE WHEN f.user1=? THEN f.user2 ELSE f.user1 END) " +
                "WHERE (f.user1=? OR f.user2=?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                friends.add(rs.getString("username"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return friends;
    }

    public static List<String> getIncomingRequests(int userId) {
        List<String> requests = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT u.username " +
                "FROM friendrequest f " +
                "JOIN Users u ON u.user_id = f.sender " +
                "WHERE f.receiver = ? AND f.status = 'pending'";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                requests.add(rs.getString("username"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return requests;
    }

    
    public static List<String> getOutgoingRequests(int userId) {
        List<String> requests = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT u.username " +
                "FROM friendrequest f " +
                "JOIN Users u ON u.user_id = f.receiver " +
                "WHERE f.sender = ? AND f.status = 'pending'";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                requests.add(rs.getString("username"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return requests;
    }

    
    public static Integer getUserIdByUsername(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT user_id FROM Users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("user_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    
    public static boolean areAlreadyFriends(int userId, int otherId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT * FROM Friendship " +
                "WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, otherId);
            ps.setInt(3, otherId);
            ps.setInt(4, userId);
            
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    
    public static boolean requestAlreadyExists(int userId, int otherId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "SELECT * FROM friendrequest " +
                "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)";
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, otherId);
            ps.setInt(3, otherId);
            ps.setInt(4, userId);
            
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    
    public static boolean sendFriendRequest(int userId, int otherId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql =
                "INSERT INTO friendrequest (sender, receiver, status) VALUES (?, ?, 'pending') " +
                "ON CONFLICT DO NOTHING";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, otherId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    
    public static boolean acceptFriendRequest(int userId, int requester) {
        try (Connection conn = DBConnection.getConnection()) {
            // Update request status
            String sql =
                "UPDATE friendrequest SET status='accepted' " +
                "WHERE sender = ? AND receiver = ? AND status='pending'";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, requester);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                // Insert into Friendship
                String sqlInsert =
                    "INSERT INTO Friendship (user1, user2, timestamp) VALUES (?, ?, CURRENT_TIMESTAMP) " +
                    "ON CONFLICT DO NOTHING";

                PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
                psInsert.setInt(1, requester);
                psInsert.setInt(2, userId);
                psInsert.executeUpdate();
                
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }

    
    public static boolean removeFriend(int userId, int otherId) {
        try (Connection conn = DBConnection.getConnection()) {
            // Delete from Friendship
            String sql =
                "DELETE FROM friendship " +
                "WHERE ((user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?))";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, otherId);
            ps.setInt(3, otherId);
            ps.setInt(4, userId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                // Also remove any friend requests between them
                String sqlReq =
                    "DELETE FROM friendrequest " +
                    "WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)";
                
                PreparedStatement psReq = conn.prepareStatement(sqlReq);
                psReq.setInt(1, userId);
                psReq.setInt(2, otherId);
                psReq.setInt(3, otherId);
                psReq.setInt(4, userId);
                psReq.executeUpdate();
                
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
}
