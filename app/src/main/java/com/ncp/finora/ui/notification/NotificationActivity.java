package com.ncp.finora.ui.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.NotificationEntity;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private NotificationAdapter adapter;
    private TextView tvEmptyNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvEmptyNotif = findViewById(R.id.tvEmptyNotif);

        RecyclerView rv = findViewById(R.id.rvNotifications);
        adapter = new NotificationAdapter();
        rv.setAdapter(adapter);

        int userId = FinoraApp.container(this).session.getUserId();
        FinoraApp.container(this).notificationRepository.observeAll(userId).observe(this, list -> {
            boolean empty = list == null || list.isEmpty();
            tvEmptyNotif.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            adapter.submitList(list);
        });

        // Mark all as read when opening notification center
        FinoraApp.container(this).notificationRepository.markAllRead(userId);
    }
}
