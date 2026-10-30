package com.example.eskwela;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

public class UpdateHelper {

    // Direct download URL for GitHub releases pointing to the APK asset (not the HTML release page)
    public static final String APK_DOWNLOAD_URL = "https://github.com/bontia/skwela/releases/latest/download/app-release.apk";

    public static void downloadAndInstallUpdate(Context context) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(APK_DOWNLOAD_URL));
            request.setTitle("ESKWELA OTA Update");
            request.setDescription("Downloading latest version...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "eskwela-latest.apk");
            request.setMimeType("application/vnd.android.package-archive");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(context, "Downloading update APK...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Download Manager not available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
