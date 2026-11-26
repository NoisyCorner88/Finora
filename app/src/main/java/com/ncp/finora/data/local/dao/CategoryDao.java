package com.ncp.finora.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ncp.finora.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insertAll(List<CategoryEntity> categories);

    @Query("SELECT * FROM categories WHERE user_id = :userId AND type = :type ORDER BY id_category")
    LiveData<List<CategoryEntity>> observeByType(int userId, String type);

    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY id_category")
    LiveData<List<CategoryEntity>> observeAll(int userId);
}
