package com.example.hospitalsnearme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    //Variables for Drawer
    private DrawerLayout Drawer;
    private ActionBarDrawerToggle toggle;

    //For Map Variables
    private static final float DEFAULT_ZOOM = 14;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 1;
    private GoogleMap mMap;
    private double latitude,longitude;
    private ArrayList<String> stringTitleArrayList;
    private ArrayList<LatLng> latLngArrayList;
    SupportMapFragment mapFragment;

    //Search and Gps Component
    private ImageView mGps;
    SearchView searchView;

    //For Back Pressed Exit
    private long backPresedTime;
    private Toast backToast;

    //For Coordinates
    public static final String ID = "id";
    public static final String TITLE = "hospitalname";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    LatLng latLng;
    String title;
    MarkerOptions markerOptions = new MarkerOptions();

    //    private String url = "https://192.168.24.43/hospitallocator/location.php";
    private String url = "http://10.0.2.2/hospitallocator/location.php";
    String tag_json_obj = "json_obj_req";

    private RequestQueue mRequestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestQueue = Volley.newRequestQueue(this);

        mGps = (ImageView) findViewById(R.id.gps);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        //Ends of Location Services

        // For Drawer
        Drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this,Drawer,R.string.nav_drawer_open,
                R.string.nav_drawer_close);
        Drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState  == null){
            navigationView.setCheckedItem(R.id.nav_home);
        }
        //End of Drawer

        //For Searching Material
        searchView = findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList=null;
                if(location !=null || !location.equals("")){
                    Geocoder geocoder=new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //End of Searching Material
    }

    //Method for Back Pressed Button
    @Override
    public void onBackPressed(){
        if (backPresedTime + 2000> System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        }else{
            backToast = Toast.makeText(getBaseContext(),"Press back again to exit",Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPresedTime = System.currentTimeMillis();
    }
    //End of  Back Pressed Button

    //For Left Side Navigation Drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_About:
//                Intent a = new Intent(MainActivity.this, about_us.class);
//                startActivity(a);
                break;
            case R.id.nav_contact:
//                Intent c = new Intent(MainActivity.this, contact_us.class);
//                startActivity(c);
                break;
            case R.id.share:
                ApplicationInfo api = getApplicationContext().getApplicationInfo();
                String apkath = api.sourceDir;
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("application/vnd.android.package-archive");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkath)));
                startActivity(Intent.createChooser(share,"ShareVia"));
                break;
            case R.id.nav_logout:
//                sessionManager.logout();
                break;
        }
        Drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //End of Left Side Navigation Drawer

    //For Map Style
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.satellite:
                // For MAP_TYPE_SATELLITE
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.normal:
                //For MAP_TYPE_NORMAL
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.hybrid:
                //For MAP_TYPE_HYBRID
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.terrain:
                //For MAP_TYPE_TERRAIN
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //End of Map Style

    //For GOOGLE MAP ACTIVITY
    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude()
                            +""+ currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    fetchLastLocation();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        latitude = currentLocation.getLatitude();
        longitude = currentLocation.getLongitude();

        LatLng latLng = new LatLng(latitude,longitude);

        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        //For GPS Icon to return at user location
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLastLocation();
            }
        });
        //End of GPS Icon to return at user location

        getMarkers();
    }
    private void addMarker(LatLng latlng, final String title) {
        markerOptions.position(latlng);
        markerOptions.title(title);
        mMap.addMarker(markerOptions);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMarkers() {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    stringTitleArrayList = new ArrayList<>();
                    latLngArrayList = new ArrayList<>();
                    JSONArray jsonArray = response.getJSONArray("hospitals");
                    JSONObject element;
                    for(int i = 0; i < jsonArray.length(); i++){
                        element = jsonArray.getJSONObject(i);
                        title = element.getString("hospitalname");
                        latLng = new LatLng(Double.parseDouble(element.getString("latitude")), Double.parseDouble(element.getString("longtude")));
                        addMarker(latLng, title);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e("Error: ", error.getMessage());
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


//        JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//                Log.e("Response: ", response.toString());
//
//                try {
//                    JSONObject jObj = new JSONObject(response);
//                    JSONArray jsonArray = new JSONArray(getObject);
//
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        title = jsonObject.getString(TITLE);
//                        latLng = new LatLng(Double.parseDouble(jsonObject.getString(LAT)), Double.parseDouble(jsonObject.getString(LNG)));
//
//                        addMarker(latLng, title);
//                    }
//
//                } catch (JSONException e) {
//                    // JSON error
//                    e.printStackTrace();
//                }
//
//            }
//        }, new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("Error: ", error.getMessage());
//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//        AppController.getInstance().addToRequestQueue(request, tag_json_obj);
        mRequestQueue.add(request);
    }
}