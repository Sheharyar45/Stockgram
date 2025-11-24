package cs.toronto.edu;
import cs.toronto.edu.model.FriendsModel;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FriendsService {

    public static void menu(int userId) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== FRIENDS MENU =====");
            System.out.println("1. View my friends");
            System.out.println("2. View incoming friend requests");
            System.out.println("3. View outgoing friend requests");
            System.out.println("4. Send friend request");
            System.out.println("5. Accept friend request");
            System.out.println("6. Decline friend request");
            System.out.println("7. Remove friend");
            System.out.println("8. Back");
            System.out.print("Choose an option: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    viewFriends(userId);
                    break;
                case "2":
                    viewIncomingRequests(userId);
                    break;
                case "3":
                    viewOutgoingRequests(userId);
                    break;
                case "4":
                    sendFriendRequest(userId, scanner);
                    break;
                case "5":
                    acceptFriendRequest(userId, scanner);
                    break;
                case "6":
                    // Decline friend request, use random function
                    declineFriendRequest(userId, scanner);
                    break;
                case "7":
                    removeFriend(userId, scanner);
                    break;
                case "8":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private static void viewFriends(int userId) {
        List<String> friends = FriendsModel.getFriends(userId);

        System.out.println("\nYour Friends:");
        
        if (friends.isEmpty()) {
            System.out.println("No friends yet.");
        } else {
            for (String friend : friends) {
                System.out.println(friend);
            }
        }
    }

    private static void viewIncomingRequests(int userId) {
        List<String> requests = FriendsModel.getIncomingRequests(userId);

        System.out.println("\nIncoming Requests:");
        
        if (requests.isEmpty()) {
            System.out.println("No incoming requests.");
        } else {
            for (String request : requests) {
                System.out.println(request);
            }
        }
    }

    private static void viewOutgoingRequests(int userId) {
        List<String> requests = FriendsModel.getOutgoingRequests(userId);

        System.out.println("\nOutgoing Requests:");
        
        if (requests.isEmpty()) {
            System.out.println("No outgoing requests.");
        } else {
            for (String request : requests) {
                System.out.println(request);
            }
        }
    }

    private static void sendFriendRequest(int userId, Scanner scanner) {
        System.out.print("Enter the username to add, or 0 to cancel: ");
        String username = scanner.nextLine();
        if (username.equals("0")) {
            System.out.println("Cancelled sending friend request.");
            return;
        }

        Integer otherId = FriendsModel.getUserIdByUsername(username);
        
        if (otherId == null) {
            System.out.println("User not found.");
            return;
        }

        if (otherId == userId) {
            System.out.println("You cannot add yourself.");
            return;
        }

        if (FriendsModel.areAlreadyFriends(userId, otherId)) {
            System.out.println("You are already friends with this user.");
            return;
        }

        if (FriendsModel.requestAlreadyExists(userId, otherId)) {
            System.out.println("A friend request already exists between you and this user.");
            return;
        }

        if (!FriendsModel.canSendRequest(userId, otherId)) {
            System.out.println("You cannot send a friend request to this user yet. Please wait before trying again.");
            return;
        }

        if (FriendsModel.sendFriendRequest(userId, otherId)) {
            System.out.println("Friend request sent.");
        } else {
            System.out.println("Request already exists.");
        }
    }

    private static void acceptFriendRequest(int userId, Scanner scanner) {
        System.out.print("Enter the username of the requester, or 0 to cancel: ");
        String requesterUsername = scanner.nextLine();

        if (requesterUsername.equals("0")) {
            System.out.println("Cancelled accepting friend request.");
            return;
        }

        Integer requester = FriendsModel.getUserIdByUsername(requesterUsername);
        
        if (requester == null) {
            System.out.println("User not found.");
            return;
        }

        if (FriendsModel.acceptFriendRequest(userId, requester)) {
            System.out.println("Friend request accepted.");
        } else {
            System.out.println("No pending request from this user.");
        }
    }

    private static void declineFriendRequest(int userId, Scanner scanner) {
        System.out.print("Enter the username of the requester, or 0 to cancel: ");
        String requesterUsername = scanner.nextLine();

        if (requesterUsername.equals("0")) {
            System.out.println("Cancelled declining friend request.");
            return;
        }

        Integer requester = FriendsModel.getUserIdByUsername(requesterUsername);
        
        if (requester == null) {
            System.out.println("User not found.");
            return;
        }

        if (FriendsModel.declineFriendRequest(userId, requester)) {
            System.out.println("Friend request declined.");
        } else {
            System.out.println("No pending request from this user.");
        }
    }

   
    private static void removeFriend(int userId, Scanner scanner) {
        System.out.print("Enter the username of the friend, or 0 to cancel: ");
        String friendUsername = scanner.nextLine();

        if (friendUsername.equals("0")) {
            System.out.println("Cancelled removing friend.");
            return;
        }

        Integer otherId = FriendsModel.getUserIdByUsername(friendUsername);
        
        if (otherId == null) {
            System.out.println("User not found.");
            return;
        }

        if (FriendsModel.removeFriend(userId, otherId)) {
            System.out.println("Friend removed.");
        } else {
            System.out.println("You are not friends with this user.");
        }
    }
}
