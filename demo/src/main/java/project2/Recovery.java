package project2;

import java.time.Instant;
import java.util.Optional;

/** SFWX0314-44 — User Account Recovery
 * -manager can unlock user accounts
 * -manager can reset user passwords
 * -user can change their own password
 * **/

public class Recovery {

    private Recovery() { }
    
    // Manager-only: unlock user who is locked out
    public static boolean unlockUser(String managerUsername, String targetUsername) {
        return BackEnd.managerUnlockUser(managerUsername, targetUsername);
    }
    // Manager-only: reset user password
    public static boolean setTemperaryPassword(String managerUsername, String targetUsername,String tempPassword) {
        return BackEnd.managerResetPassword(managerUsername, targetUsername, tempPassword);
    }

    // Optional, user can change their own password
    public static boolean changeOwnPassword(String username, String oldPassword, String newPassword) {
        if (!BackEnd.checkLogin(username, oldPassword)) {
            return false; // old password does not match
        }
        // Set new password and clear failure/lockout flags
        Optional<User> user = BackEnd.findUserByUsername(username);
        if (user.isEmpty()) {
            return false; // not found
        }
            
        User u = user.get();
        u.setPassword(newPassword);
        u.resetFailures();
        // TODO: persist changes to CSV
        System.out.println("Password changed for user: " + username);
        return true;
    }

    // login messages for UI
    public static boolean isLocked(String username) {
        return BackEnd.isLocked(username);
    }

}
