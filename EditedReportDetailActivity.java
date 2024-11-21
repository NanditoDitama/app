package com.example.laporan2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditedReportDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String reportId;
    private Map<String, Object> originalData;
    private Map<String, Object> editedData;

    // Deklarasi TextView untuk menampilkan data
    private TextView textViewOriginalTitle, textViewEditedTitle;
    private TextView textViewOriginalDescription, textViewEditedDescription;
    private TextView textViewOriginalAmount, textViewEditedAmount;
    private TextView textViewOriginalDate, textViewEditedDate;
    private ImageView imageViewOriginal, imageViewEdited;
    private Button buttonApprove, buttonReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edited_report_detail);

        // Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();

        // Inisialisasi View
        initializeViews();

        // Dapatkan ID laporan dari intent
        reportId = getIntent().getStringExtra("reportId");

        // Muat detail laporan
        loadReportDetails();
    }

    private void loadReportDetails() {
        db.collection("editedReports").document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Data asli
                        originalData = (Map<String, Object>) documentSnapshot.get("originalData");

                        // Data yang diedit
                        editedData = (Map<String, Object>) documentSnapshot.get("editedData");

                        // Pastikan data tidak null sebelum menampilkan
                        if (originalData != null && editedData != null) {
                            // Set data original dan edit
                            setOriginalData();
                            setEditedData();
                        } else {
                            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Dokumen tidak ditemukan", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat detail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EditedReportDetail", "Error loading report details", e);
                    finish();
                });
    }

    private void initializeViews() {
        // Inisialisasi TextView untuk data original
        textViewOriginalTitle = findViewById(R.id.textViewOriginalTitle);
        textViewOriginalDescription = findViewById(R.id.textViewOriginalDescription);
        textViewOriginalAmount = findViewById(R.id.textViewOriginalAmount);
        textViewOriginalDate = findViewById(R.id.textViewOriginalDate);
        imageViewOriginal = findViewById(R.id.imageViewOriginal);

        // Inisialisasi TextView untuk data yang diedit
        textViewEditedTitle = findViewById(R.id.textViewEditedTitle);
        textViewEditedDescription = findViewById(R.id.textViewEditedDescription);
        textViewEditedAmount = findViewById(R.id.textViewEditedAmount);
        textViewEditedDate = findViewById(R.id.textViewEditedDate);
        imageViewEdited = findViewById(R.id.imageViewEdited);

        // Tombol approve dan reject
        buttonApprove = findViewById(R.id.buttonApprove);
        buttonReject = findViewById(R.id.buttonReject);

        // Set listener untuk tombol
        buttonApprove.setOnClickListener(v -> approveEdit());
        buttonReject.setOnClickListener(v -> rejectEdit());
    }

    private void setOriginalData() {
        // Set judul
        textViewOriginalTitle.setText(safeGetString(originalData, "title"));

        // Set deskripsi
        textViewOriginalDescription.setText(safeGetString(originalData, "description"));

        // Set jumlah Double originalAmount = safeGetDouble(originalData, "amount");
        Double originalAmount = safeGetDouble(originalData, "amount");
        textViewOriginalAmount.setText(originalAmount != null ?
                NumberFormat.getCurrencyInstance().format(originalAmount) : "Rp 0");

        // Set tanggal
        Date originalDate = safeGetDate(originalData, "date");
        textViewOriginalDate.setText(originalDate != null ?
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(originalDate) : "Tidak ada tanggal");

        // Set gambar (jika ada)
        String originalImageUrl = safeGetString(originalData, "imageUrl");
        if (originalImageUrl != null && !originalImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(originalImageUrl)
                    .into(imageViewOriginal);
        }
    }

    private void setEditedData() {
        // Set judul yang diedit
        textViewEditedTitle.setText(safeGetString(editedData, "title"));

        // Set deskripsi yang diedit
        textViewEditedDescription.setText(safeGetString(editedData, "description"));

        // Set jumlah yang diedit
        Double editedAmount = safeGetDouble(editedData, "amount");
        textViewEditedAmount.setText(editedAmount != null ?
                NumberFormat.getCurrencyInstance().format(editedAmount) : "Rp 0");

        // Set tanggal yang diedit
        Date editedDate = safeGetDate(editedData, "date");
        textViewEditedDate.setText(editedDate != null ?
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(editedDate) : "Tidak ada tanggal");

        // Set gambar yang diedit (jika ada)
        String editedImageUrl = safeGetString(editedData, "imageUrl");
        if (editedImageUrl != null && !editedImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(editedImageUrl)
                    .into(imageViewEdited);
        }
    }

    private void approveEdit() {
        db.collection("editedReports").document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String originalReportId = documentSnapshot.getString("originalReportId");
                        Map<String, Object> editedData = (Map<String, Object>) documentSnapshot.get("editedData");

                        // Cek apakah laporan sudah ada di processedReports
                        db.collection("processedReports")
                                .whereEqualTo("reportId", originalReportId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        // Dokumen sudah ada, update dokumen yang sudah ada
                                        DocumentSnapshot existingDoc = queryDocumentSnapshots.getDocuments().get(0);
                                        String existingDocId = existingDoc.getId();

                                        Map<String, Object> updateData = new HashMap<>(editedData);
                                        updateData.put("status", "approved");
                                        updateData.put("timestamp", FieldValue.serverTimestamp());

                                        // Update dokumen di processedReports
                                        db.collection("processedReports")
                                                .document(existingDocId)
                                                .update(updateData)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Update status dan data di koleksi Reports
                                                    updateReportStatusAndData(originalReportId, editedData);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Gagal memperbarui dokumen di processedReports", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Jika dokumen belum ada, buat dokumen baru
                                        createProcessedReport(originalReportId, editedData);
                                    }
                                });
                    }
                });
    }

    private void updateReportStatusAndData(String reportId, Map<String, Object> editedData) {
        // Tambahkan status approved ke data yang akan diupdate
        Map<String, Object> updateData = new HashMap<>(editedData);
        updateData.put("status", "approved");
        updateData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("reports").document(reportId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Kirim notifikasi approval
                    sendApprovalNotification(editedData);

                    // Hapus dokumen editedReports
                    db.collection("editedReports").document(this.reportId)
                            .delete()
                            .addOnSuccessListener(deleteVoid -> {
                                Toast.makeText(this, "Laporan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Gagal menghapus dokumen edit", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui status laporan", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendApprovalNotification(Map<String, Object> processedReportData) {
        // Kirim notifikasi approval
        String userId = (String) processedReportData.get("userId");
        String title = "Pengajuan Edit Disetujui";
        String message = "Pengajuan edit laporan Anda telah disetujui.";

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("isRead", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notification", "Approval notification sent successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Failed to send approval notification", e);
                });
    }

    private void updateReportAndCleanup(String originalReportId, Map<String, Object> editedData) {
        // Update laporan asli
        db.collection("reports")
                .document(originalReportId)
                .update(editedData)
                .addOnSuccessListener(aVoid -> {
                    // Hapus dokumen editedReports
                    db.collection("editedReports")
                            .document(reportId)
                            .delete()
                            .addOnSuccessListener(deleteVoid -> {
                                // Kirim notifikasi approval
                                sendApprovalNotification(editedData);

                                Toast.makeText(this, "Laporan berhasil diapprove", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(deleteE -> {
                                Toast.makeText(this, " Gagal menghapus dokumen edit", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memperbarui data laporan", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectEdit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reject_reason, null);
        EditText editTextReason = dialogView.findViewById(R.id.editTextRejectReason);

        builder.setTitle("Alasan Penolakan")
                .setView(dialogView)
                .setPositiveButton("Tolak", (dialog, which) -> {
                    String rejectReason = editTextReason.getText().toString().trim();

                    if (rejectReason.isEmpty()) {
                        Toast.makeText(this, "Alasan penolakan harus diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("editedReports").document(reportId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String originalReportId = documentSnapshot.getString("originalReportId");
                                    Map<String, Object> editedData = (Map<String, Object>) documentSnapshot.get("editedData");

                                    // Tambahkan status rejected dan alasan penolakan
                                    Map<String, Object> updateData = new HashMap<>(editedData);
                                    updateData.put("status", "rejected");
                                    updateData.put("rejectReason", rejectReason);
                                    updateData.put("timestamp", FieldValue.serverTimestamp());

                                    // Update dokumen di reports
                                    db.collection("reports").document(originalReportId)
                                            .update(updateData)
                                            .addOnSuccessListener(aVoid -> {
                                                // Cek apakah laporan sudah ada di processedReports
                                                db.collection("processedReports")
                                                        .whereEqualTo("reportId", originalReportId)
                                                        .limit(1)
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                                // Dokumen sudah ada, update dokumen yang sudah ada
                                                                DocumentSnapshot existingDoc = queryDocumentSnapshots.getDocuments().get(0);
                                                                String existingDocId = existingDoc.getId();

                                                                Map<String, Object> processedUpdateData = new HashMap<>(updateData);
                                                                processedUpdateData.put("status", "rejected");
                                                                processedUpdateData.put("rejectReason", rejectReason);

                                                                db.collection("processedReports")
                                                                        .document(existingDocId)
                                                                        .update(processedUpdateData)
                                                                        .addOnSuccessListener(processedAVoid -> {
                                                                            // Hapus dokumen editedReports
                                                                            db.collection("editedReports")
                                                                                    .document(reportId)
                                                                                    .delete()
                                                                                    .addOnSuccessListener(deleteVoid -> {
                                                                                        // Kirim notifikasi penolakan
                                                                                        sendRejectionNotification(editedData, rejectReason);

                                                                                        Toast.makeText(this, "Laporan edit ditolak", Toast.LENGTH_SHORT).show();
                                                                                        finish();
                                                                                    })
                                                                                    .addOnFailureListener(deleteE -> {
                                                                                        Toast.makeText(this, "Gagal menghapus dokumen edit", Toast.LENGTH_SHORT).show();
                                                                                    });
                                                                        })
                                                                        .addOnFailureListener(processedE -> {
                                                                            Toast.makeText(this, "Gagal memperbarui dokumen processedReports", Toast.LENGTH_SHORT).show();
                                                                        });
                                                            } else {
                                                                // Jika dokumen belum ada, buat dokumen baru di processedReports
                                                                createRejectedProcessedReport(originalReportId, editedData, rejectReason);
                                                            }
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Gagal memperbarui status laporan", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });
                })
                .setNegativeButton("Batal", null)
                .show();
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


    // Metode utilitas untuk pengambilan data dengan aman
    private String safeGetString(Map<String, Object> data, String key) {
        if (data != null && data.containsKey(key)) {
            Object value = data.get(key);
            return value != null ? value.toString() : "Tidak ada data";
        }
        return "Tidak ada data";
    }

    private Double safeGetDouble(Map<String, Object> data, String key) {
        if (data != null && data.containsKey(key)) {
            Object value = data.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return null;
    }

    private Date safeGetDate(Map<String, Object> data, String key) {
        if (data != null && data.containsKey(key)) {
            Object value = data.get(key);
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toDate();
            } else if (value instanceof Date) {
                return (Date) value;
            }
        }
        return null;
    }


    private void createProcessedReport(String originalReportId, Map<String, Object> editedData) {
        Map<String, Object> processedReportData = new HashMap<>(editedData);
        processedReportData.put("reportId", originalReportId);
        processedReportData.put("status", "approved");
        processedReportData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("processedReports")
                .add(processedReportData)
                .addOnSuccessListener(documentReference -> {
                    updateReportAndCleanup(originalReportId, editedData);
                });
    }

    private void createRejectedProcessedReport(String originalReportId, Map<String, Object> editedData, String rejectReason) {
        Map<String, Object> processedReportData = new HashMap<>(editedData);
        processedReportData.put("reportId", originalReportId);
        processedReportData.put("status", "rejected");
        processedReportData.put("rejectReason", rejectReason);
        processedReportData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("processedReports")
                .add(processedReportData)
                .addOnSuccessListener(documentReference -> {
                    // Kirim notifikasi penolakan
                    sendRejectionNotification(editedData, rejectReason);
                    finish();
                });
    }

    private void sendRejectionNotification(Map<String, Object> editedData, String rejectReason) {
        String userId = (String) editedData.get("userId");
        String title = "Pengajuan Edit Ditolak";
        String message = "Pengajuan edit laporan Anda ditolak. Alasan: " + rejectReason;

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("isRead", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notification", "Rejection notification sent successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Failed to send rejection notification", e);
                });
    }
}