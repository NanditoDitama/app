package com.example.laporan2;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProcessedReportDetailActivity extends AppCompatActivity {
    private TextView textViewTitle, textViewSender, textViewDate,
            textViewAmount, textViewDescription,
            textViewStatus, textViewRejectReason;
    private ImageView imageViewReport;
    private FirebaseFirestore db;
    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processed_report_detail);

        // Inisialisasi Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Inisialisasi views
        initializeViews();

        // Ambil reportId dari intent
        reportId = getIntent().getStringExtra("reportId");

        if (reportId != null) {
            // Muat detail laporan dari koleksi processedReports
            loadProcessedReportDetails();
        } else {
            Toast.makeText(this, "ID Laporan tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewSender = findViewById(R.id.textViewSender);
        textViewDate = findViewById(R.id.textViewDate);
        textViewAmount = findViewById(R.id.textViewAmount);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewRejectReason = findViewById(R.id.textViewRejectReason);
        imageViewReport = findViewById(R.id.imageViewReport);
    }

    @SuppressLint("SetTextI18n")
    private void loadProcessedReportDetails() {
        db.collection("processedReports")
                .document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Ambil data laporan
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        Double amount = documentSnapshot.getDouble("amount");
                        Date date = documentSnapshot.getDate("date");
                        String status = documentSnapshot.getString("status");
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        String rejectReason = documentSnapshot.getString("rejectReason");
                        String senderName = documentSnapshot.getString("senderName");

                        // Set data ke views
                        textViewTitle.setText(title);
                        textViewDescription.setText(description);

                        // Format jumlah dengan mata uang
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        textViewAmount.setText(currencyFormat.format(amount != null ? amount : 0));

                        // Format tanggal
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                        textViewDate.setText(date != null ? dateFormat.format(date) : "Tanggal tidak tersedia");

                        // Set status
                        textViewStatus.setText("Status: " + (status != null ? status.toUpperCase() : "Tidak diketahui"));
                        textViewStatus.setTextColor(getStatusColor(status));

                        // Set sender name
                        textViewSender.setText("Pengirim: " + (senderName != null ? senderName : "Tidak diketahui"));

                        // Tampilkan alasan penolakan jika status ditolak
                        if ("rejected".equals(status)) {
                            textViewRejectReason.setVisibility(View.VISIBLE);
                            textViewRejectReason.setText("Alasan Penolakan: " +
                                    (rejectReason != null ? rejectReason : "Tidak ada alasan"));
                        } else {
                            textViewRejectReason.setVisibility(View.GONE);
                        }

                        // Muat gambar jika tersedia
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(imageViewReport);
                            imageViewReport.setVisibility(View.VISIBLE);
                        } else {
                            imageViewReport.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Laporan tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat detail laporan: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // Metode untuk mendapatkan warna status
    private int getStatusColor(String status) {
        if (status == null) return Color.GRAY;

        switch (status) {
            case "approved":
                return ContextCompat.getColor(this, R.color.green);
            case "rejected":
                return ContextCompat.getColor(this, R.color.red);
            default:
                return Color.GRAY;
        }
    }
}
