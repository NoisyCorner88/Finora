package com.ncp.finora.ui.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.ui.activity.ActivityFragment;
import com.ncp.finora.ui.auth.LoginActivity;
import com.ncp.finora.ui.home.HomeFragment;
import com.ncp.finora.ui.profile.ProfileFragment;
import com.ncp.finora.ui.stats.StatsFragment;
import com.ncp.finora.ui.transaction.AddTransactionActivity;
import com.ncp.finora.util.UiUtils;

public class MainActivity extends AppCompatActivity {
    private static final int ACTIVE = Color.parseColor("#1555E8");
    private static final int INACTIVE = Color.parseColor("#737686");

    private MainViewModel viewModel;
    private ImageView iconHome, iconStats, iconActivity, iconProfil;
    private TextView labelHome, labelStats, labelActivity, labelProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sesi hilang (mis. setelah logout / data dihapus) -> kembali ke login.
        if (!FinoraApp.container(this).session.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View bottomNav = findViewById(R.id.bottomNav);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bottomNav.getLayoutParams();
            lp.bottomMargin = UiUtils.dp(this, 16) + bars.bottom;
            bottomNav.setLayoutParams(lp);
            return insets;
        });

        // ViewModel ber-scope Activity, juga dipakai oleh fragment (mis. tombol "Analisis" di Beranda).
        viewModel = new ViewModelProvider(this, FinoraApp.factory(this)).get(MainViewModel.class);

        iconHome = findViewById(R.id.iconHome);
        iconStats = findViewById(R.id.iconSats);
        iconActivity = findViewById(R.id.iconActivity);
        iconProfil = findViewById(R.id.iconProfil);
        labelHome = findViewById(R.id.labelHome);
        labelStats = findViewById(R.id.labelStats);
        labelActivity = findViewById(R.id.labelActivity);
        labelProfil = findViewById(R.id.labelProfil);

        findViewById(R.id.navHome).setOnClickListener(v -> viewModel.select(MainViewModel.TAB_HOME));
        findViewById(R.id.navStats).setOnClickListener(v -> viewModel.select(MainViewModel.TAB_STATS));
        findViewById(R.id.navActivity).setOnClickListener(v -> viewModel.select(MainViewModel.TAB_ACTIVITY));
        findViewById(R.id.navProfil).setOnClickListener(v -> viewModel.select(MainViewModel.TAB_PROFILE));
        findViewById(R.id.navCamera).setOnClickListener(v ->
                startActivity(new Intent(this, com.ncp.finora.ui.scan.ScanReceiptActivity.class)));

        viewModel.getSelectedTab().observe(this, this::showTab);
    }

    private void showTab(int tab) {
        String tag = "tab_" + tab;
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (current == null || !tag.equals(current.getTag())) {
            Fragment fragment;
            switch (tab) {
                case MainViewModel.TAB_STATS: fragment = new StatsFragment(); break;
                case MainViewModel.TAB_ACTIVITY: fragment = new ActivityFragment(); break;
                case MainViewModel.TAB_PROFILE: fragment = new ProfileFragment(); break;
                default: fragment = new HomeFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, tag)
                    .commit();
        }
        tint(iconHome, labelHome, tab == MainViewModel.TAB_HOME);
        tint(iconStats, labelStats, tab == MainViewModel.TAB_STATS);
        tint(iconActivity, labelActivity, tab == MainViewModel.TAB_ACTIVITY);
        tint(iconProfil, labelProfil, tab == MainViewModel.TAB_PROFILE);
    }

    private void tint(ImageView icon, TextView label, boolean active) {
        int color = active ? ACTIVE : INACTIVE;
        ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(color));
        label.setTextColor(color);
    }
}
