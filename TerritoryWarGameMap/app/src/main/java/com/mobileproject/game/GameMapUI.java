package com.mobileproject.game;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


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
    private static final float MIN_ZOOM_PREF = 16f;
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
    private Location currentLoc;
    private LatLngBounds currentBounds;

    private double NorthBoundLat;
    private double SouthBoundLat;
    private double WestBoundLng;
    private double EastBoundLng;

    public static final double bdUnit = 5; // number of tiles in each direction
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
    private LinkedList<Tile.TileID> tilesSelected;
    public ArrayList<Polyline> lines;
    private boolean initalizeMap = false;
    private boolean threadDone = false;

    private MediaPlayer buttonClick;
    private MediaPlayer bgMusic;
    private User user;
    private Marker marker;

    private boolean notificationON;

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
        userDBHelper.showUsers();
        user = userDBHelper.getUser(LoginActivity.username);
        if (user == null) {
            user = new User(LoginActivity.username);
        } else
        setResourceBar(this.user);
        setUsername(user.getUsername());
        //create the new colour set to shade the tiles
        colors = new ColorSet();
        MenuListenerInit();
    }


    /**
     * Shows what the user has gained/lost when logging in again
     * @param user the current user's resources
     * @param serverUser the same user but with updated resources from the server
     */
    private void showResourcesChanged(User user, User serverUser) {
        int addedFood = serverUser.getFood() - user.getFood();
        int addedGold = serverUser.getGold() - user.getGold();
        int changedTiles = serverUser.getTiles() - user.getTiles();
        int changedSoldiers = serverUser.getTotalSoldiers() - user.getTotalSoldiers();

        StringBuilder updateString = new StringBuilder();
        if (addedFood > 0) {
            updateString.append(getString(R.string.gameUI_foodChanged, addedFood));
            updateString.append(" ");
        }
        if (addedGold > 0) {
            updateString.append(getString(R.string.gameUI_goldChanged, addedGold));
            updateString.append(" ");
        }
        if (changedSoldiers < 0) {
            updateString.append(getString(R.string.gameUI_soldiersChanged, -1 * changedSoldiers));
            updateString.append(" ");
        } else if (changedSoldiers > 0) {
            updateString.append(getString(R.string.gameUI_soldiersAdded, changedSoldiers));
            updateString.append(" ");
        }
        if (changedTiles < 0) {
            updateString.append(getString(R.string.gameUI_tilesChanged, changedTiles));
            updateString.append(" ");
        } else if (changedTiles > 0) {
            updateString.append(getString(R.string.gameUI_tilesCaptured));
            updateString.append(" ");
        }
        if (updateString.length() > 0)
            updateNotification(updateString.toString(), 50, 9000);
        this.user = serverUser;
    }



    @Override
    protected void onPause() {
        super.onPause();
        bgMusic.pause();
        UserDBHelper dbHelper = new UserDBHelper(this);
        dbHelper.saveUser(user);
        threadDone = true;
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
        threadDone = false;
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (initalizeMap)
                locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, this);
        }
        if (mMap != null) {
            new UpdateMapThread(this, this).start();
        }
    }

    /**
     * Initialization for all Listener for the internal game menus
     */
    private void MenuListenerInit(){
        notificationON = true;
        Switch debugMode = (Switch)findViewById(R.id.switchDebug);
        Switch bgMuiscMode = (Switch)findViewById(R.id.switchBgMusic);
        Switch notificationMode = (Switch)findViewById(R.id.switchNotification);
        Switch nightMode = (Switch)findViewById(R.id.switchNightMode);
        final TextView debugBar = (TextView)findViewById(R.id.debug);
        final FrameLayout notification = (FrameLayout)findViewById(R.id.notificationSystem);
        final ImageView bg = (ImageView)findViewById(R.id.imgBgg);

        debugMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    debugBar.setVisibility(View.VISIBLE);
                } else {
                    debugBar.setVisibility(View.GONE);
                }
            }
        });

        bgMuiscMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    bgMusic.start();
                }else {
                    bgMusic.pause();
                }
            }
        });

        notificationMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    notificationON = true;
                    notification.setVisibility(View.VISIBLE);
                } else {
                    notificationON = false;
                    notification.setVisibility(View.GONE);
                }
            }
        });


        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    mMap.setMapStyle(createMapStyle("night"));
                    bg.setVisibility(View.GONE);
                } else {
                    mMap.setMapStyle(createMapStyle("retro"));
                    bg.setVisibility(View.VISIBLE);
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
        tilesSelected = new LinkedList<>();
    }

    /**
     * Sets up the user with it's last known location.
     */
    private void setupFirstLocation() {
        initalizeMap = true;
        // the last known latitude and longitude of the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        double lat = locationManager.getLastKnownLocation(bestProvider).getLatitude();
        double lng = locationManager.getLastKnownLocation(bestProvider).getLongitude();
        // make a locationID from that to find the center tile
        Tile.TileID id = new Tile.TileID(new LatLng(lat, lng));
        // set the current LatID and LngID to the users position
        currentLatID = id.getLatID();
        currentLngID = id.getLngID();
        // Drawing the titles: line length in KMs
        // update the map every 30 seconds
        taskThread = new UpdateMapThread(this, this);
        taskThread.start();
        // the vertical lines that are drawn on the screen
        parallelLines = new ArrayList<LatLngLines>();
        verticalLines = new ArrayList<LatLngLines>();
        // the bounds of the tiles to render
        NorthBoundLat = (currentLatID + bdUnit) * latTileUnit - latTileUnit;
        SouthBoundLat = (currentLatID - bdUnit) * latTileUnit;
        WestBoundLng = (currentLngID - bdUnit) * lngTileUnit;
        EastBoundLng = (currentLngID + bdUnit) * lngTileUnit - lngTileUnit;
        // collect the users resources
        TileWebserviceUtility.collectResources(LoginActivity.username, LoginActivity.password, this, this);
        TileWebserviceUtility.getUser(LoginActivity.username, LoginActivity.password, this, this);
        DrawTiles(mMap);
        setResourceBar(user);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                attemptCaptureTile();
            }
        }, 5000);
    }

    class UpdateMapThread extends Thread {
        private AsyncResponse callback;
        private Context context;
        public UpdateMapThread(AsyncResponse callback, Context context) {
            this.callback = callback;
            this.context = context;
        }
        @Override
        public void run() {
           while(!threadDone) {
               UpdateTilesAsync updateTilesAsync = new UpdateTilesAsync(tiles, new LatLng(currentLatID * latTileUnit - latTileUnit / 2, currentLngID * lngTileUnit - lngTileUnit / 2), getApplicationContext(), mMap, colors);
               TileWebserviceUtility.getUser(LoginActivity.username, LoginActivity.password, callback, context);
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
        settings.setMapToolbarEnabled(false);

        mMap.setMinZoomPreference(MIN_ZOOM_PREF);
        mMap.setMaxZoomPreference(MAX_ZOOM_PREF);
        //mMap.setLatLngBoundsForCameraTarget(getMapViewBounds());


        // Customize the marker info window

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {

                View info = getLayoutInflater().inflate(R.layout.tile_info_window, null);
                info.setY(100);

                FrameLayout parent = (FrameLayout)info.findViewById(R.id.viewTileInfoWindow);

                TextView snippet = new TextView(getApplicationContext());
                snippet.setGravity(Gravity.CENTER_HORIZONTAL);
                snippet.setTextSize(15);
                snippet.setTextColor(Color.BLACK);
                snippet.setText(marker.getSnippet());

                LinearLayout content = new LinearLayout(getApplicationContext());
                content.setOrientation(LinearLayout.VERTICAL);
                content.setPadding(150,40,150,150);
                content.setGravity(Gravity.CENTER_HORIZONTAL);


                content.addView(snippet);
                parent.addView(content);

                return info;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
            }
        });
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
        // Set the map style here:
        mMap.setMapStyle(createMapStyle("retro"));
        // Get current location:
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();


        setOnPolygonClickable(mMap);

        // say something to welcome new player:
        updateNotification(getString(R.string.welcome_banner),100,5000);
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
                break;
            case R.id.btnStartStat:
                showStatPanel(0);
                break;
            case R.id.btnStatClose:
                showStatPanel(1);
                break;
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
        if (styleName == "retro"){
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_retro);
        }
        else if(styleName == "night"){
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode);
        } else {
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_retro);
        }
        return style;
    }

    /**
     * set the View Bounds of the map.
     */
    private void setMapViewBounds(LatLng loc) {

        LatLngBounds bounds;
        double boundUnit = (bdUnit * latTileUnit) / 10;

        // set upper left corner and bottom right coordinates
        LatLng northeast = Utilities.shifter(loc,+boundUnit,+boundUnit);
        LatLng southwest = Utilities.shifter(loc,-boundUnit,-boundUnit);

        showDebug("Bounds: NE-"+northeast.toString()+"  SW-"+southwest.toString());

        // finalize the bounds
        bounds = new LatLngBounds(southwest,northeast);
        currentBounds = bounds;
        mMap.setLatLngBoundsForCameraTarget(bounds);
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
        for (double s = SouthBoundLat; s < NorthBoundLat+latTileUnit; s += latTileUnit) {
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
        for (double s = WestBoundLng; s < EastBoundLng+lngTileUnit; s += lngTileUnit) {
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
            Iterator<LatLngLines> iterator = parallelLines.iterator();
            while (iterator.hasNext()) {
                LatLngLines lines = iterator.next();
                drawPolyline(mMap, lines.getA(), lines.getB());
            }
            h_flag = true;
        } else {
            show(getString(R.string.gameUI_tileGenFail));
        }
        if (populateVerticalLines()) {
            Iterator<LatLngLines> iterator = verticalLines.iterator();
            while (iterator.hasNext()) {
                LatLngLines lines = iterator.next();
                drawPolyline(mMap, lines.getA(), lines.getB());
            }
            v_flag = true;
        } else {
            show(getString(R.string.gameUI_tileGenFail));
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
        } else {
            // restart the application : http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }

    /**
     * Resume of map fragment.
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            mPermissionDenied = false;
        }
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
        Switch bgMusicMode = (Switch)findViewById(R.id.switchBgMusic);
        if(bgMusicMode.isChecked()){
            bgMusic = MediaPlayer.create(this,R.raw.jocsnight);
            bgMusic.setLooping(true);
            bgMusic.start();
        }
        setResourceBar(user);
        super.onStart();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        // save the state of the user
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
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            LatLng latlng = new LatLng(lat,lng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
            mMap.animateCamera(cameraUpdate);
            setMapViewBounds(latlng);
        } else {
            show(getString(R.string.gameUI_noLocation));
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
            currentLoc = location;
            //setMapViewBounds(new LatLng(location.getLatitude(),location.getLongitude()));
            listener.onLocationChanged(location);
        }
        if (!initalizeMap) {
            setupFirstLocation();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        setMapViewBounds(latLng);

        Tile.TileID tileID = new Tile.TileID(latLng);
        if (currentLatID != tileID.getLatID() || currentLngID != tileID.getLngID()) {
            currentLatID = tileID.getLatID();
            currentLngID = tileID.getLngID();
            attemptCaptureTile();
        }

    }

    private void attemptCaptureTile() {
        Tile t = tiles.get(new Tile.TileID(currentLatID, currentLngID));
        if (t != null) {
            if (t.getUsername() == null) {
                // capture the tile if it is unoccupied - has a chance to turn red if another person requests first
                //Utilities.updateTile(colors.green, currentLatID, currentLngID, LoginActivity.username, mMap, tiles, 0, 0, 0);
                TileWebserviceUtility.captureTile(currentLatID, currentLngID, LoginActivity.username, LoginActivity.password, this, getApplicationContext());
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
        //creaseResourceBar(soldiers,-10,0);
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
                // pending feature!
                String goldObtained = jsonObject.getString("goldObtained");
                String foodObtained = jsonObject.getString("foodObtained");
            } else if (serverResult[0].equals("3")) {
                handleIncomingUser(jsonObject);

            } else if (serverResult[0].equals("4")) {
                if (jsonObject.has("err")) {
                    // not enough gold or does not own the tile
                    show(jsonObject.getString("err"));
                } else {
                    TileWebserviceUtility.getUser(LoginActivity.username, LoginActivity.password, this, this);
                    handleIncomingTile(jsonObject);
                    int tileLatID = jsonObject.getInt("tileLatID");
                    int tileLngID = jsonObject.getInt("tileLngID");
                    Tile.TileID tileID= new Tile.TileID(tileLatID, tileLngID);
                    addTileInfoWindow(tileID.getLatLng());
                }
            } else if (serverResult[0].equals("5")) {
                JSONArray tileArray = jsonObject.getJSONArray("tiles");
                JSONObject tile1 = tileArray.getJSONObject(0);
                JSONObject tile2 = tileArray.getJSONObject(1);
                addTileInfoWindow(new Tile.TileID(tile2.getInt("tileLatID"),
                        tile2.getInt("tileLngID")).getLatLng());
                if (tile2.getString("username").equals("null")) {
                    updateNotification(getString(R.string.gameUI_wonBattle), 50, 1000);
                } else {
                    updateNotification(getString(R.string.gameUI_defeatedBattle), 50, 1000);
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
        User newUser = new User(LoginActivity.username, gold, food, tiles, tilesTaken, goldObtained,
                foodObtained, totalGoldObtained, totalFoodObtained, soldiers, 0);
        showResourcesChanged(user, newUser);
        user = newUser;
        setResourceBar(newUser);
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
            Utilities.updateTile(colors.gray, tileLatID, tileLngID, null, mMap, tiles, soldiers, gold, food);
        } else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
            System.out.println("updating soldiers: " + soldiers);
            Utilities.updateTile(colors.green, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
        } else {
            Utilities.updateTile(colors.red, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
        }
    }


    /**
     * Showing the debugging information on the top of game.
     * @param msg the messages needed to be display
     */
    private void showDebug(String msg) {
        TextView text = (TextView)findViewById(R.id.debug);
        text.setText(msg);
    }


    /**
     * Showing the tile information in a pops-out floating window above the selected tile.
     * @param location where to show the window.
     */
    private void addTileInfoWindow(LatLng location){
        if(marker!=null) {
            // if marker is there, just remove it.
            marker.remove();
        }
        // find the tile id for this tile.
        Tile.TileID tid = new Tile.TileID(location);
        Tile tile = tiles.get(tid);

        if (tile.getUsername() != null && tile.getUsername().equals(LoginActivity.username))
            setBuySoliderPopsout(tid,true);

        StringBuilder snippetValue = new StringBuilder();
        snippetValue.append("\n");
        snippetValue.append(getString(R.string.gameUI_food, tile.getFood()));
        snippetValue.append("\n");
        snippetValue.append(getString(R.string.gameUI_gold, tile.getGold()));
        snippetValue.append("\n");
        snippetValue.append(getString(R.string.gameUI_soldiers, tile.getSoldiers()));
        marker = mMap.addMarker(new MarkerOptions()
                .position(Utilities.shifter(location,0,0))
                .title(getString(R.string.gameUI_territoryProperty))
                .snippet(snippetValue.toString())
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("flag",150,150))));

        marker.showInfoWindow();

    }


    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }


    /**
     * Add the On Polygon Listener to the Map.
     * @param map
     */
    private void setOnPolygonClickable(final GoogleMap map){
        map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                LatLng point = polygon.getPoints().get(2); // Southeast Corner point !
                point = Utilities.shifter(point,-latTileUnit/2,-lngTileUnit/2); // temporary fixed only works on Lat:0~90 Lng:-180~0
                Tile.TileID tid = new Tile.TileID(point);
                Tile t = tiles.get(tid);
                String name = t.getUsername();


                if (t.select()) {
                    // selected - get username etc..
                    if (tilesSelected.size() == 2) {
                        Tile.TileID removeTile = tilesSelected.get(1);
                        Tile unselectedTile = tiles.get(removeTile);
                        unselectedTile.select();
                        unselectedTile.drawTile(mMap);
                        tilesSelected.remove(1);
                    }
                    tilesSelected.push(tid);
                    addTileInfoWindow(point);
                    if (tilesSelected.size() == 2) {
                        if(name!=null && name!=user.getUsername()) {
                            attemptShowBattlePopout();
                        } else {
                            tilesSelected.remove(0);
                            showDebug("not the case");
                        }
                        setBuySoliderPopsout(null, false);
                    }
                    if(name!=null){
                        updateNotification(getString(R.string.gameUI_territoryName, name),50,1000);
                    } else {
                        updateNotification(getString(R.string.gameUI_Unoccupied),50,1000);
                    }
                } else {
                    // tile was already selected, unselect it
                    tilesSelected.remove(tid);
                    // unselect the tile
                    // remove the marker
                    if(marker!=null) {
                        // if marker is there, just remove it.
                        marker.hideInfoWindow();
                        marker.remove();
                    }
                    // remove the buy option
                    setBuySoliderPopsout(null, false);
                    makeBattlePopout(false);
                }
                t.drawTile(mMap);
            }
        }
        );
    }

    /**
     * Tries to show the battle popout if only one tile is selected.
     */
    private void attemptShowBattlePopout() {
        String tile1Username = tiles.get(tilesSelected.get(0)).getUsername();
        String tile2Username = tiles.get(tilesSelected.get(1)).getUsername();
        boolean show = true;
        if (tile1Username != null && tile2Username != null) {
            if (tile1Username.equals(LoginActivity.username) && tile2Username.equals(LoginActivity.username)) {
                show = false;
            }
            makeBattlePopout(show);
        }
    }


    /**
     * Shows or hides the battle popout
     * @param enable true if the battle popout should be visible
     */
    private void makeBattlePopout(boolean enable) {
        final FrameLayout battlePanel = (FrameLayout)findViewById(R.id.layoutBattle);
        Button battleBtn = (Button) findViewById(R.id.battleNowBtn);
        final AsyncResponse callback = this;
        final Context context  = this;
        if(enable){
            battlePanel.setVisibility(View.VISIBLE);
            battleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    marker.hideInfoWindow();
                    setExplosionAnimation(5000);
                    battlePanel.setVisibility(View.GONE);
                    TileWebserviceUtility.battle(LoginActivity.username, LoginActivity.password, tiles.get(tilesSelected.get(0)),tiles.get(tilesSelected.get(1)), callback, context);
                }
            });
        } else {
            battlePanel.setVisibility(View.GONE);
        }
    }


    /**
     * Update ingame notification system
     * @param msg the content message need to display inside a bubble
     * @param delay the delay of each character typing animation
     * @param dismiss the delay of dismiss of entire notification bubble, if is -1 then never dismiss
     */
    private void updateNotification(String msg ,long delay, long dismiss) {
        if (notificationON) {
            final FrameLayout notification = (FrameLayout) findViewById(R.id.notificationSystem);
            Typewriter text = (Typewriter) findViewById(R.id.txtNotifiction);
            notification.setVisibility(View.VISIBLE);
            text.setCharacterDelay(delay);
            text.animateText(msg);

            if (dismiss > 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notification.setVisibility(View.GONE);
                    }
                }, delay * msg.length() + dismiss);
            }
        }
    }

    /**
     * Sets the resource bar information at the top of the screen.
     * @param u the user with the current resources
     */
    private void setResourceBar(User u){
        TextView solider = (TextView)findViewById(R.id.textResSolider);
        TextView gold = (TextView)findViewById(R.id.textResGold);
        TextView food = (TextView)findViewById(R.id.textResFood);

        solider.setText(String.valueOf(u.getTotalSoldiers()));
        gold.setText(String.valueOf(u.getGold()));
        food.setText(String.valueOf(u.getFood()));
    }

    /**
     * Sets the Username at the bottom of the screen
     * @param name the username to set
     */
    private void setUsername(String name){
        TextView username = (TextView)findViewById(R.id.textUsername);
        username.setText(name);
    }

    /**
     * Shows or hides the buy soldier option.
     * @param id the tile to select
     * @param visible true to show the popout
     */
    private void setBuySoliderPopsout(final Tile.TileID id, boolean visible){
        final FrameLayout buySoliderPanel = (FrameLayout)findViewById(R.id.layout_BuySoldier);
        final EditText buy_num = (EditText)findViewById(R.id.editBuySoliderNum);
        Button buyBtn = (Button) findViewById(R.id.buyNowBtn);

        if(visible){
            buySoliderPanel.setVisibility(View.VISIBLE);
            buyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(Integer.parseInt(buy_num.getText().toString())>0){
                        buySoldiers(id,Integer.parseInt(buy_num.getText().toString()));
                    }
                    buySoliderPanel.setVisibility(View.GONE);

                }
            });
        } else {
            buySoliderPanel.setVisibility(View.GONE);
        }
    }

    private int[] getScreenSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int res[] = {width,height};

        return res;
    }

    /**
     * Shows the stat panel with the gold, food and soldiers.
     * @param mode 0 if visible
     */
    private void showStatPanel(int mode){
        FrameLayout statPanel = (FrameLayout)findViewById(R.id.layoutStat);

        TextView soliderStat = (TextView)findViewById(R.id.txtStatSoldier);
        TextView goldStat = (TextView)findViewById(R.id.txtStatGold);
        TextView foodStat = (TextView)findViewById(R.id.txtStatFood);
        TextView tilesTakenStat = (TextView) findViewById(R.id.txtStatTilesTaken);

        // get the user's stats
        int soldier = this.user.getTotalSoldiers();
        int gold = this.user.getTotalGoldObtained();
        int food = this.user.getTotalFoodObtained();
        int tilesTaken = this.user.getTilesTaken();

        soliderStat.setText(String.valueOf(soldier));
        goldStat.setText(String.valueOf(gold));
        foodStat.setText(String.valueOf(food));
        tilesTakenStat.setText(String.valueOf(tilesTaken));

        if(mode==0){
            statPanel.setVisibility(View.VISIBLE);
        }else {
            statPanel.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the Explosion after battle
     * @param delay
     */
    private void setExplosionAnimation(long delay){
        final FrameLayout animLayout = (FrameLayout)findViewById(R.id.exp_anim_layout);
        ImageView animationIV = (ImageView)findViewById(R.id.animationExp);
        AnimationDrawable animationDrawable = (AnimationDrawable) animationIV.getDrawable();

        if(animLayout.getVisibility()==View.GONE){
            animLayout.setVisibility(View.VISIBLE);
        }

        if(delay>0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animLayout.setVisibility(View.GONE);
                }
            }, delay);
        }
        animationDrawable.start();
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