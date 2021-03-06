package appewtc.masterung.gpssurvey;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView latLocalTextView, lngLocalTextView,areaTextView;
    private LocationManager objLocationManager;
    private Criteria objCriteria;
    private boolean GPSABoolean, networkABoolean;
    private ManageTABLE objManageTABLE;
    private double[] latDoubles, lngDoubles;
    private String strArea;

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

    public void clickSaveMain(View view) {

        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ManageTABLE.survey_table, null);
        cursor.moveToFirst();
        String[] latStrings = new String[cursor.getCount()];
        String[] lngStrings = new String[cursor.getCount()];

        for (int i = 0; i < cursor.getCount();i++) {

            latStrings[i] = cursor.getString(cursor.getColumnIndex(ManageTABLE.COLUMN_lat));
            lngStrings[i] = cursor.getString(cursor.getColumnIndex(ManageTABLE.COLUMN_lng));

            cursor.moveToNext();
        }   // for
        cursor.close();

        Intent intent = new Intent(MainActivity.this, SaveData.class);
        intent.putExtra("Area", strArea);
        intent.putExtra("Lat", latStrings);
        intent.putExtra("Lng", lngStrings);
        startActivity(intent);


    }   // clickSaveMain

    public void clickNonFix(View view) {

        Intent objIntent = new Intent(this, MainActivity.class);
        objIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(objIntent);
        finish();

    }   // clickNonFix

    public void clickFinish(View view) {

        String tag = "Calculate";


        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        Cursor objCursor = objSqLiteDatabase.rawQuery("SELECT * FROM " + ManageTABLE.TABLE_latlngTABLE, null);

        objCursor.moveToFirst();

        int intPoint = objCursor.getCount();   //จำนวนเหลี่ยม

        if (intPoint >= 3) {

            //Can Calculate Area
            latDoubles = new double[intPoint];
            lngDoubles = new double[intPoint];
            LatLng[] pointLatLngs = new LatLng[intPoint];
            PolygonOptions myPolygonOptions = new PolygonOptions();

            //Get Value From SQLite
            for (int i=0; i < intPoint; i++) {
                latDoubles[i] = Double.parseDouble(objCursor.getString(objCursor.getColumnIndex(ManageTABLE.COLUMN_lat)));
                lngDoubles[i] = Double.parseDouble(objCursor.getString(objCursor.getColumnIndex(ManageTABLE.COLUMN_lng)));
                pointLatLngs[i] = new LatLng(latDoubles[i], lngDoubles[i]);
                myPolygonOptions.add(pointLatLngs[i]);
                objCursor.moveToNext();

            } // for

            myPolygonOptions.add(pointLatLngs[0]);
            myPolygonOptions.strokeWidth(5)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(50, 148, 194, 72));

            mMap.addPolygon(myPolygonOptions);

        } else {

            //Cannot Calculate Area
            Toast.makeText(MainActivity.this, "ต้องมี 3 จุดขึ้นไปถึงสามารถคำนวนพื้นที่ได้ครับ",
                    Toast.LENGTH_SHORT).show();
        }
        objCursor.close();

        //Start Calculate
        int intTriangle = intPoint - 2; // นี่คือจำนวนสามเหลี่ยมที่สร้างใน รูปหลายเหลี่ยม
        Log.d(tag, "จำนวนเหลี่ยม = " + intPoint);
        Log.d(tag, "สามเหลี่ยมที่สร้างได้ = " + intTriangle);

        //Calculate Area สามเหลี่ยม ย่อย
        double[] subTriangleDoubles = new double[intTriangle];
        double distance1, distance2, distance3, Area=0;
        int intIndex = 0;
        for (int i = 0; i < intTriangle; i++) {

            intIndex = intIndex + 1;
            distance1 = distance(latDoubles[0], lngDoubles[0],
                    latDoubles[intIndex], lngDoubles[intIndex]);

            distance2 = distance(latDoubles[intIndex], lngDoubles[intIndex],
                    latDoubles[intIndex + 1], lngDoubles[intIndex + 1]);

            distance3 = distance(latDoubles[intIndex + 1], lngDoubles[intIndex + 1],
                    latDoubles[0], lngDoubles[0]);

            Area = Area + triangleArea(distance1, distance2, distance3);

            Log.d(tag, "distance1 ==>" + distance1);
            Log.d(tag, "distance1 ==>" + distance2);
            Log.d(tag, "distance1 ==>" + distance3);
            Log.d(tag, "Area รอบที่ " + i + "==>" + Area);

        }   //for

        //Show Area
        strArea = String.format("%.4f", Area) + "Sq.Km";
        areaTextView.setText(strArea);


    } // clickFinish

    private double triangleArea(double douDis1, double douDis2, double douDis3) {

        double douArea = 0;

        double S = (douDis1 + douDis2 + douDis3) / 2;

        douArea = Math.sqrt(S*(S-douDis1)*(S-douDis2)*(S-douDis3));

        return douArea;
    }

    //นี่คือ เมทอด ที่หาระยะ ระหว่างจุด
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515* 1.609344;


        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


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
        areaTextView = (TextView) findViewById(R.id.textView7);
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

            deleteRecordOnID(intID);

        } catch (Exception e) {

            Log.d("test", e.toString());
        }

    }   // deleteMarkerFromSQLite

    private void deleteRecordOnID(int intID) {

        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        objSqLiteDatabase.delete(ManageTABLE.TABLE_latlngTABLE,
                ManageTABLE.COLUMN_ID + "=" + intID, null);

    } // deleteRecordOnID

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