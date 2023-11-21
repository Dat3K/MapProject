package com.example.map;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.example.map.model.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Variables
    private final static String TAG = "MapActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;

    private Place currentPlace;
    private Place newPlace;

    // Widgets
    EditText mSearchText;
    FloatingActionButton btnAddPlace;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = findViewById(R.id.input_search);
        btnAddPlace = findViewById(R.id.btnAddPlace);
        if (isSeviceOK()) {
            initMap();
        }
        checkLocationPermission();
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        Geocoder geocoder = new Geocoder(MapActivity.this);
                        List<Address> list = new ArrayList<>();
                        try {
                            list = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                        } catch (Exception e) {
                            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
                        }
                        currentPlace = new Place(list.get(0).getCountryName(), list.get(0).getAddressLine(0), currentLocation.getLatitude(), currentLocation.getLongitude());
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                        mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("My Location"));
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.d(TAG, "getDeviceLocation: permission not granted");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng location, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + location.latitude + ", lng: " + location.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
        if (!title.equals("My Location")) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(location).title(title));
        }
    }

    public void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);

        mSearchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                geoLocate();
            }
            return false;
        });

        btnAddPlace.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setTitle("Add Place");
            builder.setMessage("Do you want to add this place?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Geocoder geocoder = new Geocoder(MapActivity.this);
                    List<Address> list = new ArrayList<>();
                    try {
                        list = geocoder.getFromLocationName(mSearchText.getText().toString(), 1);
                    } catch (Exception e) {
                        Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
                    }
                    newPlace = new Place(list.get(0).getCountryName(), list.get(0).getAddressLine(0), list.get(0).getLatitude(), list.get(0).getLongitude());
                    float[] result = new float[1];
                    Location.distanceBetween(currentPlace.getLatitude(), currentPlace.getLongitude(), newPlace.getLatitude(), newPlace.getLongitude(), result);
                    double distance = result[0]/ 1000;
                    Intent intent = new Intent();
                    intent.putExtra("name", newPlace.getName());
                    intent.putExtra("address", newPlace.getAddress());
                    intent.putExtra("distance", distance);
                    setResult(RESULT_OK, intent);
                    finish();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
            Toast.makeText(this, list.get(0).getLocality(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    public boolean isSeviceOK() {
        Log.d(TAG, "isServiceOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServiceOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServiceOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission: checking location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1234);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1234);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case 1234: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
        }

    }
}