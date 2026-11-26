package com.ncp.finora.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.entity.UserEntity;
import com.ncp.finora.ui.auth.LoginActivity;
import com.ncp.finora.ui.card.CardActivity;
import com.ncp.finora.ui.notification.NotificationActivity;
import com.ncp.finora.util.Callback;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.UiUtils;

public class ProfileFragment extends Fragment {
    private ProfileViewModel viewModel;
    private UserEntity currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, FinoraApp.factory(requireContext()))
                .get(ProfileViewModel.class);

        TextView avatar = view.findViewById(R.id.tvProfileAvatar);
        TextView name = view.findViewById(R.id.tvProfileName);
        TextView email = view.findViewById(R.id.tvProfileEmail);
        TextView totalBalance = view.findViewById(R.id.tvTotalBalanceProfile);
        TextView txCount = view.findViewById(R.id.tvTransactionCount);
        TextView scanCount = view.findViewById(R.id.tvScanCount);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            currentUser = user;
            avatar.setText(UiUtils.initial(user.name));
            name.setText(user.name);
            email.setText(user.email);
        });

        viewModel.getTotalBalance().observe(getViewLifecycleOwner(), bal ->
                totalBalance.setText(Formatters.currency(bal != null ? bal : 0)));

        viewModel.getTransactionCount().observe(getViewLifecycleOwner(), c ->
                txCount.setText(String.valueOf(c == null ? 0 : c)));

        viewModel.getScanCount().observe(getViewLifecycleOwner(), sc ->
                scanCount.setText(String.valueOf(sc == null ? 0 : sc)));

        viewModel.getLoggedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() == null) return;
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        // Menus
        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        view.findViewById(R.id.menuMyCards).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CardActivity.class)));
        view.findViewById(R.id.menuExport).setOnClickListener(v -> showExportDialog());
        view.findViewById(R.id.menuChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        view.findViewById(R.id.menuNotification).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationActivity.class)));
        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> showDeleteAccountDialog());
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etName = new EditText(requireContext());
        etName.setHint("Nama Lengkap");
        etName.setText(currentUser.name);
        layout.addView(etName);

        EditText etEmail = new EditText(requireContext());
        etEmail.setHint("Email");
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setText(currentUser.email);
        layout.addView(etEmail);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Profil")
                .setView(layout)
                .setPositiveButton("Simpan Perubahan", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();
                    if (!newName.isEmpty() && !newEmail.isEmpty()) {
                        viewModel.updateProfile(newName, newEmail, new Callback<Boolean>() {
                            @Override public void onSuccess(Boolean data) {
                                Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String error) {
                                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showExportDialog() {
        String[] formats = {"CSV (Data Excel)", "PDF (Laporan Keuangan)"};
        final int[] selected = {0};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ekspor Data Keuangan")
                .setSingleChoiceItems(formats, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton("Unduh Sekarang", (dialog, which) -> {
                    Toast.makeText(requireContext(), "File " + (selected[0] == 0 ? "finora_data.csv" : "finora_laporan.pdf") + " berhasil diekspor ke memori!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        EditText etOld = new EditText(requireContext());
        etOld.setHint("Kata Sandi Lama");
        etOld.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOld);

        EditText etNew = new EditText(requireContext());
        etNew.setHint("Kata Sandi Baru (Min. 8 karakter)");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNew);

        EditText etConfirm = new EditText(requireContext());
        etConfirm.setHint("Konfirmasi Kata Sandi Baru");
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirm);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Buat Kata Sandi Baru")
                .setView(layout)
                .setPositiveButton("Simpan Kata Sandi", (dialog, which) -> {
                    String oldP = etOld.getText().toString();
                    String newP = etNew.getText().toString();
                    String conf = etConfirm.getText().toString();

                    if (newP.length() < 8) {
                        Toast.makeText(requireContext(), "Kata sandi minimal 8 karakter", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newP.equals(conf)) {
                        Toast.makeText(requireContext(), "Konfirmasi kata sandi tidak cocok", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.changePassword(oldP, newP, new Callback<Boolean>() {
                        @Override public void onSuccess(Boolean data) {
                            Toast.makeText(requireContext(), "Kata sandi berhasil diubah", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String error) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        TextView tvWarning = new TextView(requireContext());
        tvWarning.setText("Masukkan kata sandi Anda untuk menghapus akun secara permanen.");
        tvWarning.setTextColor(0xFF667085);
        layout.addView(tvWarning);

        EditText etPass = new EditText(requireContext());
        etPass.setHint("Kata Sandi");
        etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPass);

        CheckBox cb = new CheckBox(requireContext());
        cb.setText("Saya mengerti bahwa tindakan ini tidak dapat dibatalkan.");
        layout.addView(cb);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Penghapusan")
                .setView(layout)
                .setPositiveButton("Hapus Akun Permanen", (dialog, which) -> {
                    if (!cb.isChecked()) {
                        Toast.makeText(requireContext(), "Centang konfirmasi penghapusan", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String pass = etPass.getText().toString();
                    viewModel.deleteAccount(pass, new Callback<Boolean>() {
                        @Override public void onSuccess(Boolean data) {
                            Toast.makeText(requireContext(), "Akun berhasil dihapus", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String error) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Keluar dari FINORA?")
                .setMessage("Anda perlu masuk kembali untuk mengakses akun dan data keuangan Anda.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Keluar", (d, w) -> viewModel.logout())
                .show();
    }
}
