package com.ncp.finora.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.entity.UserEntity;
import com.ncp.finora.data.repository.AuthRepository;
import com.ncp.finora.data.repository.PaymentMethodRepository;
import com.ncp.finora.data.repository.ReceiptRepository;
import com.ncp.finora.data.repository.TransactionRepository;
import com.ncp.finora.data.session.SessionManager;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Event;

public class ProfileViewModel extends ViewModel {
    private final int userId;
    private final SessionManager session;
    private final AuthRepository authRepo;
    private final LiveData<UserEntity> user;
    private final LiveData<Integer> transactionCount;
    private final LiveData<Integer> scanCount;
    private final LiveData<Double> totalBalance;
    private final MutableLiveData<Event<Boolean>> loggedOut = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> message = new MutableLiveData<>();

    public ProfileViewModel(int userId, SessionManager session,
                            AuthRepository authRepo, TransactionRepository txRepo,
                            ReceiptRepository receiptRepo, PaymentMethodRepository pmRepo) {
        this.userId = userId;
        this.session = session;
        this.authRepo = authRepo;
        this.user = authRepo.observeUser(userId);
        this.transactionCount = txRepo.count(userId);
        this.scanCount = receiptRepo.scanCount(userId);
        this.totalBalance = Transformations.map(pmRepo.balances(userId), list -> {
            if (list == null) return 0.0;
            double sum = 0.0;
            for (com.ncp.finora.data.local.model.PaymentMethodBalance b : list) {
                sum += b.balance;
            }
            return sum;
        });
    }

    public LiveData<UserEntity> getUser() { return user; }
    public LiveData<Integer> getTransactionCount() { return transactionCount; }
    public LiveData<Integer> getScanCount() { return scanCount; }
    public LiveData<Double> getTotalBalance() { return totalBalance; }
    public LiveData<Event<Boolean>> getLoggedOut() { return loggedOut; }
    public LiveData<Event<String>> getMessage() { return message; }

    public void updateProfile(String name, String email, Callback<Boolean> cb) {
        authRepo.updateProfile(userId, name, email, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                session.saveUser(userId, name, email, session.getUsername());
                message.setValue(new Event<>("Profil berhasil diperbarui"));
                if (cb != null) cb.onSuccess(data);
            }

            @Override
            public void onError(String error) {
                message.setValue(new Event<>(error));
                if (cb != null) cb.onError(error);
            }
        });
    }

    public void changePassword(String oldPass, String newPass, Callback<Boolean> cb) {
        authRepo.changePassword(userId, oldPass, newPass, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                message.setValue(new Event<>("Kata sandi berhasil diubah"));
                if (cb != null) cb.onSuccess(data);
            }

            @Override
            public void onError(String error) {
                message.setValue(new Event<>(error));
                if (cb != null) cb.onError(error);
            }
        });
    }

    public void deleteAccount(String password, Callback<Boolean> cb) {
        authRepo.deleteAccount(userId, password, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                session.clear();
                loggedOut.setValue(new Event<>(true));
                if (cb != null) cb.onSuccess(data);
            }

            @Override
            public void onError(String error) {
                message.setValue(new Event<>(error));
                if (cb != null) cb.onError(error);
            }
        });
    }

    public void logout() {
        session.clear();
        loggedOut.setValue(new Event<>(true));
    }
}
