package com.ncp.finora.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this, FinoraApp.factory(this)).get(LoginViewModel.class);

        EditText etUser = findViewById(R.id.etUser);
        EditText etPassword = findViewById(R.id.etPassword);
        CheckBox cbRemember = findViewById(R.id.cbRemember);
        AppCompatButton btnMasuk = findViewById(R.id.btnMasuk);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgot = findViewById(R.id.tvForgot);

        btnMasuk.setOnClickListener(v -> viewModel.login(
                text(etUser.getText()), text(etPassword.getText()), cbRemember.isChecked()));
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Fitur lupa sandi belum tersedia", Toast.LENGTH_SHORT).show());

        viewModel.getEmailError().observe(this, etUser::setError);
        viewModel.getPasswordError().observe(this, etPassword::setError);
        viewModel.getLoading().observe(this, loading -> {
            btnMasuk.setEnabled(!loading);
            btnMasuk.setText(loading ? "Memproses..." : "Masuk Sekarang  →");
        });
        viewModel.getMessage().observe(this, event -> {
            String msg = event.getContentIfNotHandled();
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getLoginSuccess().observe(this, event -> {
            if (event.getContentIfNotHandled() != null) openMain();
        });
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private static String text(Editable e) {
        return e == null ? "" : e.toString();
    }
}
