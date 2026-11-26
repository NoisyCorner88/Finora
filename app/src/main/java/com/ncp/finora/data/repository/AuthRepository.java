package com.ncp.finora.data.repository;

import androidx.lifecycle.LiveData;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.local.entity.CategoryEntity;
import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.entity.UserEntity;
import com.ncp.finora.util.AppExecutors;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Constants;
import com.ncp.finora.util.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

public class AuthRepository {
    private final AppDatabase db;

    public AuthRepository(AppDatabase db) {
        this.db = db;
    }

    public void login(String identifier, String password, Callback<UserEntity> callback) {
        AppExecutors.io(() -> {
            UserEntity user = db.userDao().findByIdentifier(identifier.trim());
            if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
                AppExecutors.main(() -> callback.onError("Email atau kata sandi salah"));
            } else {
                AppExecutors.main(() -> callback.onSuccess(user));
            }
        });
    }

    public void register(String name, String email, String username, String password,
                         Callback<UserEntity> callback) {
        AppExecutors.io(() -> {
            if (db.userDao().countByEmail(email) > 0) {
                AppExecutors.main(() -> callback.onError("Email sudah terdaftar"));
                return;
            }
            if (db.userDao().countByUsername(username) > 0) {
                AppExecutors.main(() -> callback.onError("Username sudah dipakai"));
                return;
            }
            UserEntity user = new UserEntity();
            user.name = name;
            user.email = email;
            user.username = username;
            user.passwordHash = PasswordHasher.hash(password);
            user.createdAt = System.currentTimeMillis();
            final int id = (int) db.userDao().insert(user);
            user.idUser = id;
            seedDefaults(id);
            AppExecutors.main(() -> callback.onSuccess(user));
        });
    }

    /** Kategori dan sumber dana awal supaya pengguna baru langsung bisa mencatat transaksi. */
    private void seedDefaults(int userId) {
        List<CategoryEntity> categories = new ArrayList<>();
        categories.add(new CategoryEntity(userId, "Makanan", Constants.EXPENSE, "🍔", "#F97316"));
        categories.add(new CategoryEntity(userId, "Transportasi", Constants.EXPENSE, "🚗", "#1555E8"));
        categories.add(new CategoryEntity(userId, "Belanja", Constants.EXPENSE, "🛍", "#8B5CF6"));
        categories.add(new CategoryEntity(userId, "Tagihan", Constants.EXPENSE, "🧾", "#EF4444"));
        categories.add(new CategoryEntity(userId, "Hiburan", Constants.EXPENSE, "🎬", "#EC4899"));
        categories.add(new CategoryEntity(userId, "Kesehatan", Constants.EXPENSE, "💊", "#14B8A6"));
        categories.add(new CategoryEntity(userId, "Lainnya", Constants.EXPENSE, "📦", "#64748B"));
        categories.add(new CategoryEntity(userId, "Gaji", Constants.INCOME, "💵", "#00A879"));
        categories.add(new CategoryEntity(userId, "Bonus", Constants.INCOME, "🎁", "#22C55E"));
        categories.add(new CategoryEntity(userId, "Investasi", Constants.INCOME, "📈", "#0EA5E9"));
        categories.add(new CategoryEntity(userId, "Lainnya", Constants.INCOME, "💰", "#64748B"));
        db.categoryDao().insertAll(categories);
        db.paymentMethodDao().insert(new PaymentMethodEntity(userId, "UWG Pay", Constants.PM_CARD, 0, "---", "#1555E8"));
        db.paymentMethodDao().insert(new PaymentMethodEntity(userId, "SeaBank", Constants.PM_EWALLET, 0, "---", "#F97316"));
        db.paymentMethodDao().insert(new PaymentMethodEntity(userId, "Cash", Constants.PM_CASH, 0, "---", "#00A879"));
    }

    public LiveData<UserEntity> observeUser(int userId) {
        return db.userDao().observeById(userId);
    }

    public void updateProfile(int userId, String name, String email, Callback<Boolean> callback) {
        AppExecutors.io(() -> {
            try {
                db.userDao().updateProfile(userId, name.trim(), email.trim());
                AppExecutors.main(() -> callback.onSuccess(true));
            } catch (Exception e) {
                AppExecutors.main(() -> callback.onError("Gagal memperbarui profil"));
            }
        });
    }

    public void changePassword(int userId, String oldPassword, String newPassword, Callback<Boolean> callback) {
        AppExecutors.io(() -> {
            try {
                UserEntity user = db.userDao().getByIdSync(userId);
                if (user == null || !PasswordHasher.verify(oldPassword, user.passwordHash)) {
                    AppExecutors.main(() -> callback.onError("Kata sandi lama tidak cocok"));
                    return;
                }
                db.userDao().updatePassword(userId, PasswordHasher.hash(newPassword));
                AppExecutors.main(() -> callback.onSuccess(true));
            } catch (Exception e) {
                AppExecutors.main(() -> callback.onError("Gagal mengubah kata sandi"));
            }
        });
    }

    public void deleteAccount(int userId, String password, Callback<Boolean> callback) {
        AppExecutors.io(() -> {
            try {
                UserEntity user = db.userDao().getByIdSync(userId);
                if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
                    AppExecutors.main(() -> callback.onError("Kata sandi salah"));
                    return;
                }
                db.userDao().deleteUser(userId);
                AppExecutors.main(() -> callback.onSuccess(true));
            } catch (Exception e) {
                AppExecutors.main(() -> callback.onError("Gagal menghapus akun"));
            }
        });
    }
}
