package mx.uach.newcompass3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import mx.uach.newcompass3.Objects.ActiveService;
import mx.uach.newcompass3.Objects.FirebaseReferences;
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
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private LatLng currentLatLng, originLatLng, destinationLatLng;
    private GeoApiContext mGeoApiContext = null;
    private List<ActiveService> activos = new ArrayList<ActiveService>();
    private ActiveService activo;
    //Widgets
    private ImageView mGps;
    private Button btnAccept;
    private ArrayList markerPoints = new ArrayList();
    private ListView listView;
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_map);
        mGps = findViewById(R.id.ic_gps);
        btnAccept = findViewById(R.id.btn_accept);
        getLocationPermission();

        listView = findViewById(R.id.list_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        final ArrayAdapter<String> adaptador;
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        listView.setAdapter(adaptador);
        Log.d(TAG, "onCreate: Lista creada");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                activo = activos.get(arg2);
                String[] label;
                label = getResources().getStringArray(R.array.slabels);
                Toast.makeText(ProviderMap.this, "Item: " + label[activo.getService()], Toast.LENGTH_SHORT).show();
                LatLng origin, destinity;
                origin = new LatLng(activo.getOriginlat(), activo.getOriginlon());
                destinity = new LatLng(activo.getDestinitylat(), activo.getDestinitylon());
                routing(arg1, origin, destinity);
                drawerLayout.closeDrawers();
            }
        });

        // Mostramos el botón en la barra de la aplicación
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //Creación de base de datos
        Log.d(TAG, "onCreate: Creando instancia de Firebase");
        database = FirebaseDatabase.getInstance();
        Log.d(TAG, "onCreate: Instancia de Firebase creada");
        final DatabaseReference serviceRef = database.getReference(FirebaseReferences.ACTIVESERVICES_REFERENCE);
        Log.d(TAG, "onCreate: Referencia de base de datos Firebase creada");
        serviceRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "ChildEventListener: Hijo creado: " + dataSnapshot);
                ActiveService servicio = dataSnapshot.getValue(ActiveService.class);
                assert servicio != null;
                Log.d(TAG, "ChildEventListener:\nServicio: " + servicio.getService() + "\n¿Atendiendo? " + servicio.getAttending()
                        + "\nOrigen: " + servicio.getOriginlat() + ", " + servicio.getOriginlon()
                        + "\nDestino: " + servicio.getDestinitylat() + ", " + servicio.getDestinitylon());
                if (servicio.getAttending() == 0) {
                    Log.d(TAG, "ChildernEventListener: Añadiendo objeto: " + servicio);
                    activos.add(servicio);
                    String[] label;
                    label = getResources().getStringArray(R.array.slabels);
                    adaptador.add(label[servicio.getService()]);
                    Log.d(TAG, "ChildernEventListener: Objeo añadido a la lista.");
                } else {
                    Log.d(TAG, "ChildernEventListener: El objeto tiene la propiedad Attending como verdadera y no ha sido añadido a la lista.");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "ChildEventListener: Hijo modificado: " + dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "ChildEventListener: Hijo eliminado: " + dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "ChildEventListener: Hijo movido: " + dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "ChildEventListener: ERROR: " + databaseError.getMessage());
            }
        });
    }

    //Implementación de mapa
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Log.d(TAG, "initMap: Iniciando mapa");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ProviderMap.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: Obteniendo permisos de ubicación");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

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
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: Obteniendo la ubicación actual del dispositivo");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
                            moveCamera(originLatLng, DEFAUL_ZOOM, "Mi ubicación");

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
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: Moviendo la cámara a:\nLat: " + latLng.latitude + "\nLng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("Mi ubicación")) {
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

    private void init(){
        Log.d(TAG, "init: Iniciando");
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, this).build();
        //Centrar cámara en ubicación actual y asignarla como origen
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Clic en ícono de GPS");
                getDeviceLocation();
                originLatLng = currentLatLng;
                Toast.makeText(ProviderMap.this, R.string.currentSet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Mapa listo", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        markerPoints.clear();
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
    //Menú lateral
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
                mMap.animateCamera(cu);
                try {
                    String url = getDirectionsUrl(origin, destination);
                    Log.d(TAG, "Routing: Obteniendo url de dirección.\norigin: " + origin + "\ndestination: " + destination + "\nurl: " + url);
                    ProviderMap.DownloadTask downloadTask = new ProviderMap.DownloadTask();
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

            ProviderMap.ParserTask parserTask = new ProviderMap.ParserTask();


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
                    Log.d(TAG, "onPostExecute: Ciclo For iniciado");
                    points = new ArrayList();
                    Log.d(TAG, "onPostExecute: Objeto points creado");
                    if (lineOptions == null) {
                        lineOptions = new PolylineOptions();
                        Log.d(TAG, "onPostExecute: Objeto lineOptions creado");
                    }else{
                        Log.d(TAG, "onPostExecute: El objeto lineOptions ya había sido creado previamente");
                    }

                    List<HashMap<String, String>> path = result.get(i);
                    Log.d(TAG, "onPostExecute: Entrando a ciclo For con path.size = " + path.size());
                    for (int j = 0; j < path.size(); j++) {
                        Log.d(TAG, "onPostExecute: Ciclo For iniciado");
                        HashMap<String, String> point = path.get(j);

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

// Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error al generar enrutamiento. Verifica que ambos puntos sean alcanzables",  Toast.LENGTH_LONG).show();
                Log.e("Background Task", e.toString());
            }
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
