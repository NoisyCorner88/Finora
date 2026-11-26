package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "payment_methods",
        foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "id_user",
                childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = @Index("user_id"))
public class PaymentMethodEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_payment_method")
    public int idPaymentMethod;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    public String name = "";
    /** CASH, BANK, EWALLET, CARD. */
    @NonNull
    public String type = "CASH";

    @ColumnInfo(name = "initial_balance")
    public double initialBalance;

    @NonNull
    @ColumnInfo(name = "account_number", defaultValue = "821")
    public String accountNumber = "821";

    @NonNull
    @ColumnInfo(name = "color", defaultValue = "#1555E8")
    public String color = "#1555E8";

    /** 1 = aktif, 0 = diarsipkan. */
    @ColumnInfo(name = "is_active")
    public int isActive = 1;

    public PaymentMethodEntity() { }

    @Ignore
    public PaymentMethodEntity(int userId, @NonNull String name, @NonNull String type, double initialBalance) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.initialBalance = initialBalance;
    }

    @Ignore
    public PaymentMethodEntity(int userId, @NonNull String name, @NonNull String type, double initialBalance, @NonNull String accountNumber, @NonNull String color) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.initialBalance = initialBalance;
        this.accountNumber = accountNumber;
        this.color = color;
    }
}
