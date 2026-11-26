package com.ncp.finora.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.ncp.finora.util.Formatters;

/** Grafik batang berkelompok: pemasukan (hijau) vs pengeluaran (merah). Ketuk kelompok untuk detail. */
public class BarChartView extends View {
    public interface OnBucketSelected { void onSelected(int index); }

    private static final int COLOR_INCOME = 0xFF00A879;
    private static final int COLOR_EXPENSE = 0xFFFF253B;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private String[] labels = new String[0];
    private double[] income = new double[0];
    private double[] expense = new double[0];
    private int selected = -1;
    private OnBucketSelected listener;
    private final float density;

    public BarChartView(Context c) { this(c, null); }

    public BarChartView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        density = c.getResources().getDisplayMetrics().density;
        gridPaint.setColor(0xFFE8EEFF);
        gridPaint.setStrokeWidth(density);
        textPaint.setTextSize(10 * c.getResources().getDisplayMetrics().scaledDensity);
        hiPaint.setColor(0xFFEEF2FF);
    }

    public void setOnBucketSelectedListener(OnBucketSelected l) { listener = l; }

    public int getSelected() { return selected; }

    public void setData(String[] labels, double[] income, double[] expense, int selectedIndex) {
        this.labels = labels;
        this.income = income;
        this.expense = expense;
        this.selected = selectedIndex < labels.length ? selectedIndex : -1;
        invalidate();
    }

    private float dp(float v) { return v * density; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = labels.length;
        double max = 0;
        for (int i = 0; i < n; i++) max = Math.max(max, Math.max(income[i], expense[i]));

        float left = dp(42), right = getWidth() - dp(4);
        float top = dp(8), bottom = getHeight() - dp(22);
        float h = bottom - top;

        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setColor(0xFF94A3B8);
        for (int g = 0; g <= 3; g++) {
            float y = bottom - h * g / 3f;
            canvas.drawLine(left, y, right, y, gridPaint);
            if (max > 0) canvas.drawText(Formatters.compactRupiah(max * g / 3.0), left - dp(6), y + dp(3), textPaint);
        }

        if (n == 0 || max <= 0) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(0xFF94A3B8);
            canvas.drawText("Belum ada data untuk periode ini", (left + right) / 2f, (top + bottom) / 2f, textPaint);
            return;
        }

        float groupW = (right - left) / n;
        float barW = Math.min(dp(16), groupW * 0.3f);
        float gap = dp(3);
        for (int i = 0; i < n; i++) {
            float cx = left + groupW * (i + 0.5f);
            if (i == selected) {
                rect.set(left + groupW * i + dp(2), top, left + groupW * (i + 1) - dp(2), bottom + dp(18));
                canvas.drawRoundRect(rect, dp(8), dp(8), hiPaint);
            }
            drawBar(canvas, cx - gap / 2f - barW, cx - gap / 2f, bottom, h, income[i], max, COLOR_INCOME);
            drawBar(canvas, cx + gap / 2f, cx + gap / 2f + barW, bottom, h, expense[i], max, COLOR_EXPENSE);

            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(i == selected ? 0xFF1555E8 : 0xFF667085);
            canvas.drawText(labels[i], cx, getHeight() - dp(5), textPaint);
        }
    }

    private void drawBar(Canvas c, float l, float r, float bottom, float h, double v, double max, int color) {
        if (v <= 0) return;
        float bh = Math.max(dp(3), (float) (v / max) * h);
        barPaint.setColor(color);
        rect.set(l, bottom - bh, r, bottom);
        c.drawRoundRect(rect, dp(4), dp(4), barPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (labels.length == 0) return false;
        if (e.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (e.getAction() == MotionEvent.ACTION_UP) {
            float left = dp(42), right = getWidth() - dp(4);
            float groupW = (right - left) / labels.length;
            int idx = (int) ((e.getX() - left) / groupW);
            if (idx >= 0 && idx < labels.length) {
                selected = idx;
                invalidate();
                if (listener != null) listener.onSelected(idx);
                performClick();
            }
            return true;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean performClick() { return super.performClick(); }
}
