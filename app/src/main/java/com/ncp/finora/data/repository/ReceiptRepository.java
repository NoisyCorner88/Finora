package com.ncp.finora.data.repository;

import androidx.lifecycle.LiveData;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.local.entity.NotificationEntity;
import com.ncp.finora.data.local.entity.ReceiptEntity;
import com.ncp.finora.data.local.entity.ReceiptItemEntity;
import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.util.AppExecutors;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Formatters;

import java.util.List;

public class ReceiptRepository {
    private final AppDatabase db;

    public ReceiptRepository(AppDatabase db) {
        this.db = db;
    }

    public LiveData<Integer> scanCount(int userId) {
        return db.receiptDao().observeScanCount(userId);
    }

    public void saveReceiptAndTransaction(ReceiptEntity receipt, List<ReceiptItemEntity> items,
                                          TransactionEntity transaction, Callback<Long> callback) {
        AppExecutors.io(() -> {
            try {
                db.runInTransaction(() -> {
                    long receiptId = db.receiptDao().insertReceipt(receipt);
                    receipt.idReceipt = (int) receiptId;
                    if (items != null && !items.isEmpty()) {
                        for (ReceiptItemEntity item : items) {
                            item.receiptId = (int) receiptId;
                        }
                        db.receiptDao().insertReceiptItems(items);
                    }
                    transaction.receiptId = (int) receiptId;
                    long txId = db.transactionDao().insert(transaction);
                    transaction.idTransaction = (int) txId;

                    // Buat notifikasi otomatis
                    NotificationEntity notif = new NotificationEntity();
                    notif.userId = transaction.userId;
                    notif.transactionId = (int) txId;
                    notif.title = "Struk Terverifikasi • " + receipt.merchantName;
                    notif.message = "Pengeluaran " + Formatters.currency(transaction.amount) + " berhasil dicatat via scan struk.";
                    notif.isRead = 0;
                    notif.createdAt = System.currentTimeMillis();
                    db.notificationDao().insert(notif);
                });
                AppExecutors.main(() -> callback.onSuccess((long) transaction.idTransaction));
            } catch (Exception e) {
                AppExecutors.main(() -> callback.onError("Gagal menyimpan data struk: " + e.getMessage()));
            }
        });
    }
}
