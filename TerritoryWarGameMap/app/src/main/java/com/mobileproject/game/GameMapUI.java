package com.mobileproject.game;

import android.Manifest;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import android.location.LocationListener;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

    private int currentLatID;
    private int currentLngID;
    private LatLng currentLoc;
    private LatLngBounds currentBounds;

    private double NorthBoundLat;
    private double SouthBoundLat;
    private double WestBoundLng;
    private double EastBoundLng;

    private final double bdUnit = 5; // number of tiles in each direction
    private final double latTileUnit = 0.0018; // length of a tile
    private final double lngTileUnit = 0.0018; // width of a tile

    private ColorSet colors;

    private ArrayList<LatLngLines> parallelLines;
    private ArrayList<LatLngLines> verticalLines;

    private LocationManager locationManager;
    private String bestProvider;
    private final int minTime = 20000; // milliseconds
    private final int minDistance = 3; // meters

    private OnLocationChangedListener listener;

    private HashMap<TileID, Tile> tiles;

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
        initializeMap();
    }


    /**
     * Initializes the google map with tiles and location.
     */
    private void initializeMap() {
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
        // the last known latitude and longitude of the user
        double lat = locationManager.getLastKnownLocation(bestProvider).getLatitude();
        double lng = locationManager.getLastKnownLocation(bestProvider).getLongitude();
        // make a locationID from that to find the center tile
        LocationID id = LocationToID(new LatLng(lat, lng));
        // set the current LatID and LngID to the users position
        currentLatID = id.getLatID();
        currentLngID = id.getLngID();
        System.out.println("lat, lng: " + lat + "," + lng + " current location id: " + id.printID() + "\nset location id: 24413, -43831");
        System.out.println();
        //create the new colour set to shade the tiles
        colors = new ColorSet();
        // the vertical lines that are drawn on the screen
        parallelLines = new ArrayList<LatLngLines>();
        verticalLines = new ArrayList<LatLngLines>();
        // the bounds of the tiles to render
        NorthBoundLat = (currentLatID + bdUnit) * latTileUnit;
        SouthBoundLat = (currentLatID - bdUnit) * latTileUnit;
        WestBoundLng = (currentLngID - bdUnit) * lngTileUnit;
        EastBoundLng = (currentLngID + bdUnit) * lngTileUnit;
        //the hashmap containing all the tiles
        tiles = new HashMap<>();
    }

    /**
     * Changes the UISettings of the Google Map.
     */
    private void mapSettingInit() {
       UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);
        settings.setZoomControlsEnabled(false);
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
        mapSettingInit();
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


    /**
     * Creates the style options for the map.
     * @param styleName - the type of style
     * @return the options for the map
     */
    private MapStyleOptions createMapStyle(String styleName) {
        MapStyleOptions style;
        if (styleName == "retro")
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        else
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        return style;
    }

    /**
     * Gets the View Bounds of the map.
     * @return the view bounds
     */
    private LatLngBounds getMapViewBounds() {
        LatLngBounds bounds;
        double boundUnit = 0.01;
        // set upper left corner and bottom right coordinates
        LatLng UL = new LatLng(currentLoc.latitude - boundUnit, currentLoc.longitude - boundUnit);
        LatLng BR = new LatLng(currentLoc.latitude + boundUnit, currentLoc.longitude + boundUnit);
        // final the bounds
        bounds = new LatLngBounds(UL, BR);
        currentBounds = bounds;
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

    /**
     * Draws a polygon on the map
     * @param map the google map
     * @param loc the starting location to draw
     * @param width the width of the polygon
     * @param height the height of the polygon
     * @param color the ColorSet colour of the polygon
     * @return the created polygon
     */
    private Polygon drawPolygon(GoogleMap map, LatLng loc, double width, double height, int color) {
        Polygon polygon = map.addPolygon(new PolygonOptions()
                .addAll(createRectangle(loc, width / 2, height / 2))
                .fillColor(color)
                .strokeWidth(0)
                .clickable(true)
                .strokeColor(Color.argb(0, 255, 255, 255)));
        return polygon;
    }

    /**
     * Draws a line on the map
     * @param map the map
     * @param A the starting point
     * @param B the end point
     */
    private void drawPolyline(GoogleMap map, LatLng A, LatLng B) {
        Polyline line = map.addPolyline((new PolylineOptions()
                .add(A, B))
                .color(Color.argb(150, 0, 0, 0))
                .width(2));
    }

    /**
     * Populates the horizontal Lines to draw on the map.
     * @return true if successful
     */
    private boolean populateHorizontalLines() {
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

    /**
     * Populates the Vertical Lines to draw on the map.
     * @return true if successful
     */
    private boolean populateVerticalLines() {
        for (double s = WestBoundLng; s < EastBoundLng; s += lngTileUnit) {
            LatLng A = new LatLng(SouthBoundLat, s);
            LatLng B = new LatLng(NorthBoundLat, s);
            verticalLines.add(new LatLngLines(A, B));
        }
        if (verticalLines.isEmpty())
            return false;
        else
            return true;
    }

    /**
     * Draws the tile grid on the map.
     * @param mMap the Google Map
     * @return true if successful
     */
    private boolean DrawTiles(GoogleMap mMap) {
        // flags to determine if the lines were generated
        boolean h_flag = false;
        boolean v_flag = false;
        if (populateHorizontalLines()) {
            //show("getHorizontalLines");
            Iterator<LatLngLines> iterator = parallelLines.iterator();
            while (iterator.hasNext()) {
                LatLngLines lines = iterator.next();
                drawPolyline(mMap, lines.getA(), lines.getB());
            }
            h_flag = true;
        } else {
            show("Tiles Generation failed!");
        }
        if (populateVerticalLines()) {
            Iterator<LatLngLines> iterator = verticalLines.iterator();
            while (iterator.hasNext()) {
                LatLngLines lines = iterator.next();
                drawPolyline(mMap, lines.getA(), lines.getB());
            }
            v_flag = true;
        } else {
            show("Tiles Generation failed!");
        }
        return (h_flag && v_flag);
    }

    /**
     * Shifts the location by one tile.
     * @param point the location
     * @param lat the latitude
     * @param lng the longitude
     * @return the displaced latlng
     */
    public LatLng shifter(LatLng point, double lat, double lng) {
        return new LatLng(point.latitude + lat, point.longitude + lng);
    }

    /**
     * Gets a LocationID object from a latlng
     * @param loc the location
     * @return the LocationID
     */
    private LocationID LocationToID(LatLng loc){
        int LatID = (int)(loc.latitude/latTileUnit) + 1;
        int LngID = (int)(loc.longitude/lngTileUnit) ;
        return new LocationID(LatID,LngID);
    }

    /**
     * Gets the latitude longitude of the location ID
     * @param newID the location ID
     * @return the location of that ID
     */
    private LatLng IdTOLocation(LocationID newID){
        double lat = (newID.getLatID() - 1) * latTileUnit + latTileUnit/ 2.0;
        double lng = (newID.getLngID()) * lngTileUnit + lngTileUnit / 2.0 -lngTileUnit;
        System.out.println("converted lat: " + lat + " converted lng: " + lng);
        LatLng loc = new LatLng(lat,lng);
        return loc;
    }


    /**
     * Result of requesting permissions.
     * @param requestCode the request code
     * @param permissions the permission
     * @param grantResults the result
     */
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

    /**
     * Resume of map fragment.
     */
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


    /**
     * Handler for the location button click.
     * @return
     */
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    /**
     * Draws the tiles on the map within the bdUnit.
     * @param mMap the Google Map
     * @param newLoc the center location
     */
    public void DrawPolygonDemo(GoogleMap mMap, LatLng newLoc) {
        //draw tile user is in
        LocationID id = LocationToID(shifter(newLoc, 0, 0));
        TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, getApplicationContext());
        // draw tiles in cross from the user
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
    }


    /**
     * A wrapper to make a toast.
     * @param str the toast message
     */
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


    /**
     * When the user is connected to the google map this is called. It updates the last location.
     * @param bundle
     */
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

    /**
     * Activate the location listener
     * @param onLocationChangedListener the location listener
     */
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
        try {
            JSONObject jsonObject = new JSONObject(result);
            String tileLatID = jsonObject.getString("tileLatID");
            String tileLngID = jsonObject.getString("tileLngID");
            String tileUsername = jsonObject.getString("username");
            Toast.makeText(this, tileLatID + tileLngID + tileUsername, Toast.LENGTH_LONG).show();
            LocationID latLng = new LocationID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
            System.out.println("tile username: " + tileUsername + " username " + LoginActivity.username);
            Tile t;
            if(tileUsername.equals("null")) {
                updateTile(colors.gray, tileLatID, tileLngID);
            }
            else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
                updateTile(colors.green, tileLatID, tileLngID);
            } else {
                System.out.println("RED tile username: " + tileUsername + " length: " + tileUsername.length() +  " username " + LoginActivity.username + " length: " + LoginActivity.username.length());
                updateTile(colors.red, tileLatID, tileLngID);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Updates the tile in the Hash Map based on its ID
     * @param colour the colour of the new tile
     * @param tileLatID the latID of the tile
     * @param tileLngID the LngID of the tile
     */
    private void updateTile(int colour, String tileLatID, String tileLngID) {
        Tile t;
        TileID tileID = new TileID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
        LocationID latLng = new LocationID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
        if ((t = tiles.get(tileID)) != null) {
            //remove the previous tile
            t.remove();
            t.setPolygon(drawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colour));
        } else {
            t = new Tile(
                    tileID,
                    null,
                    drawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colour));
            tiles.put(tileID, t);
        }
    }
}


/**
 * Holds points for the polylines.
 */
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

/**
 * The colour pallet.
 */
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

/**
 * The LocationID class. Stores the ID of a latlng.
 */
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

/**
 * A Tile Class. Holds relavant information for a tile drawn on the screen.
 */
class Tile {
    private TileID id;
    private String username;
    private Polygon polygon;

    Tile(TileID id, String username, Polygon polygon) {
        this.id = id;
        this.username = username;
        this.polygon = polygon;
    }

    public TileID getTileID() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon p) {
        this.polygon = p;
    }

    public void remove() {
        polygon.remove();
    }
}

/**
 * The unique ID for a tile.
 */
class TileID {
    private int latID, lngID;
    public TileID(int latID, int lngID) {
        this.latID = latID;
        this.lngID = lngID;
    }

    @Override
    public int hashCode() {
        String hash = Integer.toString(latID) + Integer.toString(lngID);
        return hash.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TileID)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        TileID compareObject = (TileID) obj;
        return (compareObject.lngID == this.lngID && compareObject.latID == this.latID);
    }
}