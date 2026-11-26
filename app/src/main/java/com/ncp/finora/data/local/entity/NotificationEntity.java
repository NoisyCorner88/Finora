package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "id_user",
                        childColumns = "user_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = TransactionEntity.class, parentColumns = "id_transaction",
                        childColumns = "transaction_id", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("user_id"), @Index("transaction_id")})
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_notifications")
    public int idNotifications;

    @ColumnInfo(name = "user_id")
    public int userId;

    @Nullable
    @ColumnInfo(name = "transaction_id")
    public Integer transactionId;

    @NonNull
    public String title = "";
    @NonNull
    public String message = "";

    @ColumnInfo(name = "is_read")
    public int isRead = 0;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
