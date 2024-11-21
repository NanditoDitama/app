package com.example.laporan2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends BaseAdapter {

    private Context context;
    private List<Report> reportList;
    private OnItemClickListener listener;

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @Override
    public int getCount() {
        return reportList.size();
    }

    @Override
    public Report getItem(int position) {
        return reportList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
            holder = new ViewHolder();
            holder.textViewStatus = convertView.findViewById(R.id.textViewStatus);
            holder.textViewTitle = convertView.findViewById(R.id.textViewTitle);
            holder.textViewDate = convertView.findViewById(R.id.textViewDate);
            holder.buttonDelete = convertView.findViewById(R.id.buttonDelete);
            holder.buttonEdit = convertView.findViewById(R.id.buttonEdit);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Report report = reportList.get(position);

        // Set data ke view
        if (report != null) {
            // Untuk status "pending"
            if ("pending".equals(report.getStatus())) {
                // Sembunyikan judul dan tanggal
                holder.textViewTitle.setVisibility(View.VISIBLE);
                holder.textViewDate.setVisibility(View.VISIBLE);
                holder.textViewTitle.setText(report.getTitle());
                // Tampilkan status dengan ukuran yang lebih besar
                holder.textViewStatus.setVisibility(View.VISIBLE);
                holder.textViewDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                // Atur ukuran teks status
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                holder.textViewDate.setText(sdf.format(report.getDate()));
                // Opsional: Bisa tambahkan warna atau gaya tambahan
                holder.textViewStatus.setTypeface(null, Typeface.BOLD);
            } else {
                // Untuk status selain "pending"
                holder.textViewTitle.setVisibility(View.VISIBLE);
                holder.textViewDate.setVisibility(View.VISIBLE);
                holder.textViewStatus.setVisibility(View.GONE);

                holder.textViewTitle.setText(report.getTitle());

                // Atur ukuran tanggal lebih kecil
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                holder.textViewDate.setText(sdf.format(report.getDate()));
                holder.textViewDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13); // Ukuran teks lebih kecil
            }

            // Listener untuk seluruh item
            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(report);
                }
            });

            // Listener untuk tombol delete
            holder.buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(report);
                }
            });

            // Listener untuk tombol edit
            holder.buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(report);
                }
            });



            // Tambahkan indikasi status pending
            if ("pending".equals(report.getStatus())) {
                convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.pending_status_color));
                // Atau tambahkan TextView untuk status
                TextView statusIndicator = convertView.findViewById(R.id.textViewStatus);
                statusIndicator.setText("Menunggu Persetujuan");
                statusIndicator.setVisibility(View.VISIBLE);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                // Sembunyikan status indicator
                TextView statusIndicator = convertView.findViewById(R.id.textViewStatus);
                statusIndicator.setVisibility(View.GONE);
            }

            if ("rejected".equals(report.getStatus())) {
                // Mengambil warna dari tema
                int colorRejected = ContextCompat.getColor(context, R.color.rejected_status_color);
                TextView statusIndicator = convertView.findViewById(R.id.textViewStatus);
                // Membuat GradientDrawable untuk latar belakang dengan radius
                GradientDrawable backgroundDrawable = new GradientDrawable();
                backgroundDrawable.setColor(colorRejected); // Set warna
                backgroundDrawable.setCornerRadius(16f); // Set radius sudut (dalam dp)

                // Mengatur latar belakang convertView
                convertView.setBackground(backgroundDrawable);


                statusIndicator.setText("Ditolak");
                statusIndicator.setVisibility(View.VISIBLE);

                // Tampilkan tombol edit untuk laporan yang ditolak
                holder.buttonEdit.setVisibility(View.VISIBLE);
            } else if ("pending".equals(report.getStatus())) {
                // Mengambil warna dari tema
                int colorPending = ContextCompat.getColor(context, R.color.pending_status_color);
                TextView statusIndicator = convertView.findViewById(R.id.textViewStatus);
                // Membuat GradientDrawable untuk latar belakang dengan radius
                GradientDrawable backgroundDrawable = new GradientDrawable();
                backgroundDrawable.setColor(colorPending); // Set warna
                backgroundDrawable.setCornerRadius(16f); // Set radius sudut (dalam dp)

                // Mengatur latar belakang convertView
                convertView.setBackground(backgroundDrawable);


                statusIndicator.setText("Menunggu Persetujuan");
                statusIndicator.setVisibility(View.VISIBLE);

                // Sembunyikan tombol edit saat pending
                holder.buttonEdit.setVisibility(View.GONE);
            } else if ("approved".equals(report.getStatus())) {
                // Mengambil warna dari tema
                int colorSurface = getColorFromTheme(context, com.google.android.material.R.attr.colorSurface);

                // Membuat GradientDrawable untuk latar belakang dengan radius
                GradientDrawable backgroundDrawable = new GradientDrawable();
                backgroundDrawable.setColor(colorSurface); // Set warna
                backgroundDrawable.setCornerRadius(20f); // Set radius sudut (dalam dp)

                // Mengatur latar belakang convertView
                convertView.setBackground(backgroundDrawable);

                // Mengatur status indicator
                TextView statusIndicator = convertView.findViewById(R.id.textViewStatus);
                statusIndicator.setText("Disetujui");
                statusIndicator.setVisibility(View.VISIBLE);
                holder.buttonEdit.setVisibility(View.GONE);
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                TextView statusIndicator = convertView.findViewById(R.id.textViewStatus);
                statusIndicator.setVisibility(View.GONE);
            }



        }

        // Sembunyikan/tampilkan tombol edit berdasarkan status
        if (report.getStatus() != null) {
            switch (report.getStatus()) {
                case "pending":
                    // Sembunyikan tombol edit saat menunggu persetujuan
                    holder.buttonEdit.setVisibility(View.GONE);
                    break;
                case "rejected":
                    // Tampilkan tombol edit jika ditolak
                    holder.buttonEdit.setVisibility(View.VISIBLE);
                    break;
                case "approved":
                    // Sembunyikan tombol edit jika disetujui
                    holder.buttonEdit.setVisibility(View.GONE);
                    break;
                default:
                    holder.buttonEdit.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    public void updateData(List<Report> newReportList) {
        this.reportList = newReportList;
        notifyDataSetChanged();
    }
    private int getColorFromTheme(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
    static class ViewHolder {
        TextView textViewTitle, textViewDate, textViewStatus;
        ImageButton buttonDelete, buttonEdit;
    }

    public interface OnItemClickListener {
        void onItemClick(Report report);
        void onDeleteClick(Report report);
        void onEditClick(Report report);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}