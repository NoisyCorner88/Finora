package com.ncp.finora.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ncp.finora.data.local.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    long insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<NotificationEntity>> observeAll(int userId);

    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    void markAllAsRead(int userId);

    @Query("DELETE FROM notifications WHERE user_id = :userId")
    void clearAll(int userId);
}
