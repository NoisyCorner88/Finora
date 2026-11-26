package com.ncp.finora.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ncp.finora.data.local.entity.PaymentMethodEntity;
import com.ncp.finora.data.local.model.PaymentMethodBalance;

import java.util.List;

@Dao
public interface PaymentMethodDao {
    @Insert
    long insert(PaymentMethodEntity method);

    @Query("SELECT * FROM payment_methods WHERE user_id = :userId AND is_active = 1 ORDER BY id_payment_method")
    LiveData<List<PaymentMethodEntity>> observeActive(int userId);

    @Query("SELECT pm.id_payment_method AS id, pm.name AS name, pm.type AS type, "
            + "pm.color AS color, pm.account_number AS account_number, pm.initial_balance AS initial_balance, "
            + "pm.initial_balance + COALESCE(SUM(CASE t.type WHEN 'INCOME' THEN t.amount "
            + "WHEN 'EXPENSE' THEN -t.amount ELSE 0 END), 0) AS balance "
            + "FROM payment_methods pm "
            + "LEFT JOIN transactions t ON t.payment_method_id = pm.id_payment_method "
            + "WHERE pm.user_id = :userId AND pm.is_active = 1 "
            + "GROUP BY pm.id_payment_method ORDER BY pm.id_payment_method")
    LiveData<List<PaymentMethodBalance>> observeBalances(int userId);

    @Query("UPDATE payment_methods SET name = :name, color = :color, initial_balance = :balance, account_number = :accNum WHERE id_payment_method = :id")
    int updateDetails(int id, String name, String color, double balance, String accNum);

    @Query("UPDATE payment_methods SET is_active = 0 WHERE id_payment_method = :id")
    int softDelete(int id);
}
