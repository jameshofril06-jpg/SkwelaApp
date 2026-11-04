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

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable navigationTask = this::navigateAfterSplash;

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
        mainHandler.postDelayed(navigationTask, 2800);
    }

    private void navigateAfterSplash() {
        if (isFinishing() || isDestroyed()) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isFinishing() || isDestroyed()) return;
                        if (AccountGuard.blockIfRestricted(SplashActivity.this, FirebaseAuth.getInstance(), documentSnapshot)) {
                            return;
                        }
                        goTo(DashboardActivity.class);
                    })
                    .addOnFailureListener(e -> {
                        if (isFinishing() || isDestroyed()) return;
                        goTo(DashboardActivity.class);
                    });
        } else {
            goTo(MainActivity.class);
        }
    }

    private void goTo(Class<?> destination) {
        startActivity(new Intent(SplashActivity.this, destination));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacks(navigationTask);
        super.onDestroy();
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