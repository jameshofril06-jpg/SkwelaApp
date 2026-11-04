package com.example.eskwela;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

public final class AccountGuard {

    private AccountGuard() {}

    public static boolean isRestricted(String status) {
        return "suspended".equalsIgnoreCase(status)
                || "deactivated".equalsIgnoreCase(status)
                || "archived".equalsIgnoreCase(status);
    }

    public static boolean isAdmin(String email, String role) {
        if ("admin".equalsIgnoreCase(role)) return true;
        if (email == null) return false;
        String lower = email.toLowerCase();
        return lower.contains("admin") || "admin@eskwela.com".equals(lower);
    }

    public static boolean blockIfRestricted(AppCompatActivity activity, FirebaseAuth auth, DocumentSnapshot doc) {
        if (doc != null && doc.exists() && isRestricted(doc.getString("status"))) {
            showRestrictedAndLogout(activity, auth, doc.getString("status"));
            return true;
        }
        return false;
    }

    public static void showRestrictedAndLogout(AppCompatActivity activity, FirebaseAuth auth, String status) {
        if (activity.isFinishing()) return;
        auth.signOut();
        String label = status != null ? status : "restricted";
        new AlertDialog.Builder(activity)
                .setTitle("Account " + label.toUpperCase())
                .setMessage("Your ESKWELA account is currently " + label + ". Please contact the system administrator.")
                .setPositiveButton("OK", (dialog, which) -> goLogin(activity))
                .setCancelable(false)
                .show();
    }

    public static void goLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
