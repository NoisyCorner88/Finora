package com.ncp.finora.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.R;
import com.ncp.finora.data.local.model.PaymentMethodBalance;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private final List<PaymentMethodBalance> items = new ArrayList<>();
    private boolean masked = false;

    public void submit(List<PaymentMethodBalance> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setMasked(boolean masked) {
        if (this.masked != masked) {
            this.masked = masked;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PaymentMethodBalance item = items.get(position);
        h.type.setText(typeLabel(item.type));
        h.balance.setText(masked ? Formatters.maskedRupiah() : Formatters.rupiah(item.balance));
        if (h.bankName != null) h.bankName.setText(item.name);
        if (h.cardNumber != null) {
            h.cardNumber.setText(Formatters.maskedCardNumber(item.accountNumber));
        }
        if (h.cardLayout != null && item.color != null && item.color.startsWith("#")) {
            try {
                int c = android.graphics.Color.parseColor(item.color);
                android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[]{c, darkenColor(c)}
                );
                gd.setCornerRadius(16 * h.itemView.getResources().getDisplayMetrics().density);
                h.cardLayout.setBackground(gd);
            } catch (Exception ignored) { }
        }
    }

    private static int darkenColor(int color) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f;
        return android.graphics.Color.HSVToColor(hsv);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String typeLabel(String type) {
        if (Constants.PM_BANK.equals(type)) return "BANK";
        if (Constants.PM_EWALLET.equals(type)) return "E-WALLET";
        if (Constants.PM_CARD.equals(type)) return "DEBIT";
        return "TUNAI";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View cardLayout;
        final TextView type, balance, bankName, cardNumber;

        ViewHolder(@NonNull View v) {
            super(v);
            cardLayout = v.findViewById(R.id.cardLayout);
            type = v.findViewById(R.id.tvCardType);
            balance = v.findViewById(R.id.tvCardBalance);
            bankName = v.findViewById(R.id.tvBankName);
            cardNumber = v.findViewById(R.id.tvCardNumberMasked);
        }
    }
}
