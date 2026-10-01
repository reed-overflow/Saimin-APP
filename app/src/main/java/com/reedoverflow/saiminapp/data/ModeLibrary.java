package com.reedoverflow.saiminapp.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** All modes and their selection are written together as one versioned document. */
public final class ModeLibrary {
    public final List<ModeConfig> modes = new ArrayList<>();
    public String selectedId;

    public ModeConfig find(String id) {
        for (ModeConfig mode : modes) {
            if (mode.id.equals(id)) return mode;
        }
        return null;
    }

    public ModeConfig selected() {
        ModeConfig mode = find(selectedId);
        return mode != null ? mode : modes.get(0);
    }

    public boolean delete(String id) {
        ModeConfig mode = find(id);
        if (mode == null || modes.size() <= 1) return false;
        modes.remove(mode);
        selectedId = selected().id;
        return true;
    }

    public String toJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("version", 1);
        root.put("selectedId", selected().id);
        JSONArray entries = new JSONArray();
        for (ModeConfig mode : modes) {
            JSONObject entry = new JSONObject();
            entry.put("id", mode.id);
            entry.put("name", mode.name);
            entry.put("sizePercent", mode.animationSizePercent);
            entry.put("heightDp", mode.animationHeightDp);
            entry.put("durationMs", mode.animationDurationMs);
            entry.put("color", mode.animationColor);
            entry.put("backgroundColor", mode.backgroundColor);
            entry.put("animationType", mode.animationType);
            entry.put("fullscreen", mode.fullscreen);
            JSONArray parameters = new JSONArray();
            for (ParameterConfig parameter : mode.parameters) {
                JSONObject item = new JSONObject();
                item.put("id", parameter.id);
                item.put("name", parameter.name);
                item.put("min", parameter.min.toPlainString());
                item.put("max", parameter.max.toPlainString());
                item.put("step", parameter.step.toPlainString());
                item.put("default", parameter.defaultValue.toPlainString());
                parameters.put(item);
            }
            entry.put("parameters", parameters);
            entries.put(entry);
        }
        root.put("modes", entries);
        return root.toString();
    }

    public static ModeLibrary fromJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        ModeLibrary library = new ModeLibrary();
        JSONArray entries = root.getJSONArray("modes");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.optJSONObject(i);
            if (entry == null || entry.optString("name").trim().isEmpty()) continue;
            ModeConfig mode = new ModeConfig(entry.optString("id"), entry.getString("name"));
            if (library.find(mode.id) != null) continue;
            mode.animationSizePercent = bounded(entry.optInt("sizePercent", 90), 10, 100);
            mode.animationHeightDp = bounded(entry.optInt("heightDp", 280), 120, 800);
            mode.animationDurationMs = bounded(entry.optInt("durationMs", 5000), 1000, 20000);
            String color = entry.optString("color", mode.animationColor);
            if (color.matches("#[0-9a-fA-F]{6}")) mode.animationColor = color;
            String background = entry.optString("backgroundColor", mode.backgroundColor);
            if (background.matches("#[0-9a-fA-F]{6}")) mode.backgroundColor = background;
            String type = entry.optString("animationType", "ripple");
            if ("spiral".equals(type) || "pendulum".equals(type)) mode.animationType = type;
            mode.fullscreen = entry.optBoolean("fullscreen", false);
            JSONArray parameters = entry.optJSONArray("parameters");
            if (parameters != null) {
                for (int j = 0; j < parameters.length(); j++) {
                    try {
                        JSONObject item = parameters.getJSONObject(j);
                        ParameterConfig parameter = new ParameterConfig(item.optString("id"),
                                item.getString("name"), new BigDecimal(item.getString("min")),
                                new BigDecimal(item.getString("max")), new BigDecimal(item.getString("step")),
                                new BigDecimal(item.getString("default")));
                        boolean duplicate = false;
                        for (ParameterConfig existing : mode.parameters) {
                            if (existing.id.equals(parameter.id)) duplicate = true;
                        }
                        if (!duplicate) mode.parameters.add(parameter);
                    } catch (JSONException | IllegalArgumentException | ArithmeticException ignored) {
                        // A damaged parameter must not make other modes unavailable.
                    }
                }
            }
            library.modes.add(mode);
        }
        if (library.modes.isEmpty()) throw new JSONException("No valid modes");
        library.selectedId = root.optString("selectedId");
        library.selectedId = library.selected().id;
        return library;
    }

    private static int bounded(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
