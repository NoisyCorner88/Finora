package com.ncp.finora.ui.auth;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.entity.UserEntity;
import com.ncp.finora.data.repository.AuthRepository;
import com.ncp.finora.data.session.SessionManager;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Event;

public class RegisterViewModel extends ViewModel {
    private final AuthRepository repository;
    private final SessionManager session;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmError = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> registered = new MutableLiveData<>();

    public RegisterViewModel(AuthRepository repository, SessionManager session) {
        this.repository = repository;
        this.session = session;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getNameError() { return nameError; }
    public LiveData<String> getEmailError() { return emailError; }
    public LiveData<String> getUsernameError() { return usernameError; }
    public LiveData<String> getPasswordError() { return passwordError; }
    public LiveData<String> getConfirmError() { return confirmError; }
    public LiveData<Event<String>> getMessage() { return message; }
    public LiveData<Event<Boolean>> getRegistered() { return registered; }

    public void register(String name, String email, String username, String password, String confirm) {
        nameError.setValue(null);
        emailError.setValue(null);
        usernameError.setValue(null);
        passwordError.setValue(null);
        confirmError.setValue(null);

        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase();
        username = username == null ? "" : username.trim().toLowerCase();
        password = password == null ? "" : password;
        confirm = confirm == null ? "" : confirm;

        boolean valid = true;
        if (name.isEmpty()) { nameError.setValue("Nama wajib diisi"); valid = false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Format email tidak valid");
            valid = false;
        }
        if (username.length() < 4) { usernameError.setValue("Username minimal 4 karakter"); valid = false; }
        if (password.length() < 8) { passwordError.setValue("Kata sandi minimal 8 karakter"); valid = false; }
        if (!password.equals(confirm)) { confirmError.setValue("Konfirmasi kata sandi tidak sama"); valid = false; }
        if (!valid) return;

        loading.setValue(true);
        final String finalName = name;
        repository.register(name, email, username, password, new Callback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity user) {
                session.save(user.idUser, finalName, true);
                loading.setValue(false);
                registered.setValue(new Event<>(true));
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                message.setValue(new Event<>(msg));
            }
        });
    }
}
