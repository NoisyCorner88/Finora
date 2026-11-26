package com.ncp.finora.util;

/** Callback hasil operasi asinkron; selalu dipanggil di main thread. */
public interface Callback<T> {
    void onSuccess(T result);

    void onError(String message);
}
