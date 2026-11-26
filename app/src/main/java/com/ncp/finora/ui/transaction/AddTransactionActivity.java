package com.ncp.finora.ui.transaction;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.CategoryEntity;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.UiUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));

    private AddTransactionViewModel viewModel;
    private ChipGroup cgCategory, cgPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this, FinoraApp.factory(this)).get(AddTransactionViewModel.class);

        EditText etAmount = findViewById(R.id.etAmount);
        EditText etDescription = findViewById(R.id.etDescription);
        TextView tvCharCount = findViewById(R.id.tvCharCount);
        TextView tvDate = findViewById(R.id.tvDate);
        View boxDate = findViewById(R.id.boxDate);
        TextView chipExpense = findViewById(R.id.chipExpense);
        TextView chipIncome = findViewById(R.id.chipIncome);
        View btnSave = findViewById(R.id.btnSave);
        cgCategory = findViewById(R.id.cgCategory);
        cgPayment = findViewById(R.id.cgPayment);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        chipExpense.setOnClickListener(v -> viewModel.setType(Constants.EXPENSE));
        chipIncome.setOnClickListener(v -> viewModel.setType(Constants.INCOME));
        if (boxDate != null) boxDate.setOnClickListener(v -> showDatePicker());
        else tvDate.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.chipAdd10k).setOnClickListener(v -> addAmount(etAmount, 10000));
        findViewById(R.id.chipAdd50k).setOnClickListener(v -> addAmount(etAmount, 50000));
        findViewById(R.id.chipAdd100k).setOnClickListener(v -> addAmount(etAmount, 100000));

        etDescription.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvCharCount != null) tvCharCount.setText((s != null ? s.length() : 0) + "/40");
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        btnSave.setOnClickListener(v -> viewModel.save(
                etAmount.getText().toString(), etDescription.getText().toString(),
                selectedId(cgCategory), selectedId(cgPayment)));

        viewModel.getType().observe(this, type -> {
            boolean isExp = Constants.EXPENSE.equals(type);
            if (isExp) {
                chipExpense.setBackgroundColor(0xFFEF4444);
                chipExpense.setTextColor(0xFFFFFFFF);
                chipIncome.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                chipIncome.setTextColor(0xFF00A879);
            } else {
                chipIncome.setBackgroundColor(0xFF00C853);
                chipIncome.setTextColor(0xFFFFFFFF);
                chipExpense.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                chipExpense.setTextColor(0xFFEF4444);
            }
        });
        viewModel.getDate().observe(this, d -> tvDate.setText(d.format(DATE_FORMAT)));
        viewModel.getCategories().observe(this, this::renderCategories);
        viewModel.getPaymentMethods().observe(this, this::renderPaymentMethods);
        viewModel.getSaving().observe(this, saving -> btnSave.setEnabled(!saving));
        viewModel.getMessage().observe(this, event -> {
            String msg = event.getContentIfNotHandled();
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getSaved().observe(this, event -> {
            if (event.getContentIfNotHandled() == null) return;
            Toast.makeText(this, "Transaksi tersimpan", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void renderCategories(List<CategoryEntity> list) {
        cgCategory.removeAllViews();
        if (list == null) return;
        for (CategoryEntity c : list) {
            cgCategory.addView(buildChip(c.icon + "  " + c.name, c.idCategory));
        }
        if (cgCategory.getChildCount() > 0) {
            ((Chip) cgCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void renderPaymentMethods(List<PaymentMethodEntity> list) {
        int previous = selectedId(cgPayment);
        cgPayment.removeAllViews();
        if (list == null) return;
        Chip toCheck = null;
        for (PaymentMethodEntity p : list) {
            Chip chip = buildChip(p.name, p.idPaymentMethod);
            cgPayment.addView(chip);
            if (toCheck == null || p.idPaymentMethod == previous) toCheck = chip;
        }
        if (toCheck != null) toCheck.setChecked(true);
    }

    private Chip buildChip(String label, int id) {
        Chip chip = new Chip(this);
        chip.setId(View.generateViewId());
        chip.setText(label);
        chip.setTag(id);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        return chip;
    }

    /** Mengembalikan id data (bukan id View) dari chip terpilih, atau -1. */
    private int selectedId(ChipGroup group) {
        int checked = group.getCheckedChipId();
        if (checked == View.NO_ID) return -1;
        View chip = group.findViewById(checked);
        return chip != null && chip.getTag() instanceof Integer ? (Integer) chip.getTag() : -1;
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih tanggal")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> viewModel.setDate(
                Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate()));
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void addAmount(EditText et, int add) {
        String s = et.getText().toString().trim();
        long current = 0;
        try {
            if (!s.isEmpty()) current = Long.parseLong(s);
        } catch (Exception ignored) { }
        current += add;
        et.setText(String.valueOf(current));
        et.setSelection(et.getText().length());
    }
}
