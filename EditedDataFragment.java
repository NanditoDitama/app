package com.example.laporan2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditedDataFragment extends Fragment {
    private RecyclerView recyclerView;
    private ItemDataEditAdapter adapter;
    private List<Report> editedReports;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView textViewNoData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edited_data, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewEditedData);
        progressBar = view.findViewById(R.id.progressBar);
        textViewNoData = view.findViewById(R.id.textViewNoData);
        editedReports = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        loadEditedReports();

        return view;
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemDataEditAdapter(getContext(), editedReports); // Perhatikan parameter berbeda
        recyclerView.setAdapter(adapter);
    }


    private void loadEditedReports() {
        db.collection("editedReports")
                .whereEqualTo("editStatus", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    editedReports.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> editedData = (Map<String, Object>) document.get("editedData");

                        Report report = new Report();
                        report.setId(document.getId());
                        report.setTitle(editedData.get("title").toString());
                        report.setDate(((Timestamp) editedData.get("date")).toDate());

                        // Ambil nama pengirim
                        String userId = document.getString("userId");
                        getUserName(userId, report);

                        editedReports.add(report);
                    }
                    adapter.notifyDataSetChanged();
                });
    }


    private void getUserName(String userId, Report report) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        report.setSenderName(documentSnapshot.getString("name"));
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}