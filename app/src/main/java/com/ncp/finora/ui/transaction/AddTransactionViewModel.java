package com.ncp.finora.ui.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.entity.CategoryEntity;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.repository.CategoryRepository;
import com.ncp.finora.data.repository.PaymentMethodRepository;
import com.ncp.finora.data.repository.TransactionRepository;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.Event;

import java.time.LocalDate;
import java.util.List;

public class AddTransactionViewModel extends ViewModel {
    private final int userId;
    private final TransactionRepository txRepo;

    private final MutableLiveData<String> type = new MutableLiveData<>(Constants.EXPENSE);
    private final MutableLiveData<LocalDate> date = new MutableLiveData<>(LocalDate.now());
    private final LiveData<List<CategoryEntity>> categories;
    private final LiveData<List<PaymentMethodEntity>> paymentMethods;
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> saved = new MutableLiveData<>();

    public AddTransactionViewModel(int userId, TransactionRepository txRepo,
                                   CategoryRepository categoryRepo, PaymentMethodRepository paymentRepo) {
        this.userId = userId;
        this.txRepo = txRepo;
        this.categories = Transformations.switchMap(type, t -> categoryRepo.byType(userId, t));
        this.paymentMethods = paymentRepo.active(userId);
    }

    public LiveData<String> getType() { return type; }
    public LiveData<LocalDate> getDate() { return date; }
    public LiveData<List<CategoryEntity>> getCategories() { return categories; }
    public LiveData<List<PaymentMethodEntity>> getPaymentMethods() { return paymentMethods; }
    public LiveData<Boolean> getSaving() { return saving; }
    public LiveData<Event<String>> getMessage() { return message; }
    public LiveData<Event<Boolean>> getSaved() { return saved; }

    public void setType(String newType) {
        if (!newType.equals(type.getValue())) type.setValue(newType);
    }

    public void setDate(LocalDate newDate) {
        date.setValue(newDate);
    }

    /** categoryId / paymentMethodId bernilai -1 bila belum dipilih. */
    public void save(String amountText, String description, int categoryId, int paymentMethodId) {
        double amount = parseAmount(amountText);
        if (amount <= 0) {
            message.setValue(new Event<>("Nominal harus lebih dari 0"));
            return;
        }
        if (categoryId < 0) {
            message.setValue(new Event<>("Pilih kategori terlebih dahulu"));
            return;
        }
        if (paymentMethodId < 0) {
            message.setValue(new Event<>("Pilih sumber dana terlebih dahulu"));
            return;
        }
        LocalDate d = date.getValue() != null ? date.getValue() : LocalDate.now();

        TransactionEntity t = new TransactionEntity();
        t.userId = userId;
        t.paymentMethodId = paymentMethodId;
        t.categoryId = categoryId;
        t.amount = amount;
        t.type = type.getValue() != null ? type.getValue() : Constants.EXPENSE;
        t.description = description == null ? "" : description.trim();
        t.transactionDate = d.toString();
        t.inputMethod = Constants.INPUT_MANUAL;
        t.createdAt = System.currentTimeMillis();

        saving.setValue(true);
        txRepo.add(t, new Callback<Long>() {
            @Override
            public void onSuccess(Long id) {
                saving.setValue(false);
                saved.setValue(new Event<>(true));
            }

            @Override
            public void onError(String msg) {
                saving.setValue(false);
                message.setValue(new Event<>(msg));
            }
        });
    }

    private static double parseAmount(String text) {
        if (text == null) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 15) return 0;
        return Double.parseDouble(digits);
    }
}
