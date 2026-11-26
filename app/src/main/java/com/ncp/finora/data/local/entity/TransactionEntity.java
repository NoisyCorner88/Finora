package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions",
        foreignKeys = {
                @ForeignKey(entity = UserEntity.class, parentColumns = "id_user",
                        childColumns = "user_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = PaymentMethodEntity.class, parentColumns = "id_payment_method",
                        childColumns = "payment_method_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = CategoryEntity.class, parentColumns = "id_category",
                        childColumns = "category_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = ReceiptEntity.class, parentColumns = "id_receipt",
                        childColumns = "receipt_id", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("user_id"), @Index("payment_method_id"), @Index("category_id"), @Index("receipt_id")})
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_transaction")
    public int idTransaction;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "payment_method_id")
    public int paymentMethodId;

    @ColumnInfo(name = "category_id")
    public int categoryId;

    @Nullable
    @ColumnInfo(name = "receipt_id")
    public Integer receiptId;

    /** Nominal dalam Rupiah (selalu positif; arah ditentukan oleh {@link #type}). */
    public double amount;

    /** INCOME atau EXPENSE. */
    @NonNull
    public String type = "EXPENSE";

    @NonNull
    public String description = "";

    /** Format YYYY-MM-DD. */
    @NonNull
    @ColumnInfo(name = "transaction_date")
    public String transactionDate = "";

    /** MANUAL, OCR_SCAN, VOICE_INPUT. */
    @NonNull
    @ColumnInfo(name = "input_method")
    public String inputMethod = "MANUAL";

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
