package com.ncp.finora.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "receipt_items",
        foreignKeys = @ForeignKey(entity = ReceiptEntity.class, parentColumns = "id_receipt",
                childColumns = "receipt_id", onDelete = ForeignKey.CASCADE),
        indices = @Index("receipt_id"))
public class ReceiptItemEntity implements java.io.Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_receipt_items")
    public int idReceiptItems;

    @ColumnInfo(name = "receipt_id")
    public int receiptId;

    @NonNull
    @ColumnInfo(name = "item_name")
    public String itemName = "";

    public double quantity = 1;

    @ColumnInfo(name = "unit_price")
    public double unitPrice;
}
