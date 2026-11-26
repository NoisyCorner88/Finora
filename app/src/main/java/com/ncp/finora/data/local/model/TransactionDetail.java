package com.ncp.finora.data.local.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.ncp.finora.data.local.entity.TransactionEntity;

/** Hasil JOIN transaksi + kategori + sumber dana. */
public class TransactionDetail {
    @Embedded
    public TransactionEntity transaction;

    @ColumnInfo(name = "category_name")
    public String categoryName;
    @ColumnInfo(name = "category_icon")
    public String categoryIcon;
    @ColumnInfo(name = "category_color")
    public String categoryColor;
    @ColumnInfo(name = "payment_name")
    public String paymentName;
}
