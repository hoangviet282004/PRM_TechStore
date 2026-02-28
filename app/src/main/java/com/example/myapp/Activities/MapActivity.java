package com.example.myapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.example.myapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    // Tọa độ TechExpress (Ví dụ: Quận 9, TP.HCM)
    private final LatLng STORE_POS = new LatLng(10.8411, 106.8099);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btnDirections = findViewById(R.id.btnDirections);
        btnDirections.setOnClickListener(v -> openGoogleMapsDirections());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(STORE_POS).title("TechExpress Store"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(STORE_POS, 15f));
    }

    private void openGoogleMapsDirections() {
        String uri = "google.navigation:q=" + STORE_POS.latitude + "," + STORE_POS.longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Hãy cài đặt ứng dụng Google Maps", Toast.LENGTH_SHORT).show();
        }
    }
}