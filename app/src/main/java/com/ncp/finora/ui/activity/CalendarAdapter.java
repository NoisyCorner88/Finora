package com.ncp.finora.ui.activity;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.R;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Grid tanggal 7 kolom (Senin-Minggu) untuk satu bulan. */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    public interface OnDayClick { void onClick(LocalDate day); }

    private final List<LocalDate> cells = new ArrayList<>(); // null = sel kosong
    private Set<Integer> marked = Collections.emptySet();
    private LocalDate selected;
    private final OnDayClick listener;

    public CalendarAdapter(OnDayClick listener) { this.listener = listener; }

    public void submit(YearMonth month, Set<Integer> markedDays, LocalDate selectedDay) {
        cells.clear();
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1; // Senin = 0
        for (int i = 0; i < offset; i++) cells.add(null);
        for (int d = 1; d <= month.lengthOfMonth(); d++) cells.add(month.atDay(d));
        marked = markedDays == null ? Collections.emptySet() : markedDays;
        selected = selectedDay;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        LocalDate date = cells.get(position);
        if (date == null) {
            h.day.setText("");
            h.day.setBackground(null);
            h.dot.setVisibility(View.GONE);
            h.itemView.setOnClickListener(null);
            h.itemView.setClickable(false);
            return;
        }
        boolean isSelected = date.equals(selected);
        boolean isToday = date.equals(LocalDate.now());
        h.day.setText(String.valueOf(date.getDayOfMonth()));
        if (isSelected) {
            h.day.setBackgroundResource(R.drawable.bg_circle_blue);
            h.day.setTextColor(Color.WHITE);
        } else if (isToday) {
            h.day.setBackgroundResource(R.drawable.bg_day_today);
            h.day.setTextColor(0xFF1555E8);
        } else {
            h.day.setBackground(null);
            h.day.setTextColor(0xFF182033);
        }
        h.dot.setVisibility(marked.contains(date.getDayOfMonth()) && !isSelected ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(date); });
    }

    @Override
    public int getItemCount() { return cells.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView day;
        final View dot;

        ViewHolder(@NonNull View v) {
            super(v);
            day = v.findViewById(R.id.tvDay);
            dot = v.findViewById(R.id.dotDay);
        }
    }
}
