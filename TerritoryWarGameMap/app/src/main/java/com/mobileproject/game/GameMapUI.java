package com.mobileproject.game;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationListener;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class LatLngLines {
    private LatLng pointA, pointB;

    public LatLngLines() {
        pointA = null;
        pointB = null;
    }

    public LatLngLines(LatLng A, LatLng B) {
        pointA = A;
        pointB = B;
    }

    public void setLine(LatLng A, LatLng B) {
        pointA = A;
        pointB = B;
    }

    public LatLng getA() {
        return pointA;
    }

    public LatLng getB() {
        return pointB;
    }
}

class ColorSet {
    public int green, red, blue, gray, orange;

    ColorSet() {
        green = Color.argb(100, 0, 255, 0);
        red = Color.argb(100, 255, 0, 0);
        blue = Color.argb(100, 0, 0, 255);
        gray = Color.argb(100, 100, 100, 100);
        orange = Color.argb(100, 255, 165, 0);
    }
}

class LocationID {

    private int LatID, LngID;

    public LocationID() {
        LatID = 0;
        LngID = 0;
    }

    public LocationID(int LatID, int LngID) {
        this.LatID = LatID;
        this.LngID = LngID;
    }

    public int getLatID() {
        return LatID;
    }

    public int getLngID() {
        return LngID;
    }

    private void setID(int LatID, int LngID) {
        this.LatID = LatID;
        this.LngID = LngID;
    }

    public String printID() {
        return "LatID: " + LatID + " LngID: " + LngID;
    }
}

class TilesData {
    TilesData() {
    }
}


public class GameMapUI extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        LocationSource,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AsyncResponse{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float MIN_ZOOM_PREF = 17f;
    private static final float MAX_ZOOM_PREF = MIN_ZOOM_PREF;

    protected static final String TAG = "MainActivity";

    protected GoogleApiClient mGoogleApiClient;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private Location mLastLocation;
    private Marker mPositionMarker;
    ;
    private LatLng currentLoc;
    private LatLngBounds currentBnd;
    private Utilities utilities;

    private double NorthBoundLat;
    private double SouthBoundLat;
    private double WestBoundLng;
    private double EastBoundLng;

    private int currentLatID = 24513;
    private int currentLngID = -44031;

    private final double bdUnit = 5; // number of tiles in each direction
    private final double latTileUnit = 0.0018; // length of a tile
    private final double lngTileUnit = 0.0018; // width of a tile

    private UiSettings settings;
    private ColorSet colors;

    private ArrayList<LatLngLines> parallelLines;
    private ArrayList<LatLngLines> veriticalLines;

    private LocationManager locationManager;
    private String bestProvider;
    private final int minTime = 20000; // milliseconds
    private final int minDistance = 3; // meters

