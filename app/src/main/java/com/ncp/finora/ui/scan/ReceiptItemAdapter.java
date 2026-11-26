package com.ncp.finora.ui.scan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.ReceiptItemEntity;
import com.ncp.finora.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ViewHolder> {
    public interface OnItemActionListener {
        void onDelete(int position);
        void onEdit(int position, ReceiptItemEntity item);
    }

    private final List<ReceiptItemEntity> items = new ArrayList<>();
    private final OnItemActionListener listener;

    public ReceiptItemAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ReceiptItemEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<ReceiptItemEntity> getItems() {
        return items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReceiptItemEntity item = items.get(position);
        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.tvName.setText(item.itemName);
        double total = item.unitPrice * (item.quantity > 0 ? item.quantity : 1);
        holder.tvQtyPrice.setText(String.format(Locale.getDefault(), "%.0f pcs × %s",
                item.quantity > 0 ? item.quantity : 1, Formatters.currency(item.unitPrice)));
        holder.tvTotalPrice.setText(Formatters.currency(total));

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(holder.getAdapterPosition());
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(holder.getAdapterPosition(), item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIndex, tvName, tvQtyPrice, tvTotalPrice;
        final ImageView btnDelete;

        ViewHolder(View v) {
            super(v);
            tvIndex = v.findViewById(R.id.tvItemIndex);
            tvName = v.findViewById(R.id.tvItemName);
            tvQtyPrice = v.findViewById(R.id.tvItemQtyPrice);
            tvTotalPrice = v.findViewById(R.id.tvItemTotalPrice);
            btnDelete = v.findViewById(R.id.btnDeleteItem);
        }
    }
}
