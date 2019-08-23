package mx.uach.newcompass3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import mx.uach.newcompass3.Objects.FirebaseReferences;
import mx.uach.newcompass3.Objects.RequestingService;
import mx.uach.newcompass3.Objects.RoadSupport;
import mx.uach.newcompass3.models.PlaceInfo;

/**
 * Created by Alt on 23/08/2018.
 */

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final RectangularBounds LAT_LNG_BOUNDS = RectangularBounds.newInstance(new LatLng(28.555541, -106.187639), new LatLng(28.798408, -105.888866));
    private static final String MY_API_KEY = BuildConfig.ApiKey;
    //Vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private Marker mMarker;
    private LatLng currentLatLng, originLatLng, destinationLatLng;
    private int spOption, travelWay;
    private static String driver = "Test driver";
    private String cClient = "Test client";
    //Widgets
    private MaterialSearchBar materialSearchBar;
    private ImageView mGps, mInfo, mAdd;
    private Button btnRequest;
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
        materialSearchBar = findViewById(R.id.searchBar);
        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mAdd = findViewById(R.id.ic_add_origin);
        btnRequest = findViewById(R.id.btnRequest);

        Log.i(TAG, "onCreate: Botones definidos");

        if (btnRequest == null) {
            Log.i(TAG, "onCreate: Referencia a btnRequest nula");
            if (findViewById(R.id.btnRequest) == null) {
                Log.i(TAG, "onCreate: Referencia a elemento de layout nula");
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
        } else {
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 2, mLocationListener);
        }

        Places.initialize(MapActivity.this, MY_API_KEY);
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Log.i(TAG, "Search called");
                hideSoftKeyboard();
                if (materialSearchBar.getLastSuggestions().size() <= 0) {
                    Log.w(TAG, "No suggestion points");
                    return;
                }
                Log.i(TAG, "Suggestion points: " + materialSearchBar.getLastSuggestions().size());
                mMap.clear();
                mMarker = null;
                if (spOption != 3) btnRequest.setVisibility(View.INVISIBLE);
                final List<LatLng> boundsList = new ArrayList<>();
                for (int i = 0; i < predictionList.size() && i < 10; i++) {
                    AutocompletePrediction prediction = predictionList.get(i);
                    final String placeId = prediction.getPlaceId();

                    List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER, Place.Field.ID, Place.Field.WEBSITE_URI, Place.Field.LAT_LNG, Place.Field.RATING);

                    final FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                    placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                            Log.i(TAG, "Fetch Success");
                            Place place = fetchPlaceResponse.getPlace();
                            Log.i(TAG, "Place found: " + place.toString());
                            PlaceInfo placeInfo = getPlaceInfo(place);
                            Log.i(TAG, "Adding a bound to the list");
                            boundsList.add(placeInfo.getLatLng());
                            MarkerOptions options = createMarkerOptions(placeInfo);
                            mMap.addMarker(options);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                apiException.printStackTrace();
                                int statusCode = apiException.getStatusCode();
                                Log.i(TAG, "Place not found: " + e.getMessage());
                                Log.i(TAG, "Status code: " + statusCode);
                            }
                        }
                    });
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (boundsList.size() > 0) cameraToBounds(boundsList);
                        else Log.i(TAG, "cameraToBounds function reached without bounds.");
                    }
                }, 1000);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                Log.i(TAG, "Button pressed: " + buttonCode);
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                    Log.i(TAG, "Attempt to open nav menu");
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            materialSearchBar.clearSuggestions();
                            materialSearchBar.disableSearch();
                            materialSearchBar.hideSuggestionsList();
                        }
                    }, 100);
                }
            }
        });



        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    mMap.clear();
                    if (spOption != 3) btnRequest.setVisibility(View.INVISIBLE);
                    destinationLatLng = currentLatLng;
                    materialSearchBar.clearSuggestions();
                } else {
                    final FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                            .setCountry("mx")
                            .setSessionToken(token)
                            .setQuery(s.toString())
                            .setLocationRestriction(LAT_LNG_BOUNDS)
                            .build();
                    placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                            if (task.isSuccessful()) {
                                FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                                if (predictionsResponse != null) {
                                    predictionList = predictionsResponse.getAutocompletePredictions();
                                    Log.i(TAG, "PredictionList: " + predictionList.toString());
                                    List<String> suggestionsList = new ArrayList<>();
                                    Log.i(TAG, "Starting places search");
                                    suggestionsList.clear();
                                    for (int i = 0; i < predictionList.size(); i++) {
                                        AutocompletePrediction prediction = predictionList.get(i);
                                        suggestionsList.add(prediction.getFullText(null).toString());
                                    }
                                    materialSearchBar.updateLastSuggestions(suggestionsList);
                                    if (!materialSearchBar.isSuggestionsVisible()) {
                                        materialSearchBar.showSuggestionsList();
                                    }
                                }
                            } else {
                                Log.w(TAG, "Prediction fetching task unsuccessful");
                                Log.w(TAG, task.getException());
                            }
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                Log.i(TAG, "Element selected: " + position);
                if (position >= predictionList.size()) {
                    Log.w(TAG, "Not a valid result. List size: " + predictionList.size());
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                Log.i(TAG, "Suggestion: " + suggestion);
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.hideSuggestionsList();
                        hideSoftKeyboard();
                    }
                }, 500);

                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER, Place.Field.ID, Place.Field.WEBSITE_URI, Place.Field.LAT_LNG, Place.Field.RATING);

                final FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i(TAG, "Place found: " + place.toString());
                        LatLng placeLatLng = place.getLatLng();
                        PlaceInfo placeInfo;
                        if (placeLatLng != null) {
                            placeInfo = getPlaceInfo(place);
                            if (createMarker(placeInfo)) preparingRouting();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i(TAG, "Place not found: " + e.getMessage());
                            Log.i(TAG, "Status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    private PlaceInfo getPlaceInfo(Place place) {
        PlaceInfo placeInfo = new PlaceInfo();
        try {
            placeInfo.setName(place.getName());
            placeInfo.setAddress(place.getAddress());
            //placeInfo.setAttributions(place.getAttributions().toString());
            placeInfo.setPhoneNumber(place.getPhoneNumber());
            placeInfo.setId(place.getId());
            placeInfo.setWebsiteUri(place.getWebsiteUri());
            placeInfo.setLatLng(place.getLatLng());
            placeInfo.setRating(place.getRating());
            Log.i(TAG, "onSuccess: place: " + placeInfo.toString());
        } catch (NullPointerException e) {
            Log.e(TAG, "onResult: NullPinterException" + e.getMessage());
        }
        return placeInfo;
    }


    public LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(currentLatLng.latitude);
            temp.setLongitude(currentLatLng.longitude);
            float distance = location.distanceTo(temp);
            //Log.i(TAG, "onLocationChanged: Distance: " + distance);
            if (distance > 2) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i(TAG, "onLocationChanged: currentLatLng: " + currentLatLng);
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

    private void init() {
        Log.i(TAG, "init: Iniciando");
        //Centrar cámara en ubicación actual y asignarla como origen
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Clic en ícono de GPS");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.disableSearch();
                        materialSearchBar.hideSuggestionsList();
                    }
                }, 100);
                mMap.clear();
                if (spOption != 3) btnRequest.setVisibility(View.INVISIBLE);
                moveCamera(currentLatLng, DEFAULT_ZOOM, getString(R.string.myLocation));
                originLatLng = currentLatLng;
                Toast.makeText(MapActivity.this, R.string.currentSet, Toast.LENGTH_SHORT).show();
            }
        });
        //Añadir un puntos de origen distinto a la ubicación actual
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Asignando origen distinto a la ubicación actual");
                if (destinationLatLng != null) {
                    originLatLng = destinationLatLng;
                    mMap.clear();
                    moveCamera(originLatLng, DEFAULT_ZOOM, "Origin");
                    if (spOption != 3) btnRequest.setVisibility(View.INVISIBLE);
                    if (travelWay == 0) {
                        Toast.makeText(MapActivity.this, R.string.originSet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapActivity.this, R.string.destinationSet, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MapActivity.this, R.string.locationSetError, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "mInfo: Clic en información del lugar");
                showInfo();
            }
        });
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originLatLng == null || destinationLatLng == null) {
                    Log.e(TAG, "btnRequest: Coordenadas con valor nulo.");
                } else {
                    Log.i(TAG, "Sending request to database");
                    //sendRequestToDatabase();
                }
            }
        });
    }

    private void showInfo() {
        try {
            if (mMarker.isInfoWindowShown()) {
                mMarker.hideInfoWindow();
            } else {
                Log.i(TAG, "showInfo: Información del lugar: " + mMarker.getSnippet());
                mMarker.showInfoWindow();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "showInfo: NullPointerException: " + e.getMessage());
        }
    }

    private void preparingRouting() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSoftKeyboard();
            }
        }, 100);
        if (spOption == 3) {
            Log.i(TAG, "preparingRouting: Servicio de asistencia vial. Enviando un solo conjunto de coordenadas");
            originLatLng = destinationLatLng;
        } else if (travelWay == 0) {
            Log.i(TAG, "preparingRouting: Enviando enrutameinto estándar\noriginLatLng: " + originLatLng + "\ndestinationLatLng: " + destinationLatLng);
            routing(originLatLng, destinationLatLng);
        } else {
            Log.i(TAG, "preparingRouting: Enviando enrutameinto invertido\noriginLatLng: " + destinationLatLng + "\ndestinationLatLng: " + originLatLng);
            routing(destinationLatLng, originLatLng);
        }
        Log.i(TAG, "preparingRouting: Enrutamiento enviado");
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i(TAG, "Marker clicked: " + marker + "\nPosition: " + marker.getPosition());
        if(marker.equals(mMarker)) {
            destinationLatLng = marker.getPosition();
            preparingRouting();
        }else{
            if (mMarker == null) Log.i(TAG, "No marker to compare");
            else Log.i(TAG, "marker: " + marker.getId() + "\nmMarker: " + mMarker.getId());
            mMarker = marker;
            moveCamera(mMarker.getPosition(), DEFAULT_ZOOM, mMarker.getTitle() != null ? mMarker.getTitle() : "");
            mMarker.showInfoWindow();
        }
        return true;
    }

    private void sendRequestToDatabase() {
        Date cDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy"), tf = new SimpleDateFormat("kk:mm:ss");
        DatabaseReference lastRef = activeRef.push();
        lastRef.setValue(new RequestingService(spOption, false,
                originLatLng.latitude, originLatLng.longitude, destinationLatLng.latitude,
                destinationLatLng.longitude, cClient, df.format(cDate), tf.format(cDate)));
        mMap.clear();
        btnRequest.setVisibility(View.INVISIBLE);
        moveCamera(currentLatLng, DEFAULT_ZOOM, getString(R.string.myLocation));
        Toast.makeText(MapActivity.this, R.string.requestSent, Toast.LENGTH_SHORT).show();
        if (spOption == 3) {
            Intent receiveIntent = getIntent();
            RoadSupport rs = new RoadSupport(receiveIntent.getBooleanExtra("rsFlatTire", false),
                    receiveIntent.getBooleanExtra("rsGas", false),
                    receiveIntent.getBooleanExtra("rsLeak", false),
                    receiveIntent.getBooleanExtra("rsBrake", false),
                    receiveIntent.getBooleanExtra("rsBattery", false));
            lastRef.child("roadSupport").setValue(rs);
        } else if (spOption == 4) {
            Intent receiveIntent = getIntent();
            lastRef.child("fOrder").setValue(receiveIntent.getStringExtra("fOrder"));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Mapa listo", Toast.LENGTH_SHORT).show();
        Intent receiveIntent = getIntent();
        spOption = receiveIntent.getIntExtra("spOption", 0);
        travelWay = receiveIntent.getIntExtra("travelWay", 0);
        if (spOption == 1 && travelWay == 1) {
            mAdd.setVisibility(View.GONE);
        }
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.clear();
        if (spOption != 3) btnRequest.setVisibility(View.INVISIBLE);
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
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
    }

    private void getDeviceLocation() {
        Log.i(TAG, "getDeviceLocation: Obteniendo la ubicación actual del dispositivo");
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "onComplete: ¡Ubicación encontrada!");
                            Location currentLocation = (Location) task.getResult();

                            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            originLatLng = currentLatLng;
                            if (spOption == 3) {
                                destinationLatLng = originLatLng;
                                btnRequest.setVisibility(View.VISIBLE);
                                Log.i(TAG, "Current coordinates:\n" + currentLatLng
                                        + "\n" + originLatLng
                                        + "\n" + destinationLatLng);
                            }
                            moveCamera(currentLatLng, DEFAULT_ZOOM, getString(R.string.myLocation));
                        } else {
                            Log.i(TAG, "onComplete: La ubicación actual es nula");
                            Toast.makeText(MapActivity.this, "Incapaz de conseguir ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Excepción de seguridad: " + e.getMessage());
        }
    }

    private Boolean createMarker(PlaceInfo placeInfo) {
        LatLng latLng = placeInfo.getLatLng();
        Log.i(TAG, "Creating placeInfo");
        try {
            MarkerOptions options = createMarkerOptions(placeInfo);
            mMarker = mMap.addMarker(options);
        } catch (NullPointerException e) {
            Log.e(TAG, "createMarker: NullPointerException: " + e.getMessage());
            return false;
        }
        if (originLatLng != latLng) {
            destinationLatLng = latLng;
            Log.i(TAG, "createMarker: Valor asigando a destinationLatLng: " + destinationLatLng);
        } else {
            Log.w(TAG, "createMarker: Se ha intentado asignar el valor de origen al destino, pero ha sido evitado.");
        }
        return true;
    }

    private MarkerOptions createMarkerOptions(PlaceInfo placeInfo) {
        String snippet = "Dirección: " + placeInfo.getAddress() + "\n" +
                "Teléfono: " + placeInfo.getPhoneNumber() + "\n" +
                "Sitio web: " + placeInfo.getWebsiteUri() + "\n" +
                "Costo: " + placeInfo.getRating() + "\n";
        return new MarkerOptions().position(placeInfo.getLatLng()).title(placeInfo.getName()).snippet(snippet);
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.i(TAG, "moveCamera: Moviendo la cámara a:\nLat: " + latLng.latitude + "\nLng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals(getString(R.string.myLocation))) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }
        if (originLatLng != latLng) {
            destinationLatLng = latLng;
            Log.i(TAG, "moveCamera: Valor asigando a destinationLatLng: " + destinationLatLng);
        } else {
            Log.w(TAG, "moveCamera: Se ha intentado asignar el valor de origen al destino, pero ha sido evitado.");
        }
        hideSoftKeyboard();
    }

    private void initMap() {
        Log.i(TAG, "initMap: Iniciando mapa");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.i(TAG, "getLocationPermission: Obteniendo permisos de ubicación");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: Llamado");
        mLocationPermissionGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false;
                        Log.i(TAG, "onRequestPermissionResult: Permisos denegados");
                        return;
                    }
                }
                Log.i(TAG, "onRequestPermissionResult: Permisos concedidos");
                mLocationPermissionGranted = true;
                //Iniciar mapa
                initMap();
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        Log.i(TAG, "hideSoftKeyboard: Hiding keyboard");
        if (imm != null) {
            imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    //Métodos de enrutamiento
    //Ejecutando enrutamiento
    public void routing(LatLng origin, LatLng destination) {
        //Este código debe ir encerrado dentro de condiciones pues debe comportarse distinto según la opción elegida por el usuario en la pantalla anterior
        //Comportamiento por defecto: ir de la ubicación actual al punto señalado
        Log.i(TAG, "Routing:\norigin: " + origin + "\ndestination: " + destination);
        if (origin != null && destination != null) {
            if (origin != destination) {
                //Ajuste de marcadores y cámara
                mMap.clear();
                if (spOption != 3) btnRequest.setVisibility(View.INVISIBLE);
                Marker marker;
                marker = mMap.addMarker(new MarkerOptions().position(origin).title("Origin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.addMarker(new MarkerOptions().position(mMarker.getPosition()).title(mMarker.getTitle()).snippet(mMarker.getSnippet()));
                List<LatLng> latLngList = new ArrayList<>();
                latLngList.add(marker.getPosition());
                latLngList.add(mMarker.getPosition());
                cameraToBounds(latLngList);
                try {
                    String url = getDirectionsUrl(origin, destination);
                    Log.i(TAG, "Routing: Obteniendo url de dirección.\norigin: " + origin + "\ndestination: " + destination + "\nurl: " + url);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    String result = downloadTask.execute(url).get();
                    Log.i(TAG, "Routing: Descarga de datos Json realizada con éxito");
                    List<List<HashMap<String, String>>> routes = parsing(result);
                    mapping(routes);
                    btnRequest.setVisibility(View.VISIBLE);
                } catch (NullPointerException e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.routeError), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "routing: Error de enrutamiento: " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Routing: Las coordenadas de origen y destino son iguales. Verifica sus valores.");
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.noRouteData), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Routing: No se tienen coordenadas para enrutamiento.");
        }
    }

    private void cameraToBounds(List<LatLng> latLngList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngList) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
        Log.i(TAG, "cameraToBounds: Camera moved to bounds");
    }

    private List<List<HashMap<String, String>>> parsing(String... jsonData) {
        Log.i(TAG, "parsing: Recibiendo: " + Arrays.toString(jsonData));
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
        Log.i(TAG, "mapping: mapping: Result: " + result);
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
            if (result.size() == 0) {
                Log.w(TAG, "mapping: result.size no tiene valor. Se asignará un valor de 1 para el ciclo.");
                cfor = 1;
            } else {
                Log.i(TAG, "mapping: result.size: " + result.size());
                cfor = result.size();
            }
            Log.i(TAG, "mapping: Entrando a ciclo For con cfor = " + cfor);
            for (int i = 0; i < cfor; i++) {
                points = new ArrayList();
                List<HashMap<String, String>> path = result.get(i);
                Log.i(TAG, "mapping: Entrando a ciclo For con path.size = " + path.size());
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = point.get("distance");
                        Log.i(TAG, "Calculando distancia: " + distance);
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = point.get("duration");
                        Log.i(TAG, "Calculando duración: " + duration);
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
            Log.i(TAG, "mapping:\nDistancia: " + distance + ", Duración: " + duration);
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error al generar enrutamiento. Verifica que ambos puntos sean alcanzables", Toast.LENGTH_LONG).show();
            Log.e("Background Task", e.toString());
        }
        Log.i(TAG, "mapping: Distance in meters: " + rDistance);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.i("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "DownloadTask: onPostExecute: result: " + result);
            super.onPostExecute(result);

            //ParserTask parserTask = new ParserTask();
            //parserTask.execute(result);
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
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters + "&key=" + MY_API_KEY;

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

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.i("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
