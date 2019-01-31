package mx.uach.newcompass3;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.GeoApiContext;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import mx.uach.newcompass3.Objects.ActiveService;
import mx.uach.newcompass3.Objects.FirebaseReferences;
import mx.uach.newcompass3.Objects.ReleasedService;
import mx.uach.newcompass3.Objects.RequestingService;
import mx.uach.newcompass3.Objects.RoadSupport;
import mx.uach.newcompass3.models.PlaceInfo;

public class ProviderMap extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ProviderMap";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAUL_ZOOM = 15f;
    private static final int PLACE_PICCKER_REQUEST = 1;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(28.555541, -106.187639), new LatLng(28.798408, -105.888866));
    private static final String MY_API_KEY = BuildConfig.ApiKey;
    //Vars
    private Boolean mLocationPermissionGranted = false, drawing = true;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private LatLng currentLatLng, originLatLng, destinationLatLng;
    private GeoApiContext mGeoApiContext;
    private List<ActiveService> serviciosActivos = new ArrayList<ActiveService>();
    private List<RoadSupport> asistenciaVialRef = new ArrayList<>();
    private ActiveService activo;
    private RoadSupport rsActivo;
    //private LocationListener mLocationListener;
    private float rDistance = 0, mDistance = 0;
    private String rDistUnit;
    private String sDistance = "";
    private String sDuration = "";
    private int children = 0, working = 0;
    //Widgets
    private ImageView mGps;
    private Button btnAccept, btnCancel, btnRelease;
    private ArrayList markerPoints = new ArrayList();
    private ListView listView;
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference activeRef = database.getReference(FirebaseReferences.ACTIVESERVICES_REFERENCE);
    private DatabaseReference releasedRef = database.getReference(FirebaseReferences.RELEASEDSERVICES_REFERENCE);
    private ArrayAdapter<String> adaptador = null;
    private ArrayList servicesKeys = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_map);
        mGps = findViewById(R.id.ic_gps);
        btnAccept = findViewById(R.id.btn_accept);
        btnCancel = findViewById(R.id.btn_cancel);
        /**Botón con fines de prueba. Eliminar en versión final.*/
        btnRelease = findViewById(R.id.btn_release);
        getLocationPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onCreate: Permisos de ubicación denegados.");
        }else {
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 2, mLocationListener);
        }
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        listView = findViewById(R.id.list_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        listView.setAdapter(adaptador);
        Log.d(TAG, "onCreate: Lista creada");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                activo = serviciosActivos.get(arg2);
                String[] label;
                label = getResources().getStringArray(R.array.slabels);
                Toast.makeText(ProviderMap.this, "Item: " + label[activo.getService()], Toast.LENGTH_SHORT).show();
                LatLng origin;
                origin = new LatLng(activo.getOriginlat(), activo.getOriginlon());
                drawing = true;
                if (activo.getService() != 3) {
                    LatLng destination = new LatLng(activo.getDestinationlat(), activo.getDestinationlon());
                    routing(origin, destination);
                    rsActivo = null;
                }else{
                    rsActivo = asistenciaVialRef.get(activo.getRoadSupportIndex());
                    MarkerOptions options = new MarkerOptions().position(origin).title(label[3]);
                    mMap.clear();
                    mMap.addMarker(options);
                    moveCamera(origin, DEFAUL_ZOOM, label[3]);
                }
                drawerLayout.closeDrawers();
                btnAccept.setVisibility(View.VISIBLE);
            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                working = 1;
                activeRef.child(activo.getKey()).child("attending").setValue(true);
                listView.setVisibility(View.INVISIBLE);
                btnAccept.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnRelease.setVisibility(View.VISIBLE);
                drawing = true;
                originLatLng = new LatLng(activo.getOriginlat(), activo.getOriginlon());
                destinationLatLng = new LatLng(activo.getDestinationlat(), activo.getDestinationlon());
                routing(currentLatLng, originLatLng);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                working = 0;
                mMap.clear();
                activeRef.child(activo.getKey()).child("attending").setValue(false);
                listView.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                btnRelease.setVisibility(View.INVISIBLE);
                moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
            }
        });
        btnRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseService();
            }
        });
        activeRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                children++;
                Log.d(TAG, "onChildAdded: Inserción " + children);
                Log.d(TAG, "onChildAdded: Hijo a crear: " + dataSnapshot);
                ActiveService servicio = dataSnapshot.getValue(ActiveService.class);
                if (servicio.getService() == 3){
                    RoadSupport apoyoVial = dataSnapshot.child("roadSupport").getValue(RoadSupport.class);
                    asistenciaVialRef.add(apoyoVial);
                    servicio.setRoadSupportIndex(asistenciaVialRef.indexOf(apoyoVial));
                }else if (servicio.getService() == 4){
                    servicio.setfOrder((String) dataSnapshot.child("fOrder").getValue());
                }
                //Calcular distancia por enrutamiento
                if (currentLatLng == null) {
                    Log.d(TAG, "onChildAdded: Ubicación actual nula. No es posible generar enrutamiento.");
                }else {
                    servicio = gettingDistance(servicio);
                }
                servicio.setKey(dataSnapshot.getKey());
                assert servicio != null;
                Log.d(TAG, "onChildAdded:\nServicio: " + servicio.getService() + "\n¿Atendiendo? " + servicio.getAttending()
                        + "\nOrigen: " + servicio.getOriginlat() + ", " + servicio.getOriginlon()
                        + "\nDestino: " + servicio.getDestinationlat() + ", " + servicio.getDestinationlon()
                        + "\nDistancia: " + servicio.getDistance() + " m.");
                if (!servicio.getAttending()) {
                    Log.d(TAG, "onChildAdded: Añadiendo objeto: " + servicio);
                    Log.d(TAG, "onChildAdded: CurrentLatLng: " + currentLatLng + " Origin: " + new LatLng(servicio.getOriginlat(), servicio.getOriginlon()));
                    serviciosActivos.add(servicio);
                    servicesKeys.add(dataSnapshot.getKey());
                    updateAdapter();
                    Log.d(TAG, "onChildAdded: Objeto añadido a la lista.");
                } else {
                    Log.d(TAG, "onChildAdded: El objeto tiene la propiedad Attending como verdadera y no ha sido añadido a la lista.");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged: Hijo a modificar: " + dataSnapshot);
                try{
                    ActiveService servicio = dataSnapshot.getValue(ActiveService.class);
                    assert servicio != null;
                    Log.d(TAG, "onChildChanged:\nServicio: " + servicio.getService() + "\n¿Atendiendo? " + servicio.getAttending()
                            + "\nOrigen: " + servicio.getOriginlat() + ", " + servicio.getOriginlon()
                            + "\nDestino: " + servicio.getDestinationlat() + ", " + servicio.getDestinationlon());
                    Log.d(TAG, "onChildChanged: Modificando objeto: " + servicio);
                    int index = servicesKeys.indexOf(dataSnapshot.getKey());
                    Log.d(TAG, "onChildChanged: Índice de elemento a modificar: " + index);
                    if (index == -1) {
                        Log.d(TAG, "onChildChanged: Elemento que vuelve a ser activo. Trasladando a onChildAdded");
                        onChildAdded(dataSnapshot, s);
                    } else if (index >= 0) {
                        if (!servicio.getAttending()) {
                            if (servicio.getService() == 3) {
                                RoadSupport apoyoVial = dataSnapshot.child("roadSupport").getValue(RoadSupport.class);
                                asistenciaVialRef.set((serviciosActivos.get(index)).getRoadSupportIndex(), apoyoVial);
                                if ((serviciosActivos.get(index)).getService() != 3) {
                                    servicio.setRoadSupportIndex(asistenciaVialRef.indexOf(apoyoVial));
                                }
                            }else if ((serviciosActivos.get(index)).getService() == 3){
                                removeRSRef((serviciosActivos.get(index)).getRoadSupportIndex());
                            }
                            serviciosActivos.set(index, servicio);
                            Log.d(TAG, "onChildChanged: Objeto modificado en la lista.");
                        } else {
                            if (servicio.getService() == 3){
                                removeRSRef((serviciosActivos.get(index)).getRoadSupportIndex());
                            }
                            serviciosActivos.remove(index);
                            servicesKeys.remove(index);
                            Log.d(TAG, "onChildChanged: El objeto tiene la propiedad Attending como verdadera y ha sido retirado de la lista.");
                        }
                        updateAdapter();
                    } else {
                        Log.e(TAG, "onChildChanged: Error, número de lista incorrecto.");
                    }
                }catch (Exception e){
                    Log.e(TAG, "onChildChanged: Error: " + e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved: Hijo a eliminar: " + dataSnapshot);
                ActiveService servicio = dataSnapshot.getValue(ActiveService.class);
                assert servicio != null;
                Log.d(TAG, "onChildRemoved:\nServicio: " + servicio.getService() + "\n¿Atendiendo? " + servicio.getAttending()
                        + "\nOrigen: " + servicio.getOriginlat() + ", " + servicio.getOriginlon()
                        + "\nDestino: " + servicio.getDestinationlat() + ", " + servicio.getDestinationlon());
                Log.d(TAG, "onChildRemoved: eliminando objeto: " + servicio);
                int index = servicesKeys.indexOf(dataSnapshot.getKey());
                Log.d(TAG, "onChildRemoved: Índice de elemento a eliminar: " + index);
                try {
                    if (servicio.getService() == 3){
                        removeRSRef((serviciosActivos.get(index)).getRoadSupportIndex());
                    }
                    serviciosActivos.remove(index);
                    servicesKeys.remove(index);
                    updateAdapter();
                }catch (ArrayIndexOutOfBoundsException ob){
                    Log.d(TAG, "onChildRemoved: Intento de borrar información que eliminada previamente.");
                    Log.d(TAG, "Detalles: " + ob.getMessage());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved: Hijo a mover: " + dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: ERROR: " + databaseError.getMessage());
            }
        });
    }

    private void removeRSRef(int roadSupportIndex) {
        asistenciaVialRef.remove(roadSupportIndex);
        int i = 0;
        for (ActiveService elemento : serviciosActivos){
            if (elemento.getRoadSupportIndex() > roadSupportIndex){
                elemento.setRoadSupportIndex(elemento.getRoadSupportIndex() - 1);
                serviciosActivos.set(i, elemento);
            }
            i++;
        }
    }

    private ActiveService gettingDistance(ActiveService servicio) {
        drawing = false;
        LatLng origin = new LatLng(servicio.getOriginlat(), servicio.getOriginlon());
        routing(currentLatLng, origin);
        Log.d(TAG, "gettingDistance: Distancia: " + sDistance + ", Duración: " + sDuration);
        //Insertar en listas
        String[] distanceSplit = sDistance.split(" ");
        rDistance = Float.parseFloat(distanceSplit[0]);
        rDistUnit = distanceSplit[1];
        Log.d(TAG, "gettingDistance: Asignando distancia de " + rDistance + " " + rDistUnit + " al elemento actual.");
        if (rDistUnit.compareTo("km") == 0) {
            rDistance = rDistance * 1000;
        }
        servicio.setDistance(rDistance);
        return servicio;
    }

    public  LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "onLocationChanged: currentLatLng: " + currentLatLng);
            //Toast.makeText(MapActivity.this, "currentLatLng: " + currentLatLng, Toast.LENGTH_SHORT).show();
            Location location1 = new Location(LocationManager.GPS_PROVIDER), location2 = new Location(LocationManager.GPS_PROVIDER);
            if(working == 1){
                location1.setLatitude(currentLatLng.latitude);
                location1.setLongitude(currentLatLng.longitude);
                location2.setLatitude(originLatLng.latitude);
                location2.setLatitude(originLatLng.longitude);
                if(location1.distanceTo(location2) < 10){
                    routing(currentLatLng, destinationLatLng);
                    working = 2;
                }
            }else if (working == 2){
                location1.setLatitude(currentLatLng.latitude);
                location1.setLatitude(currentLatLng.longitude);
                location2.setLatitude(destinationLatLng.latitude);
                location2.setLatitude(destinationLatLng.longitude);
                if (activo.getService() == 3){
                    btnRelease.setVisibility(View.VISIBLE);
                }
                if(location1.distanceTo(location2) < 10 && activo.getService() != 3){
                    releaseService();
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
    };

    private void updateAdapter(){
        String[] label = getResources().getStringArray(R.array.slabels);
        adaptador.clear();
        Log.d(TAG, "updateAdapter: Actualizando adaptador.");
        int i = 0;
        float aDistance;
        for(ActiveService elemento : serviciosActivos){
            aDistance = elemento.getDistance();
            String aUnit = " m.";
            if(aDistance > 1000){
                aDistance = aDistance/1000;
                aUnit = " km.";
            }
            String supportLabel = "";
            if (elemento.getService() == 3){
                RoadSupport support = asistenciaVialRef.get(elemento.getRoadSupportIndex());
                if (support.getBattery()){
                    supportLabel = supportLabel + "\nBattery";
                }
                if (support.getBrakeFail()){
                    supportLabel = supportLabel + "\nBrake Fail";
                }
                if (support.getDeflatedTire()){
                    supportLabel = supportLabel + "\nDeflated tire";
                }
                if (support.getLeak()){
                    supportLabel = supportLabel + "\nLeak";
                }
                if (support.getNoGas()){
                    supportLabel = supportLabel + "\nNo gas";
                }
            }
            adaptador.add(label[elemento.getService()] + supportLabel + "\n" + getString(R.string.aDistance) + aDistance + aUnit);
            Log.d(TAG, "updateAdapter: " + label[elemento.getService()]);
            i++;
        }
    }

    private void updateDistance() {
        if(currentLatLng == null){
            Log.e(TAG, "Error, la ubicación actual sigue siendo desconocida y el método updateDistance ha sido llamado por error.");
            return;
        }
        int i = 0;
        for(ActiveService elemento : serviciosActivos){
            if (elemento.getDistance() == 0){
                elemento = gettingDistance(elemento);
                Log.d(TAG, "updateDistance: Actualizando elemento con índice " + i + " y distancia de " + elemento.getDistance() + "m.");
                serviciosActivos.set(i, elemento);
            }
            i++;
        }
        updateAdapter();
    }

    private void releaseService() {
        mMap.clear();
        btnCancel.setVisibility(View.INVISIBLE);
        btnRelease.setVisibility(View.INVISIBLE);
        moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
        listView.setVisibility(View.VISIBLE);
        working = 0;
        if (rDistUnit.compareTo("km") == 0){
            rDistance = rDistance * 1000;
        }
        Date cDate = Calendar.getInstance().getTime();
        SimpleDateFormat tf = new SimpleDateFormat("kk:mm:ss");
        String driver = "Test driver";
        String cClient = "Test client";
        DatabaseReference nRel = releasedRef.push();
        nRel.setValue(new ReleasedService(activo.getService(),
                activo.getOriginlat(), activo.getOriginlon(), activo.getDestinationlat(),
                activo.getDestinationlon(), cClient, driver, activo.getDate(), activo.getrTime(), tf.format(cDate)));
        if (activo.getService() == 3){
            nRel.child("roadSupport").setValue(rsActivo);
            rsActivo = null;
        }
        activeRef.child(activo.getKey()).removeValue();
        activo = null;
    }

    /**Implementación de mapa*/
    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Obteniendo permisos de ubicación");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "getLocationPermission: Permisos concedidos");
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Log.d(TAG, "initMap: Iniciando mapa");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ProviderMap.this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Mapa listo");
        Toast.makeText(this, R.string.mapReady, Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        markerPoints.clear();
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getDeviceLocation();
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
                            moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
                            updateDistance();
                        }else{
                            Log.d(TAG, "onComplete: La ubicación actual es nula");
                            Toast.makeText(ProviderMap.this, "Incapaz de conseguir ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: Excepción de seguridad: " + e.getMessage());
        }
    }

    private void init(){
        Log.d(TAG, "init: Iniciando");
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, this).build();
        //Centrar cámara en ubicación actual y asignarla como origen
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clic en ícono de GPS");
                moveCamera(currentLatLng, DEFAUL_ZOOM, getString(R.string.myLocation));
            }
        });
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: Moviendo la cámara a:\nLat: " + latLng.latitude + "\nLng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals(getString(R.string.myLocation))) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.clear();
            markerPoints.clear();
            mMap.addMarker(options);
        }
        if(originLatLng != latLng) {
            destinationLatLng = latLng;
            Log.d(TAG, "moveCamera: Valor asigando a destinationLatLng: " + destinationLatLng);
        }else{
            Log.w(TAG, "moveCamera: Se ha intentado asignar el valor de origen al destino, pero ha sido evitado.");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: Error de conexión.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        Log.d(TAG, "onRequestPermissionresult: Llamado");
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
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

    /**Menú lateral*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(listView)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(listView);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**Métodos de enrutamiento*/
    //Ejecutando enrutamiento
    public void routing(LatLng origin, LatLng destination){
        //Este código debe ir encerrado dentro de condiciones pues debe comportarse distinto según la opción elegida por el usuario en la pantalla anterior
        //Comportamiento por defecto: ir de la ubicación actual al punto señalado
        Log.d(TAG, "Routing:\norigin: " + origin + "\ndestination: " +destination);
        if(origin!=null && destination!=null) {
            if(origin != destination) {
                //Ajuste de marcadores y cámara
                if(drawing) {
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
                    mMap.animateCamera(cu);
                }
                try {
                    String url = getDirectionsUrl(origin, destination);
                    Log.d(TAG, "Routing: Obteniendo url de dirección.\norigin: " + origin + "\ndestination: " + destination + "\nurl: " + url);
                    // Start downloading json data from Google Directions API
                    ProviderMap.DownloadTask dt = new ProviderMap.DownloadTask();
                    String result = dt.execute(url).get();
                    Log.d(TAG, "Routing: Proceso asíncrono devolvió: " + result);
                    List<List<HashMap<String, String>>> routes = parsing(result);
                    mapping(routes);
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.routeError), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "routing: Error de enrutamiento: " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }else{
                Log.e(TAG, "Routing: Las coordenadas de origen y destino son iguales. Verifica sus valores.");
            }
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.noRouteData), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Routing: No se tienen coordenadas para enrutamiento.");
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
            Log.d("downloadUrl: Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d(TAG, "DownloadTask: data: " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "DownloadTask: onPostExecute: result: " + result);
            super.onPostExecute(result);

            //ProviderMap.parsing parsing = new ProviderMap.parsing();
            //parsing.execute(result);
        }
    }
    /**Métodos para convertir la información*/
    private List<List<HashMap<String, String>>> parsing(String... jsonData) {
        Log.d(TAG, "parsing: Recibiendo: " + jsonData);
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
    private void mapping(List<List<HashMap<String, String>>> result) {
        Log.d(TAG, "mapping: Result: " + result);
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.width(2);
        lineOptions.color(Color.BLUE);
        MarkerOptions markerOptions = new MarkerOptions();
        try {
            int cfor;
            if(result.size()==0){
                Log.w(TAG, "mapping: result.size no tiene valor. Se asignará un valor de 1 para el ciclo.");
                cfor = 1;
            }else{
                Log.d(TAG, "mapping: result.size: " + result.size());
                cfor = result.size();
            }
            Log.d(TAG, "mapping: Entrando a ciclo For con cfor = " + cfor);
            for (int i = 0; i < cfor; i++) {
                points = new ArrayList();

                List<HashMap<String, String>> path = result.get(i);
                Log.d(TAG, "mapping: Entrando a ciclo For con path.size = " + path.size());
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        sDistance = point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        sDuration = point.get("duration");
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
            Log.d(TAG, "mapping: Almacenando Distancia: "+sDistance + ", Duración: "+sDuration);
            if(drawing) {
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error al generar enrutamiento. Verifica que ambos puntos sean alcanzables",  Toast.LENGTH_LONG).show();
            Log.e("Background Task", e.toString());
        }
    }
    /**
     * A class to parse the Google Places in JSON format
     */
    /**
    private class parsing extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            Log.d(TAG, "parsing: Recibiendo: " + jsonData);
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
            MarkerOptions markerOptions = new MarkerOptions();
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
                            sDistance = point.get("distance");
                            continue;
                        }else if(j==1){ // Get duration from the list
                            sDuration = point.get("duration");
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
                Log.d(TAG, "onPostExecute: Almacenando Distancia: "+sDistance + ", Duración: "+sDuration);
                if(drawing) {
                    // Drawing polyline in the Google Map for the i-th route
                    mMap.addPolyline(lineOptions);
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error al generar enrutamiento. Verifica que ambos puntos sean alcanzables",  Toast.LENGTH_LONG).show();
                Log.e("Background Task", e.toString());
            }
        }
    }
    */
}
