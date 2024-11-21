package com.example.laporan2;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProcessedReportAdapter extends RecyclerView.Adapter<ProcessedReportAdapter.ProcessedReportViewHolder> {
    private List<Report> processedReports;
    private OnItemClickListener listener;
    private Context context; // Tambahkan context

    public interface OnItemClickListener {
        void onItemClick(Report report);
    }

    // Update konstruktor untuk menerima context
    public ProcessedReportAdapter(Context context, List<Report> processedReports, OnItemClickListener listener) {
        this.context = context;
        this.processedReports = processedReports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProcessedReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_processed_report, parent, false);
        return new ProcessedReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProcessedReportViewHolder holder, int position) {
        Report report = processedReports.get(position);

        // Set judul
        holder.textViewTitle.setText(report.getTitle());

        // Set tanggal
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.textViewDate.setText(sdf.format(report.getDate()));

        // Set status
        String statusText = getStatusText(report.getStatus());
        holder.textViewStatus.setText(statusText);

        // Set status background color
        int statusColor = getStatusColor(report.getStatus());
        holder.textViewStatus.setBackgroundTintList(ColorStateList.valueOf(statusColor));

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return processedReports.size();
    }

    private String getStatusText(String status) {
        if (status == null) return "Status Tidak Dikenal";

        switch (status) {
            case "approved":
                return "Disetujui";
            case "rejected": // Pastikan case "rejected" ada
                return "Ditolak";
                case "Sudah Di Perbarui":
                    return "Data diperbarui, menunggu persetujuan";
            default:
                return "Menunggu Persetujuan";
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.gray);

        switch (status) {
            case "approved":
                return ContextCompat.getColor(context, R.color.green);
            case "rejected": // Tambahkan case untuk rejected
                return ContextCompat.getColor(context, R.color.red);
            case "Sudah Di Perbarui":
                return  ContextCompat.getColor(context, R.color.colorPrimaryDark);
            default:
                return ContextCompat.getColor(context, R.color.pending_status_color);
        }
    }

    public void updateData(List<Report> newReports) {
        processedReports.clear();
        processedReports.addAll(newReports);
        notifyDataSetChanged();
    }

    static class ProcessedReportViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDate;
        TextView textViewStatus;

        public ProcessedReportViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
        }
    }
}