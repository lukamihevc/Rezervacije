package model;

public class Session {
    private static int currentUserId = -1; // Privzeto je -1 (nobody logged in)

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static boolean isUserLoggedIn() {
        return currentUserId != -1;
    }

    public static void logout() {
        currentUserId = -1;
    }
}

