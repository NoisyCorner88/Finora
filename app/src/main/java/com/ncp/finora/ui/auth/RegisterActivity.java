package com.ncp.finora.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.ui.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RegisterViewModel viewModel = new ViewModelProvider(this, FinoraApp.factory(this))
                .get(RegisterViewModel.class);

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirm = findViewById(R.id.etConfirm);
        AppCompatButton btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> viewModel.register(
                text(etName.getText()), text(etEmail.getText()), text(etUsername.getText()),
                text(etPassword.getText()), text(etConfirm.getText())));
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());

        bindError(viewModel.getNameError(), R.id.layoutName);
        bindError(viewModel.getEmailError(), R.id.layoutEmail);
        bindError(viewModel.getUsernameError(), R.id.layoutUsername);
        bindError(viewModel.getPasswordError(), R.id.layoutPassword);
        bindError(viewModel.getConfirmError(), R.id.layoutConfirm);

        viewModel.getLoading().observe(this, loading -> {
            btnRegister.setEnabled(!loading);
            btnRegister.setText(loading ? "Memproses..." : "Daftar Sekarang");
        });
        viewModel.getMessage().observe(this, event -> {
            String msg = event.getContentIfNotHandled();
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
        viewModel.getRegistered().observe(this, event -> {
            if (event.getContentIfNotHandled() == null) return;
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void bindError(androidx.lifecycle.LiveData<String> source, int layoutId) {
        TextInputLayout layout = findViewById(layoutId);
        source.observe(this, layout::setError);
    }

    private static String text(Editable e) {
        return e == null ? "" : e.toString();
    }
}
