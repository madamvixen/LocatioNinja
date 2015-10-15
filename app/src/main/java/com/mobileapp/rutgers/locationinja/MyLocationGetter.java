package com.mobileapp.rutgers.locationinja;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malabika on 10/8/2015.
 */
public class MyLocationGetter extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    Context context;
    Location myLocation;
    LocationManager myLocManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    static final LatLng BUSCH_CAMPUS_CENTER = new LatLng(40.523128,-74.458797);
    static final LatLng HIGH_POINT_SOLUTIONS_STADIUM = new LatLng(40.513817,-74.464844);
    static final LatLng ELECTRICAL_ENG_BUILDING = new LatLng(40.521663,-74.460665);
    static final LatLng RUTGERS_STUDENT_CENTER = new LatLng(40.502661,-74.451771);
    static final LatLng OLD_QUEENS = new LatLng(40.498720,-74.446229);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyLocationGetter(String name) {
        super(name);
    }

    public MyLocationGetter(Context context, GoogleApiClient _mapiclient){
        super(String.valueOf(context));
        this.context = context;
        this.mGoogleApiClient = _mapiclient;
        myLocManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

    }

    protected void createLocationRequest() {

        Log.e("LOCATIONINJA", "inside create location request method of mylcoationgetter");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(8000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if(intent.getAction().equals("GETLOCATION")) {

            Toast.makeText(context, "in on handle intent of my location getter", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);
    }

    ArrayList<String> distances = new ArrayList<>();


    @Override
    public void onLocationChanged(Location location) {

        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(myLocation!=null)
        {
//            Toast.makeText(context, "Latitude: " +myLocation.getLatitude() +" , " + "Longitude: "+myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(context, MapsActivity._bReceiver.class);
            intent.addCategory("LOCATION CHANGED");
            intent.setAction("UPDATELOCATION");
            intent.putExtra("newlocation", myLocation);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        distances.add(getDistance(myLocation, BUSCH_CAMPUS_CENTER));
                        distances.add(getDistance(myLocation, RUTGERS_STUDENT_CENTER));
                        distances.add(getDistance(myLocation,OLD_QUEENS));
                        distances.add(getDistance(myLocation, HIGH_POINT_SOLUTIONS_STADIUM));
                        distances.add(getDistance(myLocation, ELECTRICAL_ENG_BUILDING));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            intent.putStringArrayListExtra("distances", distances);

            if(Looper.myLooper() == Looper.getMainLooper()) {
                // Current Thread is Main Thread.
                context.sendBroadcast(intent);
            }
        }
    }

    private String getDistance(Location src, LatLng dest) throws IOException, JSONException {

        StringBuilder urlString = new StringBuilder();

        urlString.append("http://maps.googleapis.com/maps/api/directions/json?");
        urlString.append("origin=");//from
        urlString.append( Double.toString((double)src.getLatitude()));
        urlString.append(",");
        urlString.append( Double.toString((double)src.getLongitude()));
        urlString.append("&destination=");//to
        urlString.append( Double.toString((double)dest.latitude));
        urlString.append(",");
        urlString.append( Double.toString((double)dest.longitude));
        urlString.append("&mode=driving&sensor=true");
//        Log.e("LOcatioNinja","URL="+urlString.toString());

        // get the JSON And parse it to get the directions data.
        HttpURLConnection urlConnection= null;
        URL url = null;

        url = new URL(urlString.toString());
        try {
            urlConnection=(HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }


        InputStream inStream = null;
        try {
            inStream = urlConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

        String temp, response = "";
        while((temp = bReader.readLine()) != null){
            //Parse data
            response += temp;
        }
        //Close the reader, stream & connection
        bReader.close();
        inStream.close();
        urlConnection.disconnect();

        //Sortout JSONresponse
        JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
        JSONArray array = object.getJSONArray("routes");

        //Routes is a combination of objects and arrays
        JSONObject routes = array.getJSONObject(0);
        //Log.d("JSON","routes: "+routes.toString());

//        String summary = routes.getString("summary");
        //Log.d("JSON","summary: "+summary);

        JSONArray legs = routes.getJSONArray("legs");
        //Log.d("JSON","legs: "+legs.toString());

        JSONObject steps = legs.getJSONObject(0);
        //Log.d("JSON","steps: "+steps.toString());

        JSONObject distance = steps.getJSONObject("distance");
        Log.e("LocatioNinja","distance: "+distance.toString());

        String sDistance = distance.getString("text");
        int iDistance = distance.getInt("value");
        return sDistance;
    }

}