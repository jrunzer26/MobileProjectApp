package com.mobileproject.game;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class GameMapUI extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        LocationSource,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AsyncResponse {
    public static boolean mapLock = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float MIN_ZOOM_PREF = 15f;
    private static final float MAX_ZOOM_PREF = 18f;
    private Criteria criteria;
    private UpdateMapThread taskThread;

    protected static final String TAG = "MainActivity";

    protected GoogleApiClient mGoogleApiClient;
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private Location mLastLocation;

    public static int currentLatID;
    public static int currentLngID;
    private LatLng currentLoc;
    private LatLngBounds currentBounds;

    private double NorthBoundLat;
    private double SouthBoundLat;
    private double WestBoundLng;
    private double EastBoundLng;

    public static final double bdUnit = 6; // number of tiles in each direction
    public static final double latTileUnit = 0.0018; // length of a tile
    public static final double lngTileUnit = 0.0018; // width of a tile

    private ColorSet colors;

    private ArrayList<LatLngLines> parallelLines;
    private ArrayList<LatLngLines> verticalLines;

    private LocationManager locationManager;
    private String bestProvider;
    private final int minTime = 20000; // milliseconds
    private final int minDistance = 2; // meters

    private OnLocationChangedListener listener;

    public HashMap<Tile.TileID, Tile> tiles;
    public ArrayList<Polyline> lines;
    private boolean initalizeMap = false;
    private boolean threadDone = false;

    private MediaPlayer buttonClick;
    private MediaPlayer bgMusic;
    private User user;

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
        UserDBHelper userDBHelper = new UserDBHelper(this);
        user = userDBHelper.getUser(LoginActivity.username);
        if (user == null) {
            user = new User(LoginActivity.username);
        }



        listenerInit();

    }

    /**
     * Updates the HUB with the users resources
     * @param user - the user's resources to update the HUD with
     */
    private void updateHUD(User user) {
        show("saved gold: " + user.getGold() + " food: " + user.getFood() + " soldiers available: "
                    + user.getSoldiersAvailable() + " totalSoldiers: " + user.getTotalSoldiers());
        System.out.println("update HUD");

    }

    /**
     * Shows what the user has gained/lost when logging in again
     * @param user the current user's resources
     * @param serverUser the same user but with updated resources from the server
     */
    private void showResourcesChanged(User user, User serverUser) {
        int addedFood = serverUser.getFood() - user.getFood();
        int addedGold = serverUser.getGold() - user.getGold();
        int changedTiles = user.getTiles() - serverUser.getTiles();
        int changedSoldiers = user.getTotalSoldiers() - serverUser.getTotalSoldiers();

        if (addedFood > 0) {
            show("you gained: " + addedFood + " food");
        }
        if (addedGold > 0) {
            show("you gained: " + addedGold + " gold");
        }
        if (changedSoldiers < 0) {
            show("you lost: " + changedSoldiers + " soldiers");
        }
        if (changedTiles < 0) {
            show("you lost: " +  -1 * changedTiles + " tile(s)");
        }

        show("resources changed");
        this.user = serverUser;
    }



    @Override
    protected void onPause() {
        super.onPause();
        bgMusic.pause();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        bgMusic.start();
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (initalizeMap)
                locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, this);
        }
    }

    /**
     * Initialization for all Listener
     */
    private void listenerInit(){
        CheckBox debugMode = (CheckBox)findViewById(R.id.ckbDebug);
        CheckBox bgMuiscMode = (CheckBox)findViewById(R.id.ckbBgMusic);

        final AsyncResponse battleCall = this;
        final Context context = this;
        final TextView debugBar = (TextView)findViewById(R.id.debug);

        debugMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    debugBar.setVisibility(View.VISIBLE);
                } else {
                    debugBar.setVisibility(View.GONE);
                }
            }
        });

        bgMuiscMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    bgMusic.start();
                }else {
                    bgMusic.pause();
                }
            }
        });
    }

    /**
     * Initializes the google map with tiles and location.
     */
    private void initializeMap() {
        buttonClick = MediaPlayer.create(getApplicationContext(), R.raw.soho);
        // Set map initial location here:
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // set the criteria of the provider
        criteria = new Criteria();
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
        // the hashmap containing all the tiles
        tiles = new HashMap<>();
    }

    /**
     * Sets up the user with it's last known location.
     */
    private void setupFirstLocation() {
        initalizeMap = true;
        // the last known latitude and longitude of the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        double lat = locationManager.getLastKnownLocation(bestProvider).getLatitude();
        double lng = locationManager.getLastKnownLocation(bestProvider).getLongitude();
        // make a locationID from that to find the center tile
        Tile.TileID id = new Tile.TileID(new LatLng(lat, lng));
        // set the current LatID and LngID to the users position
        currentLatID = id.getLatID();
        currentLngID = id.getLngID();
        //System.out.println("lat, lng: " + lat + "," + lng + " current location id: " + id.printID() + "\nset location id: 24413, -43831");
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
        // collect the users resources
        TileWebserviceUtility.collectResources(LoginActivity.username, LoginActivity.password, this, this);
        TileWebserviceUtility.getUser(LoginActivity.username, LoginActivity.password, this, this);
        DrawTiles(mMap);
        // Drawing the titles: line length in KMs
        // update the map every 30 seconds
        taskThread = new UpdateMapThread();
        taskThread.start();
        updateHUD(user);
    }

    class UpdateMapThread extends Thread {

        @Override
        public void run() {
           while(!threadDone) {
               UpdateTilesAsync updateTilesAsync = new UpdateTilesAsync(tiles, new LatLng(currentLatID * latTileUnit - latTileUnit / 2, currentLngID * lngTileUnit - lngTileUnit / 2), getApplicationContext(), mMap, colors);
               updateTilesAsync.start();
               try {
                   Thread.sleep(45000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        }
    }


    /**
     * Changes the UISettings of the Google Map.
     */
    private void mapSettingInit() {
        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);
        settings.setZoomControlsEnabled(false);
        mMap.setMinZoomPreference(MIN_ZOOM_PREF);
        mMap.setMaxZoomPreference(MAX_ZOOM_PREF);
        //mMap.setLatLngBoundsForCameraTarget(getMapViewBounds());

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


        setOnPolygonClickable(mMap);

        updateNotification("Notification System Sample Text Here This is juat a example text so don't feel asasasas",200);
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
     * Make Camera back to current location
     */
    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bestProvider == null) {
            bestProvider = locationManager.getBestProvider(criteria, true);
        } else {
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, MAX_ZOOM_PREF);
                mMap.animateCamera(cameraUpdate);
            } else {
                finish();
                startActivity(getIntent());
            }
        }
    }


    /**
     * Creates a button event handlers set
     */
    public void process(View view){
        // Play clicker sound
        buttonClick.start();
        switch (view.getId()){
            case R.id.menuIMButton:
                RelativeLayout layout1 =(RelativeLayout)findViewById(R.id.internalMenu);
                if(layout1.getVisibility()==View.VISIBLE){
                    layout1.setVisibility(View.GONE);
                } else {
                    layout1.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btnIMBack:
                RelativeLayout layout2 =(RelativeLayout)findViewById(R.id.internalMenu);
                layout2.setVisibility(View.GONE);
                break;
            case R.id.btnBackLocation:
                getMyLocation();
            default:
                break;
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
        //LatLng northeast = new LatLng(mLastLocation.getLatitude() + boundUnit, mLastLocation.getLongitude() - boundUnit);
        //LatLng southwest = new LatLng(mLastLocation.getLatitude() - boundUnit, mLastLocation.getLongitude() + boundUnit);
        // finalize the bounds
        bounds = new LatLngBounds(new LatLng(SouthBoundLat,WestBoundLng), new LatLng(NorthBoundLat,EastBoundLng));
        currentBounds = bounds;
        return bounds;
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
     * A wrapper to make a toast.
     * @param str the toast message
     */
    private void show(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        //mGoogleApiClient.connect();
        CheckBox bgMusicMode = (CheckBox)findViewById(R.id.ckbBgMusic);
        if(bgMusicMode.isChecked()){
            bgMusic = MediaPlayer.create(this,R.raw.jocsnight);
            bgMusic.setLooping(true);
            bgMusic.start();
        }
        super.onStart();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        // save the state of the user
        UserDBHelper dbHelper = new UserDBHelper(this);
        dbHelper.saveUser(user);
        bgMusic.pause();
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
            //("Last Location:"+String.valueOf(lat)+"  "+String.valueOf(lng));
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


    /**
     * Executed when the location of the user changed
     * @param location - the newest location of the user
     */
    @Override
    public void onLocationChanged(Location location) {
        // added to ensure that the map is animated
        if (listener != null) {
            listener.onLocationChanged(location);
        }
        if (!initalizeMap) {
            setupFirstLocation();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


        Tile.TileID tileID = new Tile.TileID(latLng);
        if (currentLatID != tileID.getLatID() || currentLngID != tileID.getLngID()) {
            currentLatID = tileID.getLatID();
            currentLngID = tileID.getLngID();
            Tile t;
            if ((t = tiles.get(new Tile.TileID(currentLatID, currentLngID))) != null) {
                if (t.getUsername() == null) {
                    // capture the tile if it is unoccupied - has a chance to turn red if another person requests first
                    Utilities.updateTile(colors.green, currentLatID, currentLngID, LoginActivity.username, mMap, tiles, 0, 0, 0);
                    TileWebserviceUtility.captureTile(currentLatID, currentLngID, LoginActivity.username, LoginActivity.password, this, getApplicationContext());
                }
            }
        }

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
     * Buys soldiers for the given tile.
     * @param tileID - the tile to place the soldiers on
     * @param soldiers - the number of soldiers to buy
     */
    private void buySoldiers(Tile.TileID tileID, int soldiers) {
        //check if they have enough money,
        // post to the server
        TileWebserviceUtility.buySoldiers(LoginActivity.username, LoginActivity.password, tileID.getLatID(), tileID.getLngID(), soldiers, this, this);
    }

    /**
     * Processes the result from the server
     * @param result - the json from the server
     */
    @Override
    public void processResult(String result) {
        System.out.println("Result from server: " + result);
        String[] serverResult = result.split(";");
        try {
            JSONObject jsonObject = new JSONObject(serverResult[1]);
            if (serverResult[0].equals("1")) {
                handleIncomingTile(jsonObject);
            } else if (serverResult[0].equals("2")) {
                String goldObtained = jsonObject.getString("goldObtained");
                String foodObtained = jsonObject.getString("foodObtained");
                //show("gold obtained: " + goldObtained + " food obtained: " + foodObtained);
            } else if (serverResult[0].equals("3")) {
                handleIncomingUser(jsonObject);

            } else if (serverResult[0].equals("4")) {
                if (jsonObject.has("err")) {
                    // not enough gold or does not own the tile
                    show(jsonObject.getString("err"));
                } else {
                    show("soldiers added!");
                    handleIncomingTile(jsonObject);
                }
            } else if (serverResult[0].equals("5")) {
                show("battle results!!!");
                show(serverResult[1]);
                System.out.println("battle results");
                System.out.println(serverResult[1]);
                JSONArray tileArray = jsonObject.getJSONArray("tiles");
                show("new food: " + jsonObject.getString("food"));
                JSONObject tile1 = tileArray.getJSONObject(0);
                JSONObject tile2 = tileArray.getJSONObject(1);
                if (tile2.getString("username").equals("null")) {
                    show("you won!");
                } else {
                    show("oh no you lost the battle!");
                    // compare the tiles before and after here and notify the user.
                }
                handleIncomingTile(tile1);
                handleIncomingTile(tile2);
            }
            mapLock = false;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles incoming user data from the server on the UI thread
     * @param jsonObject the user json
     * @throws JSONException
     */
    private void handleIncomingUser(JSONObject jsonObject) throws JSONException {
        int gold = Integer.parseInt(jsonObject.getString("gold"));
        int food = Integer.parseInt(jsonObject.getString("food"));
        int tiles = Integer.parseInt(jsonObject.getString("tiles"));
        int tilesTaken = Integer.parseInt(jsonObject.getString("tilesTaken"));
        int soldiers = jsonObject.getInt("totalSoldiers");
        int goldObtained = jsonObject.getInt("goldObtained");
        int foodObtained = jsonObject.getInt("foodObtained");
        int totalGoldObtained = jsonObject.getInt("totalGoldObtained");
        int totalFoodObtained = jsonObject.getInt("totalFoodObtained");
        showResourcesChanged(user, new User("", gold, food, tiles, tilesTaken, goldObtained,
                foodObtained, totalGoldObtained, totalFoodObtained, soldiers, 0));
    }

    /**
     * Handels an incoming tile and updates the map in the main UI thread.
     * @param jsonObject the tile from the server
     * @throws JSONException
     */
    private void handleIncomingTile(JSONObject jsonObject) throws JSONException {
        int tileLatID = jsonObject.getInt("tileLatID");
        int tileLngID = jsonObject.getInt("tileLngID");
        String tileUsername = jsonObject.getString("username");
        int soldiers = jsonObject.getInt("soldiers");
        int food = jsonObject.getInt("food");
        int gold = jsonObject.getInt("gold");
        System.out.println("tile username: " + tileUsername + " username " + LoginActivity.username);
        Tile t;
        if (tileUsername.equals("null")) {
            if (currentLngID == tileLngID && currentLatID == tileLatID)
                TileWebserviceUtility.captureTile(tileLatID, tileLngID, LoginActivity.username, LoginActivity.password, this, getApplicationContext());
            Utilities.updateTile(colors.gray, tileLatID, tileLngID, null, mMap, tiles, soldiers, gold, food);
        } else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
            Utilities.updateTile(colors.green, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
        } else {
            Utilities.updateTile(colors.red, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
        }
    }


    /**
     * Showing the debugging information on the top of game.
     * @param msg
     */
    private void showDebug(String msg) {
        TextView text = (TextView)findViewById(R.id.debug);
        text.setText(msg);
    }

    /**
     * Add the On Polygon Listener to the Map.
     * @param map
     */
    private void setOnPolygonClickable(GoogleMap map){
        map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                LatLng point = polygon.getPoints().get(2);
                Tile.TileID tid = new Tile.TileID(point);
                Tile t = tiles.get(tid);
                if (t.select()) {
                    // selected - get username etc..
                } else {
                    // unselect
                }
                t.drawTile(mMap);
                showDebug(polygon.getPoints().get(0).toString()+polygon.getPoints().get(1).toString()+polygon.getPoints().get(2).toString()+polygon.getPoints().get(3).toString());
            }
        }
        );

    }

    /**
     * Update ingame notification system
     * @param msg the content message need to display inside a buble
     * @param delay the delay of each character typing anmiation
     */
    private void updateNotification(String msg ,long delay){
        Typewriter text = (Typewriter) findViewById(R.id.txtNotifiction);
        text.setVisibility(View.VISIBLE);
        text.setCharacterDelay(delay);
        text.animateText(msg);
    }

}


/**
 * The colour pallet.
 */
class ColorSet {
    public int green, red, blue, gray, orange, yellow;

    ColorSet() {
        green = Color.argb(100, 0, 255, 0);
        red = Color.argb(100, 255, 0, 0);
        blue = Color.argb(100, 0, 0, 255);
        gray = Color.argb(100, 100, 100, 100);
        orange = Color.argb(100, 255, 165, 0);
        yellow = Color.argb(200,255,255,0);
    }
}



/**
 * A Typewriter Effect textview type class, achieve a typing animation.
 */

class Typewriter extends TextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500; //Default 500ms delay

    public Typewriter(Context context) {
        super(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}



