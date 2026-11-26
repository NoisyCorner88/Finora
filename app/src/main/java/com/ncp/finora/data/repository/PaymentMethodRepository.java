package com.ncp.finora.data.repository;

import androidx.lifecycle.LiveData;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.model.PaymentMethodBalance;
import com.ncp.finora.util.AppExecutors;

import java.util.List;

public class PaymentMethodRepository {
    private final AppDatabase db;

    public PaymentMethodRepository(AppDatabase db) {
        this.db = db;
    }

    public LiveData<List<PaymentMethodBalance>> balances(int userId) {
        return db.paymentMethodDao().observeBalances(userId);
    }

    public LiveData<List<PaymentMethodEntity>> active(int userId) {
        return db.paymentMethodDao().observeActive(userId);
    }

    public void add(PaymentMethodEntity method) {
        AppExecutors.io(() -> db.paymentMethodDao().insert(method));
    }

    public void update(int id, String name, String color, double balance, String accNum, Runnable onDone) {
        AppExecutors.io(() -> {
            db.paymentMethodDao().updateDetails(id, name, color, balance, accNum);
            if (onDone != null) AppExecutors.main(onDone);
        });
    }

    public void delete(int id, Runnable onDone) {
        AppExecutors.io(() -> {
            db.paymentMethodDao().softDelete(id);
            if (onDone != null) AppExecutors.main(onDone);
        });
    }
}
