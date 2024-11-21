package com.example.laporan2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProcessedDataFragment extends Fragment {
    private RecyclerView recyclerViewProcessedData;
    private ProcessedReportAdapter adapter;
    private List<Report> processedReportsList;
    private FirebaseFirestore db;
    private ProgressBar progressBarProcessed;
    private TextView textViewEmptyProcessed;
    private ListenerRegistration processedReportsListener; // Tambahkan ini

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_processed_data, container, false);

        // Inisialisasi view
        recyclerViewProcessedData = view.findViewById(R.id.recyclerViewProcessedData);
        progressBarProcessed = view.findViewById(R.id.progressBarProcessed);
        textViewEmptyProcessed = view.findViewById(R.id.textViewEmptyProcessed);

        // Inisialisasi database dan list
        db = FirebaseFirestore.getInstance();
        processedReportsList = new ArrayList<>();

        // Setup RecyclerView
        setupRecyclerView();

        // Ambil data
        fetchProcessedReports();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ProcessedReportAdapter(
                getContext(), // Tambahkan context
                processedReportsList,
                report -> {
                    // Navigasi ke halaman detail
                    Intent intent = new Intent(getActivity(), ProcessedReportDetailActivity.class);
                    intent.putExtra("reportId", report.getId());
                    startActivity(intent);
                }
        );

        recyclerViewProcessedData.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewProcessedData.setAdapter(adapter);
    }

    private void fetchProcessedReports() {
        progressBarProcessed.setVisibility(View.VISIBLE);

        processedReportsListener = db.collection("processedReports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        progressBarProcessed.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Gagal mengambil data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    processedReportsList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Report report = document.toObject(Report.class);
                            report.setId(document.getId());
                            processedReportsList.add(report);
                        }
                    }

                    progressBarProcessed.setVisibility(View.GONE);

                    if (processedReportsList.isEmpty()) {
                        textViewEmptyProcessed.setVisibility(View.VISIBLE);
                        recyclerViewProcessedData.setVisibility(View.GONE);
                    } else {
                        textViewEmptyProcessed.setVisibility(View.GONE);
                        recyclerViewProcessedData.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (processedReportsListener != null) {
            processedReportsListener.remove(); // Hapus listener saat fragment tidak aktif
        }
    }
}