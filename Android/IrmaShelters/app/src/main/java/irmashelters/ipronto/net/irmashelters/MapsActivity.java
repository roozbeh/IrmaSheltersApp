package irmashelters.ipronto.net.irmashelters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;

    static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    static final int MY_PERMISSIONS_CALL_PHONE = 11;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initLocationObjects();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

//        mMap.setOnMarkerClickListener(this);
        fetchSheltersInfo();
    }

    private void initLocationObjects() {
        mMap.setMyLocationEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

        mMap.setOnInfoWindowClickListener(this);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocationObjects();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }

                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Provider Disabled", Toast.LENGTH_SHORT).show();
    }

    private class ShelterInfo {
        LatLng point;
        String title;
        JSONObject data;

        ShelterInfo(LatLng p, String t, JSONObject d) {
            point = p;
            title = t;
            data = d;
        }
    }
    private ArrayList<ShelterInfo> shelters;
    private HashMap<Marker, ShelterInfo> eventMarkerMap;

    private void fetchSheltersInfo() {
        String url = "https://irma-api.herokuapp.com/api/v1/shelters";

        shelters = new ArrayList<>();

        //Instantiate new instance of our class
        HttpGetRequest getRequest = new HttpGetRequest();
        //Perform the doInBackground method, passing in our url
        try {
            String result = getRequest.execute(url).get();
            JSONObject response = new JSONObject(result);
            JSONArray jShelters = response.getJSONArray("shelters");
            for (int i=0;i<jShelters.length();i++) {
                JSONObject jShelter = jShelters.getJSONObject(i);
                if (jShelter.isNull("longitude") || jShelter.isNull("latitude")) {
                    continue;
                }

                LatLng point = new LatLng(jShelter.getDouble("latitude"), jShelter.getDouble("longitude"));
                String title = jShelter.getString("shelter");
                ShelterInfo shelterInfo = new ShelterInfo(point, title, jShelter);

                shelters.add(shelterInfo);
            }

            markSheltersOnMap();

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void markSheltersOnMap() {
        eventMarkerMap = new HashMap<>(shelters.size());

        for (ShelterInfo s : shelters) {
            System.out.println("POint: " + s.point.latitude + ", " + s.point.longitude);
            System.out.println("Title: " + s.title);

            Marker marker = mMap.addMarker(new MarkerOptions().position(s.point).title(s.title));
            eventMarkerMap.put(marker, s);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        ShelterInfo shelter = eventMarkerMap.get(marker);
        Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
        intent.putExtra("shelterInfo", shelter.data.toString());
        startActivity(intent);

        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        ShelterInfo shelter = eventMarkerMap.get(marker);
        Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
        intent.putExtra("shelterInfo", shelter.data.toString());
        startActivity(intent);
    }


}
