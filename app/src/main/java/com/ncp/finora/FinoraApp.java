package com.ncp.finora;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

import com.ncp.finora.ui.ViewModelFactory;

public class FinoraApp extends Application {
    private AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);
    }

    public static AppContainer container(Context context) {
        return ((FinoraApp) context.getApplicationContext()).container;
    }

    public static ViewModelProvider.Factory factory(Context context) {
        return new ViewModelFactory(container(context));
    }
}
