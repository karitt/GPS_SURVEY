package appewtc.masterung.gpssurvey;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by masterUNG on 1/25/16 AD.
 */
public class MyOpenHelper extends SQLiteOpenHelper{

    //Explicit
    public static final String DATABASE_NAME = "GPSsurvey.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_latlngTABLE = "create table latlngTABLE (" +
            "_id integer primary key, " +
            "lat text, " +
            "lng text);";

    private static final String create_survey_table = "create table surveyTABLE(" +
            "_id integer primary key," +
            "Date text," +
            "Name text," +
            "Address text," +
            "Lat text," +
            "Lng text," +
            "Area text);";

    public MyOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }   // Constructor

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_latlngTABLE);
        db.execSQL(create_survey_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}   // Main Class
