package com.ncp.finora.ui.stats;

import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.model.CategoryTotal;
import com.ncp.finora.data.local.model.TransactionDetail;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Hasil hitung statistik untuk satu periode. Semua angka (total, grafik batang, donat)
 * dihitung dari SATU daftar transaksi yang sama, sehingga selalu konsisten.
 */
public class StatsData {
    public double income;
    public double expense;
    public int count;
    public List<CategoryTotal> breakdown = new ArrayList<>();
    public String[] bucketLabels = new String[0];
    public double[] bucketIncome = new double[0];
    public double[] bucketExpense = new double[0];
    public String trendText = "";
    public boolean trendGood = true;
    /** Indeks bucket yang dipilih secara default (minggu/bulan terakhir). */
    public int defaultBucket = -1;

    public double net() { return income - expense; }

    /** Persentase sisa pemasukan, atau -1 bila tidak ada pemasukan. */
    public double savingsRate() { return income > 0 ? (income - expense) / income * 100.0 : -1; }

    public static StatsData compute(StatsViewModel.Period period, List<TransactionDetail> list, String type) {
        StatsData s = new StatsData();
        Map<Integer, CategoryTotal> cats = new HashMap<>();
        TreeMap<YearMonth, double[]> months = new TreeMap<>();

        YearMonth base = null;
        if (period == StatsViewModel.Period.THIS_MONTH) base = YearMonth.now();
        else if (period == StatsViewModel.Period.LAST_MONTH) base = YearMonth.now().minusMonths(1);

        int weeks = base == null ? 0 : (base.lengthOfMonth() + 6) / 7;
        double[] wInc = new double[weeks];
        double[] wExp = new double[weeks];

        if (list != null) {
            for (TransactionDetail d : list) {
                TransactionEntity t = d.transaction;
                boolean inc = Constants.INCOME.equals(t.type);
                if (inc) s.income += t.amount; else s.expense += t.amount;
                s.count++;

                if (type.equals(t.type)) {
                    CategoryTotal c = cats.get(t.categoryId);
                    if (c == null) {
                        c = new CategoryTotal();
                        c.id = t.categoryId;
                        c.name = d.categoryName;
                        c.icon = d.categoryIcon;
                        c.color = d.categoryColor;
                        cats.put(t.categoryId, c);
                    }
                    c.total += t.amount;
                }

                LocalDate date;
                try { date = LocalDate.parse(t.transactionDate); } catch (Exception e) { continue; }
                if (base != null) {
                    if (YearMonth.from(date).equals(base)) {
                        int idx = Math.min(weeks - 1, (date.getDayOfMonth() - 1) / 7);
                        if (inc) wInc[idx] += t.amount; else wExp[idx] += t.amount;
                    }
                } else {
                    double[] arr = months.computeIfAbsent(YearMonth.from(date), k -> new double[2]);
                    arr[inc ? 0 : 1] += t.amount;
                }
            }
        }

        s.breakdown = new ArrayList<>(cats.values());
        Collections.sort(s.breakdown, (a, b) -> Double.compare(b.total, a.total));

        if (base != null) {
            s.bucketLabels = new String[weeks];
            for (int i = 0; i < weeks; i++) s.bucketLabels[i] = "M" + (i + 1);
            s.bucketIncome = wInc;
            s.bucketExpense = wExp;
            s.defaultBucket = period == StatsViewModel.Period.THIS_MONTH
                    ? Math.min(weeks - 1, (LocalDate.now().getDayOfMonth() - 1) / 7)
                    : weeks - 1;
        } else {
            List<Map.Entry<YearMonth, double[]>> entries = new ArrayList<>(months.entrySet());
            int from = Math.max(0, entries.size() - 6);
            int n = entries.size() - from;
            s.bucketLabels = new String[n];
            s.bucketIncome = new double[n];
            s.bucketExpense = new double[n];
            for (int i = 0; i < n; i++) {
                Map.Entry<YearMonth, double[]> e = entries.get(from + i);
                s.bucketLabels[i] = Formatters.monthShort(e.getKey());
                s.bucketIncome[i] = e.getValue()[0];
                s.bucketExpense[i] = e.getValue()[1];
            }
            s.defaultBucket = n - 1;
        }

        int idx = s.defaultBucket;
        if (idx >= 1 && s.bucketExpense[idx - 1] > 0) {
            double prev = s.bucketExpense[idx - 1];
            double pct = (s.bucketExpense[idx] - prev) / prev * 100.0;
            long rounded = Math.round(Math.abs(pct));
            s.trendGood = pct <= 0;
            s.trendText = String.format(new Locale("id", "ID"), "%s %d%% vs %s",
                    pct <= 0 ? "\u2193" : "\u2191", rounded, s.bucketLabels[idx - 1]);
        }
        return s;
    }
}
