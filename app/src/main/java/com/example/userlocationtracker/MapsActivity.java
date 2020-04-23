package com.example.userlocationtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationrequest;
    private Location lastlocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 1500;
    double latitude, longitude;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermision();
        }
        //Check if google play services are available or not
        if(! checkGooglePlayServices())
        {
            Log.d("OnCreate: ","Finishing test case, since google play services are not available");
                    finish();
        }
        else
        {
            Log.d("OnCreate: ","Google play services are available");
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    public boolean checkGooglePlayServices()
    {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS)
        {
            if(googleAPI.isUserResolvableError(result))
            {
                googleAPI.getErrorDialog(this,result,0).show();
            }
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //Permision is granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (client == null) {
                            buildGoogleAPiCilent();
                        }
                        mMap.setMyLocationEnabled(true);
                    } else //Permision not granted
                    {
                        Toast.makeText(this, "Access Denied", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this
     case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to
     install
     * it inside the SupportMapFragment. This method will only be triggered once the user
     has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if
            (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                buildGoogleAPiCilent();
                mMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleAPiCilent();
            mMap.setMyLocationEnabled(true);
        }
    }
    public void onClick(View v)
    {
        Object dataTransfer [] = new Object[2];
        String url;
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        switch (v.getId()) {
            case R.id.btn_search:
            {
                EditText tf_location = (EditText) findViewById(R.id.TF_Location);
                String location = tf_location.getText().toString();
                if (!location.equals(""))
                {
                    List<Address> addressList = null;
                    MarkerOptions mo = new MarkerOptions();
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < addressList.size(); i++) {
                        Address myAddress = addressList.get(i);
                        LatLng latlang = new LatLng(myAddress.getLatitude(),
                                myAddress.getLongitude());
                        mo.position(latlang);
                        mo.title("Search Result:");
                        mMap.addMarker(mo);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlang));
                    }
                }
                Toast.makeText(this, "Enter name of place to search",
                        Toast.LENGTH_SHORT).show();
            }
            break;
            case R.id.btn_Hospitals:
            {
                mMap.clear();
                String hospitals = "hospital";
                url = getUrl(lastlocation.getLatitude(), lastlocation.getLongitude(),
                        hospitals);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby hospitals",
                        Toast.LENGTH_LONG).show();
            }
            break;
            case R.id.btn_School:
            {
                mMap.clear();
                String school = "school";
                url = getUrl(lastlocation.getLatitude(), lastlocation.getLongitude(),
                        school);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby schools",
                        Toast.LENGTH_LONG).show();
            }
            break;
            case R.id.btn_Theatre:
            {
                mMap.clear();
                String theatre = "movie_theater";
                url = getUrl(lastlocation.getLatitude(), lastlocation.getLongitude(),
                        theatre);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby theatres",
                        Toast.LENGTH_LONG).show();
            }
            break;
            case R.id.btn_atm:
            {
                mMap.clear();
                String atm = "atm";
                url = getUrl(lastlocation.getLatitude(), lastlocation.getLongitude(), atm);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing nearby ATM's",
                        Toast.LENGTH_LONG).show();
            }
            break;
        }
    }
    private String getUrl(Double latitude, Double longitude, String nearbyPlace)
    {
        StringBuffer googlePlacesUrl = new
                StringBuffer("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location="+ latitude + "," + longitude);
        googlePlacesUrl.append("&radius="+ PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type="+ nearbyPlace);
        googlePlacesUrl.append("&key=" + "AIzaSyDJWptXndgPqvS4b0n9ZRQ71FiCXagCYWM");
        return googlePlacesUrl.toString();
    }
    protected synchronized void buildGoogleAPiCilent()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }
    @Override
    public void onLocationChanged(Location location)
    {
        lastlocation = location;
        if(currentLocationMarker != null)
        {
            currentLocationMarker.remove();
        }
        LatLng latlang = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(latlang);
        markeroptions.title("Current Location");

        markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        ;
        currentLocationMarker = mMap.addMarker(markeroptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlang));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    public boolean checkLocationPermision() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new
                        String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new
                        String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
                //ActivityCompat.requestPermissions(this, new String[]
            }
            return false;
        }
        else
        {
            return true;
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationrequest = new LocationRequest();
        locationrequest.setInterval(1000);
        locationrequest.setFastestInterval(1000);
        locationrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationrequest, this);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
}

