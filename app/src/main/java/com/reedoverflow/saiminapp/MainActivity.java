package com.reedoverflow.saiminapp;

import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import androidx.preference.PreferenceManager;
import com.reedoverflow.saiminapp.ui.settings.IntroDialog;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        if (savedInstanceState == null) handleEntry(getIntent());
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(IntroDialog.COMPLETED, false)
                && getSupportFragmentManager().findFragmentByTag("intro") == null) {
            new IntroDialog().show(getSupportFragmentManager(), "intro");
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleEntry(intent);
    }

    private void handleEntry(Intent intent) {
        String action = intent.getAction();
        if (!QuickEntry.HOME.equals(action) && !QuickEntry.SETTINGS.equals(action)) return;
        NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);
        controller.popBackStack(R.id.nav_home, false);
        if (QuickEntry.SETTINGS.equals(action)) {
            controller.navigate(R.id.nav_settings, null, new NavOptions.Builder().setLaunchSingleTop(true).build());
        }
    }

    @Override protected void onResume() {
        super.onResume();
        QuickEntry.sync(this);
    }

    public void enableNotificationEntry() {
        QuickEntry.createChannel(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 41);
        } else if (QuickEntry.allowed(this)) {
            QuickEntry.setEnabled(this, true);
        } else {
            Toast.makeText(this, R.string.notification_permission, Toast.LENGTH_LONG).show();
            openNotificationSettings();
        }
    }

    public void openNotificationSettings() {
        QuickEntry.createChannel(this);
        Intent intent = Build.VERSION.SDK_INT >= 26
                ? new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                : new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 41) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && QuickEntry.allowed(this);
            QuickEntry.setEnabled(this, granted);
            if (!granted) Toast.makeText(this, R.string.notification_permission, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
