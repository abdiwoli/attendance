package com.example.attendance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceItem> attendanceList;

    // Constructor to initialize the attendance list
    public AttendanceAdapter(List<AttendanceItem> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceItem attendanceItem = attendanceList.get(position);
        holder.bind(attendanceItem);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    // ViewHolder class to hold the item views
    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewStatus;
        private final TextView textViewDate;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }

        public void bind(AttendanceItem attendanceItem) {
            textViewStatus.setText("Status: " + attendanceItem.getStatus());
            textViewDate.setText("Date: " + attendanceItem.getDate());
        }
    }
}
