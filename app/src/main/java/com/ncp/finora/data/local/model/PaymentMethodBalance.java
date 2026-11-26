package com.ncp.finora.data.local.model;

import androidx.room.ColumnInfo;

/** Sumber dana beserta saldo berjalan (saldo awal + pemasukan - pengeluaran). */
public class PaymentMethodBalance {
    public int id;
    public String name;
    public String type;
    public double balance;
    @ColumnInfo(name = "initial_balance")
    public double initialBalance;
    @ColumnInfo(name = "account_number")
    public String accountNumber = "---";
    public String color = "#1555E8";
}
