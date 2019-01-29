package mx.uach.newcompass3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.uach.newcompass3.Objects.ActiveService;
import mx.uach.newcompass3.Objects.FirebaseReferences;
import mx.uach.newcompass3.Objects.ReleasedService;
import mx.uach.newcompass3.Objects.RequestingService;
import mx.uach.newcompass3.Objects.RoadSupport;
import mx.uach.newcompass3.models.PlaceInfo;

import static java.lang.StrictMath.abs;

/**
 * Created by Alt on 23/08/2018.
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAUL_ZOOM = 15f;
    private static final int PLACE_PICCKER_REQUEST = 1;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(28.555541, -106.187639), new LatLng(28.798408, -105.888866));
    private static final String MY_API_KEY = BuildConfig.ApiKey;
    //Vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private LatLng currentLatLng, originLatLng, destinationLatLng;
    private GeoApiContext mGeoApiContext = null;
    private int spOption, travelWay;
    private static String driver = "Test driver";
    private String cClient = "Test client";
    //Widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mInfo, mPlacePicker, mClear, mAdd, mRouting;
    private Button btnRequest;
    private ArrayList markerPoints = new ArrayList();
    //Base de datos
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference activeRef = database.getReference(FirebaseReferences.ACTIVESERVICES_REFERENCE);

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        mSearchText = findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker = findViewById(R.id.place_picker);
        mClear = findViewById(R.id.ic_clear);
        mAdd = findViewById(R.id.ic_add_origin);
        mRouting = findViewById(R.id.ic_direction);
        btnRequest = findViewById(R.id.btnRequest);
        Log.d(TAG, "onCreate: Botones definidos");
        if (btnRequest == null) {
            Log.d(TAG, "onCreate: Referencia a btnRequest nula");
            if (findViewById(R.id.btnRequest) == null) {
                Log.d(TAG, "onCreate: Referencia a elemento de layout nula");
            }
        }
        getLocationPermission();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onCreate: Permisos de ubicación denegados.");
        }else {
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 2, mLocationListener);
        }
    }


    public  LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(currentLatLng.latitude);
            temp.setLongitude(currentLatLng.longitude);
            float distance = location.distanceTo(temp);
            //Log.d(TAG, "onLocationChanged: Distance: " + distance);
            if(distance > 2){
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d(TAG, "onLocationChanged: currentLatLng: " + currentLatLng);
                //Toast.makeText(MapActivity.this, "currentLatLng: " + currentLatLng, Toast.LENGTH_SHORT).show();
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
    };

    private void init(){
        Log.d(TAG, "init: Iniciando");

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, this).build();
        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER){
                    //Execute method for searching
                    geoLocate();
                }
                return false;
            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchText.setText("");
            }
        });
        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICCKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage());
                }
            }
        });
        hideSoftKeyboard();
        //Centrar cámara en ubicación actual y asignarla como origen
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clic en ícono de GPS");
                mMap.clear();
                btnRequest.setVisibility(View.INVISIBLE);
                moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
                originLatLng = currentLatLng;
                Toast.makeText(MapActivity.this, R.string.currentSet, Toast.LENGTH_SHORT).show();
            }
        });
        //Añadir un puntos de origen distinto a la ubicación actual
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Asignando origen distinto a la ubicación actual");
                if(destinationLatLng != null) {
                    originLatLng = destinationLatLng;
                    if(travelWay == 0) {
                        Toast.makeText(MapActivity.this, R.string.originSet, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MapActivity.this, R.string.destinationSet, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MapActivity.this, R.string.locationSetError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "onClick: Clic en información del lugar");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }
                    else{
                        Log.d(TAG, "onClick: Información del lugar: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage());
                }
            }
        });
        mRouting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mRouting: Preparando enrutamiento");
                if (spOption == 3){
                    Log.d(TAG, "mRouting: Servicio de asistencia vial. Enviando un solo conjunto de coordenadas");
                    originLatLng = destinationLatLng;
                }else if(travelWay == 0){
                    Log.d(TAG, "mRouting: Enviando enrutameinto estándar\noriginLatLng: " + originLatLng + "\ndestinationLatLng: " + destinationLatLng);
                    routing(v, originLatLng, destinationLatLng);
                }else{
                    Log.d(TAG, "mRouting: Enviando enrutameinto invertido\noriginLatLng: " + destinationLatLng + "\ndestinationLatLng: " + originLatLng);
                    routing(v, destinationLatLng, originLatLng);
                }
                Log.d(TAG, "mRouting: Enrutamiento enviado");
                btnRequest.setVisibility(View.VISIBLE);
            }
        });
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date cDate = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy"), tf = new SimpleDateFormat("kk:mm:ss");
                if (originLatLng == null || destinationLatLng == null) {
                    Log.e(TAG, "btnRequest: Coordenadas con valor nulo.");
                }else{
                    DatabaseReference lastRef = activeRef.push();
                    lastRef.setValue(new RequestingService(spOption, 0,
                            originLatLng.latitude, originLatLng.longitude, destinationLatLng.latitude,
                            destinationLatLng.longitude, cClient, df.format(cDate), tf.format(cDate)));
                    if (spOption == 3){
                        Intent receiveIntent = getIntent();
                        RoadSupport rs = new RoadSupport(receiveIntent.getBooleanExtra("rsFlatTire", false),
                                receiveIntent.getBooleanExtra("rsGas", false),
                                receiveIntent.getBooleanExtra("rsLeak", false),
                                receiveIntent.getBooleanExtra("rsBrake", false),
                                receiveIntent.getBooleanExtra("rsBattery", false));
                        lastRef.child("roadSupport").setValue(rs);
                    }else if (spOption == 4){
                        Intent receiveIntent = getIntent();
                        lastRef.child("fOrder").setValue(receiveIntent.getStringExtra("fOrder"));
                    }
                    mMap.clear();
                    btnRequest.setVisibility(View.INVISIBLE);
                    moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
                    Toast.makeText(MapActivity.this, R.string.requestSended, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PLACE_PICCKER_REQUEST){
            if (resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(this, data);
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                //String toastMsg = String.format("Place: %s", place.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: Excepción IO" + e.getMessage());
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: Ubicación encontrada: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAUL_ZOOM, address.getAddressLine(0));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Mapa listo", Toast.LENGTH_SHORT).show();
        Intent receiveIntent = getIntent();
        spOption = receiveIntent.getIntExtra("spOption", 0);
        travelWay = receiveIntent.getIntExtra("travelWay", 0);
        if(spOption == 1 && travelWay == 1){
            mAdd.setVisibility(View.GONE);
        }
        mMap = googleMap;
        markerPoints.clear();
        mMap.clear();

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: Obteniendo la ubicación actual del dispositivo");
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: ¡Ubicación encontrada!");
                            Location currentLocation = (Location) task.getResult();

                            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            originLatLng = currentLatLng;
                            if (spOption == 3){
                                destinationLatLng = originLatLng;
                            }
                            moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
                        }else{
                            Log.d(TAG, "onComplete: La ubicación actual es nula");
                            Toast.makeText(MapActivity.this, "Incapaz de conseguir ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: Excepción de seguridad: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: Moviendo la cámara a:\nLat: " + latLng.latitude + "\nLng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();
        markerPoints.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
        if(placeInfo != null){
            try{
                String snnippet = "Dirección: "+ placeInfo.getAddress() + "\n" +
                        "Teléfono: "+ placeInfo.getPhoneNumber() + "\n" +
                        "Sitio web: "+ placeInfo.getWebsiteUri() + "\n" +
                        "Costo: "+ placeInfo.getRating() + "\n";
                MarkerOptions options = new MarkerOptions().position(latLng).title(placeInfo.getName()).snippet(snnippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage());
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        if(originLatLng != latLng) {
            destinationLatLng = latLng;
            Log.d(TAG, "moveCamera: Valor asigando a destinationLatLng: " + destinationLatLng);
        }else{
            Log.w(TAG, "moveCamera: Se ha intentado asignar el valor de origen al destino, pero ha sido evitado.");
        }
        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: Moviendo la cámara a:\nLat: " + latLng.latitude + "\nLng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals(getString(R.string.myLocation))) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            markerPoints.clear();
            mMap.addMarker(options);
        }
        if(originLatLng != latLng) {
            destinationLatLng = latLng;
            Log.d(TAG, "moveCamera: Valor asigando a destinationLatLng: " + destinationLatLng);
        }else{
            Log.w(TAG, "moveCamera: Se ha intentado asignar el valor de origen al destino, pero ha sido evitado.");
        }
        hideSoftKeyboard();
    }

    private void initMap() {
        Log.d(TAG, "initMap: Iniciando mapa");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Obteniendo permisos de ubicación");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        Log.d(TAG, "onRequestPermissionresult: Llamado");
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for (int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionResult: Permisos denegados");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionResult: Permisos concedidos");
                    mLocationPermissionGranted = true;
                    //Iniciar mapa
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /*-------------------------------------- Google Places autocomplete suggestions --------------------------------------------*/
    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Consulta de lugar no completada de forma exitosa: "+places.getStatus().toString());
                places.release();
                return;
            }else{
                final Place place = places.get(0);

                try {
                    mPlace = new PlaceInfo();
                    mPlace.setName(place.getName().toString());
                    mPlace.setAddress(place.getAddress().toString());
                    //mPlace.setAttributions(place.getAttributions().toString());
                    mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                    mPlace.setId(place.getId());
                    mPlace.setWebsiteUri(place.getWebsiteUri());
                    mPlace.setLatLng(place.getLatLng());
                    mPlace.setRating(place.getRating());
                    Log.d(TAG, "onResult: place: " + mPlace.toString());
                }catch (NullPointerException e){
                    Log.e(TAG, "onResult: NullPinterException" + e.getMessage());
                }
                moveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude), DEFAUL_ZOOM, mPlace);

                places.release();
            }
        }
    };

    //Métodos de enrutamiento
    //Ejecutando enrutamiento
    public void routing(View view, LatLng origin, LatLng destination){
        //Este código debe ir encerrado dentro de condiciones pues debe comportarse distinto según la opción elegida por el usuario en la pantalla anterior
        //Comportamiento por defecto: ir de la ubicación actual al punto señalado
        Log.d(TAG, "Routing:\norigin: " + origin + "\ndestination: " +destination);
        if(origin!=null && destination!=null) {
            if(origin != destination) {
                //Ajuste de marcadores y cámara
                mMap.clear();
                Marker marker;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                marker = mMap.addMarker(new MarkerOptions().position(origin).title("Origin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                builder.include(marker.getPosition());
                marker = mMap.addMarker(new MarkerOptions().position(destination).title("destination"));
                builder.include(marker.getPosition());
                LatLngBounds bounds = builder.build();
                int padding = 200; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                hideSoftKeyboard();
                mMap.animateCamera(cu);
                try {
                    String url = getDirectionsUrl(origin, destination);
                    Log.d(TAG, "Routing: Obteniendo url de dirección.\norigin: " + origin + "\ndestination: " + destination + "\nurl: " + url);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                    Log.d(TAG, "Routing: Descarga de datos Json realizada con éxito");
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.routeError), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "routing: Error de enrutamiento: " + e.getMessage(), e);
                }
            }else{
                Log.e(TAG, "Routing: Las coordenadas de origen y destino son iguales. Verifica sus valores.");
            }
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.noRouteData), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Routing: No se tienen coordenadas para enrutamiento.");
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "DownloadTask: onPostExecute: result: " + result);
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            Log.d(TAG, "ParseTask: onPostExecute: Result: " + result);
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.width(2);
            lineOptions.color(Color.BLUE);
            float rDistance = 0;
            String distance = "";
            String duration = "";
            //MarkerOptions markerOptions = new MarkerOptions();
            try {
                int cfor;
                if(result.size()==0){
                    Log.w(TAG, "onPostExecute: result.size no tiene valor. Se asignará un valor de 1 para el ciclo.");
                    cfor = 1;
                }else{
                    Log.d(TAG, "onPostExecute: result.size: " + result.size());
                    cfor = result.size();
                }
                Log.d(TAG, "onPostExecute: Entrando a ciclo For con cfor = " + cfor);
                for (int i = 0; i < cfor; i++) {
                    points = new ArrayList();
                    List<HashMap<String, String>> path = result.get(i);
                    Log.d(TAG, "onPostExecute: Entrando a ciclo For con path.size = " + path.size());
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        if(j==0){    // Get distance from the list
                            distance = point.get("distance");
                            Log.d(TAG, "Calculando distancia: " + distance);
                            continue;
                        }else if(j==1){ // Get duration from the list
                            duration = point.get("duration");
                            Log.d(TAG, "Calculando duración: " + duration);
                            continue;
                        }

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.width(12);
                    lineOptions.color(Color.BLUE);
                    lineOptions.geodesic(true);

                }
                String[] distanceSplit = distance.split(" ");
                rDistance = Float.parseFloat(distanceSplit[0]);
                Log.d(TAG, "onPostExecute:\nDistancia: "+distance + ", Duración: "+duration);
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error al generar enrutamiento. Verifica que ambos puntos sean alcanzables",  Toast.LENGTH_LONG).show();
                Log.e("Background Task", e.toString());
            }
            Log.d(TAG, "onPostExecute: Distance in meters: " + rDistance);
        }
    }
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + MY_API_KEY;


        return url;
    }
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
