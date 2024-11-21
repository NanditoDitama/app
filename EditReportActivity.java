package com.example.laporan2;

import static android.app.ProgressDialog.show;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditReportActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextDescription, editTextAmount;
    private Button buttonUpdateReport, buttonSelectImage;
    private ImageView imageViewPreview;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    private Calendar selectedDate;
    private String reportId;
    private Report currentReport;
    private TextView buttonSelectDate;
    private ProgressBar progressBar;
    private ImageView openEnlargedImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewEnlarged;
    private ImageView closeEnlargedImage;
    private Matrix matrix = new Matrix();
    private Matrix initialMatrix;
    private float scale;
    private float initialScale;
    private ScaleGestureDetector scaleGestureDetector;
    private int mode = NONE;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private FrameLayout enlargedImageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);

        reportId = getIntent().getStringExtra("reportId");
        if (reportId == null || reportId.isEmpty()) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextAmount = findViewById(R.id.editTextAmount);
        buttonUpdateReport = findViewById(R.id.buttonUpdate);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        enlargedImageContainer = findViewById(R.id.enlargedImageContainer);
        closeEnlargedImage = findViewById(R.id.closeEnlargedImage);
        openEnlargedImage = findViewById(R.id.openEnlargedImage);
        selectedDate = Calendar.getInstance();
        imageViewEnlarged = findViewById(R.id.imageViewEnlarged);
        initializeFirebase();
        initializeViews();
        setupZoomFeature();
        loadReportData();
        setupImageClickListeners();
        progressBar = findViewById(R.id.progressBar);
        buttonSelectDate.setOnClickListener(v -> showDatePicker());
        buttonSelectImage.setOnClickListener(v -> selectImage());
        buttonUpdateReport.setOnClickListener(v -> updateReport());
    }

    private void loadReportData() {
        db.collection("reports").document(reportId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentReport = task.getResult().toObject(Report.class);
                if (currentReport != null) {
                    editTextTitle.setText(currentReport.getTitle());
                    editTextDescription.setText(currentReport.getDescription());
                    editTextAmount.setText(String.valueOf(currentReport.getAmount()));

                    if (currentReport.getDate() != null) {
                        selectedDate.setTime(currentReport.getDate());
                        updateDateButtonText();
                    }

                    if (currentReport.getImageUrl() != null && !currentReport.getImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(currentReport.getImageUrl())
                                .into(imageViewPreview);
                        openEnlargedImage.setVisibility(View.VISIBLE); // Tampilkan icon zoom
                        setupImageClickListeners();
                        setupZoomFeature();
                    }
                } else {
                    Toast.makeText(this, "Report data is null", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to load report data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateDateButtonText() {
        @SuppressLint("DefaultLocale") String dateString = String.format("%d/%d/%d",
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.YEAR));
        buttonSelectDate.setText(dateString);
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateDateButtonText();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imageViewPreview.setImageURI(selectedImageUri);
            openEnlargedImage.setVisibility(View.VISIBLE); // Tampilkan icon zoom
            setupImageClickListeners();
            setupZoomFeature();
        }
    }
    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        openEnlargedImage = findViewById(R.id.openEnlargedImage);
        openEnlargedImage.setVisibility(View.GONE); // Set awalnya tidak terlihat
    }


    private void initializeViews() {
        enlargedImageContainer = findViewById(R.id.enlargedImageContainer);
        imageViewEnlarged = findViewById(R.id.imageViewEnlarged);
        closeEnlargedImage = findViewById(R.id.closeEnlargedImage);
        openEnlargedImage = findViewById(R.id.openEnlargedImage);
        matrix = new Matrix();
        initialMatrix = new Matrix();
    }



    private void updateReport() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tambahkan dialog pilihan
        new AlertDialog.Builder(this)
                .setTitle("Update Report")
                .setMessage("Pilih metode update:")
                .setPositiveButton("Kirim ke Admin", (dialog, which) -> {
                    // Jika memilih kirim ke admin
                    if (selectedImageUri != null) {
                        uploadImageForAdminEdit(title, description, amount);
                    } else {
                        sendEditRequestToAdmin();
                    }
                })
                .setNeutralButton("Batal", null)
                .show();
    }

    private void uploadImageForAdminEdit(String title, String description, double amount) {
        // Tampilkan progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonUpdateReport.setEnabled(false);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Kompres gambar dengan kualitas 70%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            // Buat referensi storage dengan nama unik
            StorageReference storageRef = storage.getReference().child("report_images/" + UUID.randomUUID().toString());

            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Dapatkan URL gambar baru
                            String imageUrl = uri.toString();

                            // Pastikan pengguna terotentikasi
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser == null) {
                                progressBar.setVisibility(View.GONE);
                                buttonUpdateReport.setEnabled(true);
                                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Siapkan data asli
                            Map<String, Object> originalData = new HashMap<>();
                            originalData.put("title", currentReport.getTitle());
                            originalData.put("description", currentReport.getDescription());
                            originalData.put("amount", currentReport.getAmount());
                            originalData.put("date", currentReport.getDate());
                            originalData.put("imageUrl", currentReport.getImageUrl());

                            // Siapkan data yang diedit
                            Map<String, Object> editedData = new HashMap<>();
                            editedData.put("title", title);
                            editedData.put("description", description);
                            editedData.put("amount", amount);
                            editedData.put("date", selectedDate.getTime());
                            editedData.put("imageUrl", imageUrl);

                            // Update status laporan asli menjadi pending
                            db.collection("reports").document(reportId)
                                    .update("status", "pending")
                                    .addOnSuccessListener(statusUpdateVoid -> {
                                        // Buat dokumen baru di koleksi editedReports
                                        Map<String, Object> editRequest = new HashMap<>();
                                        editRequest.put("userId", currentUser.getUid());
                                        editRequest.put("originalReportId", reportId);
                                        editRequest.put("originalData", originalData);
                                        editRequest.put("editedData", editedData);
                                        editRequest.put("editStatus", "pending");
                                        editRequest.put("timestamp", FieldValue.serverTimestamp());

                                        db.collection("editedReports")
                                                .add(editRequest)
                                                .addOnSuccessListener(documentReference -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    buttonUpdateReport.setEnabled(true);
                                                    Toast.makeText(this, "Edit request sent to admin", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    buttonUpdateReport.setEnabled(true);
                                                    Toast.makeText(this, "Failed to send edit request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        buttonUpdateReport.setEnabled(true);
                                        Toast.makeText(this, "Gagal memperbarui status laporan", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        buttonUpdateReport.setEnabled(true);
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            buttonUpdateReport.setEnabled(true);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadImage(String title, String description, double amount, Timestamp date) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            StorageReference storageRef = storage.getReference().child("report_images/" + UUID.randomUUID().toString());
            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            updateReportInFirestore(title, description, amount, date, imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        buttonUpdateReport.setEnabled(true);
                        Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateReportInFirestore(String title, String description, double amount, Timestamp date, String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Menambahkan null sebagai recipientId karena ini update laporan normal
        Report updatedReport = new Report(
                reportId,
                title,
                description,
                amount,
                date.toDate(),
                imageUrl,
                currentUser.getUid(),
                null  // recipientId
        );

        db.collection("reports").document(reportId)
                .set(updatedReport)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    buttonUpdateReport.setEnabled(true);
                    Toast.makeText(this, "Report updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedReport", updatedReport);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonUpdateReport.setEnabled(true);
                    Toast.makeText(this, "Error updating report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }





    private void setupImageClickListeners() {
        Uri imageUri = selectedImageUri != null ? selectedImageUri :
                (currentReport != null && currentReport.getImageUrl() != null ?
                        Uri.parse(currentReport.getImageUrl()) : null);

        if (imageUri != null) {
            openEnlargedImage.setOnClickListener(v -> {
                enlargedImageContainer.setVisibility(View.VISIBLE);
                imageViewEnlarged.setScaleType(ImageView.ScaleType.MATRIX);

                Glide.with(this)
                        .load(imageUri)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable drawable,
                                                        @Nullable Transition<? super Drawable> transition) {
                                float imageWidth = drawable.getIntrinsicWidth();
                                float imageHeight = drawable.getIntrinsicHeight();
                                float containerWidth = enlargedImageContainer.getWidth();
                                float containerHeight = enlargedImageContainer.getHeight();

                                float scaleX = containerWidth / imageWidth;
                                float scaleY = containerHeight / imageHeight;
                                initialScale = Math.min(scaleX, scaleY);
                                scale = initialScale;

                                float scaledImageWidth = imageWidth * initialScale;
                                float scaledImageHeight = imageHeight * initialScale;
                                float translateX = (containerWidth - scaledImageWidth) / 2f;
                                float translateY = (containerHeight - scaledImageHeight) / 2f;

                                matrix.reset();
                                matrix.postScale(initialScale, initialScale);
                                matrix.postTranslate(translateX, translateY);

                                imageViewEnlarged.setImageDrawable(drawable);
                                imageViewEnlarged.setImageMatrix(matrix);

                                initialMatrix.set(matrix);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                        });
            });

            closeEnlargedImage.setOnClickListener(v -> {
                enlargedImageContainer.setVisibility(View.GONE);
                resetZoom();
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupZoomFeature() {
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float newScale = scale * scaleFactor;

                if (newScale >= initialScale && newScale <= initialScale * 5.0f) {
                    scale = newScale;
                    float focusX = detector.getFocusX();
                    float focusY = detector.getFocusY();
                    matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                    imageViewEnlarged.setImageMatrix(matrix);
                }
                return true;
            }
        });

        imageViewEnlarged.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            float deltaX = event.getX() - lastX;
                            float deltaY = event.getY() - lastY;

                            float[] values = new float[9];
                            matrix.getValues(values);
                            float transX = values[Matrix.MTRANS_X];
                            float transY = values[Matrix.MTRANS_Y];
                            float scaleX = values[Matrix.MSCALE_X];
                            float scaleY = values[Matrix.MSCALE_Y];

                            float imageWidth = imageViewEnlarged.getDrawable().getIntrinsicWidth() * scaleX;
                            float imageHeight = imageViewEnlarged.getDrawable().getIntrinsicHeight() * scaleY;
                            float viewWidth = imageViewEnlarged.getWidth();
                            float viewHeight = imageViewEnlarged.getHeight();

                            if (imageWidth > viewWidth) {
                                if (transX + deltaX > 0) deltaX = -transX;
                                else if (transX + deltaX < viewWidth - imageWidth)
                                    deltaX = viewWidth - imageWidth - transX;
                            } else {
                                deltaX = 0;
                            }

                            if (imageHeight > viewHeight) {
                                if (transY + deltaY > 0) deltaY = -transY;
                                else if (transY + deltaY < viewHeight - imageHeight)
                                    deltaY = viewHeight - imageHeight - transY;
                            } else {
                                deltaY = 0;
                            }

                            matrix.postTranslate(deltaX, deltaY);
                            imageViewEnlarged.setImageMatrix(matrix);

                            lastX = event.getX();
                            lastY = event.getY();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;
                }
                return true;
            }
        });
    }

    private void resetZoom() {
        scale = initialScale;
        matrix.set(initialMatrix);
        imageViewEnlarged.setImageMatrix(matrix);
        mode = NONE;
    }

    private void sendEditRequestToAdmin() {
        // Pastikan pengguna terotentikasi
        FirebaseUser  currentUser  = mAuth.getCurrentUser ();
        if (currentUser  == null) {
            Toast.makeText(this, "User  not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonUpdateReport.setEnabled(false);

        // Siapkan data asli
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("title", currentReport.getTitle());
        originalData.put("description", currentReport.getDescription());
        originalData.put("amount", currentReport.getAmount());
        originalData.put("date", currentReport.getDate());
        originalData.put("imageUrl", currentReport.getImageUrl());

        // Siapkan data yang diedit
        Map<String, Object> editedData = new HashMap<>();
        editedData.put("title", editTextTitle.getText().toString().trim());
        editedData.put("description", editTextDescription.getText().toString().trim());
        editedData.put("amount", Double.parseDouble(editTextAmount.getText().toString().trim()));
        editedData.put("date", selectedDate.getTime());

        // Tambahkan URL gambar jika ada perubahan
        if (selectedImageUri != null) {
            editedData.put("imageUrl", selectedImageUri.toString());
        } else {
            editedData.put("imageUrl", currentReport.getImageUrl());
        }

        // Update status laporan asli menjadi pending dan tambahkan field "EditedData"
        db.collection("reports").document(reportId)
                .update("status", "pending", "EditedData", "pernah") // Tambahkan field "EditedData"
                .addOnSuccessListener(statusUpdateVoid -> {
                    // Update status di koleksi processedReports
                    db.collection("processedReports")
                            .whereEqualTo("reportId", reportId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        // Update status menjadi pending
                                        document.getReference().update("status", "Sudah Di Perbarui")
                                                .addOnSuccessListener(aVoid -> {
                                                    // Buat dokumen baru di koleksi editedReports
                                                    Map<String, Object> editRequest = new HashMap<>();
                                                    editRequest.put("userId", currentUser .getUid());
                                                    editRequest.put("originalReportId", reportId);
                                                    editRequest.put("originalData", originalData);
                                                    editRequest.put("editedData", editedData);
                                                    editRequest.put("editStatus", "pending");
                                                    editRequest.put("timestamp", FieldValue.serverTimestamp());

                                                    db.collection("editedReports")
                                                            .add(editRequest)
                                                            .addOnSuccessListener(documentReference -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                buttonUpdateReport.setEnabled(true);
                                                                Toast.makeText(this, "Edit request sent to admin", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                progressBar.setVisibility(View.GONE);
                                                                buttonUpdateReport.setEnabled(true);
                                                                Toast.makeText(this, "Failed to send edit request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressBar.setVisibility(View.GONE);
                                                    buttonUpdateReport.setEnabled(true);
                                                    Toast.makeText(this, "Failed to update processed report status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    // Jika tidak ada dokumen di processedReports
                                    progressBar.setVisibility(View.GONE);
                                    buttonUpdateReport.setEnabled(true);
                                    Toast.makeText(this, "No processed report found for this report ID", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                buttonUpdateReport.setEnabled(true);
                                Toast.makeText(this, "Failed to retrieve processed reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    buttonUpdateReport.setEnabled(true);
                    Toast.makeText(this, "Gagal memperbarui status laporan", Toast.LENGTH_SHORT).show();
                });
    }



}