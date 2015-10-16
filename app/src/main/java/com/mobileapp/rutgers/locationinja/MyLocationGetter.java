package com.mobileapp.rutgers.locationinja;

import android.Manifest;
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
import android.widget.RadioButton;
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
public class MyLocationGetter extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, com.google.android.gms.location.LocationListener {

    Context context;
    Location myLocation;
    LocationManager myLocManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    boolean OnlyGPS;
    boolean OnlyNetwork;


    static final LatLng BUSCH_CAMPUS_CENTER = new LatLng(40.523128, -74.458797);
    static final LatLng HIGH_POINT_SOLUTIONS_STADIUM = new LatLng(40.513817, -74.464844);
    static final LatLng ELECTRICAL_ENG_BUILDING = new LatLng(40.521663, -74.460665);
    static final LatLng RUTGERS_STUDENT_CENTER = new LatLng(40.502661, -74.451771);
    static final LatLng OLD_QUEENS = new LatLng(40.498720, -74.446229);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyLocationGetter(String name) {
        super(name);
    }

    public MyLocationGetter(Context context, GoogleApiClient _mapiclient, boolean _GPS, boolean _Network) {
        super(String.valueOf(context));
        this.context = context;
        this.mGoogleApiClient = _mapiclient;
        this.OnlyGPS = _GPS;
        this.OnlyNetwork = _Network;

    }

    protected void createLocationRequest() {
        if (OnlyGPS && OnlyNetwork) {
//            Log.e("LOCATIONINJA", "inside create location request method of mylcoationgetter");
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            myLocManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        }
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
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals("GETLOCATION")) {
//            Toast.makeText(context, "in on handle intent of my location getter", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {

        if (OnlyGPS && OnlyNetwork) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else if (OnlyNetwork && !OnlyGPS) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            myLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } else  if(OnlyGPS&& !OnlyNetwork){
            myLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }


    ArrayList<String> distances = new ArrayList<>();

    @Override
    public void onLocationChanged(Location location) {

        if (OnlyGPS && !OnlyNetwork) {
            if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }

            myLocation = myLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Log.e("LOCATIONINJA", "The accuracy of the locatios (GPS) is : " + myLocation.getAccuracy());
        } else if (OnlyNetwork && !OnlyGPS) {
            myLocation = myLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            Log.e("LOCATIONINJA", "The accuracy of the locatios (NETWORK) is : "+ myLocation.getAccuracy());
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (myLocation != null) {
//            Toast.makeText(context, "Latitude: " +myLocation.getLatitude() +" , " + "Longitude: "+myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            final Intent intent = new Intent(context, MapsActivity._bReceiver.class);
            intent.addCategory("LOCATION CHANGED");
            intent.setAction("UPDATELOCATION");
            intent.putExtra("newlocation", myLocation);
            //Attempt to get driving distance from Google Map Directions API
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        distances.add(getDistance(myLocation, BUSCH_CAMPUS_CENTER));
                        distances.add(getDistance(myLocation, RUTGERS_STUDENT_CENTER));
                        distances.add(getDistance(myLocation, OLD_QUEENS));
                        distances.add(getDistance(myLocation, HIGH_POINT_SOLUTIONS_STADIUM));
                        distances.add(getDistance(myLocation, ELECTRICAL_ENG_BUILDING));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            //Straight Line distances between the 2 locations
            if(distances.isEmpty())
            {
                distances.add(getStraightDistance(myLocation, BUSCH_CAMPUS_CENTER));
                distances.add(getStraightDistance(myLocation, RUTGERS_STUDENT_CENTER));
                distances.add(getStraightDistance(myLocation, OLD_QUEENS));
                distances.add(getStraightDistance(myLocation, HIGH_POINT_SOLUTIONS_STADIUM));
                distances.add(getStraightDistance(myLocation, ELECTRICAL_ENG_BUILDING));
            }
            intent.putStringArrayListExtra("distances", distances);
            context.sendBroadcast(intent);
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

    //Getting the path-distance between my present location and the marker.
    //requests to API exhausted
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
//        Log.e("LOcatioNinja", "URL=" + urlString.toString());

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

        JSONArray legs = routes.getJSONArray("legs");

        JSONObject steps = legs.getJSONObject(0);

        JSONObject distance = steps.getJSONObject("distance");

        String sDistance = distance.getString("text");
        int iDistance = distance.getInt("value");
        return sDistance;
    }

    //Finding the straight length distance between my location and the marker
    static final double M_PI = Math.PI;

    private String getStraightDistance(Location src, LatLng dest)
    {
        double _lat = src.getLatitude();
        double _long = src.getLongitude();
        double _lat2 = dest.latitude;
        double _long2 = dest.longitude;

        // Convert degrees to radians
        _lat = _lat * M_PI / 180.0;
        _long = _long * M_PI / 180.0;

        _lat2 = _lat2 * M_PI / 180.0;
        _long2= _long2 * M_PI / 180.0;

        // radius of earth in metres
        double r = 6378100;

        // P
        double rho1 = r * Math.cos(_lat);
        double z1 = r * Math.sin(_lat);
        double x1 = rho1 * Math.cos(_long);
        double y1 = rho1 * Math.sin(_long);

        // Q
        double rho2 = r * Math.cos(_lat2);
        double z2 = r * Math.sin(_lat2);
        double x2 = rho2 * Math.cos(_long2);
        double y2 = rho2 * Math.sin(_long2);

        // Dot product
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (r * r);

        double theta = Math.acos(cos_theta);

        Log.e("CitizenV2V", "in get distance "+ String.valueOf(r*theta));
        // Distance in Metres
        return String.format("%.3f",((r * theta)* 0.000621371));

    }



}