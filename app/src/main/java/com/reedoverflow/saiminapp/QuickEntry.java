package com.reedoverflow.saiminapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

/** An optional launcher notification; no background service is needed. */
public final class QuickEntry {
    public static final String KEY = "notification_entry";
    public static final String HOME = "com.reedoverflow.saiminapp.OPEN_HOME";
    public static final String SETTINGS = "com.reedoverflow.saiminapp.OPEN_SETTINGS";
    public static final String CHANNEL = "quick_entry";
    private static final int ID = 17;

    private QuickEntry() { }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL,
                    context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    public static boolean allowed(Context context) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false;
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = context.getSystemService(NotificationManager.class).getNotificationChannel(CHANNEL);
            if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) return false;
        }
        return true;
    }

    public static void setEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY, enabled).apply();
        sync(context);
    }

    public static void sync(Context context) {
        createChannel(context);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY, false) || !allowed(context)) {
            manager.cancel(ID);
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_menu_home)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_body))
                .setContentIntent(pending(context, HOME, 0))
                .addAction(R.drawable.ic_menu_settings, context.getString(R.string.shortcut_settings), pending(context, SETTINGS, 1))
                .setOngoing(true).setOnlyAlertOnce(true).setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        try {
            manager.notify(ID, builder.build());
        } catch (SecurityException ignored) {
            // Permission can be revoked between checking and posting.
        }
    }

    private static PendingIntent pending(Context context, String action, int request) {
        Intent intent = new Intent(context, MainActivity.class).setAction(action)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, request, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