    private OnLocationChangedListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map_ui);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Set map initial location here:

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // set the criteria of the provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        // find the best location manager that meets the criteria
        bestProvider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        // add the location manager
        locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, this);

        initializeMap();

    }


    public void initializeMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

            return;
        }

        double lat = locationManager.getLastKnownLocation(bestProvider).getLatitude();
        double lng = locationManager.getLastKnownLocation(bestProvider).getLongitude();

        LocationID id = LocationToID(new LatLng(lat, lng));
        currentLatID = id.getLatID();
        currentLngID = id.getLngID();

        System.out.println("lat, lng: " + lat + "," + lng + " current location id: " + id.printID() + "\nset location id: 24413, -43831");
        System.out.println();

        colors = new ColorSet();

        parallelLines = new ArrayList<LatLngLines>();
        veriticalLines = new ArrayList<LatLngLines>();

        NorthBoundLat = (currentLatID + bdUnit) * latTileUnit;
        SouthBoundLat = (currentLatID - bdUnit) * latTileUnit;
        WestBoundLng = (currentLngID - bdUnit) * lngTileUnit;
        EastBoundLng = (currentLngID + bdUnit) * lngTileUnit;

        mapSettingInit();
    }

    private void mapSettingInit() {

        //settings.setZoomControlsEnabled(false);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set bounds
        //mMap.setLatLngBoundsForCameraTarget(getMapViewBounds());

        // Set the map style here:
        mMap.setMapStyle(createMapStyle("retro"));

        // Get current location:
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();


        DrawTiles(mMap);

        // Drawing the titles: line length in KMs
        DrawPolygonDemo(mMap, new LatLng(currentLatID * latTileUnit - latTileUnit / 2, currentLngID * lngTileUnit - lngTileUnit / 2));

        System.out.println("username" + LoginActivity.username + " password: " + LoginActivity.password);
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }


    public MapStyleOptions createMapStyle(String styleName) {
        MapStyleOptions style;

        if (styleName == "retro")
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        else
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        return style;
    }

    public LatLngBounds getMapViewBounds() {
        LatLngBounds bounds;
        double boundUnit = 0.01;

        // set upper left corner and bottom right coordinates
        LatLng UL = new LatLng(currentLoc.latitude - boundUnit, currentLoc.longitude - boundUnit);
        LatLng BR = new LatLng(currentLoc.latitude + boundUnit, currentLoc.longitude + boundUnit);
        // final the bounds
        bounds = new LatLngBounds(UL, BR);
        currentBnd = bounds;

        return bounds;
    }

    /**
     * Creates a List of LatLngs that form a rectangle with the given dimensions.
     */
    private List<LatLng> createRectangle(LatLng center, double halfWidth, double halfHeight) {
        return Arrays.asList(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
    }

    private Polygon DrawPolygon(GoogleMap map, LatLng loc, double width, double height, int color) {
        System.out.println("add tileID: " + loc.toString());

        Polygon polygon = map.addPolygon(new PolygonOptions()
                .addAll(createRectangle(loc, width / 2, height / 2))
                .fillColor(color)
                .strokeWidth(0)
                .clickable(true)
                .strokeColor(Color.argb(0, 255, 255, 255)));
        return polygon;

    }


    private void DrawPolyline(GoogleMap map, LatLng A, LatLng B) {
        Polyline line = map.addPolyline((new PolylineOptions()
                .add(A, B))
                .color(Color.argb(150, 0, 0, 0))
                .width(2));
    }

    private boolean getHorizontalLines() {
        for (double s = SouthBoundLat; s < NorthBoundLat; s += latTileUnit) {
            LatLng A = new LatLng(s, WestBoundLng);
            LatLng B = new LatLng(s, EastBoundLng);
            parallelLines.add(new LatLngLines(A, B));
        }

        if (parallelLines.isEmpty())
            return false;
        else
            return true;
    }

    private boolean getVeriticalLines() {
        for (double s = WestBoundLng; s < EastBoundLng; s += lngTileUnit) {
            LatLng A = new LatLng(SouthBoundLat, s);
            LatLng B = new LatLng(NorthBoundLat, s);
            veriticalLines.add(new LatLngLines(A, B));
        }

        if (veriticalLines.isEmpty())
            return false;
        else
            return true;
    }


    private boolean DrawTiles(GoogleMap mMap) {

        boolean h_flag = false;
        boolean v_flag = false;

        if (getHorizontalLines()) {
            //show("getHorizontalLines");
            Iterator<LatLngLines> iterator = parallelLines.iterator();
            while (iterator.hasNext()) {
                LatLngLines lines = iterator.next();
                DrawPolyline(mMap, lines.getA(), lines.getB());
            }
            h_flag = true;
        } else {
            show("Tiles Generation failed!");
        }

        if (getVeriticalLines()) {
            Iterator<LatLngLines> iterator = veriticalLines.iterator();
            while (iterator.hasNext()) {
                LatLngLines lines = iterator.next();
                DrawPolyline(mMap, lines.getA(), lines.getB());
            }
            v_flag = true;
        } else {
            show("Tiles Generation failed!");
        }

        return (h_flag && v_flag);
    }


    public LatLng shifter(LatLng point, double lat, double lng) {
        return new LatLng(point.latitude + lat, point.longitude + lng);
    }

    public LocationID LocationToID(LatLng loc){
        // TODO: An algorithm convert location data(LatLng) to ID(Int)

        int LatID = (int)(loc.latitude/latTileUnit) + 1;
        int LngID = (int)(loc.longitude/lngTileUnit) ;
        return new LocationID(LatID,LngID);
    }

    public LatLng IdTOLocation(LocationID newID){
        double lat = (newID.getLatID() - 1) * latTileUnit + latTileUnit/ 2.0;
        double lng = (newID.getLngID()) * lngTileUnit + lngTileUnit / 2.0 -lngTileUnit;
        System.out.println("converted lat: " + lat + " converted lng: " + lng);
        LatLng loc = new LatLng(lat,lng);
        // TODO: A better algorithm convert ID(Int) location to data(LatLng)
        return loc;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }


    public void DrawPolygonDemo(GoogleMap mMap, LatLng newLoc) {
        // Drawing the titles: line length in KMs
        //DrawPolygon(mMap, newLoc, latTileUnit, lngTileUnit, colors.red);


        // TODO: 11/8/2016 add polygons to the locaitonID associated with latid and lng id, then clear when painting the polygon on the map 
        //draw tile user is in
        LocationID id = LocationToID(shifter(newLoc, 0, 0));
        TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());

        // draw tiles in cross
        for (int i = 1; i < bdUnit; i++) {
            id = LocationToID(shifter(newLoc, i * latTileUnit, 0));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
            id = LocationToID(shifter(newLoc, (-i) * latTileUnit, 0));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
            id = LocationToID(shifter(newLoc, 0, i * lngTileUnit));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
            id = LocationToID(shifter(newLoc, 0, (-i) * lngTileUnit));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
        }
        // draw polygons in corners
        for (int i = 1; i < bdUnit; i++) {
            for (int j = 1; j < bdUnit; j++) {
                id = LocationToID(shifter(newLoc, i * latTileUnit, j * lngTileUnit));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
                id = LocationToID(shifter(newLoc, i * (-latTileUnit), j * lngTileUnit));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
                id = LocationToID(shifter(newLoc, i * (-latTileUnit), j * (-lngTileUnit)));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
                id = LocationToID(shifter(newLoc, i * (latTileUnit), j * (-lngTileUnit)));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
            }
        }

        //capture in the users location
        TileWebserviceUtility.captureTile(currentLatID, currentLngID,LoginActivity.username, LoginActivity.password, this, getApplicationContext());
        /*
        LocationID id = LocationToID(newLoc);
        TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
        */
        /*
        id = LocationToID(shifter(newLoc, 0, lngTileUnit));
        TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
        DrawPolygon(mMap, shifter(newLoc, 0, lngTileUnit), latTileUnit, lngTileUnit, colors.red);
        DrawPolygon(mMap, shifter(newLoc, latTileUnit, 0), latTileUnit, lngTileUnit, colors.green);
        DrawPolygon(mMap, shifter(newLoc, -latTileUnit, -lngTileUnit), latTileUnit, lngTileUnit, colors.green);
        DrawPolygon(mMap, shifter(newLoc, latTileUnit, lngTileUnit), latTileUnit, lngTileUnit, colors.red);
        DrawPolygon(mMap, shifter(newLoc, -latTileUnit, lngTileUnit), latTileUnit, lngTileUnit, colors.blue);
        DrawPolygon(mMap, shifter(newLoc, latTileUnit, -lngTileUnit), latTileUnit, lngTileUnit, colors.gray);
        DrawPolygon(mMap, shifter(newLoc, latTileUnit * 2, lngTileUnit * 2), latTileUnit, lngTileUnit, colors.green);
        */
    }


    private void show(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        //mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // require permissions here:
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            LatLng latlng = new LatLng(lat,lng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
            mMap.animateCamera(cameraUpdate);
            show("Last Location:"+String.valueOf(lat)+"  "+String.valueOf(lng));
        } else {
            show("No Location Detected");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        show("Debug:Google API Connection Failed!");
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


    // Every time when the location changed:
    @Override
    public void onLocationChanged(Location location) {
        System.out.println("location changed");
        // added to ensure that the map is animated
        if (listener != null) {
            listener.onLocationChanged(location);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void animateMarker(final Marker marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                double lng = t * location.getLongitude() + (1 - t) * startLatLng.longitude;
                double lat = t * location.getLatitude() + (1 - t) * startLatLng.latitude;

                float rotation = (float) (t * location.getBearing() + (1 - t) * startRotation);

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(rotation);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {

    }

    /**
     * Processes the result from the server
     * @param result - the json from the server
     */
    @Override
    public void processResult(String result) {
        System.out.println("Result from server: " + result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        try {
            JSONObject jsonObject = new JSONObject(result);
            String tileLatID = jsonObject.getString("tileLatID");
            String tileLngID = jsonObject.getString("tileLngID");
            String tileUsername = jsonObject.getString("username");
            Toast.makeText(this, tileLatID + tileLngID + tileUsername, Toast.LENGTH_LONG).show();
            LocationID latLng = new LocationID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
            System.out.println("tile username: " + tileUsername + " username " + LoginActivity.username);
            if(tileUsername.equals("null"))
                DrawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colors.gray);
            else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
                DrawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colors.green);
            } else {
                System.out.println("RED tile username: " + tileUsername + " length: " + tileUsername.length() +  " username " + LoginActivity.username + " length: " + LoginActivity.username.length());
                DrawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colors.red);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

