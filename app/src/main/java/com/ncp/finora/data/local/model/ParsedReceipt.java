package com.ncp.finora.data.local.model;

import com.ncp.finora.data.local.entity.ReceiptItemEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParsedReceipt implements Serializable {
    public String merchantName = "Toko / Merchant";
    public String transactionDate = "";
    public String formattedDateTime = "";
    public double subtotal = 0;
    public double discount = 0;
    public double tax = 0;
    public double total = 0;
    public String categoryName = "Belanja";
    public String imagePath = "";
    public String rawText = "";
    public List<ReceiptItemEntity> items = new ArrayList<>();
}
