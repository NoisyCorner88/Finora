package com.ncp.finora.ui.splash;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ncp.finora.data.session.SessionManager;
import com.ncp.finora.util.Event;

public class SplashViewModel extends ViewModel {
    private static final int TICK_MS = 40;
    private static final int STEP = 3;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);
    private final MutableLiveData<Event<Boolean>> navigate = new MutableLiveData<>();
    private final SessionManager session;
    private int current = 0;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            current = Math.min(100, current + STEP);
            progress.setValue(current);
            if (current >= 100) {
                navigate.setValue(new Event<>(session.shouldAutoLogin()));
            } else {
                handler.postDelayed(this, TICK_MS);
            }
        }
    };

    public SplashViewModel(SessionManager session) {
        this.session = session;
        handler.postDelayed(ticker, TICK_MS);
    }

    public LiveData<Integer> getProgress() {
        return progress;
    }

    /** true = langsung ke beranda, false = ke halaman login. */
    public LiveData<Event<Boolean>> getNavigate() {
        return navigate;
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacks(ticker);
    }
}
