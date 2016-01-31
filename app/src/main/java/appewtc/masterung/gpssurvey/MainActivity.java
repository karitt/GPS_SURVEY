package appewtc.masterung.gpssurvey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView latLocalTextView, lngLocalTextView;
    private LocationManager objLocationManager;
    private Criteria objCriteria;
    private boolean GPSABoolean, networkABoolean;
    private ManageTABLE objManageTABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_main_map);

        //Bind Widget
        bindWidget();

        //Create Database
        objManageTABLE = new ManageTABLE(this);

        //Test Add Value
        //objManageTABLE.addNewValueLatLng("testLat", "testLng");

        //Delete All Data in latlngTABLE
        deleteLatLngTABLE();

        //Setup Location
        setUpLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }   // Main Method

    private void deleteLatLngTABLE() {
        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        objSqLiteDatabase.delete(ManageTABLE.TABLE_latlngTABLE, null, null);
    }

    public void clickFix(View view) {

        String strLat = latLocalTextView.getText().toString();
        String strLng = lngLocalTextView.getText().toString();

        Double douLat = Double.parseDouble(strLat);
        Double douLng = Double.parseDouble(strLng);

        LatLng myLatLng = createLatLng(douLat, douLng);

        mMap.addMarker(new MarkerOptions().position(myLatLng));

        objManageTABLE.addNewValueLatLng(strLat, strLng);

    }   // clickFix

    @Override
    protected void onResume() {
        super.onResume();

        afterResume();

    }

    private void afterResume() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        objLocationManager.removeUpdates(objLocationListener);
        String strLat = "Unknow";
        String strLng = "Unknow";

        Location networkLocation = requestUpdateFromProvider(LocationManager.NETWORK_PROVIDER, "Cannot Connected Internet");
        if (networkLocation != null) {
            strLat = String.format("%.7f", networkLocation.getLatitude());
            strLng = String.format("%.7f", networkLocation.getLongitude());
        }

        Location GPSLocation = requestUpdateFromProvider(LocationManager.GPS_PROVIDER, "No Card GPS");
        if (GPSLocation != null) {
            strLat = String.format("%.7f", GPSLocation.getLatitude());
            strLng = String.format("%.7f", GPSLocation.getLongitude());
        }

        latLocalTextView.setText(strLat);
        lngLocalTextView.setText(strLng);




    }   // afterResume

    @Override
    protected void onStop() {
        super.onStop();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        objLocationManager.removeUpdates(objLocationListener);

    }

    @Override
    protected void onStart() {
        super.onStart();

        GPSABoolean = objLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!GPSABoolean) {

            //No GPS Card
            networkABoolean = objLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!networkABoolean) {

                //Cannot Connected Internet
                Intent objIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(objIntent);
            }   // if2

        }   // if1


    }

    public Location requestUpdateFromProvider(String strProvider, String strMyError) {

        Location objLocation = null;

        if (objLocationManager.isProviderEnabled(strProvider)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            objLocationManager.requestLocationUpdates(strProvider, 1000, 10, objLocationListener);
            objLocation = objLocationManager.getLastKnownLocation(strProvider);

        } else {
            Log.d("GPS", "MyError = " + strMyError);
        }


        return objLocation;
    }


    public final LocationListener objLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            latLocalTextView.setText(String.format("%.7f", location.getLatitude()));
            lngLocalTextView.setText(String.format("%.7f", location.getLongitude()));

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    private void setUpLocation() {
        objLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        objCriteria = new Criteria();
        objCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        objCriteria.setAltitudeRequired(false);
        objCriteria.setBearingRequired(false);
    }

    private void bindWidget() {
        latLocalTextView = (TextView) findViewById(R.id.textView3);
        lngLocalTextView = (TextView) findViewById(R.id.textView5);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Create Center Map
        createCenterMap();

        //Set on Marker for Delete
        setOnMarkerForDelete();


    }   // onMapReady

    private void setOnMarkerForDelete() {

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                marker.remove();

                Log.d("GPSsurvey", "position lat = " + marker.getPosition().latitude);

                String strLatMarker = String.format("%.7f", marker.getPosition().latitude);

                Log.d("GPSsurvey", "strLatMarker = " + strLatMarker);

                deleteMarkerFromSQLite(strLatMarker);



                return true;
            }   // event
        });

    } // setOnMarkerForDelete

    private void deleteMarkerFromSQLite(String strLatMarker) {

        try {

            String[] resultStrings = objManageTABLE.searchLat(strLatMarker);
            int intID = Integer.parseInt(resultStrings[0]);
            Log.d("GPSsurvey", "ID = " + intID);

        } catch (Exception e) {
            Log.d("test", e.toString());
        }

    }   // deleteMarkerFromSQLite

    private LatLng createLatLng(Double douLat, Double douLng) {

        LatLng objLatLng = null;

        objLatLng = new LatLng(douLat, douLng);

        return objLatLng;
    }

    private void createCenterMap() {

        Double douLat;
        Double douLng;

        if (latLocalTextView.getText().toString().equals("Unknow") ||
                lngLocalTextView.getText().toString().equals("Unknow")) {

            douLat = 13.66742166;
            douLng = 100.62176228;  // EWTC Bangna

        } else {

            douLat = Double.parseDouble(latLocalTextView.getText().toString());
            douLng = Double.parseDouble(lngLocalTextView.getText().toString());

        }   // if

        LatLng centerMapLatLng = createLatLng(douLat, douLng);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerMapLatLng, 16));

    }   // createCenterMap

}   // Main Class
