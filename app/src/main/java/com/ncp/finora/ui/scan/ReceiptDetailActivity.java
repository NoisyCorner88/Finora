package com.ncp.finora.ui.scan;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.CategoryEntity;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.entity.ReceiptEntity;
import com.ncp.finora.data.local.entity.ReceiptItemEntity;
import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.model.ParsedReceipt;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.UiUtils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PARSED_RECEIPT = "extra_parsed_receipt";

    private ParsedReceipt receipt;
    private List<CategoryEntity> categories = new ArrayList<>();
    private List<PaymentMethodEntity> paymentMethods = new ArrayList<>();
    private CategoryEntity selectedCategory;
    private PaymentMethodEntity selectedPaymentMethod;

    private ImageView ivReceiptThumb;
    private TextView tvReceiptTitle, tvReceiptSummary, tvReceiptFileName;
    private TextView tvMerchantName, tvTransactionDate, tvCategoryName, tvCategoryIcon;
    private TextView tvItemCountBadge, tvSubtotalLabel, tvSubtotalVal, tvDiscountVal, tvTaxVal, tvTotalPayment;
    private Spinner spPaymentMethod;
    private ReceiptItemAdapter itemAdapter;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        receipt = (ParsedReceipt) getIntent().getSerializableExtra(EXTRA_PARSED_RECEIPT);
        if (receipt == null) {
            receipt = new ParsedReceipt();
            receipt.merchantName = "Struk Pembelian";
            receipt.transactionDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            receipt.formattedDateTime = new SimpleDateFormat("d MMMM yyyy, HH.mm 'WIB'", new Locale("id", "ID")).format(new Date());
        }

        initViews();
        loadReceiptImage();
        setupCategories();
        setupPaymentMethods();
        renderReceiptData();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        String userName = FinoraApp.container(this).session.getName();
        tvAvatar.setText(UiUtils.initial(userName));

        ivReceiptThumb = findViewById(R.id.ivReceiptThumb);
        tvReceiptTitle = findViewById(R.id.tvReceiptTitle);
        tvReceiptSummary = findViewById(R.id.tvReceiptSummary);
        tvReceiptFileName = findViewById(R.id.tvReceiptFileName);

        tvMerchantName = findViewById(R.id.tvMerchantName);
        tvTransactionDate = findViewById(R.id.tvTransactionDate);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvCategoryIcon = findViewById(R.id.tvCategoryIcon);

        tvItemCountBadge = findViewById(R.id.tvItemCountBadge);
        tvSubtotalLabel = findViewById(R.id.tvSubtotalLabel);
        tvSubtotalVal = findViewById(R.id.tvSubtotalVal);
        tvDiscountVal = findViewById(R.id.tvDiscountVal);
        tvTaxVal = findViewById(R.id.tvTaxVal);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);

        findViewById(R.id.btnEnlargeReceipt).setOnClickListener(v -> showEnlargedReceipt());
        findViewById(R.id.boxMerchant).setOnClickListener(v -> showEditMerchantDialog());
        findViewById(R.id.boxDate).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.boxCategory).setOnClickListener(v -> showCategoryPicker());
        findViewById(R.id.boxTotalPayment).setOnClickListener(v -> showEditTotalDialog());

        RecyclerView rvItems = findViewById(R.id.rvReceiptItems);
        itemAdapter = new ReceiptItemAdapter(new ReceiptItemAdapter.OnItemActionListener() {
            @Override
            public void onDelete(int position) {
                receipt.items.remove(position);
                itemAdapter.notifyItemRemoved(position);
                recalculateTotals();
            }

            @Override
            public void onEdit(int position, ReceiptItemEntity item) {
                showEditItemDialog(position, item);
            }
        });
        rvItems.setAdapter(itemAdapter);

        findViewById(R.id.btnAddItem).setOnClickListener(v -> showAddItemDialog());
        findViewById(R.id.btnConfirmSave).setOnClickListener(v -> saveReceiptTransaction());
        findViewById(R.id.btnRescan).setOnClickListener(v -> finish());
    }

    private void loadReceiptImage() {
        if (receipt.imagePath == null || receipt.imagePath.isEmpty()) return;
        try {
            File f = new File(receipt.imagePath);
            if (f.exists()) {
                tvReceiptFileName.setText(f.getName());
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) ivReceiptThumb.setImageBitmap(bmp);
            } else {
                Uri uri = Uri.parse(receipt.imagePath);
                tvReceiptFileName.setText("STRUK_" + System.currentTimeMillis() + ".JPG");
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (bmp != null) ivReceiptThumb.setImageBitmap(bmp);
                if (is != null) is.close();
            }
        } catch (Exception ignored) { }
    }

    private void renderReceiptData() {
        tvReceiptTitle.setText("Struk Belanja Fisik");
        tvMerchantName.setText(receipt.merchantName);
        tvTransactionDate.setText(receipt.formattedDateTime);

        itemAdapter.submitList(receipt.items);
        updateSummaryTexts();
    }

    private void updateSummaryTexts() {
        int count = receipt.items.size();
        tvItemCountBadge.setText(count + " Unit Terbaca");
        tvReceiptSummary.setText(count + " item terdeteksi • " + (receipt.tax > 0 ? "1 potongan pajak" : "0 potongan pajak"));
        tvSubtotalLabel.setText("Subtotal (" + count + " item)");
        tvSubtotalVal.setText(Formatters.currency(receipt.subtotal));
        tvDiscountVal.setText("-" + Formatters.currency(receipt.discount));
        tvTaxVal.setText(Formatters.currency(receipt.tax));
        tvTotalPayment.setText(Formatters.currency(receipt.total));
    }

    private void recalculateTotals() {
        double sub = 0;
        for (ReceiptItemEntity item : receipt.items) {
            sub += item.unitPrice * (item.quantity > 0 ? item.quantity : 1);
        }
        receipt.subtotal = sub;
        receipt.total = Math.max(0, sub + receipt.tax - receipt.discount);
        updateSummaryTexts();
    }

    private void setupCategories() {
        int userId = FinoraApp.container(this).session.getUserId();
        FinoraApp.container(this).categoryRepository.all(userId).observe(this, list -> {
            if (list == null) return;
            categories.clear();
            for (CategoryEntity c : list) {
                if (Constants.EXPENSE.equalsIgnoreCase(c.type)) {
                    categories.add(c);
                }
            }
            // Auto match predicted category
            for (CategoryEntity c : categories) {
                if (c.name.equalsIgnoreCase(receipt.categoryName)
                        || c.name.toLowerCase(Locale.ROOT).contains(receipt.categoryName.toLowerCase(Locale.ROOT))) {
                    selectedCategory = c;
                    break;
                }
            }
            if (selectedCategory == null && !categories.isEmpty()) {
                selectedCategory = categories.get(0);
            }
            if (selectedCategory != null) {
                tvCategoryName.setText(selectedCategory.name);
                tvCategoryIcon.setText(selectedCategory.icon != null ? selectedCategory.icon : "🏷");
            }
        });
    }

    private void setupPaymentMethods() {
        int userId = FinoraApp.container(this).session.getUserId();
        FinoraApp.container(this).paymentMethodRepository.active(userId).observe(this, list -> {
            if (list == null) return;
            paymentMethods = list;
            List<String> names = new ArrayList<>();
            for (PaymentMethodEntity pm : list) {
                names.add(pm.name + " (" + pm.type + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, names);
            spPaymentMethod.setAdapter(adapter);
            if (!paymentMethods.isEmpty()) {
                selectedPaymentMethod = paymentMethods.get(0);
            }
        });
    }

    private void showEnlargedReceipt() {
        if (receipt.imagePath == null || receipt.imagePath.isEmpty()) {
            Toast.makeText(this, "Gambar struk tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ImageView iv = new ImageView(this);
            iv.setAdjustViewBounds(true);
            iv.setPadding(20, 20, 20, 20);

            File f = new File(receipt.imagePath);
            if (f.exists()) {
                iv.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
            } else {
                Uri uri = Uri.parse(receipt.imagePath);
                InputStream is = getContentResolver().openInputStream(uri);
                iv.setImageBitmap(BitmapFactory.decodeStream(is));
                if (is != null) is.close();
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Foto Struk")
                    .setView(iv)
                    .setPositiveButton("Tutup", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditMerchantDialog() {
        EditText input = new EditText(this);
        input.setText(receipt.merchantName);
        input.setSelection(input.getText().length());
        new MaterialAlertDialogBuilder(this)
                .setTitle("Ubah Nama Toko / Merchant")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (!val.isEmpty()) {
                        receipt.merchantName = val;
                        tvMerchantName.setText(val);
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
            receipt.transactionDate = isoFormat.format(calendar.getTime());
            receipt.formattedDateTime = displayFormat.format(calendar.getTime()) + ", " +
                    new SimpleDateFormat("HH.mm", Locale.US).format(new Date()) + " WIB";
            tvTransactionDate.setText(receipt.formattedDateTime);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void showCategoryPicker() {
        if (categories.isEmpty()) return;
        String[] names = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            names[i] = (categories.get(i).icon != null ? categories.get(i).icon + "  " : "") + categories.get(i).name;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle("Pilih Kategori")
                .setItems(names, (dialog, which) -> {
                    selectedCategory = categories.get(which);
                    tvCategoryName.setText(selectedCategory.name);
                    tvCategoryIcon.setText(selectedCategory.icon != null ? selectedCategory.icon : "🏷");
                })
                .show();
    }

    private void showEditTotalDialog() {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.format(Locale.US, "%.0f", receipt.total));
        new MaterialAlertDialogBuilder(this)
                .setTitle("Ubah Total Pembayaran")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    try {
                        double val = Double.parseDouble(input.getText().toString().trim());
                        receipt.total = val;
                        tvTotalPayment.setText(Formatters.currency(val));
                    } catch (Exception ignored) { }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAddItemDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null);
        // Let's create a clean prompt with an item name and price
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setHint("Nama Barang / Item");
        layout.addView(etName);

        EditText etPrice = new EditText(this);
        etPrice.setHint("Harga (Rp)");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPrice);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Tambah Item Belanja")
                .setView(layout)
                .setPositiveButton("Tambah", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    if (!name.isEmpty() && !priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            ReceiptItemEntity item = new ReceiptItemEntity();
                            item.itemName = name;
                            item.quantity = 1;
                            item.unitPrice = price;
                            receipt.items.add(item);
                            itemAdapter.notifyItemInserted(receipt.items.size() - 1);
                            recalculateTotals();
                        } catch (Exception ignored) { }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEditItemDialog(int pos, ReceiptItemEntity item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(this);
        etName.setText(item.itemName);
        layout.addView(etName);

        EditText etPrice = new EditText(this);
        etPrice.setText(String.format(Locale.US, "%.0f", item.unitPrice));
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPrice);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Item Belanja")
                .setView(layout)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    if (!name.isEmpty() && !priceStr.isEmpty()) {
                        try {
                            item.itemName = name;
                            item.unitPrice = Double.parseDouble(priceStr);
                            itemAdapter.notifyItemChanged(pos);
                            recalculateTotals();
                        } catch (Exception ignored) { }
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void saveReceiptTransaction() {
        if (receipt.total <= 0) {
            Toast.makeText(this, "Total pembayaran harus lebih dari 0", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategory == null && !categories.isEmpty()) {
            selectedCategory = categories.get(0);
        }
        int spPos = spPaymentMethod.getSelectedItemPosition();
        if (spPos >= 0 && spPos < paymentMethods.size()) {
            selectedPaymentMethod = paymentMethods.get(spPos);
        }
        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = FinoraApp.container(this).session.getUserId();

        ReceiptEntity rEntity = new ReceiptEntity();
        rEntity.userId = userId;
        rEntity.imagePath = receipt.imagePath != null ? receipt.imagePath : "";
        rEntity.merchantName = receipt.merchantName;
        rEntity.receiptTotal = receipt.total;
        rEntity.ocrText = receipt.rawText != null ? receipt.rawText : "";
        rEntity.ocrStatus = "PARSED";
        rEntity.createdAt = System.currentTimeMillis();

        TransactionEntity tEntity = new TransactionEntity();
        tEntity.userId = userId;
        tEntity.amount = receipt.total;
        tEntity.type = Constants.EXPENSE;
        tEntity.categoryId = selectedCategory.idCategory;
        tEntity.paymentMethodId = selectedPaymentMethod.idPaymentMethod;
        tEntity.description = receipt.merchantName;
        tEntity.transactionDate = receipt.transactionDate != null && !receipt.transactionDate.isEmpty()
                ? receipt.transactionDate
                : new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        tEntity.inputMethod = "OCR_SCAN";
        tEntity.createdAt = System.currentTimeMillis();

        FinoraApp.container(this).receiptRepository.saveReceiptAndTransaction(
                rEntity, receipt.items, tEntity, new Callback<Long>() {
                    @Override
                    public void onSuccess(Long data) {
                        Toast.makeText(ReceiptDetailActivity.this, "Struk & transaksi berhasil disimpan ke saldo!", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ReceiptDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
