package appewtc.masterung.gpssurvey;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    public void clickSaveData(View view) {

        nameString = nameEditText.getText().toString().trim();
        addressString = addressEditText.getText().toString().trim();

        //Check Space
        if (nameString.equals("") || addressString.equals("")) {
            Toast.makeText(this, "กรุณากรอกให้ครบทุกช่องครับ", Toast.LENGTH_SHORT).show();
        } else {

            AddValueToSQLite();

        }


    }   //clickSaveData

    private void AddValueToSQLite() {

        ManageTABLE manageTABLE = new ManageTABLE(this);
        manageTABLE.addPlate(nameString, dateString, areaString);

        for (int i = 0; i < latStrings.length; i++) {

            manageTABLE.addSurvey(dateString, nameString,
                    addressString, latStrings[i], lngStrings[i], areaString);

        }   //for

        Toast.makeText(this, "ขอบคุณครับ บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SaveData.this, ShowHistory.class);
        startActivity(intent);
        finish();


    }   //AddValueToSQLite

    private void createListView() {

        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase(MyOpenHelper.DATABASE_NAME,
                MODE_PRIVATE, null);
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ManageTABLE.TABLE_latlngTABLE, null);
        cursor.moveToFirst();
        int intCount = cursor.getCount();
        String[] pointStrings = new String[intCount];
        latStrings = new String[intCount];
        lngStrings = new String[intCount];

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
