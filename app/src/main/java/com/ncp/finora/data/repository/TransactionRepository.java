package com.ncp.finora.data.repository;

import androidx.lifecycle.LiveData;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.model.CategoryTotal;
import com.ncp.finora.data.local.model.TransactionDetail;
import com.ncp.finora.util.AppExecutors;
import com.ncp.finora.util.Callback;

import java.util.List;

public class TransactionRepository {
    private final AppDatabase db;

    public TransactionRepository(AppDatabase db) {
        this.db = db;
    }

    public LiveData<List<TransactionDetail>> recent(int userId, int limit) {
        return db.transactionDao().observeRecent(userId, limit);
    }

    public LiveData<List<TransactionDetail>> byType(int userId, String type) {
        return db.transactionDao().observeByType(userId, type);
    }

    public LiveData<List<TransactionDetail>> detailRange(int userId, String from, String to) {
        return db.transactionDao().observeDetailRange(userId, from, to);
    }

    public LiveData<Double> total(int userId, String type, String from, String to) {
        return db.transactionDao().observeTotal(userId, type, from, to);
    }

    public LiveData<List<CategoryTotal>> categoryTotals(int userId, String type, String from, String to) {
        return db.transactionDao().observeCategoryTotals(userId, type, from, to);
    }

    public LiveData<Integer> count(int userId) {
        return db.transactionDao().observeCount(userId);
    }

    public void add(TransactionEntity transaction, Callback<Long> callback) {
        AppExecutors.io(() -> {
            try {
                long id = db.transactionDao().insert(transaction);
                AppExecutors.main(() -> callback.onSuccess(id));
            } catch (Exception e) {
                AppExecutors.main(() -> callback.onError("Gagal menyimpan transaksi"));
            }
        });
    }
}
