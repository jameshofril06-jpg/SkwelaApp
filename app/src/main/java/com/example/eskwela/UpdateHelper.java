package com.example.eskwela;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class UpdateHelper {

    public static final String GITHUB_API_LATEST_RELEASE = "https://api.github.com/repos/jameshofril06-jpg/SkwelaApp/releases/latest";
    public static final String DEFAULT_FALLBACK_DOWNLOAD_URL = "https://github.com/jameshofril06-jpg/SkwelaApp/releases/latest/download/app-release.apk";

    public static void downloadAndInstallUpdate(Context context) {
        ProgressDialog checkingDialog = new ProgressDialog(context);
        checkingDialog.setTitle("Checking for Updates 🔍");
        checkingDialog.setMessage("Connecting to GitHub release servers...");
        checkingDialog.setCancelable(false);
        checkingDialog.show();

        new Thread(() -> {
            String latestTagName = "1.0.7";
            String downloadUrl = DEFAULT_FALLBACK_DOWNLOAD_URL;
            String releaseBody = "• ⏳ Admin Suspension Time Tracking\n• 📢 Announcement Log & Sharing\n• ⏱️ Focus Session Presets (2m, 5m, 10m, 15m, 25m, 45m, 60m)\n• 🌙 Dark Mode Profile Text Contrast\n• ⚠️ Time Schedule Conflict Detection\n• 📷 OCR Study Load Document Scanner\n• 🤖 Smart AI Quizzes & Ranking Leaderboard";

            try {
                URL url = new URL(GITHUB_API_LATEST_RELEASE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "ESKWELA-App");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    if (json.has("tag_name")) {
                        latestTagName = json.getString("tag_name").replace("v", "").trim();
                    }
                    if (json.has("body") && !json.getString("body").trim().isEmpty()) {
                        releaseBody = json.getString("body");
                    }
                    if (json.has("assets")) {
                        JSONArray assets = json.getJSONArray("assets");
                        for (int i = 0; i < assets.length(); i++) {
                            JSONObject asset = assets.getJSONObject(i);
                            if (asset.has("name") && asset.getString("name").contains("app-release") && asset.has("browser_download_url")) {
                                downloadUrl = asset.getString("browser_download_url");
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            final String finalLatestTag = latestTagName;
            final String finalDownloadUrl = downloadUrl;
            final String finalReleaseBody = releaseBody;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (checkingDialog.isShowing()) checkingDialog.dismiss();

                String currentVersion = "1.0.0";
                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    currentVersion = pInfo.versionName != null ? pInfo.versionName : "1.0.0";
                } catch (Exception ignored) {}

                // Compare version strings
                boolean isUpToDate = compareVersions(currentVersion, finalLatestTag) >= 0;

                if (isUpToDate) {
                    new AlertDialog.Builder(context)
                            .setTitle("You're Up to Date! ✨")
                            .setMessage("You are currently running ESKWELA v" + currentVersion + ", which is the latest version available on GitHub.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    String whatsNew = "🚀 What's New in ESKWELA v" + finalLatestTag + ":\n\n" +
                            finalReleaseBody + "\n\n" +
                            "Would you like to download and install v" + finalLatestTag + " now?";

                    new AlertDialog.Builder(context)
                            .setTitle("📦 ESKWELA Update Available (v" + finalLatestTag + ")")
                            .setMessage(whatsNew)
                            .setPositiveButton("Download & Install", (dialog, which) -> startDownloadWithProgress(context, finalDownloadUrl, finalLatestTag))
                            .setNegativeButton("Later", null)
                            .show();
                }
            });
        }).start();
    }

    public static int compareVersions(String v1, String v2) {
        String[] vals1 = v1.replaceAll("[^0-9.]", "").split("\\.");
        String[] vals2 = v2.replaceAll("[^0-9.]", "").split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        return Integer.signum(vals1.length - vals2.length);
    }

    private static void startDownloadWithProgress(Context context, String apkUrl, String versionTag) {
        try {
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

            String cacheBustedUrl = apkUrl + "?t=" + System.currentTimeMillis();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(cacheBustedUrl));
            request.setTitle("ESKWELA OTA Update");
            request.setDescription("Downloading latest version v" + versionTag + " APK...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "eskwela-latest.apk");
            request.setMimeType("application/vnd.android.package-archive");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) {
                Toast.makeText(context, "Download Manager not available", Toast.LENGTH_SHORT).show();
                return;
            }

            long downloadId = manager.enqueue(request);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Downloading Update 📦");
            builder.setMessage("Please wait while v" + versionTag + " is being downloaded...");
            builder.setCancelable(false);

            final AlertDialog progressDialog = builder.create();
            progressDialog.show();

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
                            Toast.makeText(context, "Download failed. Check if GitHub release asset is public.", Toast.LENGTH_LONG).show();
                            return;
                        } else if (total > 0) {
                            int progress = (int) ((downloaded * 100L) / total);
                            progressDialog.setMessage(String.format(Locale.getDefault(), "Downloading v%s... %d%% (%d KB / %d KB)", versionTag, progress, downloaded / 1024, total / 1024));
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
