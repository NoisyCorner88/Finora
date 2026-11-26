package com.ncp.finora.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.ncp.finora.AppContainer;
import com.ncp.finora.ui.activity.ActivityViewModel;
import com.ncp.finora.ui.auth.LoginViewModel;
import com.ncp.finora.ui.auth.RegisterViewModel;
import com.ncp.finora.ui.home.HomeViewModel;
import com.ncp.finora.ui.main.MainViewModel;
import com.ncp.finora.ui.profile.ProfileViewModel;
import com.ncp.finora.ui.splash.SplashViewModel;
import com.ncp.finora.ui.stats.StatsViewModel;
import com.ncp.finora.ui.transaction.AddTransactionViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final AppContainer c;

    public ViewModelFactory(AppContainer container) {
        this.c = container;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        int userId = c.session.getUserId();
        if (modelClass.isAssignableFrom(SplashViewModel.class)) {
            return (T) new SplashViewModel(c.session);
        } else if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(c.authRepository, c.session);
        } else if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
            return (T) new RegisterViewModel(c.authRepository, c.session);
        } else if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel();
        } else if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(userId, c.session, c.paymentMethodRepository, c.transactionRepository);
        } else if (modelClass.isAssignableFrom(StatsViewModel.class)) {
            return (T) new StatsViewModel(userId, c.transactionRepository);
        } else if (modelClass.isAssignableFrom(ActivityViewModel.class)) {
            return (T) new ActivityViewModel(userId, c.transactionRepository);
        } else if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(userId, c.session, c.authRepository,
                    c.transactionRepository, c.receiptRepository, c.paymentMethodRepository);
        } else if (modelClass.isAssignableFrom(AddTransactionViewModel.class)) {
            return (T) new AddTransactionViewModel(userId, c.transactionRepository,
                    c.categoryRepository, c.paymentMethodRepository);
        }
        throw new IllegalArgumentException("ViewModel tidak dikenal: " + modelClass.getName());
    }
}
