package com.reedoverflow.saiminapp.data;

import java.math.BigDecimal;
import java.util.UUID;

/** Exact decimal arithmetic keeps the slider endpoints and defaults on the same grid. */
public final class ParameterConfig {
    public final String id;
    public final String name;
    public final BigDecimal min;
    public final BigDecimal max;
    public final BigDecimal step;
    public final BigDecimal defaultValue;

    public ParameterConfig(String id, String name, BigDecimal min, BigDecimal max,
                           BigDecimal step, BigDecimal defaultValue) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A parameter needs a name");
        }
        if (min.compareTo(max) >= 0 || step.signum() <= 0
                || defaultValue.compareTo(min) < 0 || defaultValue.compareTo(max) > 0) {
            throw new IllegalArgumentException("Invalid parameter range");
        }
        BigDecimal range = max.subtract(min);
        if (range.remainder(step).signum() != 0
                || defaultValue.subtract(min).remainder(step).signum() != 0
                || range.divide(step).compareTo(BigDecimal.valueOf(100000)) > 0) {
            throw new IllegalArgumentException("Range and default must align to step (at most 100000 steps)");
        }
        this.id = id == null || id.isEmpty() ? UUID.randomUUID().toString() : id;
        this.name = name.trim();
        this.min = min;
        this.max = max;
        this.step = step;
        this.defaultValue = defaultValue;
    }

    public int stepCount() {
        return max.subtract(min).divide(step).intValueExact();
    }

    public int defaultProgress() {
        return defaultValue.subtract(min).divide(step).intValueExact();
    }

    public String valueAt(int progress) {
        int bounded = Math.max(0, Math.min(stepCount(), progress));
        return format(min.add(step.multiply(BigDecimal.valueOf(bounded))));
    }

    public static String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
