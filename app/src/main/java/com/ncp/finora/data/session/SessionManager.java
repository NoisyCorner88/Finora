package com.ncp.finora.data.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "finora_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_REMEMBER = "remember";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(int userId, String name, boolean remember) {
        prefs.edit().putInt(KEY_USER_ID, userId).putString(KEY_NAME, name)
                .putBoolean(KEY_REMEMBER, remember).apply();
    }

    public void saveUser(int userId, String name, String email, String username) {
        prefs.edit().putInt(KEY_USER_ID, userId).putString(KEY_NAME, name).apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getUsername() {
        return prefs.getString(KEY_NAME, "");
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    /** True bila ada sesi tersimpan dan pengguna memilih "Ingat Saya". */
    public boolean shouldAutoLogin() {
        return isLoggedIn() && prefs.getBoolean(KEY_REMEMBER, false);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
