package com.ncp.finora.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.R;
import com.ncp.finora.ui.model.TransactionUi;

/** Adapter transaksi; dipakai di Beranda dan Aktivitas. */
public class TransactionAdapter extends ListAdapter<TransactionUi, TransactionAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<TransactionUi> DIFF = new DiffUtil.ItemCallback<TransactionUi>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionUi a, @NonNull TransactionUi b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionUi a, @NonNull TransactionUi b) {
            return a.equals(b);
        }
    };

    public TransactionAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionUi item = getItem(position);
        holder.title.setText(item.title);
        holder.subtitle.setText(item.subtitle);
        holder.amount.setText(item.amount);
        holder.symbol.setText(item.symbol);
        holder.amount.setTextColor(Color.parseColor(item.income ? "#00A879" : "#FF253B"));
        holder.symbol.setBackgroundResource(item.income
                ? R.drawable.bg_icon_income : R.drawable.bg_icon_expense);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title, subtitle, amount, symbol;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTransactionTitle);
            subtitle = itemView.findViewById(R.id.tvTransactionSubtitle);
            amount = itemView.findViewById(R.id.tvTransactionAmount);
            symbol = itemView.findViewById(R.id.tvTransactionSymbol);
        }
    }
}
