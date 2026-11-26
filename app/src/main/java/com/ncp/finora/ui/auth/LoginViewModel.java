package com.ncp.finora.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.local.entity.UserEntity;
import com.ncp.finora.data.repository.AuthRepository;
import com.ncp.finora.data.session.SessionManager;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Event;

public class LoginViewModel extends ViewModel {
    private final AuthRepository repository;
    private final SessionManager session;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> loginSuccess = new MutableLiveData<>();

    public LoginViewModel(AuthRepository repository, SessionManager session) {
        this.repository = repository;
        this.session = session;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getEmailError() { return emailError; }
    public LiveData<String> getPasswordError() { return passwordError; }
    public LiveData<Event<String>> getMessage() { return message; }
    public LiveData<Event<Boolean>> getLoginSuccess() { return loginSuccess; }

    public void login(String email, String password, boolean remember) {
        emailError.setValue(null);
        passwordError.setValue(null);
        boolean valid = true;
        if (email == null || email.trim().isEmpty()) {
            emailError.setValue("Email wajib diisi");
            valid = false;
        }
        if (password == null || password.length() < 8) {
            passwordError.setValue("Kata sandi minimal 8 karakter");
            valid = false;
        }
        if (!valid) return;

        loading.setValue(true);
        repository.login(email, password, new Callback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity user) {
                session.save(user.idUser, user.name, remember);
                loading.setValue(false);
                loginSuccess.setValue(new Event<>(true));
            }

            @Override
            public void onError(String msg) {
                loading.setValue(false);
                message.setValue(new Event<>(msg));
            }
        });
    }
}
