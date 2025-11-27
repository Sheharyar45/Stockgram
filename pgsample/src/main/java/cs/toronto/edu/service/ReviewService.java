package cs.toronto.edu.service;

import java.util.List;
import java.util.Scanner;
import cs.toronto.edu.model.ReviewModel;
import cs.toronto.edu.model.ReviewModel.Review;

public class ReviewService {

    public static void menu(int userId, int stocklistId) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n----- REVIEW MENU -----");
            System.out.println("1. View reviews");
            System.out.println("2. Add review");
            System.out.println("3. Edit your review");
            System.out.println("4. Delete your review");
            System.out.println("5. Like a review");
            System.out.println("6. Dislike a review");
            System.out.println("7. Back");
            System.out.print("Choose an option: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    viewReviews(stocklistId, userId);
                    break;
                case "2":
                    addReview(userId, stocklistId, sc);
                    break;
                case "3":
                    editReview(userId, stocklistId, sc);
                    break;
                case "4":
                    deleteReview(userId, stocklistId, sc);
                    break;
                case "5":
                    likeReview(sc);
                    break;
                case "6":
                    dislikeReview(sc);
                    break;
                case "7":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewReviews(int stocklistId, int userId) {
        List<Review> reviews = ReviewModel.getReviewsForStocklist(stocklistId, userId);

        if (reviews.isEmpty()) {
            System.out.println("No reviews for this stocklist.");
            return;
        }

        System.out.println("\n--- REVIEWS ---");
        for (Review r : reviews) {
            System.out.printf("ID: %d | User: %d | Likes: %d | Dislikes: %d\n",
                              r.reviewId, r.userId, r.likes, r.dislikes);
            System.out.println("Created: " + r.timeCreated + " | Updated: " + r.timeUpdated);
            System.out.println("Text: " + r.text);
            System.out.println("---------------------------");
        }
    }

    private static void addReview(int userId, int stocklistId, Scanner sc) {
        System.out.print("Enter your review text: ");
        String text = sc.nextLine().trim();

        if (text.isEmpty()) {
            System.out.println("Review text cannot be empty.");
            return;
        }

        if (ReviewModel.createReview(userId, stocklistId, text)) {
            System.out.println("Review added successfully.");
        }
    }

    private static void editReview(int userId, int stocklistId, Scanner sc) {
        List<Review> reviews = ReviewModel.getReviewsForStocklist(stocklistId, userId);

        // Find the user's review
        Review userReview = reviews.stream()
                                   .filter(r -> r.userId == userId)
                                   .findFirst()
                                   .orElse(null);

        if (userReview == null) {
            System.out.println("You haven't written a review for this stocklist.");
            return;
        }

        System.out.println("Current review: " + userReview.text);
        System.out.print("Enter new review text: ");
        String newText = sc.nextLine().trim();

        if (newText.isEmpty()) {
            System.out.println("Review text cannot be empty.");
            return;
        }

        if (ReviewModel.editReview(userReview.reviewId, userId, newText)) {
            System.out.println("Review updated successfully.");
        }
    }

    private static void deleteReview(int userId, int stocklistId, Scanner sc) {
        List<Review> reviews = ReviewModel.getReviewsForStocklist(stocklistId, userId);

        // Find the user's review
        Review userReview = reviews.stream()
                                   .filter(r -> r.userId == userId)
                                   .findFirst()
                                   .orElse(null);

        if (userReview == null) {
            System.out.println("You haven't written a review for this stocklist.");
            return;
        }

        System.out.print("Are you sure you want to delete your review? (yes/no): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            if (ReviewModel.deleteReview(userReview.reviewId, userId)) {
                System.out.println("Review deleted successfully.");
            }
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    private static void likeReview(Scanner sc) {
        System.out.print("Enter the review ID to like: ");
        try {
            int reviewId = Integer.parseInt(sc.nextLine());
            if (ReviewModel.likeReview(reviewId)) {
                System.out.println("Liked the review.");
            } else {
                System.out.println("Review not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid review ID.");
        }
    }

    private static void dislikeReview(Scanner sc) {
        System.out.print("Enter the review ID to dislike: ");
        try {
            int reviewId = Integer.parseInt(sc.nextLine());
            if (ReviewModel.dislikeReview(reviewId)) {
                System.out.println("Disliked the review.");
            } else {
                System.out.println("Review not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid review ID.");
        }
    }
}
