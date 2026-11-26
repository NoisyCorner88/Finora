package com.ncp.finora.ui.stats;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.model.TransactionDetail;
import com.ncp.finora.data.repository.TransactionRepository;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.time.YearMonth;
import java.util.List;

public class StatsViewModel extends ViewModel {
    public enum Period { THIS_MONTH, LAST_MONTH, ALL }

    private final MutableLiveData<Period> period = new MutableLiveData<>(Period.THIS_MONTH);
    private final MutableLiveData<String> type = new MutableLiveData<>(Constants.EXPENSE);
    private final MediatorLiveData<StatsData> data = new MediatorLiveData<>();
    private final LiveData<String> periodLabel;

    public StatsViewModel(int userId, TransactionRepository repo) {
        // Periode ikut dibawa bersama datanya agar tidak pernah tercampur saat berganti filter.
        LiveData<Pair<Period, List<TransactionDetail>>> details = Transformations.switchMap(period, p -> {
            String[] r = range(p);
            return Transformations.map(repo.detailRange(userId, r[0], r[1]), l -> new Pair<Period, List<TransactionDetail>>(p, l));
        });

        Runnable recompute = () -> {
            Pair<Period, List<TransactionDetail>> d = details.getValue();
            String t = type.getValue();
            if (d == null || d.first == null || t == null) return;
            data.setValue(StatsData.compute(d.first, d.second, t));
        };
        data.addSource(details, v -> recompute.run());
        data.addSource(type, v -> recompute.run());

        periodLabel = Transformations.map(period, p -> {
            if (p == Period.THIS_MONTH) return Formatters.monthLabel(YearMonth.now());
            if (p == Period.LAST_MONTH) return Formatters.monthLabel(YearMonth.now().minusMonths(1));
            return "Semua waktu";
        });
    }

    private static String[] range(Period p) {
        if (p == Period.ALL) return new String[]{"0000-01-01", "9999-12-31"};
        YearMonth m = p == Period.THIS_MONTH ? YearMonth.now() : YearMonth.now().minusMonths(1);
        return new String[]{Formatters.monthStart(m), Formatters.nextMonthStart(m)};
    }

    public LiveData<Period> getPeriod() { return period; }
    public LiveData<String> getType() { return type; }
    public LiveData<StatsData> getData() { return data; }
    public LiveData<String> getPeriodLabel() { return periodLabel; }

    public void setPeriod(Period p) {
        if (p != period.getValue()) period.setValue(p);
    }

    public void setType(String newType) {
        if (!newType.equals(type.getValue())) type.setValue(newType);
    }
}
