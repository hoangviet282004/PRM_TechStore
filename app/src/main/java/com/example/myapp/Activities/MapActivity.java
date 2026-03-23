package com.example.myapp.Activities;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.widget.Button;
import com.example.myapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnDirections;

    private static final LatLng STORE_POS = new LatLng(10.8411, 106.8099);
    private static final int ROUTE_COLOR = 0xFF1976D2; // blue

    private final ActivityResultLauncher<String> locationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    enableMyLocation();
                    getLocationAndRoute();
                } else {
                    focusStore();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(v -> onDirectionsClicked());

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(STORE_POS).title("Tech Store"));
        focusStore();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    private void onDirectionsClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getLocationAndRoute();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void enableMyLocation() {
        if (mMap != null) mMap.setMyLocationEnabled(true);
    }

    @SuppressWarnings("MissingPermission")
    private void getLocationAndRoute() {
        btnDirections.setEnabled(false);
        btnDirections.setText("Đang tìm vị trí...");

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .build();

        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                fusedLocationClient.removeLocationUpdates(this);
                android.location.Location location = result.getLastLocation();
                if (location != null) {
                    LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                    btnDirections.setText("Đang tải đường đi...");
                    fetchDirections(origin);
                } else {
                    resetButton();
                    focusStore();
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(request, callback, getMainLooper());
    }

    private void resetButton() {
        btnDirections.setEnabled(true);
        btnDirections.setText("Chỉ đường tới cửa hàng");
    }

    private void fetchDirections(LatLng origin) {
        String apiKey = getApiKey();
        if (apiKey == null) { focusStore(); return; }

        String bodyJson = "{"
                + "\"origin\":{\"location\":{\"latLng\":{\"latitude\":" + origin.latitude + ",\"longitude\":" + origin.longitude + "}}},"
                + "\"destination\":{\"location\":{\"latLng\":{\"latitude\":" + STORE_POS.latitude + ",\"longitude\":" + STORE_POS.longitude + "}}},"
                + "\"travelMode\":\"DRIVE\""
                + "}";

        Request request = new Request.Builder()
                .url("https://routes.googleapis.com/directions/v2:computeRoutes")
                .post(RequestBody.create(MediaType.get("application/json"), bodyJson))
                .addHeader("X-Goog-Api-Key", apiKey)
                .addHeader("X-Goog-FieldMask", "routes.polyline.encodedPolyline")
                .build();

        new OkHttpClient().newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        android.util.Log.e("MAP", "Directions API failure: " + e.getMessage());
                        runOnUiThread(() -> { resetButton(); focusStore(); });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body() != null ? response.body().string() : "null";
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> { resetButton(); focusStore(); });
                            return;
                        }
                        try {
                            JSONObject json = new JSONObject(body);
                            JSONArray routes = json.getJSONArray("routes");
                            if (routes.length() == 0) {
                                runOnUiThread(() -> { resetButton(); focusStore(); });
                                return;
                            }
                            String encoded = routes.getJSONObject(0)
                                    .getJSONObject("polyline")
                                    .getString("encodedPolyline");
                            runOnUiThread(() -> { drawRoute(origin, encoded); resetButton(); });
                        } catch (Exception e) {
                            android.util.Log.e("MAP", "Directions parse error: " + e.getMessage());
                            runOnUiThread(() -> { resetButton(); focusStore(); });
                        }
                    }
                });
    }

    private void drawRoute(LatLng origin, String encodedPolyline) {
        List<LatLng> points = decodePolyline(encodedPolyline);

        mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(12f)
                .color(ROUTE_COLOR)
                .geodesic(true));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origin)
                .include(STORE_POS)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }

    private void focusStore() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(STORE_POS, 15f));
    }

    private String getApiKey() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    // Standard Google encoded polyline decoder
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length(), lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            shift = 0; result = 0;
            do { b = encoded.charAt(index++) - 63; result |= (b & 0x1f) << shift; shift += 5; } while (b >= 0x20);
            lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            poly.add(new LatLng(lat / 1e5, lng / 1e5));
        }
        return poly;
    }
}
