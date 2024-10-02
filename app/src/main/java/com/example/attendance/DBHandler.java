package com.example.attendance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    private static final String USERS_TABEL_NAME = "users";
    private static final String ATTENDANCE_TABEL_NAME = "attendance";
    private static int VERSION_NUMBER = 2;
    private static String DATABASE_NAME = "attendance.db";
    private Context context;

    public DBHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the users table
        String userQuery = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "email TEXT, " +
                "password TEXT)";

        // Create the attendance table
        String attendanceQuery = "CREATE TABLE attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "date TEXT, " +
                "biometric_registered INTEGER, " +
                "biometric_reference TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))";

        String checkInQuery = "CREATE TABLE check_in (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "date TEXT, " +
                "status TEXT, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))";

        db.execSQL(checkInQuery);
        db.execSQL(userQuery);
        db.execSQL(attendanceQuery);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABEL_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ATTENDANCE_TABEL_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + "check_in");
        onCreate(db);
    }

    Long  addUser(String name, String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        long result = db.insert("users", null, values);
        return result;
    }

    Long cheInOrOut(String userId, String check){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys=ON;");
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("status", check);
        values.put("date", new  Date().toString());
        long result = db.insert("check_in", null, values);
        return result;
    }

    public String getLastCheckValue(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String lastCheckValue = null;

        // Query to get the last status for the user
        String query = "SELECT status FROM check_in WHERE user_id = ? ORDER BY id DESC LIMIT 1"; // Updated column name
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Get the status from the cursor
                int index = cursor.getColumnIndex("status"); // Updated column name
                lastCheckValue = cursor.getString(index);
            }
            cursor.close();
        }

        return lastCheckValue;
    }


    Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor user = null;

        if (db != null) {
            String query = "SELECT * FROM users WHERE email = ?";
            user = db.rawQuery(query, new String[]{email});
        }
        return user;
    }

    Long addAttendance(String userId,  String hashed_data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys=ON;"); // Enable foreign keys
        String date = new Date().toString();
        int regstered = 1;
        ContentValues values = new ContentValues();
        values.put("user_id", userId.toString());
        values.put("biometric_registered", regstered);
        values.put("biometric_reference", hashed_data);
        values.put("date", date);
        long newRowId = db.insert("attendance", null, values);
        return newRowId;
    }



    boolean registered(String userId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor biometric_registered = null;

        if (db != null) {
            String query = "SELECT biometric_registered FROM attendance WHERE user_id = ?";
            biometric_registered = db.rawQuery(query, new String[]{userId});
        }
        if (biometric_registered != null && biometric_registered.moveToFirst()) {
            int biometricRegisteredIndex = biometric_registered.getColumnIndex("biometric_registered");
            if (biometricRegisteredIndex != -1) {
                int biometricRegistered = biometric_registered.getInt(biometricRegisteredIndex);
                biometric_registered.close();
                return biometricRegistered == 1;
            }
        }
        if (biometric_registered != null) {
            biometric_registered.close();
            }
        return false;
    }

    String getBiometric(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String hashedData = null; // Initialize to null

        if (db != null) {
            String query = "SELECT biometric_reference FROM attendance WHERE user_id = ?";
            cursor = db.rawQuery(query, new String[]{userId});
        }

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex("biometric_reference");
                if (index >= 0) {
                    hashedData = cursor.getString(index);
                }
            }
            cursor.close();
        }
        return hashedData;
    }

    public List<AttendanceItem> getStatus(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<AttendanceItem> attendanceList = new ArrayList<>(); // Initialize the list

        if (db != null) {
            String query = "SELECT status, date FROM check_in WHERE user_id = ?";
            Cursor cursor = db.rawQuery(query, new String[]{userId});

            if (cursor != null) {
                try {
                    // Check if cursor has any data
                    if (cursor.moveToFirst()) {
                        do {
                            // Retrieve the values from the cursor
                            int indexa = cursor.getColumnIndex("status");
                            String status = cursor.getString(indexa);
                            int indexb = cursor.getColumnIndex("date");
                            String date = cursor.getString(indexb);

                            // Create an AttendanceItem object and add it to the list
                            AttendanceItem attendanceItem = new AttendanceItem(status, date);
                            attendanceList.add(attendanceItem);
                        } while (cursor.moveToNext()); // Move to the next row
                    }
                } finally {
                    cursor.close(); // Ensure the cursor is closed to avoid memory leaks
                }
            }
        }

        return attendanceList; // Return the populated list
    }


}
