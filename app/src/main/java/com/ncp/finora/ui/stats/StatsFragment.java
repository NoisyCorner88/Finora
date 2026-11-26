package com.ncp.finora.ui.stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.model.CategoryTotal;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.UiUtils;

import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {
    private static final int GREEN = 0xFF00A879;
    private static final int RED = 0xFFFF253B;

    private StatsViewModel viewModel;
    private StatsData current;
    private StatsViewModel.Period currentPeriod = StatsViewModel.Period.THIS_MONTH;
    private int userId;

    private TextView tvNet, tvNetHint, tvIncome, tvExpense, tvPeriodLabel;
    private TextView tvBudgetLimit, tvBudgetRemaining, tvBudgetPercent, tvTrendBadge, tvChartDetail, tvChartTitle;
    private TextView tvCategoryTitle;
    private View cardBudget, empty;
    private ProgressBar progressBudget;
    private BarChartView barChart;
    private DonutChartView donut;
    private RecyclerView rv;
    private CategoryStatAdapter adapter;
    private TextView pillThis, pillLast, pillAll, chipExpense, chipIncome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, FinoraApp.factory(requireContext())).get(StatsViewModel.class);
        userId = FinoraApp.container(requireContext()).session.getUserId();

        tvNet = view.findViewById(R.id.tvNet);
        tvNetHint = view.findViewById(R.id.tvNetHint);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvPeriodLabel = view.findViewById(R.id.tvPeriodLabel);
        tvBudgetLimit = view.findViewById(R.id.tvBudgetLimit);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        tvBudgetPercent = view.findViewById(R.id.tvBudgetPercent);
        tvTrendBadge = view.findViewById(R.id.tvTrendBadge);
        tvChartDetail = view.findViewById(R.id.tvChartDetail);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        cardBudget = view.findViewById(R.id.cardBudget);
        progressBudget = view.findViewById(R.id.progressBudget);
        barChart = view.findViewById(R.id.barChart);
        donut = view.findViewById(R.id.donutChart);
        empty = view.findViewById(R.id.tvEmptyStats);
        rv = view.findViewById(R.id.rvCategories);
        pillThis = view.findViewById(R.id.pillThisMonth);
        pillLast = view.findViewById(R.id.pillLastMonth);
        pillAll = view.findViewById(R.id.pillAllTime);
        chipExpense = view.findViewById(R.id.chipCatExpense);
        chipIncome = view.findViewById(R.id.chipCatIncome);

        TextView tvAvatar = view.findViewById(R.id.tvAvatarStats);
        tvAvatar.setText(UiUtils.initial(FinoraApp.container(requireContext()).session.getName()));

        adapter = new CategoryStatAdapter();
        rv.setAdapter(adapter);

        pillThis.setOnClickListener(v -> viewModel.setPeriod(StatsViewModel.Period.THIS_MONTH));
        pillLast.setOnClickListener(v -> viewModel.setPeriod(StatsViewModel.Period.LAST_MONTH));
        pillAll.setOnClickListener(v -> viewModel.setPeriod(StatsViewModel.Period.ALL));
        chipExpense.setOnClickListener(v -> viewModel.setType(Constants.EXPENSE));
        chipIncome.setOnClickListener(v -> viewModel.setType(Constants.INCOME));
        cardBudget.setOnClickListener(v -> showBudgetDialog());
        barChart.setOnBucketSelectedListener(this::showBucketDetail);

        viewModel.getPeriod().observe(getViewLifecycleOwner(), p -> {
            currentPeriod = p;
            stylePill(pillThis, p == StatsViewModel.Period.THIS_MONTH);
            stylePill(pillLast, p == StatsViewModel.Period.LAST_MONTH);
            stylePill(pillAll, p == StatsViewModel.Period.ALL);
            tvChartTitle.setText(p == StatsViewModel.Period.ALL ? "Tren Bulanan" : "Tren Mingguan");
            cardBudget.setVisibility(p == StatsViewModel.Period.ALL ? View.GONE : View.VISIBLE);
            renderBudget();
        });
        viewModel.getPeriodLabel().observe(getViewLifecycleOwner(), tvPeriodLabel::setText);
        viewModel.getType().observe(getViewLifecycleOwner(), t -> {
            boolean exp = Constants.EXPENSE.equals(t);
            UiUtils.setChipSelected(chipExpense, exp);
            UiUtils.setChipSelected(chipIncome, !exp);
            tvCategoryTitle.setText(exp ? "Pengeluaran per Kategori" : "Pemasukan per Kategori");
        });
        viewModel.getData().observe(getViewLifecycleOwner(), this::render);
    }

    private void stylePill(TextView pill, boolean selected) {
        pill.setBackgroundResource(selected ? R.drawable.bg_chip_selected : android.R.color.transparent);
        pill.setTextColor(selected ? Color.WHITE : 0xFF667085);
    }

    private void render(StatsData d) {
        if (d == null) return;
        current = d;

        tvIncome.setText(Formatters.currency(d.income));
        tvExpense.setText(Formatters.currency(d.expense));
        boolean pos = d.net() >= 0;
        tvNet.setText((pos && d.net() > 0 ? "+" : "") + Formatters.currency(d.net()));
        tvNet.setTextColor(d.net() == 0 ? 0xFF182033 : (pos ? GREEN : RED));
        double rate = d.savingsRate();
        tvNetHint.setText(rate < 0
                ? (d.count == 0 ? "Belum ada transaksi" : "Belum ada pemasukan pada periode ini")
                : String.format(new Locale("id", "ID"), "%.1f%% dari pemasukan %s", Math.abs(rate),
                        rate >= 0 ? "berhasil disisihkan" : "terlampaui oleh pengeluaran"));

        barChart.setData(d.bucketLabels, d.bucketIncome, d.bucketExpense, d.defaultBucket);
        showBucketDetail(d.defaultBucket);
        if (d.trendText.isEmpty()) {
            tvTrendBadge.setVisibility(View.GONE);
        } else {
            tvTrendBadge.setVisibility(View.VISIBLE);
            tvTrendBadge.setText(d.trendText + " (pengeluaran)");
            tvTrendBadge.setTextColor(d.trendGood ? GREEN : RED);
        }

        adapter.submit(d.breakdown);
        boolean isEmpty = d.breakdown.isEmpty();
        empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        renderDonut(d.breakdown);
        renderBudget();
    }

    private void renderDonut(List<CategoryTotal> list) {
        double[] values = new double[list.size()];
        int[] colors = new int[list.size()];
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i).total;
            colors[i] = UiUtils.parseColor(list.get(i).color, 0xFF1555E8);
            sum += values[i];
        }
        boolean exp = Constants.EXPENSE.equals(viewModel.getType().getValue());
        donut.setData(values, colors, exp ? "Pengeluaran" : "Pemasukan", "Rp " + Formatters.compactRupiah(sum));
    }

    private void showBucketDetail(int idx) {
        if (current == null || idx < 0 || idx >= current.bucketLabels.length) {
            tvChartDetail.setText(current == null || current.bucketLabels.length == 0
                    ? "" : "Ketuk batang untuk melihat detail");
            return;
        }
        tvChartDetail.setText(String.format("%s  \u2022  Masuk %s  \u2022  Keluar %s",
                current.bucketLabels[idx],
                Formatters.currency(current.bucketIncome[idx]),
                Formatters.currency(current.bucketExpense[idx])));
    }

    // ---- Anggaran bulanan (disimpan per pengguna) ----

    private SharedPreferences prefs() {
        return requireContext().getSharedPreferences("finora_budget", Context.MODE_PRIVATE);
    }

    private long budget() { return prefs().getLong("budget_" + userId, 0L); }

    private void renderBudget() {
        if (current == null || currentPeriod == StatsViewModel.Period.ALL) return;
        long limit = budget();
        if (limit <= 0) {
            tvBudgetLimit.setText("Batas anggaran belum diatur");
            tvBudgetRemaining.setText("Atur");
            tvBudgetRemaining.setTextColor(0xFF1555E8);
            progressBudget.setProgress(0);
            tvBudgetPercent.setText("Ketuk untuk menetapkan batas pengeluaran bulanan");
            return;
        }
        double remaining = limit - current.expense;
        int percent = (int) Math.min(100, Math.round(current.expense / limit * 100.0));
        tvBudgetLimit.setText("Batas: " + Formatters.currency(limit));
        progressBudget.setProgress(percent);
        if (remaining >= 0) {
            tvBudgetRemaining.setText("Tersisa " + Formatters.currency(remaining));
            tvBudgetRemaining.setTextColor(0xFF1555E8);
            tvBudgetPercent.setText(percent + "% terpakai");
        } else {
            tvBudgetRemaining.setText("Lebih " + Formatters.currency(-remaining));
            tvBudgetRemaining.setTextColor(RED);
            tvBudgetPercent.setText("Melebihi batas anggaran");
        }
    }

    private void showBudgetDialog() {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Mis. 5000000");
        long cur = budget();
        if (cur > 0) input.setText(String.valueOf(cur));
        FrameLayout box = new FrameLayout(requireContext());
        int pad = UiUtils.dp(requireContext(), 24);
        box.setPadding(pad, UiUtils.dp(requireContext(), 8), pad, 0);
        box.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Batas anggaran bulanan")
                .setView(box)
                .setPositiveButton("Simpan", (d, w) -> {
                    String digits = input.getText().toString().replaceAll("[^0-9]", "");
                    long v = 0;
                    try { if (!digits.isEmpty() && digits.length() <= 15) v = Long.parseLong(digits); } catch (Exception ignored) { }
                    prefs().edit().putLong("budget_" + userId, v).apply();
                    renderBudget();
                })
                .setNeutralButton("Hapus batas", (d, w) -> {
                    prefs().edit().remove("budget_" + userId).apply();
                    renderBudget();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rv.setAdapter(null);
    }
}
