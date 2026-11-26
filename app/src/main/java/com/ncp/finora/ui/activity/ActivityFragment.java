package com.ncp.finora.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.ui.home.TransactionAdapter;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.UiUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ActivityFragment extends Fragment {
    private static final Locale ID = new Locale("id", "ID");
    private static final DateTimeFormatter FULL_DAY = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID);

    private ActivityViewModel viewModel;
    private CalendarAdapter calendarAdapter;
    private RecyclerView rv, rvCalendar;
    private YearMonth shownMonth = YearMonth.now();
    private LocalDate shownDay;
    private ActivityViewModel.State lastState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, FinoraApp.factory(requireContext())).get(ActivityViewModel.class);

        TextView chipAll = view.findViewById(R.id.chipAll);
        TextView chipIncome = view.findViewById(R.id.chipIncome);
        TextView chipExpense = view.findViewById(R.id.chipExpense);
        TextView tvIncome = view.findViewById(R.id.tvActivityIncome);
        TextView tvExpense = view.findViewById(R.id.tvActivityExpense);
        TextView tvAvatar = view.findViewById(R.id.tvAvatarActivity);
        TextView tvMonthTop = view.findViewById(R.id.tvActivityMonth);
        TextView tvCalendarMonth = view.findViewById(R.id.tvCalendarMonth);
        TextView tvSelectedDay = view.findViewById(R.id.tvSelectedDay);
        View empty = view.findViewById(R.id.tvEmptyActivity);

        tvAvatar.setText(UiUtils.initial(FinoraApp.container(requireContext()).session.getName()));

        rv = view.findViewById(R.id.rvActivity);
        TransactionAdapter adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        rvCalendar.setNestedScrollingEnabled(false);
        calendarAdapter = new CalendarAdapter(viewModel::toggleDay);
        rvCalendar.setAdapter(calendarAdapter);

        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> viewModel.previousMonth());
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> viewModel.nextMonth());
        tvMonthTop.setOnClickListener(v -> showMonthPicker());
        tvCalendarMonth.setOnClickListener(v -> showMonthPicker());
        tvSelectedDay.setOnClickListener(v -> viewModel.clearDay());

        chipAll.setOnClickListener(v -> viewModel.setFilter(Constants.ALL));
        chipIncome.setOnClickListener(v -> viewModel.setFilter(Constants.INCOME));
        chipExpense.setOnClickListener(v -> viewModel.setFilter(Constants.EXPENSE));

        viewModel.getFilter().observe(getViewLifecycleOwner(), f -> {
            UiUtils.setChipSelected(chipAll, Constants.ALL.equals(f));
            UiUtils.setChipSelected(chipIncome, Constants.INCOME.equals(f));
            UiUtils.setChipSelected(chipExpense, Constants.EXPENSE.equals(f));
        });

        viewModel.getMonth().observe(getViewLifecycleOwner(), m -> {
            shownMonth = m;
            String label = capitalize(Formatters.monthLabel(m));
            tvMonthTop.setText(label + " \u25BE");
            tvCalendarMonth.setText(label);
            refreshCalendar();
        });

        viewModel.getSelectedDay().observe(getViewLifecycleOwner(), d -> {
            shownDay = d;
            if (d == null) {
                tvSelectedDay.setVisibility(View.GONE);
            } else {
                tvSelectedDay.setVisibility(View.VISIBLE);
                tvSelectedDay.setText(capitalize(d.format(FULL_DAY)) + "  \u2022  ketuk untuk tampilkan semua");
            }
            refreshCalendar();
        });

        viewModel.getState().observe(getViewLifecycleOwner(), st -> {
            if (st == null) return;
            lastState = st;
            adapter.submitList(st.list);
            tvIncome.setText(Formatters.currency(st.income));
            tvExpense.setText(Formatters.currency(st.expense));
            boolean isEmpty = st.list.isEmpty();
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            refreshCalendar();
        });
    }

    private void refreshCalendar() {
        if (calendarAdapter == null) return;
        calendarAdapter.submit(shownMonth, lastState == null ? null : lastState.markedDays, shownDay);
    }

    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase(ID) + s.substring(1);
    }

    /** Dialog pilih bulan + tahun. */
    private void showMonthPicker() {
        YearMonth cur = shownMonth;
        int thisYear = YearMonth.now().getYear();

        NumberPicker monthPicker = new NumberPicker(requireContext());
        String[] names = new String[12];
        for (int i = 0; i < 12; i++) names[i] = capitalize(YearMonth.of(2000, i + 1).format(DateTimeFormatter.ofPattern("MMMM", ID)));
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(names);
        monthPicker.setValue(cur.getMonthValue());
        monthPicker.setWrapSelectorWheel(true);

        NumberPicker yearPicker = new NumberPicker(requireContext());
        yearPicker.setMinValue(thisYear - 10);
        yearPicker.setMaxValue(thisYear + 2);
        yearPicker.setValue(Math.max(thisYear - 10, Math.min(thisYear + 2, cur.getYear())));
        yearPicker.setWrapSelectorWheel(false);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER);
        int pad = UiUtils.dp(requireContext(), 16);
        row.setPadding(pad, pad, pad, 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(monthPicker, lp);
        row.addView(yearPicker, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Pilih bulan")
                .setView(row)
                .setPositiveButton("Pilih", (d, w) ->
                        viewModel.setMonth(YearMonth.of(yearPicker.getValue(), monthPicker.getValue())))
                .setNeutralButton("Bulan ini", (d, w) -> viewModel.setMonth(YearMonth.now()))
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rv != null) rv.setAdapter(null);
        if (rvCalendar != null) rvCalendar.setAdapter(null);
        calendarAdapter = null;
    }
}
