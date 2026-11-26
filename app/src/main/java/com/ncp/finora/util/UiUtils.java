package com.ncp.finora.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.TextView;

import com.ncp.finora.R;

public final class UiUtils {
    private UiUtils() { }

    public static int dp(Context context, int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    /** Gaya "chip" filter sesuai desain: terpilih = biru solid, tidak = putih berbingkai. */
    public static void setChipSelected(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(selected ? Color.WHITE : Color.parseColor("#1555E8"));
    }

    public static int parseColor(String hex, int fallback) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static String initial(String name) {
        if (name == null || name.trim().isEmpty()) return "F";
        return name.trim().substring(0, 1).toUpperCase();
    }
}
