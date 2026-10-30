package com.example.eskwela;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Make status bar transparent so the background extends behind it
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // REMOVED: The ViewCompat.setOnApplyWindowInsetsListener block.
        // This ensures no white padding is added at the top or bottom.

        // 1. Initialize Views for Entrance Animation
        View ivLogo = findViewById(R.id.imageViewLogo);
        View tvTitle = findViewById(R.id.textViewTitle);
        View tvSubtitle = findViewById(R.id.textViewSubtitle);
        View tvDescription = findViewById(R.id.textViewDescription);
        View btnGetStarted = findViewById(R.id.btnGetStarted);
        View btnLogin = findViewById(R.id.btnLogin);

        // 2. Trigger the Cascading Animation Sequence
        animateIn(ivLogo, 100);
        animateIn(tvTitle, 250);
        animateIn(tvSubtitle, 400);
        animateIn(tvDescription, 550);
        animateIn(btnGetStarted, 700);
        animateIn(btnLogin, 850);

        // 3. Start Background Bubble Floating Animations
        startBubbleAnimation();

        // 4. Button Click Listeners
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void startBubbleAnimation() {
        View bubble1 = findViewById(R.id.bubbleTopRight);
        View bubble2 = findViewById(R.id.bubbleBottomLeft);

        // Animate Top Right Bubble
        ObjectAnimator scaleDown1 = ObjectAnimator.ofPropertyValuesHolder(
                bubble1,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.15f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.15f)
        );
        scaleDown1.setDuration(4000);
        scaleDown1.setRepeatCount(ValueAnimator.INFINITE);
        scaleDown1.setRepeatMode(ValueAnimator.REVERSE);
        scaleDown1.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDown1.start();

        // Animate Bottom Left Bubble
        ObjectAnimator scaleDown2 = ObjectAnimator.ofPropertyValuesHolder(
                bubble2,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.2f)
        );
        scaleDown2.setDuration(5000);
        scaleDown2.setStartDelay(1000);
        scaleDown2.setRepeatCount(ValueAnimator.INFINITE);
        scaleDown2.setRepeatMode(ValueAnimator.REVERSE);
        scaleDown2.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleDown2.start();
    }

    private void animateIn(View view, long delayMs) {
        view.setAlpha(0f);
        view.setTranslationY(60f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(delayMs)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }
}