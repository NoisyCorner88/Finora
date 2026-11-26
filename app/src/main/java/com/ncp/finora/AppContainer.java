package com.ncp.finora;

import android.content.Context;

import com.ncp.finora.data.local.AppDatabase;
import com.ncp.finora.data.repository.AuthRepository;
import com.ncp.finora.data.repository.CategoryRepository;
import com.ncp.finora.data.repository.NotificationRepository;
import com.ncp.finora.data.repository.PaymentMethodRepository;
import com.ncp.finora.data.repository.ReceiptRepository;
import com.ncp.finora.data.repository.TransactionRepository;
import com.ncp.finora.data.session.SessionManager;

/** Penyedia dependensi sederhana (pengganti DI framework). */
public class AppContainer {
    public final SessionManager session;
    public final AuthRepository authRepository;
    public final TransactionRepository transactionRepository;
    public final PaymentMethodRepository paymentMethodRepository;
    public final CategoryRepository categoryRepository;
    public final ReceiptRepository receiptRepository;
    public final NotificationRepository notificationRepository;

    public AppContainer(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        session = new SessionManager(context);
        authRepository = new AuthRepository(db);
        transactionRepository = new TransactionRepository(db);
        paymentMethodRepository = new PaymentMethodRepository(db);
        categoryRepository = new CategoryRepository(db);
        receiptRepository = new ReceiptRepository(db);
        notificationRepository = new NotificationRepository(db);
    }
}
