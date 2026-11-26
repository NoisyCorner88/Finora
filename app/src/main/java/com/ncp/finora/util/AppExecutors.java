package com.ncp.finora.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class AppExecutors {
    private static final Executor DISK_IO = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() { }

    public static void io(Runnable r) {
        DISK_IO.execute(r);
    }

    public static void main(Runnable r) {
        MAIN.post(r);
    }
}
