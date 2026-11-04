package com.example.eskwela;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Locale;

public class UpdateHelper {

    public static final String BASE_APK_DOWNLOAD_URL = "https://github.com/jameshofril06-jpg/SkwelaApp/releases/download/v1.0.7/app-release.apk";
    public static final String LATEST_VERSION_NAME = "1.0.7";
    public static final int LATEST_VERSION_CODE = 7;

    public static void downloadAndInstallUpdate(Context context) {
        // 1. Visual checking loading indicator
        ProgressDialog checkingDialog = new ProgressDialog(context);
        checkingDialog.setTitle("Checking for Updates 🔍");
        checkingDialog.setMessage("Comparing installed version with latest GitHub release...");
        checkingDialog.setCancelable(false);
        checkingDialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (checkingDialog.isShowing()) checkingDialog.dismiss();

            String currentVersion;
            int currentCode = 0;
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                currentVersion = pInfo.versionName != null ? pInfo.versionName : "1.0.0";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    currentCode = (int) pInfo.getLongVersionCode();
                } else {
                    @SuppressWarnings("deprecation")
                    int legacyCode = pInfo.versionCode;
                    currentCode = legacyCode;
                }
            } catch (Exception e) {
                currentVersion = "1.0.0";
            }

            // 2. Robust intelligent version detection (Checks both versionCode and versionName)
            boolean isLatestOrNewer = currentCode >= LATEST_VERSION_CODE ||
                    (currentVersion != null && (currentVersion.trim().equalsIgnoreCase(LATEST_VERSION_NAME) || currentVersion.compareTo(LATEST_VERSION_NAME) >= 0));

            if (isLatestOrNewer) {
                new AlertDialog.Builder(context)
                        .setTitle("You're Up to Date! ✨")
                        .setMessage("You are currently running ESKWELA v" + currentVersion + " (Build " + currentCode + "), which is the latest version available.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            // 3. Otherwise, show update release notes and prompt download
            String whatsNew = "🚀 What's New & Fixes in ESKWELA v" + LATEST_VERSION_NAME + ":\n\n" +
                    "• ⏳ Admin Suspension Time Tracking: Displays real-time remaining suspension countdowns (e.g. 6h 42m remaining).\n" +
                    "• 📢 Announcement Log & Sharing: Clickable announcement detail modal with broadcast share features.\n" +
                    "• ⏱️ Focus Session Presets: Added 2m, 5m, 10m, 15m, 25m, 45m, and 60m focus session timer options.\n" +
                    "• 🌙 Dark Mode Profile Text Contrast: High-contrast visibility when editing profiles in Dark Mode.\n" +
                    "• 🔍 Intelligent OTA Version Detection: Automatically detects if you are already on the latest version (v1.0.7).\n" +
                    "• ⚠️ Time Schedule Conflict Detection: Checks for overlapping class times when adding schedules.\n" +
                    "• 📦 Live Download Progress Bar: Real-time percentage tracking when downloading OTA updates.\n" +
                    "• 📷 OCR Study Load Document Scanner & Auto-Plotter across weekdays.\n" +
                    "• 🤖 Smart AI Quizzes & Ranking Leaderboard.\n" +
                    "• 🛡️ Admin Control Center with user moderation.\n" +
                    "• 📝 Task Drop Confirmation with Reason & Class Deletion Code Validation.\n" +
                    "• ℹ️ 'About This App' settings section.\n\n" +
                    "Would you like to download and install v" + LATEST_VERSION_NAME + " now?";

            new AlertDialog.Builder(context)
                    .setTitle("📦 ESKWELA Update Available (v" + LATEST_VERSION_NAME + ")")
                    .setMessage(whatsNew)
                    .setPositiveButton("Download & Install", (dialog, which) -> startDownloadWithProgress(context))
                    .setNegativeButton("Later", null)
                    .show();
        }, 1200);
    }

    private static void startDownloadWithProgress(Context context) {
        try {
            // Delete any stale cached APK files to prevent installing old cached versions
            try {
                File oldFile1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "eskwela-latest.apk");
                if (oldFile1.exists()) {
                    boolean ignored = oldFile1.delete();
                }
                File oldFile2 = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "eskwela-latest.apk");
                if (oldFile2.exists()) {
                    boolean ignored = oldFile2.delete();
                }
            } catch (Exception ignored) {}

            // Append cache-busting timestamp query parameter so DownloadManager always fetches fresh APK
            String cacheBustedUrl = BASE_APK_DOWNLOAD_URL + "?t=" + System.currentTimeMillis();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(cacheBustedUrl));
            request.setTitle("ESKWELA OTA Update");
            request.setDescription("Downloading latest version v1.0.7 APK...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "eskwela-latest.apk");
            request.setMimeType("application/vnd.android.package-archive");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) {
                Toast.makeText(context, "Download Manager not available", Toast.LENGTH_SHORT).show();
                return;
            }

            long downloadId = manager.enqueue(request);

            // Create Live Progress Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Downloading Update 📦");
            builder.setMessage("Please wait while v1.0.7 is being downloaded...");
            builder.setCancelable(false);

            final AlertDialog progressDialog = builder.create();
            progressDialog.show();

            // Poll progress in background handler
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable progressChecker = new Runnable() {
                @Override
                public void run() {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = manager.query(query);
                    if (cursor != null && cursor.moveToFirst()) {
                        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                        int status = cursor.getInt(statusIndex);
                        long downloaded = cursor.getLong(downloadedIndex);
                        long total = cursor.getLong(totalIndex);
                        cursor.close();

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            installApk(context);
                            return;
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            Toast.makeText(context, "Download failed. Check if GitHub release v1.0.7 asset is public.", Toast.LENGTH_LONG).show();
                            return;
                        } else if (total > 0) {
                            int progress = (int) ((downloaded * 100L) / total);
                            progressDialog.setMessage(String.format(Locale.getDefault(), "Downloading v1.0.7... %d%% (%d KB / %d KB)", progress, downloaded / 1024, total / 1024));
                        }
                    }
                    handler.postDelayed(this, 500);
                }
            };
            handler.post(progressChecker);

        } catch (Exception e) {
            Toast.makeText(context, "Download error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void installApk(Context context) {
        try {
            File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "eskwela-latest.apk");
            if (!apkFile.exists()) {
                apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "eskwela-latest.apk");
            }

            if (apkFile.exists()) {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(intent);
                Toast.makeText(context, "Opening installer...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Downloaded APK file not found", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Installation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
