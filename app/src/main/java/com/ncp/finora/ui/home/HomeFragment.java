package com.ncp.finora.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.ui.main.MainViewModel;
import com.ncp.finora.ui.transaction.AddTransactionActivity;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.UiUtils;

public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private TextView tvTotalBalance;
    private ImageView btnToggle;
    private Double total = 0.0;
    private boolean visible = true;

    private static final String[] TYPE_LABELS = {"Tunai", "Bank", "E-Wallet", "Kartu"};
    private static final String[] TYPE_VALUES = {
            Constants.PM_CASH, Constants.PM_BANK, Constants.PM_EWALLET, Constants.PM_CARD};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewModelProvider.Factory factory = FinoraApp.factory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity(), factory).get(MainViewModel.class);

        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        btnToggle = view.findViewById(R.id.btnToggleBalance);

        ((TextView) view.findViewById(R.id.tvGreeting)).setText(viewModel.getGreeting() + " ✨");
        ((TextView) view.findViewById(R.id.tvUserName)).setText(viewModel.getUserName());
        ((TextView) view.findViewById(R.id.tvAvatar)).setText(UiUtils.initial(viewModel.getUserName()));

        // Kartu (geser horizontal, snap per kartu)
        RecyclerView rvCards = view.findViewById(R.id.rvCards);
        CardAdapter cardAdapter = new CardAdapter();
        rvCards.setAdapter(cardAdapter);
        new PagerSnapHelper().attachToRecyclerView(rvCards);

        // Transaksi terbaru
        RecyclerView rvTransactions = view.findViewById(R.id.rvTransactions);
        TransactionAdapter transactionAdapter = new TransactionAdapter();
        rvTransactions.setAdapter(transactionAdapter);
        View empty = view.findViewById(R.id.tvEmptyTransactions);

        viewModel.getCards().observe(getViewLifecycleOwner(), cardAdapter::submit);
        viewModel.getTotalBalance().observe(getViewLifecycleOwner(), value -> {
            total = value;
            renderBalance();
        });
        viewModel.getBalanceVisible().observe(getViewLifecycleOwner(), show -> {
            visible = Boolean.TRUE.equals(show);
            btnToggle.setImageResource(visible ? R.drawable.ic_eye : R.drawable.ic_eye_off);
            cardAdapter.setMasked(!visible);
            renderBalance();
        });
        viewModel.getRecent().observe(getViewLifecycleOwner(), list -> {
            transactionAdapter.submitList(list);
            boolean isEmpty = list == null || list.isEmpty();
            empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            rvTransactions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
        viewModel.getMonthNet().observe(getViewLifecycleOwner(), net -> renderGrowth(view, net));

        // Aksi
        btnToggle.setOnClickListener(v -> viewModel.toggleBalanceVisibility());
        View.OnClickListener openAdd = v ->
                startActivity(new Intent(requireContext(), AddTransactionActivity.class));
        view.findViewById(R.id.actionScan).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), com.ncp.finora.ui.scan.ScanReceiptActivity.class)));
        view.findViewById(R.id.actionTransaction).setOnClickListener(openAdd);
        view.findViewById(R.id.actionAnalysis).setOnClickListener(v ->
                mainViewModel.select(MainViewModel.TAB_STATS));
        view.findViewById(R.id.btnHistory).setOnClickListener(v ->
                mainViewModel.select(MainViewModel.TAB_ACTIVITY));
        View.OnClickListener openCards = v ->
                startActivity(new Intent(requireContext(), com.ncp.finora.ui.card.CardActivity.class));
        view.findViewById(R.id.actionCard).setOnClickListener(openCards);
        view.findViewById(R.id.btnManageCards).setOnClickListener(openCards);
        view.findViewById(R.id.btnBell).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), com.ncp.finora.ui.notification.NotificationActivity.class)));
    }

    private void renderBalance() {
        tvTotalBalance.setText(visible ? Formatters.rupiah(total == null ? 0 : total)
                : Formatters.maskedRupiah());
    }

    private void renderGrowth(View root, Double net) {
        if (net == null) return;
        TextView tv = root.findViewById(R.id.tvGrowth);
        ImageView icon = root.findViewById(R.id.ivGrowth);
        boolean positive = net >= 0;
        tv.setText(Formatters.signedRupiah(net, positive) + " bln ini");
        tv.setTextColor(Color.parseColor(positive ? "#00A879" : "#FF253B"));
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(
                Color.parseColor(positive ? "#00A879" : "#FF253B")));
        icon.setRotation(positive ? 0 : 90);
    }

    private void showAddCardDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_card, null);
        EditText etName = dialogView.findViewById(R.id.etCardName);
        EditText etBalance = dialogView.findViewById(R.id.etCardBalance);
        Spinner spType = dialogView.findViewById(R.id.spCardType);
        spType.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, TYPE_LABELS));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tambah Sumber Dana")
                .setView(dialogView)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", (d, w) -> {
                    String name = etName.getText().toString();
                    if (name.trim().isEmpty()) {
                        Toast.makeText(requireContext(), "Nama wajib diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String digits = etBalance.getText().toString().replaceAll("[^0-9]", "");
                    double balance = digits.isEmpty() || digits.length() > 15 ? 0 : Double.parseDouble(digits);
                    viewModel.addPaymentMethod(name, TYPE_VALUES[spType.getSelectedItemPosition()], balance);
                })
                .show();
    }
}
