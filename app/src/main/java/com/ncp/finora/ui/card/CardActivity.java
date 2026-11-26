package com.ncp.finora.ui.card;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.model.PaymentMethodBalance;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CardActivity extends AppCompatActivity {
    private List<PaymentMethodBalance> allBalances = new ArrayList<>();
    private List<PaymentMethodBalance> filteredBalances = new ArrayList<>();
    private int selectedId = -1;
    private String currentFilter = "ALL";

    private TextView tabAll, tabDebit, tabEWallet, tabCash;
    private RelativeLayout cardPreviewBg;
    private TextView tvCardTypeBadge, tvCardNumber, tvCardBalance, tvBankName;
    private TextView tvInfoAccountName, tvInfoCardNumber, tvInfoBalance;
    private RecyclerView rvCards;
    private WalletAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        initViews();
        setupData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddCard).setOnClickListener(v -> showAddCardDialog());

        tabAll = findViewById(R.id.tabAll);
        tabDebit = findViewById(R.id.tabDebit);
        tabEWallet = findViewById(R.id.tabEWallet);
        tabCash = findViewById(R.id.tabCash);

        tabAll.setOnClickListener(v -> setFilter("ALL"));
        tabDebit.setOnClickListener(v -> setFilter(Constants.PM_BANK));
        tabEWallet.setOnClickListener(v -> setFilter(Constants.PM_EWALLET));
        tabCash.setOnClickListener(v -> setFilter(Constants.PM_CASH));

        cardPreviewBg = findViewById(R.id.cardPreviewBg);
        tvCardTypeBadge = findViewById(R.id.tvCardTypeBadge);
        tvCardNumber = findViewById(R.id.tvCardNumber);
        tvCardBalance = findViewById(R.id.tvCardBalance);
        tvBankName = findViewById(R.id.tvBankName);

        tvInfoAccountName = findViewById(R.id.tvInfoAccountName);
        tvInfoCardNumber = findViewById(R.id.tvInfoCardNumber);
        tvInfoBalance = findViewById(R.id.tvInfoBalance);

        findViewById(R.id.btnEditCard).setOnClickListener(v -> {
            PaymentMethodBalance c = selectedCard();
            if (c != null) showEditCardDialog(c);
            else Toast.makeText(this, "Belum ada kartu untuk diedit", Toast.LENGTH_SHORT).show();
        });

        rvCards = findViewById(R.id.rvWalletCards);
        adapter = new WalletAdapter(card -> {
            selectedId = card.id;
            renderSelectedCard();
            adapter.notifyDataSetChanged();
        });
        rvCards.setAdapter(adapter);
    }

    private void setupData() {
        int userId = FinoraApp.container(this).session.getUserId();
        FinoraApp.container(this).paymentMethodRepository.balances(userId).observe(this, list -> {
            if (list == null) return;
            allBalances = list;
            applyFilter();
        });
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateTabStyles();
        applyFilter();
    }

    private void updateTabStyles() {
        tabAll.setBackgroundResource("ALL".equals(currentFilter) ? R.drawable.bg_chip_selected : android.R.color.transparent);
        tabAll.setTextColor("ALL".equals(currentFilter) ? 0xFFFFFFFF : 0xFF667085);

        tabDebit.setBackgroundResource(Constants.PM_BANK.equals(currentFilter) ? R.drawable.bg_chip_selected : android.R.color.transparent);
        tabDebit.setTextColor(Constants.PM_BANK.equals(currentFilter) ? 0xFFFFFFFF : 0xFF667085);

        tabEWallet.setBackgroundResource(Constants.PM_EWALLET.equals(currentFilter) ? R.drawable.bg_chip_selected : android.R.color.transparent);
        tabEWallet.setTextColor(Constants.PM_EWALLET.equals(currentFilter) ? 0xFFFFFFFF : 0xFF667085);

        tabCash.setBackgroundResource(Constants.PM_CASH.equals(currentFilter) ? R.drawable.bg_chip_selected : android.R.color.transparent);
        tabCash.setTextColor(Constants.PM_CASH.equals(currentFilter) ? 0xFFFFFFFF : 0xFF667085);
    }

    private boolean matchesFilter(PaymentMethodBalance b) {
        if ("ALL".equals(currentFilter)) return true;
        if (Constants.PM_BANK.equals(currentFilter)) {
            return Constants.PM_BANK.equalsIgnoreCase(b.type) || Constants.PM_CARD.equalsIgnoreCase(b.type);
        }
        return currentFilter.equalsIgnoreCase(b.type);
    }

    @Nullable
    private PaymentMethodBalance selectedCard() {
        for (PaymentMethodBalance b : filteredBalances) if (b.id == selectedId) return b;
        return null;
    }

    private void applyFilter() {
        filteredBalances.clear();
        for (PaymentMethodBalance b : allBalances) if (matchesFilter(b)) filteredBalances.add(b);
        PaymentMethodBalance sel = selectedCard();
        if (sel == null) selectedId = filteredBalances.isEmpty() ? -1 : filteredBalances.get(0).id;
        adapter.submitList(filteredBalances);
        renderSelectedCard();
    }

    private static String lastGroup(String digits) {
        String masked = Formatters.maskedCardNumber(digits);
        return masked.substring(masked.lastIndexOf(' ') + 1);
    }

    private static String typeLabel(String type) {
        if (Constants.PM_BANK.equals(type)) return "BANK";
        if (Constants.PM_EWALLET.equals(type)) return "E-WALLET";
        if (Constants.PM_CARD.equals(type)) return "DEBIT";
        return "TUNAI";
    }

    private void renderSelectedCard() {
        PaymentMethodBalance card = selectedCard();
        if (card == null) {
            tvCardTypeBadge.setText("-");
            tvCardNumber.setText(Formatters.maskedCardNumber(null));
            tvCardBalance.setText(Formatters.currency(0));
            tvBankName.setText("Belum ada kartu");
            applyCardBackground(cardPreviewBg, 0xFF94A3B8);
            tvInfoAccountName.setText("-");
            tvInfoCardNumber.setText("-");
            tvInfoBalance.setText(Formatters.currency(0));
            return;
        }
        tvCardTypeBadge.setText(typeLabel(card.type));
        tvCardNumber.setText(Formatters.maskedCardNumber(card.accountNumber));
        tvCardBalance.setText(Formatters.currency(card.balance));
        tvBankName.setText(card.name);
        applyCardBackground(cardPreviewBg, parseCardColor(card.color));

        tvInfoAccountName.setText(card.name + " (" + typeLabel(card.type) + ")");
        tvInfoCardNumber.setText(Formatters.maskedCardNumber(card.accountNumber));
        tvInfoBalance.setText(Formatters.currency(card.balance));
    }

    private int parseCardColor(String colorStr) {
        try {
            if (colorStr != null && colorStr.startsWith("#")) {
                return Color.parseColor(colorStr);
            }
        } catch (Exception ignored) { }
        return 0xFF1555E8;
    }

    private void applyCardBackground(View view, int color) {
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{color, darkenColor(color)}
        );
        gd.setCornerRadius(18 * getResources().getDisplayMetrics().density);
        view.setBackground(gd);
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f;
        return Color.HSVToColor(hsv);
    }

    private static final String[] SWATCH_HEX = {"#1555E8", "#F97316", "#00A879", "#8B5CF6", "#EC4899", "#182033"};
    private static final int[] SWATCH_IDS = {R.id.colorBlue, R.id.colorOrange, R.id.colorGreen,
            R.id.colorPurple, R.id.colorPink, R.id.colorDark};

    private static String digitsOnly(String s) { return s == null ? "" : s.replaceAll("[^0-9]", ""); }

    private void styleSwatch(View v, int color, boolean selected) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        if (selected) gd.setStroke(Math.round(3 * getResources().getDisplayMetrics().density), 0xFF94A3B8);
        v.setBackground(gd);
        float scale = selected ? 1.15f : 1f;
        v.setScaleX(scale);
        v.setScaleY(scale);
    }

    private void showEditCardDialog(PaymentMethodBalance card) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null);
        dialog.setContentView(view);
        dialog.setOnShowListener(d -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.setBackgroundResource(R.drawable.bg_sheet_top);
                BottomSheetBehavior<FrameLayout> b = BottomSheetBehavior.from(sheet);
                b.setSkipCollapsed(true);
                b.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        RelativeLayout preview = view.findViewById(R.id.dialogCardPreviewBg);
        TextView tvType = view.findViewById(R.id.dialogCardTypeBadge);
        TextView tvNum = view.findViewById(R.id.dialogCardNumber);
        TextView tvBal = view.findViewById(R.id.dialogCardBalance);
        TextView tvBank = view.findViewById(R.id.dialogBankName);
        EditText etName = view.findViewById(R.id.etDialogBankName);
        EditText etBal = view.findViewById(R.id.etDialogBalance);
        EditText etDigits = view.findViewById(R.id.etDialogCardLastDigits);

        final String[] chosenColor = {card.color != null ? card.color : "#1555E8"};
        final View[] swatches = new View[SWATCH_IDS.length];

        Runnable refreshSwatches = () -> {
            for (int i = 0; i < swatches.length; i++) {
                styleSwatch(swatches[i], Color.parseColor(SWATCH_HEX[i]),
                        SWATCH_HEX[i].equalsIgnoreCase(chosenColor[0]));
            }
            applyCardBackground(preview, parseCardColor(chosenColor[0]));
        };
        for (int i = 0; i < SWATCH_IDS.length; i++) {
            final int idx = i;
            swatches[i] = view.findViewById(SWATCH_IDS[i]);
            swatches[i].setOnClickListener(v -> {
                chosenColor[0] = SWATCH_HEX[idx];
                refreshSwatches.run();
            });
        }

        tvType.setText(typeLabel(card.type));
        etName.setText(card.name);
        etName.setSelection(etName.getText().length());
        etBal.setText(String.format(Locale.US, "%.0f", card.balance));
        String curDigits = digitsOnly(card.accountNumber);
        etDigits.setText(curDigits.length() > 3 ? curDigits.substring(curDigits.length() - 3) : curDigits);
        tvNum.setText(Formatters.maskedCardNumber(card.accountNumber));
        tvBal.setText(Formatters.currency(card.balance));
        tvBank.setText(card.name);
        refreshSwatches.run();

        // Pratinjau langsung saat mengetik
        etName.addTextChangedListener(new SimpleWatcher(t -> tvBank.setText(t.isEmpty() ? card.name : t)));
        etDigits.addTextChangedListener(new SimpleWatcher(t -> tvNum.setText(Formatters.maskedCardNumber(t))));
        etBal.addTextChangedListener(new SimpleWatcher(t -> {
            Double v = parseBalance(t);
            tvBal.setText(v == null ? Formatters.currency(0) : Formatters.currency(v));
        }));

        view.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btnSaveCardChanges).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            Double newBal = parseBalance(etBal.getText().toString());
            String digits = digitsOnly(etDigits.getText().toString());
            if (name.isEmpty()) {
                etName.setError("Nama wajib diisi");
                return;
            }
            if (newBal == null) {
                etBal.setError("Saldo tidak valid");
                return;
            }
            // Saldo yang tampil = saldo awal + mutasi transaksi. Agar angka yang diketik
            // pengguna benar-benar menjadi saldo saat ini, sesuaikan saldo awalnya.
            double netTransactions = card.balance - card.initialBalance;
            double newInitial = newBal - netTransactions;
            String storedDigits = digits.isEmpty() ? "---" : digits;
            FinoraApp.container(this).paymentMethodRepository.update(
                    card.id, name, chosenColor[0], newInitial, storedDigits, () -> {
                        Toast.makeText(this, "Perubahan kartu berhasil disimpan", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        view.findViewById(R.id.btnDeleteCard).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Hapus Kartu")
                        .setMessage("Hapus " + card.name + "? Kartu disembunyikan dari daftar, "
                                + "sedangkan riwayat transaksinya tetap tersimpan.")
                        .setPositiveButton("Hapus", (d, w) ->
                                FinoraApp.container(this).paymentMethodRepository.delete(card.id, () -> {
                                    Toast.makeText(this, "Kartu berhasil dihapus", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }))
                        .setNegativeButton("Batal", null)
                        .show());

        dialog.show();
    }

    @Nullable
    private static Double parseBalance(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        boolean neg = t.startsWith("-");
        String digits = digitsOnly(t);
        if (digits.isEmpty() || digits.length() > 15) return null;
        try {
            double v = Double.parseDouble(digits);
            return neg ? -v : v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private interface TextCallback { void on(String text); }

    private static class SimpleWatcher implements TextWatcher {
        private final TextCallback cb;
        SimpleWatcher(TextCallback cb) { this.cb = cb; }
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) { cb.on(s == null ? "" : s.toString().trim()); }
        @Override public void afterTextChanged(Editable s) { }
    }

    private void showAddCardDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null);
        EditText etName = dialogView.findViewById(R.id.etCardName);
        EditText etBalance = dialogView.findViewById(R.id.etCardBalance);
        android.widget.Spinner spType = dialogView.findViewById(R.id.spCardType);

        String[] types = {"Bank", "E-Wallet", "Tunai", "Kartu"};
        String[] typeKeys = {Constants.PM_BANK, Constants.PM_EWALLET, Constants.PM_CASH, Constants.PM_CARD};

        android.widget.ArrayAdapter<String> a = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, types);
        spType.setAdapter(a);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Tambah Kartu / Dompet Baru")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String balDigits = digitsOnly(etBalance.getText().toString());
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double bal = 0;
                    try { if (!balDigits.isEmpty() && balDigits.length() <= 15) bal = Double.parseDouble(balDigits); }
                    catch (NumberFormatException ignored) { }
                    int pos = spType.getSelectedItemPosition();
                    String type = typeKeys[pos];
                    String[] defaultColors = {"#1555E8", "#F97316", "#00A879", "#8B5CF6"};
                    int userId = FinoraApp.container(this).session.getUserId();
                    PaymentMethodEntity entity = new PaymentMethodEntity(userId, name, type, bal, "---", defaultColors[pos]);
                    FinoraApp.container(this).paymentMethodRepository.add(entity);
                    Toast.makeText(this, "Kartu berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    interface OnCardClick { void onClick(PaymentMethodBalance b); }

    class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.Holder> {
        private final List<PaymentMethodBalance> items = new ArrayList<>();
        private final OnCardClick listener;

        WalletAdapter(OnCardClick listener) { this.listener = listener; }

        void submitList(List<PaymentMethodBalance> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_card, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            PaymentMethodBalance item = items.get(position);
            holder.tvName.setText(item.name);
            holder.tvTypeNumber.setText(typeLabel(item.type) + " \u2022 " + lastGroup(item.accountNumber));
            holder.tvBalance.setText(Formatters.currency(item.balance));

            int c = parseCardColor(item.color);
            holder.viewPill.setBackgroundColor(c);

            boolean isSelected = selectedId == item.id;
            holder.tvSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(item);
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class Holder extends RecyclerView.ViewHolder {
            final View viewPill;
            final TextView tvName, tvTypeNumber, tvBalance, tvSelected;
            Holder(View v) {
                super(v);
                viewPill = v.findViewById(R.id.viewColorPill);
                tvName = v.findViewById(R.id.tvWalletName);
                tvTypeNumber = v.findViewById(R.id.tvWalletTypeNumber);
                tvBalance = v.findViewById(R.id.tvWalletBalance);
                tvSelected = v.findViewById(R.id.tvWalletSelectedBadge);
            }
        }
    }
}
