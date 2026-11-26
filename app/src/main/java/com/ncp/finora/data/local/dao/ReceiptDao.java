package com.ncp.finora.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ncp.finora.data.local.entity.ReceiptEntity;
import com.ncp.finora.data.local.entity.ReceiptItemEntity;

import java.util.List;

@Dao
public interface ReceiptDao {
    @Insert
    long insertReceipt(ReceiptEntity receipt);

    @Insert
    void insertReceiptItems(List<ReceiptItemEntity> items);

    @Query("SELECT * FROM receipts WHERE id_receipt = :id LIMIT 1")
    ReceiptEntity getById(int id);

    @Query("SELECT * FROM receipt_items WHERE receipt_id = :receiptId")
    List<ReceiptItemEntity> getItems(int receiptId);

    @Query("SELECT COUNT(*) FROM receipts WHERE user_id = :userId")
    LiveData<Integer> observeScanCount(int userId);

    @Query("SELECT COUNT(*) FROM receipts WHERE user_id = :userId")
    int getScanCountSync(int userId);
}
