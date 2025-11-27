package cs.toronto.edu.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import cs.toronto.edu.db.DBConnection;

public class ReviewModel {

    public static class Review {
        public int reviewId;
        public int userId;
        public int stocklistId;
        public String text;
        public Timestamp timeCreated;
        public Timestamp timeUpdated;
        public int likes;
        public int dislikes;
    }

    public static boolean createReview(int userId, int stocklistId, String text) {

        if (text.length() > 4000) {
            System.out.println("Review exceeds 4000 characters.");
            return false;
        }

        // Check permission
        String visibility = getStocklistVisibility(stocklistId);
        if (visibility == null) {
            System.out.println("Stocklist not found.");
            return false;
        }
        if (visibility.equals("private")) {
            int owner = getStocklistOwner(stocklistId);
            if (owner != userId) {
                System.out.println("You cannot review a private stocklist you do not own.");
                return false;
            }
        }

        String sql = "INSERT INTO reviews (user_id, stocklist_id, text) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, stocklistId);
            stmt.setString(3, text);
            stmt.executeUpdate();
            System.out.println("Review created successfully.");
            return true;

        } catch (Exception e) { 
            if (e instanceof SQLException sqlEx && sqlEx.getSQLState().equals("23505")) {
                System.out.println("You have already reviewed this stocklist. Use edit to update your review.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean editReview(int reviewId, int userId, String newText) {

        Integer reviewOwner = getReviewOwner(reviewId);
        if (reviewOwner == null) {
            System.out.println("Review not found.");
            return false;
        }
        if (reviewOwner != userId) {
            System.out.println("You can only edit your own reviews.");
            return false;
        }

        String sql = "UPDATE reviews SET text = ?, time_updated = NOW() WHERE review_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newText);
            stmt.setInt(2, reviewId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Review updated successfully.");
                return true;
            } else {
                System.out.println("Review not found.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteReview(int reviewId, int userId) {
        Integer reviewOwner = getReviewOwner(reviewId);
        if (reviewOwner == null) {
            System.out.println("Review not found.");
            return false;
        }

        int stocklistId;
        try (Connection conn = DBConnection.getConnection()) {
            // Get the stocklist_id for the review
            String selectSql = "SELECT stocklist_id FROM reviews WHERE review_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, reviewId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) return false;
                stocklistId = rs.getInt("stocklist_id");
            }

            int owner = getStocklistOwner(stocklistId);

            if (userId != reviewOwner && userId != owner) {
                System.out.println("Only the reviewer or the stocklist owner can delete this review.");
                return false;
            }

            // Delete the review
            String deleteSql = "DELETE FROM reviews WHERE review_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, reviewId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Review deleted successfully.");
                    return true;
                } else {
                    System.out.println("Review not found.");
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean likeReview(int reviewId) {
        String sql = "UPDATE reviews SET likes = likes + 1 WHERE review_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean dislikeReview(int reviewId) {
        String sql = "UPDATE reviews SET dislikes = dislikes + 1 WHERE review_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Review> getReviewsForStocklist(int stocklistId, int requestingUserId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE stocklist_id = ? ORDER BY time_created DESC";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stocklistId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int reviewOwner = rs.getInt("user_id");
                // check permission
                if (!canUserView(requestingUserId, stocklistId, reviewOwner)) {
                    continue;
                }
                Review r = new Review();
                r.reviewId = rs.getInt("review_id");
                r.userId = rs.getInt("user_id");
                r.stocklistId = rs.getInt("stocklist_id");
                r.text = rs.getString("text");
                r.timeCreated = rs.getTimestamp("time_created");
                r.timeUpdated = rs.getTimestamp("time_updated");
                r.likes = rs.getInt("likes");
                r.dislikes = rs.getInt("dislikes");
                reviews.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reviews;
    }

    private static Integer getStocklistOwner(int stocklistId) {
        String sql = "SELECT user_id FROM stocklist WHERE stocklist_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stocklistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private static String getStocklistVisibility(int stocklistId) {
        String sql = "SELECT visibility FROM stocklist WHERE stocklist_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stocklistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("visibility");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private static Integer getReviewOwner(int reviewId) {
        String sql = "SELECT user_id FROM reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reviewId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private static boolean canUserView(int userId, int stocklistId, int reviewOwnerId) {
        String visibility = getStocklistVisibility(stocklistId);
        int owner = getStocklistOwner(stocklistId);

        if (visibility == null) return false;

        if (visibility.equals("public")) return true;

        // private: owner OR invited reviewer OR review owner
        return userId == owner || userId == reviewOwnerId || StocklistModel.isInvitedReviewer(userId, stocklistId);
    }

}
