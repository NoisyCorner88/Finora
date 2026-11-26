package com.ncp.finora.ui.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ncp.finora.FinoraApp;
import com.ncp.finora.R;
import com.ncp.finora.data.local.model.ParsedReceipt;
import com.ncp.finora.util.Formatters;
import com.ncp.finora.util.ReceiptOcrParser;
import com.ncp.finora.util.UiUtils;

import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanReceiptActivity extends AppCompatActivity {

    private PreviewView viewFinder;
    private ImageView btnFlash;
    private ProgressBar progressScan;
    private LinearLayout layoutLiveDetection;
    private TextView tvLiveMerchant, tvLiveTotal;

    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private boolean isFlashOn = false;
    private volatile boolean isProcessing = false;
    private volatile boolean destroyed = false;
    private long lastAnalysisTime = 0;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Izin kamera diperlukan. Anda masih bisa memilih foto dari galeri.",
                            Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    processImageUri(uri);
                }
            });

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_receipt);

        initViews();
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initViews() {
        viewFinder = findViewById(R.id.viewFinder);
        btnFlash = findViewById(R.id.btnFlash);
        progressScan = findViewById(R.id.progressScan);
        layoutLiveDetection = findViewById(R.id.layoutLiveDetection);
        tvLiveMerchant = findViewById(R.id.tvLiveMerchant);
        tvLiveTotal = findViewById(R.id.tvLiveTotal);

        TextView tvAvatar = findViewById(R.id.tvAvatar);
        String name = FinoraApp.container(this).session.getName();
        tvAvatar.setText(UiUtils.initial(name));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnFlash.setOnClickListener(v -> toggleFlash());

        findViewById(R.id.btnGallery).setOnClickListener(v -> openGallery());

        findViewById(R.id.btnCapture).setOnClickListener(v -> captureAndProcess());

        findViewById(R.id.btnAutoScan).setOnClickListener(v -> captureAndProcess());

        findViewById(R.id.btnSwitchCamera).setOnClickListener(v -> switchCamera());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Gagal menginisialisasi kamera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        try {
            if (!cameraProvider.hasCamera(cameraSelector)) {
                Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            Toast.makeText(this, "Informasi kamera tidak dapat diakses", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kualitas foto diutamakan karena teks struk kecil; resolusi analisis live dinaikkan.
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        ResolutionSelector analysisRes = new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(new Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setResolutionSelector(analysisRes)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeLiveFrame);

        try {
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis);
            isFlashOn = false;
            btnFlash.setAlpha(0.5f);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal menghubungkan kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeLiveFrame(@NonNull ImageProxy imageProxy) {
        long current = System.currentTimeMillis();
        // Analisis tiap 1,2 detik agar CPU tidak terbebani; berhenti saat foto sedang diproses.
        if (destroyed || isProcessing || current - lastAnalysisTime < 1200
                || imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        lastAnalysisTime = current;
        try {
            InputImage inputImage = InputImage.fromMediaImage(
                    imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            textRecognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        if (!destroyed) updateLiveDetection(visionText);
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> imageProxy.close());
        } catch (Exception e) {
            imageProxy.close();
        }
    }

    private void updateLiveDetection(Text text) {
        if (isFinishing() || isDestroyed()) return;
        String raw = text.getText().trim();
        if (raw.length() < 5) {
            runOnUiThread(() -> layoutLiveDetection.setVisibility(View.GONE));
            return;
        }

        ParsedReceipt parsed = ReceiptOcrParser.parse(text, null);
        runOnUiThread(() -> {
            if (parsed.merchantName != null && !ReceiptOcrParser.DEFAULT_MERCHANT.equals(parsed.merchantName)) {
                layoutLiveDetection.setVisibility(View.VISIBLE);
                tvLiveMerchant.setText(parsed.merchantName);
                if (parsed.total > 0) {
                    tvLiveTotal.setText(Formatters.currency(parsed.total) + " ✔");
                } else {
                    tvLiveTotal.setText("Estimasi Total...");
                }
            } else {
                layoutLiveDetection.setVisibility(View.GONE);
            }
        });
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            btnFlash.setImageResource(R.drawable.ic_flash);
            btnFlash.setAlpha(isFlashOn ? 1.0f : 0.5f);
        } else {
            Toast.makeText(this, "Lampu kilat tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchCamera() {
        if (cameraProvider == null) return;
        int targetFacing = (lensFacing == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
        CameraSelector targetSelector = new CameraSelector.Builder()
                .requireLensFacing(targetFacing)
                .build();
        try {
            if (!cameraProvider.hasCamera(targetSelector)) {
                Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            Toast.makeText(this, "Informasi kamera tidak dapat diakses", Toast.LENGTH_SHORT).show();
            return;
        }
        lensFacing = targetFacing;
        bindCameraUseCases();
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private File receiptsDir() {
        File dir = new File(getFilesDir(), "receipts");
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        return dir;
    }

    private void finishProcessing() {
        isProcessing = false;
        progressScan.setVisibility(View.GONE);
    }

    private void captureAndProcess() {
        if (imageCapture == null || isProcessing) return;

        isProcessing = true;
        progressScan.setVisibility(View.VISIBLE);

        // Simpan di penyimpanan internal permanen (cache bisa dihapus sistem kapan saja).
        File photoFile = new File(receiptsDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        recognizeFile(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        finishProcessing();
                        Toast.makeText(ScanReceiptActivity.this,
                                "Gagal mengambil foto: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Gambar dari galeri disalin ke file lokal dulu, supaya tetap bisa dibuka setelah izin URI hilang. */
    private void processImageUri(Uri uri) {
        if (isProcessing) return;
        isProcessing = true;
        progressScan.setVisibility(View.VISIBLE);

        cameraExecutor.execute(() -> {
            File dest = new File(receiptsDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(dest)) {
                if (in == null) throw new IOException("Tidak bisa membuka gambar");
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    finishProcessing();
                    Toast.makeText(this, "Gagal membuka gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                return;
            }
            runOnUiThread(() -> {
                if (!destroyed) recognizeFile(dest);
            });
        });
    }

    private void recognizeFile(File file) {
        try {
            InputImage inputImage = InputImage.fromFilePath(this, Uri.fromFile(file));
            textRecognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        finishProcessing();
                        if (destroyed) return;
                        ParsedReceipt parsed = ReceiptOcrParser.parse(visionText, file.getAbsolutePath());
                        if (visionText.getText().trim().isEmpty()) {
                            Toast.makeText(this, "Teks tidak terbaca. Isi data struk secara manual.",
                                    Toast.LENGTH_LONG).show();
                        }
                        openDetailScreen(parsed);
                    })
                    .addOnFailureListener(e -> {
                        finishProcessing();
                        Toast.makeText(this, "Gagal mengenali teks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            finishProcessing();
            Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openDetailScreen(ParsedReceipt receipt) {
        Intent intent = new Intent(this, ReceiptDetailActivity.class);
        intent.putExtra(ReceiptDetailActivity.EXTRA_PARSED_RECEIPT, receipt);
        detailLauncher.launch(intent);
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}
