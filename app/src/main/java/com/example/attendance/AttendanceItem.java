package com.example.attendance;

public class AttendanceItem {
    private final String status;
    private final String date;

    public AttendanceItem(String status, String date) {
        this.status = status;
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }
}
