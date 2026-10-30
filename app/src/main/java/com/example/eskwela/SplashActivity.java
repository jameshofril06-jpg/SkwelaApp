package com.example.eskwela;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_splash);

        // Initialize Views
        View ivLogo = findViewById(R.id.ivSplashLogo);
        View tvTitle = findViewById(R.id.tvSplashTitle);
        View tvTagline = findViewById(R.id.tvSplashTagline);
        View progressBar = findViewById(R.id.splashProgressBar);

        // Start Background Bubble Animations (Continuous)
        startBubbleAnimations();

        // --- ENTRANCE ANIMATION SEQUENCE ---

        // 1. Logo Animation: Drop in from top and scale down (Overshoot)
        ivLogo.setAlpha(0f);
        ivLogo.setTranslationY(-150f); // Start high up
        ivLogo.setScaleX(1.3f);
        ivLogo.setScaleY(1.3f);

        ivLogo.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new OvershootInterpolator(1.2f)) // Gives it a slight bounce
                .start();

        // 2. Title Animation: Fade and slide up
        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(50f);
        tvTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 3. Tagline Animation: Fade and slide up
        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(40f);
        tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 4. Loading Bar Animation: Stretch and Fade
        progressBar.setAlpha(0f);
        progressBar.setScaleX(0.2f); // Start small
        progressBar.animate()
                .alpha(1f)
                .scaleX(1f) // Stretch to full width
                .setDuration(800)
                .setStartDelay(900)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // --- NAVIGATION LOGIC ---
        // Wait for animations to finish, then check session and navigate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(currentUser.getUid()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String status = documentSnapshot.getString("status");
                                if ("suspended".equalsIgnoreCase(status) || "deactivated".equalsIgnoreCase(status) || "archived".equalsIgnoreCase(status)) {
                                    FirebaseAuth.getInstance().signOut();
                                    new androidx.appcompat.app.AlertDialog.Builder(SplashActivity.this)
                                            .setTitle("Account " + status.toUpperCase())
                                            .setMessage("Your ESKWELA account is currently " + status + ". Please contact the system administrator.")
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                                finish();
                                            })
                                            .setCancelable(false)
                                            .show();
                                    return;
                                }
                            }
                            startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        });
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 2800); // 2.8 seconds gives enough time for the staggered animations
    }

    private void startBubbleAnimations() {
        View bubbleTop = findViewById(R.id.bubbleSplashTop);
        View bubbleBottom = findViewById(R.id.bubbleSplashBottom);

        // Top Bubble Breathing
        ObjectAnimator scaleTop = ObjectAnimator.ofPropertyValuesHolder(
                bubbleTop,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.15f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.15f)
        );
        scaleTop.setDuration(4000);
        scaleTop.setRepeatCount(ValueAnimator.INFINITE);
        scaleTop.setRepeatMode(ValueAnimator.REVERSE);
        scaleTop.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleTop.start();

        // Bottom Bubble Breathing (Offset)
        ObjectAnimator scaleBottom = ObjectAnimator.ofPropertyValuesHolder(
                bubbleBottom,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f)
        );
        scaleBottom.setDuration(5000);
        scaleBottom.setStartDelay(1000);
        scaleBottom.setRepeatCount(ValueAnimator.INFINITE);
        scaleBottom.setRepeatMode(ValueAnimator.REVERSE);
        scaleBottom.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleBottom.start();
    }
}