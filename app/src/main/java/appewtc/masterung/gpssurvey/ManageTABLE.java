package appewtc.masterung.gpssurvey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by masterUNG on 1/25/16 AD.
 */
public class ManageTABLE {

    //Explicit
    private MyOpenHelper objMyOpenHelper;
    private SQLiteDatabase writeSqLiteDatabase, readSqLiteDatabase;

    public static final String TABLE_latlngTABLE = "latlngTABLE";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_lat = "lat";
    public static final String COLUMN_lng = "lng";

    public static final String survey_table = "surveyTABLE";
    public static final String column_Date = "Date";
    public static final String column_Name = "Name";
    public static final String column_Address = "Address";
    public static final String column_Lat = "Lat";
    public static final String column_Lng = "Lng";
    public static final String column_Area = "Area";

    public static final String plate_table = "plateTABLE";


    public ManageTABLE(Context context) {
        objMyOpenHelper = new MyOpenHelper(context);
        writeSqLiteDatabase = objMyOpenHelper.getWritableDatabase();
        readSqLiteDatabase = objMyOpenHelper.getReadableDatabase();
    }   // Constructor


    public long addPlate(String strName,
                         String strDate,
                         String strArea) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(column_Name, strName);
        contentValues.put(column_Date, strDate);
        contentValues.put(column_Area, strArea);

        return writeSqLiteDatabase.insert(plate_table, null, contentValues);
    }

    public long addSurvey(String strDate,
                          String strName,
                          String strAddress,
                          String strLat,
                          String strLng,
                          String strArea) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(column_Date, strDate);
        contentValues.put(column_Name, strName);
        contentValues.put(column_Address, strAddress);
        contentValues.put(column_Lat, strLat);
        contentValues.put(column_Lng, strLng);
        contentValues.put(column_Area, strArea);

        return writeSqLiteDatabase.insert(survey_table,null,contentValues);
    }

    public String[] searchLat(String strLat) {

        try {

            String[] resultStrings = null;
            Cursor objCursor = readSqLiteDatabase.query(TABLE_latlngTABLE,
                    new String[]{COLUMN_ID, COLUMN_lat, COLUMN_lng},
                    COLUMN_lat + "=?",
                    new String[]{String.valueOf(strLat)},
                    null, null, null, null);

            if (objCursor != null) {
                if (objCursor.moveToFirst()) {
                    resultStrings = new String[3];
                    resultStrings[0] = objCursor.getString(0);
                    resultStrings[1] = objCursor.getString(1);
                    resultStrings[2] = objCursor.getString(2);
                }
            }
            objCursor.close();
            return resultStrings;
        } catch (Exception e) {
            Log.d("test", e.toString());
            return null;
        }


    }


    public long addNewValueLatLng(String strLat, String strLng) {

        ContentValues objContentValues = new ContentValues();
        objContentValues.put(COLUMN_lat, strLat);
        objContentValues.put(COLUMN_lng, strLng);
        return writeSqLiteDatabase.insert(TABLE_latlngTABLE, null, objContentValues);
    }

}   // Main Class
