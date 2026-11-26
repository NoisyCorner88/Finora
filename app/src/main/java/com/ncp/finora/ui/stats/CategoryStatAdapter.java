package com.ncp.finora.ui.stats;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.R;
import com.ncp.finora.data.local.model.CategoryTotal;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {
    private final List<CategoryTotal> items = new ArrayList<>();
    private double sum = 0;

    public void submit(List<CategoryTotal> list) {
        items.clear();
        sum = 0;
        if (list != null) {
            items.addAll(list);
            for (CategoryTotal c : list) sum += c.total;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_stat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CategoryTotal item = items.get(position);
        double percent = sum > 0 ? item.total / sum * 100 : 0;
        int color = UiUtils.parseColor(item.color, Color.parseColor("#1555E8"));
        h.name.setText(item.icon + "  " + item.name);
        h.amount.setText(Formatters.rupiah(item.total));
        h.percent.setText(String.format(new Locale("id", "ID"), "%.1f%%", percent));
        h.bar.setProgress((int) Math.round(percent));
        h.bar.setProgressTintList(ColorStateList.valueOf(color));
        h.bar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8EEFF")));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, amount, percent;
        final ProgressBar bar;

        ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tvCategoryName);
            amount = v.findViewById(R.id.tvCategoryAmount);
            percent = v.findViewById(R.id.tvCategoryPercent);
            bar = v.findViewById(R.id.pbCategory);
        }
    }
}
