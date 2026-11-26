package com.ncp.finora.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ncp.finora.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert
    long insert(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :identifier OR username = :identifier LIMIT 1")
    UserEntity findByIdentifier(String identifier);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);

    @Query("SELECT * FROM users WHERE id_user = :id LIMIT 1")
    LiveData<UserEntity> observeById(int id);

    @Query("SELECT * FROM users WHERE id_user = :id LIMIT 1")
    UserEntity getByIdSync(int id);

    @Query("UPDATE users SET name = :name, email = :email WHERE id_user = :userId")
    int updateProfile(int userId, String name, String email);

    @Query("UPDATE users SET password_hash = :hash WHERE id_user = :userId")
    int updatePassword(int userId, String hash);

    @Query("DELETE FROM users WHERE id_user = :userId")
    int deleteUser(int userId);
}
