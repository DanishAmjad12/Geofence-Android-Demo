package com.example.daamjad.androidgeofence;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daamjad.androidgeofence.constants.ApplicationConstants;
import com.example.daamjad.androidgeofence.models.AddTasks;
import com.example.daamjad.androidgeofence.services.GeoFenceService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import io.realm.Realm;

import static com.example.daamjad.androidgeofence.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 15000;
    public static final String TAG = "Location";

    private Button buttonGeoFenceMonitor;
    private EditText editTextDestinationLocation;
    private TextView textViewCurrentRadius;
    double destinationLatitude, destinationLongitude, currentLatitude, currentLongitude;
    private SupportMapFragment mapFragment;
    private GoogleMap newMap;
    private Marker myMarker;
    private Circle circle;
    private DiscreteSeekBar seekBar;
    private CameraUpdate cameraUpdate;
    private int radiusValue = 100;
    private float cameraZoomValue = 17.0f;
    private LatLng latLng;
    private Location lastLocation;
    private Realm realm;
    private String tasks;
    private GoogleApiClient googleApiClient;
    private GoogleApiClient wearApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);


        buttonGeoFenceMonitor = (Button) findViewById(R.id.buttonGeoFenceMonitor);
        editTextDestinationLocation = (EditText) findViewById(R.id.search_edit_frame);
        seekBar = (DiscreteSeekBar) findViewById(R.id.seekbar);
        textViewCurrentRadius = (TextView) findViewById(R.id.currentRadius);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);

        realm = Realm.getDefaultInstance();

        editTextDestinationLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        buttonGeoFenceMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerReceiver(Receiver, new IntentFilter("GeoFence"));
                startGeoFenceMonitoring();
            }
        });

        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                textViewCurrentRadius.setText("Current Radius: " + value + "m");
                radiusValue = value;
                showCircle();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        setupGoogleClient(this);
    }

    private void setupGoogleClient(final Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "connected to googleAPiClient: ");
                        setWearApiClient(MainActivity.this);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    ApplicationConstants.REQUEST_CODE_FOR_LOCATION);
                        } else {
                            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                            setupLocationRequest();
                        }

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "suspended to googleAPiClient: ");
                    }
                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "failed to connect google api client: " + connectionResult);
                    }
                }).build();
    }

    private void setWearApiClient(Context context) {
        wearApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "connected to wearAPiClient: ");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "suspended to wearAPiClient: ");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "failed to connect wear api client: " + connectionResult);
                    }
                })
                .build();
        wearApiClient.connect();
    }

    private void setupLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "location lat/lng: " + location.getLatitude() + " " + location.getLongitude());
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    mapFragment.getMapAsync(MainActivity.this);
                }
            });
        }

    }

    private void startGeoFenceMonitoring() {
        try {

            Geofence geofence = new Geofence.Builder()
                    .setRequestId("1234")
                    .setCircularRegion(destinationLatitude, destinationLongitude, radiusValue) // first lat,then lng,then radius
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence).build();

            Intent intent = new Intent(this, GeoFenceService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (!googleApiClient.isConnected()) {
                Log.d(TAG, "not connected: ");
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ApplicationConstants.REQUEST_CODE_FOR_LOCATION);
                    LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent)
                            .setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    if (status.isSuccess()) {
                                        showNotification("Start", "Trip start for your desired location");
                                        Log.d(TAG, "added geofence successfully: ");
                                    } else {
                                        Log.d(TAG, "failed to add geofence: ");
                                    }
                                }
                            });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        wearApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getId());
                editTextDestinationLocation.setText(place.getName());
                LatLng latLng = place.getLatLng();
                destinationLatitude = latLng.latitude;
                destinationLongitude = latLng.longitude;
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
            }
        } else {
            enableStartButton();
        }
    }

    private void enableStartButton() {
        tasks = getTask();
        if (!TextUtils.isEmpty(tasks)) {
            buttonGeoFenceMonitor.setEnabled(true);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (currentLatitude > 0 && currentLongitude > 0) {
            newMap = googleMap;
            latLng = new LatLng(currentLatitude, currentLongitude);
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, cameraZoomValue);
            newMap.animateCamera(cameraUpdate);
            newMap.setMyLocationEnabled(true);
            if (myMarker != null) {
                myMarker.remove();
                myMarker = newMap.addMarker(new MarkerOptions().position(latLng).title("You are Here").snippet("You are Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker)));
            } else {
                myMarker = newMap.addMarker(new MarkerOptions().position(latLng).title("You are Here").snippet("You are Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker)));
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuAddTask:
                deleteRealmTable();
                Intent intent = new Intent(this, AddTaskActivity.class);
                startActivityForResult(intent, 200);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String getTask() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AddTasks addTasks = realm.where(AddTasks.class).findFirst();
                if (addTasks != null)
                    tasks = addTasks.getTask();
            }
        });
        return tasks;
    }

    private BroadcastReceiver Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals("GeoFence")) {
                    boolean enter = intent.getBooleanExtra("Enter", false);
                    boolean exit = intent.getBooleanExtra("Exit", false);
                    if (enter) {
                        // Toast.makeText(context, "Entered in your location", Toast.LENGTH_SHORT).show();
                        showNotification("Task Need to be completed", getTask());
                        showCircle();
                    } else if (exit) {
                        showNotification("Finished", "Your task is completed");
                        Toast.makeText(context, "Leaving from your location", Toast.LENGTH_SHORT).show();
                        circle.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void showNotification(final String title, final String subject) {

        boolean wearAvailable = wearApiClient.hasConnectedApi(Wearable.API);
        Log.d(TAG, "wearAvailable: " + wearAvailable);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(subject);
        bigText.setBigContentTitle(title);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(getNotificationIcon())
                .setColor(getNotificationColor())
                .setContentText(subject)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(bigText);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/Notification");
        // Make sure the data item is unique. Usually, this will not be required, as the payload
        // (in this case the title and the content of the notification) will be different for almost all
        // situations. However, in this example, the text and the content are always the same, so we need
        // to disambiguate the data item by adding a field that contains teh current time in milliseconds.

        DataMap map = dataMapRequest.getDataMap();
        map.putString(ApplicationConstants.NOTIFICATION_TITLE, title);
        map.putString(ApplicationConstants.NOTIFICATION_MESSAGE, subject);
        PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();

        // Wearable.DataApi.putDataItem(apiClient, putDataRequest);
        putDataRequest.setUrgent();
        Wearable.DataApi.putDataItem(wearApiClient, putDataRequest)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(TAG, "AddPoint:onClick(): Failed to set the data, "
                                    + "status: " + dataItemResult.getStatus()
                                    .getStatusCode());
                        } else {
                            Log.d(TAG, "onResult: " + "Message send to wear");
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ApplicationConstants.REQUEST_CODE_FOR_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocationRequest();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Permission Denied for Location", Toast.LENGTH_SHORT).show();
                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
        }
    }

    private void showCircle() {
        if (circle != null) {
            circle.remove();
            circle = newMap.addCircle(new CircleOptions()
                    .center(new LatLng(currentLatitude, currentLongitude))
                    .radius(radiusValue)
                    .strokeColor(Color.parseColor("#FF000000"))
                    .fillColor(Color.parseColor("#87CEFA")));
            animateMapCamera();
        } else {
            circle = newMap.addCircle(new CircleOptions()
                    .center(new LatLng(currentLatitude, currentLongitude))
                    .radius(radiusValue)
                    .strokeColor(Color.parseColor("#FF000000"))
                    .fillColor(Color.parseColor("#87CEFA")));
        }
    }

    private void animateMapCamera() {
        if (radiusValue > 150) {
            cameraZoomValue = 15.5f;
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, cameraZoomValue);
            newMap.animateCamera(cameraUpdate);
        } else {
            cameraZoomValue = 17.0f;
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, cameraZoomValue);
            newMap.animateCamera(cameraUpdate);
        }
    }

    private static void deleteRealmTable() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();
                }
            });
        } finally {
            realm.close();
        }
    }

    private int getNotificationIcon() {
        boolean icon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return icon ? R.drawable.location_marker : R.drawable.location_marker;
    }

    private int getNotificationColor() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return ContextCompat.getColor(this, R.color.colorPrimary);
        } else {
            return -1;
        }

    }

}
