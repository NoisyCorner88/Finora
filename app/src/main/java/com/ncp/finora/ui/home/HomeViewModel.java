package com.ncp.finora.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.model.PaymentMethodBalance;
import com.ncp.finora.data.repository.PaymentMethodRepository;
import com.ncp.finora.data.repository.TransactionRepository;
import com.ncp.finora.data.session.SessionManager;
import com.ncp.finora.ui.model.TransactionUi;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private static final int RECENT_LIMIT = 5;

    private final int userId;
    private final PaymentMethodRepository paymentRepo;

    private final String userName;
    private final LiveData<List<PaymentMethodBalance>> cards;
    private final LiveData<Double> totalBalance;
    private final LiveData<List<TransactionUi>> recent;
    private final MediatorLiveData<Double> monthNet = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> balanceVisible = new MutableLiveData<>(true);

    public HomeViewModel(int userId, SessionManager session,
                         PaymentMethodRepository paymentRepo, TransactionRepository txRepo) {
        this.userId = userId;
        this.paymentRepo = paymentRepo;
        this.userName = session.getName();

        cards = paymentRepo.balances(userId);
        totalBalance = Transformations.map(cards, list -> {
            double sum = 0;
            if (list != null) for (PaymentMethodBalance b : list) sum += b.balance;
            return sum;
        });
        recent = Transformations.map(txRepo.recent(userId, RECENT_LIMIT), TransactionUi::fromList);

        YearMonth now = YearMonth.now();
        String from = Formatters.monthStart(now);
        String to = Formatters.nextMonthStart(now);
        LiveData<Double> income = txRepo.total(userId, Constants.INCOME, from, to);
        LiveData<Double> expense = txRepo.total(userId, Constants.EXPENSE, from, to);
        Runnable update = () -> {
            Double i = income.getValue();
            Double e = expense.getValue();
            if (i != null && e != null) monthNet.setValue(i - e);
        };
        monthNet.addSource(income, v -> update.run());
        monthNet.addSource(expense, v -> update.run());
    }

    public String getUserName() { return userName; }

    public String getGreeting() {
        return Formatters.greeting(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    public LiveData<List<PaymentMethodBalance>> getCards() { return cards; }
    public LiveData<Double> getTotalBalance() { return totalBalance; }
    public LiveData<List<TransactionUi>> getRecent() { return recent; }
    /** Selisih pemasukan - pengeluaran bulan berjalan. */
    public LiveData<Double> getMonthNet() { return monthNet; }
    public LiveData<Boolean> getBalanceVisible() { return balanceVisible; }

    public void toggleBalanceVisibility() {
        balanceVisible.setValue(!Boolean.TRUE.equals(balanceVisible.getValue()));
    }

    public void addPaymentMethod(String name, String type, double initialBalance) {
        if (name == null || name.trim().isEmpty()) return;
        paymentRepo.add(new PaymentMethodEntity(userId, name.trim(), type, initialBalance));
    }
}
