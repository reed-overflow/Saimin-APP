package com.reedoverflow.saiminapp.ui.settings;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.reedoverflow.saiminapp.BuildConfig;
import com.reedoverflow.saiminapp.R;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Only checks on request. Rotation cancels the old UI callback and restarts the request. */
public class UpdateCheckDialog extends DialogFragment {
    private static final String RELEASES = "https://github.com/reed-overflow/Saimin-APP/releases";
    private final Handler main = new Handler(Looper.getMainLooper());
    private ExecutorService worker;
    private volatile HttpURLConnection connection;
    private volatile boolean cancelled;

    @NonNull
    @Override public Dialog onCreateDialog(Bundle state) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle(R.string.check_updates)
                .setMessage(R.string.checking_updates)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.update_open, (ignored, which) -> {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES))); }
                    catch (ActivityNotFoundException exception) {
                        Toast.makeText(requireContext(), R.string.no_browser, Toast.LENGTH_LONG).show();
                    }
                }).create();
        cancelled = false;
        worker = Executors.newSingleThreadExecutor();
        worker.execute(() -> {
            int result = R.string.update_failed;
            String version = null;
            HttpURLConnection request = null;
            try {
                request = (HttpURLConnection) new URL("https://api.github.com/repos/reed-overflow/Saimin-APP/releases/latest").openConnection();
                connection = request;
                if (cancelled) return;
                request.setConnectTimeout(10000);
                request.setReadTimeout(10000);
                request.setRequestProperty("Accept", "application/vnd.github+json");
                request.setRequestProperty("User-Agent", "Saimin-APP/" + BuildConfig.VERSION_NAME);
                int status = request.getResponseCode();
                if (status == 404) result = R.string.update_no_release;
                else if (status == 200) {
                    try (InputStream input = request.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int count;
                        while (!cancelled && (count = input.read(buffer)) != -1) {
                            if (output.size() + count > 1024 * 1024) throw new java.io.IOException("Response too large");
                            output.write(buffer, 0, count);
                        }
                        JSONObject release = new JSONObject(output.toString("UTF-8"));
                        version = release.getString("tag_name");
                        Integer comparison = compareVersions(version, BuildConfig.VERSION_NAME);
                        result = comparison == null ? R.string.update_unknown_version
                                : comparison > 0 ? R.string.update_available : R.string.update_latest;
                    }
                }
            } catch (Exception ignored) {
                result = R.string.update_failed;
            } finally {
                if (request != null) request.disconnect();
                connection = null;
            }
            final int message = result;
            final String tag = version;
            if (!cancelled) main.post(() -> {
                if (cancelled || !isAdded() || getDialog() != dialog) return;
                if (message == R.string.update_available) {
                    dialog.setTitle(getString(message, tag));
                    dialog.setMessage(getString(R.string.update_current, BuildConfig.VERSION_NAME, tag));
                } else if (message == R.string.update_unknown_version) dialog.setMessage(getString(message, tag));
                else if (message == R.string.update_latest) dialog.setMessage(getString(message, BuildConfig.VERSION_NAME));
                else dialog.setMessage(getString(message));
            });
        });
        return dialog;
    }

    // Unknown suffixes are reported without claiming that the installed version is current.
    private static Integer compareVersions(String remote, String local) {
        Pattern pattern = Pattern.compile("(?i)^v?(\\d+(?:\\.\\d+)*)(?:[-.]?(alpha|beta|rc|a|b)(\\d*))?(?:\\+[0-9a-z.-]+)?$");
        Matcher left = pattern.matcher(remote.trim());
        Matcher right = pattern.matcher(local.trim());
        if (!left.matches() || !right.matches()) return null;
        String[] a = left.group(1).split("\\.");
        String[] b = right.group(1).split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int difference = new BigInteger(i < a.length ? a[i] : "0").compareTo(new BigInteger(i < b.length ? b[i] : "0"));
            if (difference != 0) return difference;
        }
        int difference = Integer.compare(rank(left.group(2)), rank(right.group(2)));
        if (difference != 0) return difference;
        String an = left.group(3), bn = right.group(3);
        return new BigInteger(an == null || an.isEmpty() ? "0" : an)
                .compareTo(new BigInteger(bn == null || bn.isEmpty() ? "0" : bn));
    }

    private static int rank(String qualifier) {
        if (qualifier == null) return 3;
        if (qualifier.equalsIgnoreCase("rc")) return 2;
        if (qualifier.equalsIgnoreCase("b") || qualifier.equalsIgnoreCase("beta")) return 1;
        return 0;
    }

    @Override public void onDestroyView() {
        cancelled = true;
        main.removeCallbacksAndMessages(null);
        if (worker != null) worker.shutdownNow();
        HttpURLConnection request = connection;
        if (request != null) request.disconnect();
        super.onDestroyView();
    }
}
