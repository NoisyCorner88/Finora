package com.ncp.finora.data.repository;

import androidx.lifecycle.LiveData;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.local.entity.CategoryEntity;

import java.util.List;

public class CategoryRepository {
    private final AppDatabase db;

    public CategoryRepository(AppDatabase db) {
        this.db = db;
    }

    public LiveData<List<CategoryEntity>> byType(int userId, String type) {
        return db.categoryDao().observeByType(userId, type);
    }

    public LiveData<List<CategoryEntity>> all(int userId) {
        return db.categoryDao().observeAll(userId);
    }
}
