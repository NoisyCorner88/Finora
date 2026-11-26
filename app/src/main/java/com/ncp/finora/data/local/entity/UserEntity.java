package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users",
        indices = {@Index(value = "email", unique = true), @Index(value = "username", unique = true)})
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_user")
    public int idUser;

    @NonNull
    public String name = "";
    @NonNull
    public String email = "";
    @NonNull
    public String username = "";

    @NonNull
    @ColumnInfo(name = "password_hash")
    public String passwordHash = "";

    /** Unix epoch millis. */
    @ColumnInfo(name = "created_at")
    public long createdAt;
}
