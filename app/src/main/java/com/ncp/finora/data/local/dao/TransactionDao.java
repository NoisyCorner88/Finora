package com.ncp.finora.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ncp.finora.data.local.entity.TransactionEntity;
import com.ncp.finora.data.local.model.CategoryTotal;
import com.ncp.finora.data.local.model.TransactionDetail;

import java.util.List;

@Dao
public interface TransactionDao {
    String DETAIL_SELECT = "SELECT t.*, c.name AS category_name, c.icon AS category_icon, "
            + "c.color AS category_color, p.name AS payment_name "
            + "FROM transactions t "
            + "JOIN categories c ON c.id_category = t.category_id "
            + "JOIN payment_methods p ON p.id_payment_method = t.payment_method_id ";

    @Insert
    long insert(TransactionEntity transaction);

    @Query(DETAIL_SELECT + "WHERE t.user_id = :userId "
            + "ORDER BY t.transaction_date DESC, t.id_transaction DESC LIMIT :limit")
    LiveData<List<TransactionDetail>> observeRecent(int userId, int limit);

    /** type = "ALL" untuk semua jenis transaksi. */
    @Query(DETAIL_SELECT + "WHERE t.user_id = :userId AND (:type = 'ALL' OR t.type = :type) "
            + "ORDER BY t.transaction_date DESC, t.id_transaction DESC")
    LiveData<List<TransactionDetail>> observeByType(int userId, String type);

    /** Semua transaksi (lengkap dengan kategori) pada rentang [from, to). */
    @Query(DETAIL_SELECT + "WHERE t.user_id = :userId AND t.transaction_date >= :from "
            + "AND t.transaction_date < :to "
            + "ORDER BY t.transaction_date DESC, t.id_transaction DESC")
    LiveData<List<TransactionDetail>> observeDetailRange(int userId, String from, String to);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE user_id = :userId AND type = :type "
            + "AND transaction_date >= :from AND transaction_date < :to")
    LiveData<Double> observeTotal(int userId, String type, String from, String to);

    @Query("SELECT c.id_category AS id, c.name AS name, c.icon AS icon, c.color AS color, "
            + "SUM(t.amount) AS total "
            + "FROM transactions t JOIN categories c ON c.id_category = t.category_id "
            + "WHERE t.user_id = :userId AND t.type = :type "
            + "AND t.transaction_date >= :from AND t.transaction_date < :to "
            + "GROUP BY c.id_category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> observeCategoryTotals(int userId, String type, String from, String to);

    @Query("SELECT COUNT(*) FROM transactions WHERE user_id = :userId")
    LiveData<Integer> observeCount(int userId);
}
