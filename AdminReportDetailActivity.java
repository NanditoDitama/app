package com.example.laporan2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AdminReportDetailActivity extends AppCompatActivity {
    private TextView textViewTitle, textViewDescription, textViewSender,
            textViewDate, textViewAmount, textViewStatus;
    private ImageView imageViewReport;
    private Button buttonApprove, buttonReject;
    private FirebaseFirestore db;
    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_report_detail);

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();

        // Inisialisasi View
        initializeViews();

        // Ambil reportId dari Intent
        reportId = getIntent().getStringExtra("reportId");

        // Muat detail laporan
        loadReportDetails();
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewSender = findViewById(R.id.textViewSender);
        textViewDate = findViewById(R.id.textViewDate);
        textViewAmount = findViewById(R.id.textViewAmount);
        textViewStatus = findViewById(R.id.textViewStatus);
        imageViewReport = findViewById(R.id.imageViewReport);
        buttonApprove = findViewById(R.id.buttonApprove);
        buttonReject = findViewById(R.id.buttonReject);

        // Set up listener untuk tombol approve dan reject
        buttonApprove.setOnClickListener(v -> approveReport());
        buttonReject.setOnClickListener(v -> rejectReport());
    }

    private void loadReportDetails() {
        db.collection("reports").document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Report report = documentSnapshot.toObject(Report.class);

                        if (report != null) {
                            // Set detail laporan
                            textViewTitle.setText(report.getTitle());
                            textViewDescription.setText(report.getDescription());

                            // Format amount
                            DecimalFormat formatter = new DecimalFormat("#,###");
                            textViewAmount.setText("Rp " + formatter.format(report.getAmount()));

                            // Format tanggal
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                            textViewDate.setText(dateFormat.format(report.getDate()));

                            // Tampilkan gambar jika ada
                            if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(report.getImageUrl())
                                        .into(imageViewReport);
                            }

                            // Ambil nama pengirim
                            db.collection("users").document(report.getUserId())
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String senderName = userDoc.getString("name");
                                            textViewSender.setText("Pengirim: " + senderName);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat detail laporan", Toast.LENGTH_SHORT).show();
                });
    }

    private void approveReport() {
        db.collection("reports").document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Report report = documentSnapshot.toObject(Report.class);

                        if (report != null) {
                            // Cari dokumen di processedReports
                            db.collection("processedReports")
                                    .whereEqualTo("reportId", reportId)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            // Dokumen sudah ada, update existing document
                                            DocumentSnapshot processedReportDoc = queryDocumentSnapshots.getDocuments().get(0);
                                            String processedReportDocId = processedReportDoc.getId();

                                            // Siapkan data update
                                            Map<String, Object> updateData = new HashMap<>();
                                            updateData.put("status", "approved");
                                            updateData.put("timestamp", FieldValue.serverTimestamp());

                                            // Update dokumen processedReports
                                            db.collection("processedReports")
                                                    .document(processedReportDocId)
                                                    .update(updateData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Update status di reports
                                                        updateReportStatus(reportId, "approved");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(this, "Gagal memperbarui status laporan", Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            // Jika tidak ada dokumen, buat dokumen baru di processedReports
                                            createProcessedReport(report, "approved");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Gagal mencari dokumen", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mengambil detail laporan", Toast.LENGTH_SHORT).show();
                });
    }


    private void createProcessedReport(Report report, String status) {
        Map<String, Object> processedReportData = new HashMap<>();
        processedReportData.put("reportId", report.getId());
        processedReportData.put("status", status);
        processedReportData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("processedReports")
                .add(processedReportData)
                .addOnSuccessListener(documentReference -> {
                    // Update status di reports setelah dokumen processedReports dibuat
                    updateReportStatus(report.getId(), status);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal membuat dokumen processed report", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateReportStatus(String reportId, String status) {
        db.collection("reports")
                .document(reportId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Laporan berhasil disetujui", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup aktivitas setelah berhasil
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui status laporan", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectReport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reject_reason, null);
        EditText editTextReason = dialogView.findViewById(R.id.editTextRejectReason);

        builder.setTitle("Alasan Penolakan")
                .setView(dialogView)
                .setPositiveButton("Tolak", null) // Set null untuk menunda penanganan tombol
                .setNegativeButton("Batal", null);

        // Buat dialog
        AlertDialog dialog = builder.create();

        // Set listener untuk tombol positif
        dialog.setOnShowListener(dialogInterface -> {
            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonPositive.setOnClickListener(v -> {
                String rejectReason = editTextReason.getText().toString().trim();

                if (rejectReason.isEmpty()) {
                    Toast.makeText(this, "Alasan penolakan harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update status di Firestore untuk koleksi reports
                db.collection("reports").document(reportId)
                        .update("status", "rejected", "rejectReason", rejectReason) // Update status dan alasan penolakan
                        .addOnSuccessListener(aVoid -> {
                            // Jika ada processedReportId, update juga di processedReports
                            db.collection("reports").document(reportId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String processedReportId = documentSnapshot.getString("processedReportId");
                                            if (processedReportId != null) {
                                                updateProcessedReport(processedReportId, "rejected", rejectReason);
                                            } else {
                                                Toast.makeText(this, "Dokumen tidak ditemukan", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            Toast.makeText(this, "Laporan berhasil ditolak", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Tutup dialog setelah berhasil
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Gagal memperbarui status laporan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        });

        dialog.show(); // Tampilkan dialog
    }

    private void updateProcessedReport(String processedReportDocId, String status, String rejectReason) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", status);
        if (rejectReason != null) {
            updateData.put("rejectReason", rejectReason);
        }
        updateData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("processedReports")
                .document(processedReportDocId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status laporan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish(); // Kembali setelah berhasil
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui status laporan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}