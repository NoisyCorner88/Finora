package com.ncp.finora.ui.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<NotificationEntity> list = new ArrayList<>();

    public void submitList(List<NotificationEntity> items) {
        list.clear();
        if (items != null) list.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationEntity item = list.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvMessage.setText(item.message);
        holder.viewDot.setVisibility(item.isRead == 0 ? View.VISIBLE : View.GONE);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm", Locale.getDefault());
        holder.tvTime.setText(timeFormat.format(new Date(item.createdAt > 0 ? item.createdAt : System.currentTimeMillis())));

        if (item.title.toLowerCase(Locale.ROOT).contains("masuk") || item.title.toLowerCase(Locale.ROOT).contains("gaji")) {
            holder.tvAmount.setTextColor(0xFF00A879);
            holder.ivIcon.setImageResource(R.drawable.ic_trending_up);
        } else {
            holder.tvAmount.setTextColor(0xFFFF253B);
            holder.ivIcon.setImageResource(R.drawable.ic_receipt_check);
        }
        holder.tvAmount.setText(item.title.contains("•") ? item.title.substring(item.title.lastIndexOf("•") + 1).trim() : "Tercatat");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle, tvMessage, tvAmount, tvTime;
        final View viewDot;

        ViewHolder(View v) {
            super(v);
            ivIcon = v.findViewById(R.id.ivNotifIcon);
            tvTitle = v.findViewById(R.id.tvNotifTitle);
            tvMessage = v.findViewById(R.id.tvNotifMessage);
            tvAmount = v.findViewById(R.id.tvNotifAmount);
            tvTime = v.findViewById(R.id.tvNotifTime);
            viewDot = v.findViewById(R.id.viewUnreadDot);
        }
    }
}
