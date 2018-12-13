package mx.uach.newcompass3;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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

/**
 * Created by Alt on 06/12/2018.
 */

public class Routing implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "Routing";
    private static final String MY_API_KEY = BuildConfig.ApiKey;
    //Vars
    private GoogleMap mMap;
    //Ejecutando enrutamiento
    public GoogleMap routing(View view, LatLng origin, LatLng destination, GoogleMap importMap){
        //Este código debe ir encerrado dentro de condiciones pues debe comportarse distinto según la opción elegida por el usuario en la pantalla anterior
        //Comportamiento por defecto: ir de la ubicación actual al punto señalado
        mMap = importMap;
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
                    Routing.DownloadTask downloadTask = new Routing.DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                    Log.d(TAG, "Routing: Descarga de datos Json realizada con éxito");
                    return mMap;
                } catch (NullPointerException e) {
                    Log.e(TAG, "routing: Error de enrutamiento: " + e.getMessage(), e);
                }
            }else{
                Log.e(TAG, "Routing: Las coordenadas de origen y destino son iguales. Verifica sus valores.");
            }
        }else{
            Log.e(TAG, "Routing: No se tienen coordenadas para enrutamiento.");
        }
        return importMap;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

            Routing.ParserTask parserTask = new Routing.ParserTask();


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
