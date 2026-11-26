package com.ncp.finora.ui.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/** Grafik donat sederhana dengan teks di tengah. */
public class DonutChartView extends View {
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private final float density;

    private double[] values = new double[0];
    private int[] colors = new int[0];
    private String centerTop = "Total";
    private String centerBottom = "Rp 0";

    public DonutChartView(Context c) { this(c, null); }

    public DonutChartView(Context c, @Nullable AttributeSet a) {
        super(c, a);
        density = c.getResources().getDisplayMetrics().density;
        float sd = c.getResources().getDisplayMetrics().scaledDensity;
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);
        topPaint.setTextAlign(Paint.Align.CENTER);
        topPaint.setColor(0xFF667085);
        topPaint.setTextSize(11 * sd);
        bottomPaint.setTextAlign(Paint.Align.CENTER);
        bottomPaint.setColor(0xFF182033);
        bottomPaint.setFakeBoldText(true);
        bottomPaint.setTextSize(15 * sd);
    }

    public void setData(double[] values, int[] colors, String top, String bottom) {
        this.values = values;
        this.colors = colors;
        this.centerTop = top;
        this.centerBottom = bottom;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = 22 * density;
        float size = Math.min(getWidth(), getHeight()) - stroke;
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        oval.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f);
        arcPaint.setStrokeWidth(stroke);

        double sum = 0;
        for (double v : values) sum += v;

        if (sum <= 0) {
            arcPaint.setColor(0xFFE8EEFF);
            canvas.drawArc(oval, 0, 360, false, arcPaint);
        } else {
            float start = -90f;
            float gap = values.length > 1 ? 2f : 0f;
            for (int i = 0; i < values.length; i++) {
                float sweep = (float) (values[i] / sum * 360.0);
                arcPaint.setColor(colors[i]);
                canvas.drawArc(oval, start, Math.max(0.5f, sweep - gap), false, arcPaint);
                start += sweep;
            }
        }
        canvas.drawText(centerTop, cx, cy - 2 * density, topPaint);
        canvas.drawText(centerBottom, cx, cy + 18 * density, bottomPaint);
    }
}
