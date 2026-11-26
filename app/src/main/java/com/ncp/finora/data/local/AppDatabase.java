package com.ncp.finora.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ncp.finora.data.local.dao.CategoryDao;
import com.ncp.finora.data.local.dao.NotificationDao;
import com.ncp.finora.data.local.dao.PaymentMethodDao;
import com.ncp.finora.data.local.dao.ReceiptDao;
import com.ncp.finora.data.local.dao.TransactionDao;
import com.ncp.finora.data.local.dao.UserDao;
import com.ncp.finora.data.local.entity.CategoryEntity;
import com.ncp.finora.data.local.entity.NotificationEntity;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.entity.ReceiptEntity;
import com.ncp.finora.data.local.entity.ReceiptItemEntity;
import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.entity.UserEntity;

@Database(entities = {
        UserEntity.class, CategoryEntity.class, PaymentMethodEntity.class,
        ReceiptEntity.class, ReceiptItemEntity.class, TransactionEntity.class,
        NotificationEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract PaymentMethodDao paymentMethodDao();
    public abstract TransactionDao transactionDao();
    public abstract ReceiptDao receiptDao();
    public abstract NotificationDao notificationDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "finora.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
