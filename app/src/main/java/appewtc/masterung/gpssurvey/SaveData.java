package appewtc.masterung.gpssurvey;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveData extends AppCompatActivity {

    //Explicit
    private TextView dataTextView, areaTextView;
    private EditText nameEditText, addressEditText;
    private ListView listView;
    private String dateString, nameString, addressString, areaString;
    private String[] latStrings, lngStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_data);

        // Bind Widget
        bindWidget();

        // Show TextView
        showTextView();

        // Create ListView
        createListView();

    }   // Main Method

    private void createListView() {

        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ManageTABLE.TABLE_latlngTABLE, null);
        cursor.moveToFirst();
        int intCount = cursor.getCount();
        String[] pointStrings = new String[intCount];
        String[] latStrings = new String[intCount];
        String[] lngStrings = new String[intCount];

        for (int i = 0; i < intCount; i++) {

            pointStrings[i] = cursor.getString(cursor.getColumnIndex(ManageTABLE.COLUMN_ID));
            latStrings[i] = cursor.getString(cursor.getColumnIndex(ManageTABLE.COLUMN_lat));
            lngStrings[i] = cursor.getString(cursor.getColumnIndex(ManageTABLE.COLUMN_lng));

            cursor.moveToNext();
        }   //for
        cursor.close();

        SaveAdapter saveAdapter = new SaveAdapter(this, pointStrings, latStrings, lngStrings);
        listView.setAdapter(saveAdapter);

    }   // createListView

    private void showTextView() {

        java.text.DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();
        dateString = dateFormat.format(date);
        dataTextView.setText(dateString);

        // show area
        areaString = getIntent().getStringExtra("Area");
        areaTextView.setText("Area = " + areaString);

    } // show TextView

    private void bindWidget() {

        dataTextView = (TextView) findViewById(R.id.textView8);
        areaTextView = (TextView) findViewById(R.id.textView11);
        nameEditText = (EditText) findViewById(R.id.editText);
        addressEditText = (EditText) findViewById(R.id.editText2);
        listView = (ListView) findViewById(R.id.listView);

    }   // Bind Widget

}   // Main Class
