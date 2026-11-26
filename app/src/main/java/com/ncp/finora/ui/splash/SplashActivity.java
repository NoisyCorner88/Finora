package com.ncp.finora.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.ui.auth.LoginActivity;
import com.ncp.finora.ui.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spalsh_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SplashViewModel viewModel = new ViewModelProvider(this, FinoraApp.factory(this))
                .get(SplashViewModel.class);
        ProgressBar progressBar = findViewById(R.id.progressBarHorizontal);

        viewModel.getProgress().observe(this, progressBar::setProgress);
        viewModel.getNavigate().observe(this, event -> {
            Boolean goHome = event.getContentIfNotHandled();
            if (goHome == null) return;
            startActivity(new Intent(this, goHome ? MainActivity.class : LoginActivity.class));
            finish();
        });
    }
}
