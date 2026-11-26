package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipts",
        foreignKeys = @ForeignKey(entity = UserEntity.class, parentColumns = "id_user",
                childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        indices = @Index("user_id"))
public class ReceiptEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_receipt")
    public int idReceipt;

    @ColumnInfo(name = "user_id")
    public int userId;

    @NonNull
    @ColumnInfo(name = "image_path")
    public String imagePath = "";

    @NonNull
    @ColumnInfo(name = "merchant_name")
    public String merchantName = "";

    @ColumnInfo(name = "receipt_total")
    public double receiptTotal;

    @NonNull
    @ColumnInfo(name = "ocr_text")
    public String ocrText = "";

    /** PARSED, MANUAL_EDIT, FAILED. */
    @NonNull
    @ColumnInfo(name = "ocr_status")
    public String ocrStatus = "PARSED";

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
