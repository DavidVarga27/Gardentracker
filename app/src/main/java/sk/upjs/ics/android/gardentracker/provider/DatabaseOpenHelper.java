package sk.upjs.ics.android.gardentracker.provider;

import android.content.ContentValues;
import android.content.Context;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;


import static sk.upjs.ics.android.util.Defaults.*;


public class DatabaseOpenHelper extends SQLiteOpenHelper {
    //TODO: zmenit stringy
    public static final String DATABASE_NAME = "garden_tracker";
    public static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {

        super(context, DATABASE_NAME, DEFAULT_CURSOR_FACTORY, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableMaintenance());
        db.execSQL(createTablePhotoDiary());
        db.execSQL(createTableWeather());
        db.execSQL(createTableSettings());

        insertSettings(db);
    }

    private String createTableMaintenance() {
        String sqlTemplate = "CREATE TABLE %s ("
                + "%s INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "%s TEXT,"
                + "%s TEXT,"
                + "%s LONG,"
                + "%s LONG,"
                + "%s INTEGER"
                + ")";
        return String.format(sqlTemplate,
                Contract.Maintenance.TABLE_NAME,
                Contract.Maintenance._ID,
                Contract.Maintenance.NAME,
                Contract.Maintenance.DESCRIPTION,
                Contract.Maintenance.LAST_CHECK,
                Contract.Maintenance.NEXT_CHECK,
                Contract.Maintenance.INTERVAL_IN_DAYS);
    }

    private String createTablePhotoDiary() {
        String sqlTemplate = "CREATE TABLE %s ("
                + "%s INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "%s TEXT,"
                + "%s TEXT,"
                + "%s BLOB,"
                + "%s LONG"
                + ")";
        return String.format(sqlTemplate,
                Contract.PhotoDiary.TABLE_NAME,
                Contract.PhotoDiary._ID,
                Contract.PhotoDiary.NAME,
                Contract.PhotoDiary.DESCRIPTION,
                Contract.PhotoDiary.PHOTO,
                Contract.PhotoDiary.DATE);
    }

    private String createTableWeather() {
        String sqlTemplate = "CREATE TABLE %s ("
                + "%s INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "%s TEXT"
                + ")";
        return String.format(sqlTemplate,
                Contract.Weather.TABLE_NAME,
                Contract.Weather._ID,
                Contract.Weather.CITY);
    }

    private String createTableSettings() {
        String sqlTemplate = "CREATE TABLE %s ("
                + "%s INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "%s INTEGER"
                + ")";
        return String.format(sqlTemplate,
                Contract.Settings.TABLE_NAME,
                Contract.Settings._ID,
                Contract.Settings.NOTIFICATION_TIME);
    }

    private void insertSettings(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Settings.NOTIFICATION_TIME, 485 );
        db.insert(Contract.Settings.TABLE_NAME, NO_NULL_COLUMN_HACK, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // upgrade is not supported
    }

}