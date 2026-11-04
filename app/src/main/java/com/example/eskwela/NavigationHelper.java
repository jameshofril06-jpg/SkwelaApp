package com.example.eskwela;

import android.app.Activity;
import android.content.Intent;

public class NavigationHelper {

    public static int getTabIndex(int itemId) {
        if (itemId == R.id.nav_home) return 0;
        if (itemId == R.id.nav_schedule) return 1;
        if (itemId == R.id.nav_tasks) return 2;
        if (itemId == R.id.nav_study) return 3;
        if (itemId == R.id.nav_profile) return 4;
        return 0;
    }

    public static void navigateWithTabAnimation(Activity activity, int currentTabIndex, int targetTabIndex, Class<?> targetActivityClass) {
        if (currentTabIndex == targetTabIndex) return;

        Intent intent = new Intent(activity, targetActivityClass);
        activity.startActivity(intent);

        if (targetTabIndex > currentTabIndex) {
            // Navigating Right: slide in from right, exit to left
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            // Navigating Left: slide in from left, exit to right
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

        activity.finish();
    }
}
