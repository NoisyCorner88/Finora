package com.ncp.finora.ui.model;

import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.model.TransactionDetail;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Model siap-tampil untuk satu baris transaksi. */
public class TransactionUi {
    public final int id;
    public final String title;
    public final String subtitle;
    public final String amount;
    public final boolean income;
    public final String symbol;
    public final double rawAmount;

    public TransactionUi(int id, String title, String subtitle, String amount, boolean income, String symbol) {
        this(id, title, subtitle, amount, income, symbol, 0);
    }

    public TransactionUi(int id, String title, String subtitle, String amount, boolean income, String symbol, double rawAmount) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.income = income;
        this.symbol = symbol;
        this.rawAmount = rawAmount;
    }

    public static TransactionUi from(TransactionDetail d) {
        TransactionEntity t = d.transaction;
        boolean income = Constants.INCOME.equals(t.type);
        String title = t.description.trim().isEmpty() ? d.categoryName : t.description;
        String subtitle = d.paymentName + " • " + Formatters.relativeDate(t.transactionDate);
        return new TransactionUi(t.idTransaction, title, subtitle,
                Formatters.signedRupiah(t.amount, income), income, d.categoryIcon, t.amount);
    }

    public static List<TransactionUi> fromList(List<TransactionDetail> list) {
        List<TransactionUi> result = new ArrayList<>();
        if (list != null) for (TransactionDetail d : list) result.add(from(d));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionUi)) return false;
        TransactionUi that = (TransactionUi) o;
        return id == that.id && income == that.income && title.equals(that.title)
                && subtitle.equals(that.subtitle) && amount.equals(that.amount)
                && Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, subtitle, amount, income, symbol);
    }
}
