package com.example.attendance;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Button;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button checkInButton, checkOutButton, showAttendanceButton;
    String user_email, user_name, user_id;
    DBHandler dbHandler;
    boolean isLoggedIn = false;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private List<AttendanceItem> attendanceList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private double OFFICE_LATITUDE, OFFICE_LONGITUDE;
    TextView message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent_user = getIntent();
        isLoggedIn = intent_user.getBooleanExtra("logedIn", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(MainActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else {
            checkInButton = findViewById(R.id.checkInButton);
            checkOutButton = findViewById(R.id.checkOutButton);
            showAttendanceButton = findViewById(R.id.showAttendanceButton);
            recyclerView = findViewById(R.id.recyclerViewAttendance);
            adapter = new AttendanceAdapter(attendanceList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);

            message = findViewById(R.id.message);

            user_name = intent_user.getStringExtra("name");
            user_email = intent_user.getStringExtra("email");
            user_id = intent_user.getStringExtra("id");

            checkInButton.setOnClickListener(view -> {
                handleCheckIn("checkIn");
            });

            checkOutButton.setOnClickListener(view -> {
                handleCheckIn("checkOut");
            });

            BiometricHandler biometricHandler = new BiometricHandler(this);
            showAttendanceButton.setOnClickListener(view -> {
                dbHandler = new DBHandler(MainActivity.this);
                Boolean registered = dbHandler.registered(user_id);

                if (registered) {
                    String hashed_data = dbHandler.getBiometric(user_id);
                    if (hashed_data == null) {
                        Toast.makeText(MainActivity.this, "No biometric data found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    biometricHandler.loginBiometric(new BiometricHandler.BiometricCallback() {
                        @Override
                        public void onBiometricAuthenticated(String biometricReference) {
                            if (hashed_data.equals(biometricReference)) {
                                dbHandler.addAttendance(user_id, hashed_data);
                                List<AttendanceItem> fetchedItems = dbHandler.getStatus(user_id);
                                attendanceList.clear();
                                attendanceList.addAll(fetchedItems);
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed login", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onBiometricRegistered(String hashedTemplate) {
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    biometricHandler.registerBiometric(new BiometricHandler.BiometricCallback() {
                        @Override
                        public void onBiometricAuthenticated(String biometricReference) {
                        }

                        @Override
                        public void onBiometricRegistered(String hashedTemplate) {
                            dbHandler.addAttendance(user_id, hashedTemplate);
                            Toast.makeText(MainActivity.this, "Biometric registered successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void handleCheckIn(String check) {
        // Get the last known location
        checkInButton.setEnabled(false);  // Disable button to prevent multiple clicks
        checkOutButton.setEnabled(false);
        getLocationAsArray(locationArray -> {
            // Check if the location is valid
            if (locationArray[0] != null && locationArray[1] != null) {
                double currentLatitude = locationArray[0];
                double currentLongitude = locationArray[1];
                OFFICE_LATITUDE = currentLatitude;
                OFFICE_LONGITUDE = currentLongitude;

                // Check if the current location is the same as the office location
                if (isSameLocation(currentLatitude, currentLongitude, OFFICE_LATITUDE, OFFICE_LONGITUDE)) {
                    // Proceed with check-in/check-out logic
                    DBHandler dbHandler1 = new DBHandler(MainActivity.this);
                    String value = dbHandler1.getLastCheckValue(user_id);
                    if (check.equals("checkOut") && value == null) {
                        message.setText("You can not check out before checking in");
                        message.setTextColor(getResources().getColor(R.color.purple_700));
                        checkInButton.setEnabled(true);
                        checkOutButton.setEnabled(true);
                        return;
                    }
                    if (check.equals(value)) {
                        message.setText("already " + check);
                    } else {
                        dbHandler1.cheInOrOut(user_id, check);
                        message.setText("You are now " + check);
                    }
                } else {
                    message.setText("You are not at the office");
                    message.setTextColor(getResources().getColor(R.color.purple_700));
                }
            } else {
                message.setText("Location not available");
            }
            checkInButton.setEnabled(true);
            checkOutButton.setEnabled(true);
        });
    }

    // Method to compare two locations
    private boolean isSameLocation(double lat1, double lon1, double lat2, double lon2) {
        final double THRESHOLD = 0.0001; // Define a threshold for location comparison
        return Math.abs(lat1 - lat2) < THRESHOLD && Math.abs(lon1 - lon2) < THRESHOLD;
    }
    // Define a callback interface to return location
    public interface LocationCallback {
        void onLocationReceived(Double[] locationArray);
    }

    private void getLocationAsArray(LocationCallback callback) {
        Log.d("MainActivity", "Requesting location...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("MainActivity", "Location permissions not granted, requesting permissions...");

            // Request the necessary permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            callback.onLocationReceived(new Double[]{null, null});
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            Log.d("MainActivity", "LocationManager is available, requesting location updates...");

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.d("MainActivity", "Location changed: " + location.getLatitude() + ", " + location.getLongitude());

                    Double[] locationArray = new Double[2];
                    locationArray[0] = location.getLatitude();
                    locationArray[1] = location.getLongitude();
                    callback.onLocationReceived(locationArray);
                    locationManager.removeUpdates(this); // Stop listening for updates after getting the location
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d("MainActivity", "Provider status changed: " + provider + ", status: " + status);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    Log.d("MainActivity", "Provider enabled: " + provider);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Log.d("MainActivity", "Provider disabled: " + provider);
                }
            });

            // Get the last known location as a fallback
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                Log.d("MainActivity", "Last known location found: " + lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude());
                Double[] locationArray = new Double[2];
                locationArray[0] = lastKnownLocation.getLatitude();
                locationArray[1] = lastKnownLocation.getLongitude();
                callback.onLocationReceived(locationArray);
            } else {
                Log.d("MainActivity", "No last known location available.");
                callback.onLocationReceived(new Double[]{null, null});
            }
        } else {
            Log.d("MainActivity", "LocationManager is null.");
            callback.onLocationReceived(new Double[]{null, null});
        }
    }

}
