package com.ncp.finora.data.repository;

import androidx.lifecycle.LiveData;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.local.entity.NotificationEntity;
import com.ncp.finora.util.AppExecutors;

import java.util.List;

public class NotificationRepository {
    private final AppDatabase db;

    public NotificationRepository(AppDatabase db) {
        this.db = db;
    }

    public LiveData<List<NotificationEntity>> observeAll(int userId) {
        return db.notificationDao().observeAll(userId);
    }

    public void add(NotificationEntity notif) {
        AppExecutors.io(() -> db.notificationDao().insert(notif));
    }

    public void markAllRead(int userId) {
        AppExecutors.io(() -> db.notificationDao().markAllAsRead(userId));
    }

    public void clearAll(int userId) {
        AppExecutors.io(() -> db.notificationDao().clearAll(userId));
    }
}
