package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories",
        foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "id_user",
                childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = @Index("user_id"))
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_category")
    public int idCategory;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    public String name = "";
    /** EXPENSE atau INCOME. */
    @NonNull
    public String type = "";
    /** Simbol/ikon kategori (saat ini emoji, bisa diganti nama resource ikon). */
    @NonNull
    public String icon = "";
    /** Hex warna, mis. #FF5722. */
    @NonNull
    public String color = "#64748B";

    public CategoryEntity() { }

    @Ignore
    public CategoryEntity(int userId, @NonNull String name, @NonNull String type,
                          @NonNull String icon, @NonNull String color) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
    }
}
