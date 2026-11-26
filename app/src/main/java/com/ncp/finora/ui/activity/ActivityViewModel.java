package com.ncp.finora.ui.activity;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.model.TransactionDetail;
import com.ncp.finora.data.repository.TransactionRepository;
import com.ncp.finora.ui.model.TransactionUi;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityViewModel extends ViewModel {

    /** Semua yang dibutuhkan layar Aktivitas, dihitung dari satu daftar transaksi bulan terpilih. */
    public static class State {
        public final List<TransactionUi> list;
        public final double income;
        public final double expense;
        /** Tanggal (1..31) yang punya transaksi, untuk titik penanda di kalender. */
        public final Set<Integer> markedDays;

        State(List<TransactionUi> list, double income, double expense, Set<Integer> markedDays) {
            this.list = list;
            this.income = income;
            this.expense = expense;
            this.markedDays = markedDays;
        }
    }

    private final MutableLiveData<YearMonth> month = new MutableLiveData<>(YearMonth.now());
    private final MutableLiveData<LocalDate> selectedDay = new MutableLiveData<>(null);
    private final MutableLiveData<String> filter = new MutableLiveData<>(Constants.ALL);
    private final MediatorLiveData<State> state = new MediatorLiveData<>();

    public ActivityViewModel(int userId, TransactionRepository repo) {
        LiveData<List<TransactionDetail>> monthData = Transformations.switchMap(month, m ->
                repo.detailRange(userId, Formatters.monthStart(m), Formatters.nextMonthStart(m)));

        Runnable recompute = () -> {
            List<TransactionDetail> all = monthData.getValue();
            if (all == null) return;
            LocalDate day = selectedDay.getValue();
            String f = filter.getValue() == null ? Constants.ALL : filter.getValue();

            Set<Integer> marked = new HashSet<>();
            double inc = 0, exp = 0;
            List<TransactionDetail> shown = new ArrayList<>();
            for (TransactionDetail d : all) {
                TransactionEntity t = d.transaction;
                try { marked.add(LocalDate.parse(t.transactionDate).getDayOfMonth()); } catch (Exception ignored) { }
                if (day != null && !day.toString().equals(t.transactionDate)) continue;
                if (Constants.INCOME.equals(t.type)) inc += t.amount; else exp += t.amount;
                if (Constants.ALL.equals(f) || f.equals(t.type)) shown.add(d);
            }
            state.setValue(new State(TransactionUi.fromList(shown), inc, exp, marked));
        };
        state.addSource(monthData, v -> recompute.run());
        state.addSource(selectedDay, v -> recompute.run());
        state.addSource(filter, v -> recompute.run());
    }

    public LiveData<YearMonth> getMonth() { return month; }
    public LiveData<LocalDate> getSelectedDay() { return selectedDay; }
    public LiveData<String> getFilter() { return filter; }
    public LiveData<State> getState() { return state; }

    public void setFilter(String type) {
        if (!type.equals(filter.getValue())) filter.setValue(type);
    }

    public void setMonth(YearMonth m) {
        if (m.equals(month.getValue())) return;
        selectedDay.setValue(null);
        month.setValue(m);
    }

    public void previousMonth() {
        YearMonth m = month.getValue();
        if (m != null) setMonth(m.minusMonths(1));
    }

    public void nextMonth() {
        YearMonth m = month.getValue();
        if (m != null) setMonth(m.plusMonths(1));
    }

    /** Ketuk tanggal yang sama sekali lagi untuk membatalkan pilihan. */
    public void toggleDay(LocalDate day) {
        selectedDay.setValue(day.equals(selectedDay.getValue()) ? null : day);
    }

    public void clearDay() { selectedDay.setValue(null); }
}
