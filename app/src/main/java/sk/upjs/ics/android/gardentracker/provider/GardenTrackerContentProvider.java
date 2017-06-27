package sk.upjs.ics.android.gardentracker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


import static sk.upjs.ics.android.util.Defaults.*;

public class GardenTrackerContentProvider extends ContentProvider {
    //TODO: prerobit porovnavanie uri na switch
    private DatabaseOpenHelper databaseOpenHelper;
    
    @Override
    public boolean onCreate() {
        databaseOpenHelper = new DatabaseOpenHelper(getContext());
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();


        if (uri.toString().equals(Contract.Maintenance.CONTENT_URI.toString())) {
            Cursor cursor = db.query(Contract.Maintenance.TABLE_NAME, ALL_COLUMNS,
                    NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
            cursor.setNotificationUri(getContext().getContentResolver(), Contract.Maintenance.CONTENT_URI);
            return cursor;
        } else if (uri.toString().equals(Contract.PhotoDiary.CONTENT_URI.toString())) {
            Cursor cursor = db.query(Contract.PhotoDiary.TABLE_NAME, ALL_COLUMNS,
                    NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
            cursor.setNotificationUri(getContext().getContentResolver(), Contract.PhotoDiary.CONTENT_URI);
            return cursor;
        }else if (uri.toString().equals(Contract.Weather.CONTENT_URI.toString())) {
            Cursor cursor = db.query(Contract.Weather.TABLE_NAME, ALL_COLUMNS,
                    NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
            cursor.setNotificationUri(getContext().getContentResolver(), Contract.Weather.CONTENT_URI);
            return cursor;
        }else if (uri.toString().equals(Contract.Settings.CONTENT_URI.toString())) {
            Cursor cursor = db.query(Contract.Settings.TABLE_NAME, ALL_COLUMNS,
                    NO_SELECTION, NO_SELECTION_ARGS, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);
            cursor.setNotificationUri(getContext().getContentResolver(), Contract.Settings.CONTENT_URI);
            return cursor;
        }
        return null;
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();

        if(uri.toString().equals(Contract.Maintenance.CONTENT_URI.toString())){
            long id = db.insert(Contract.Maintenance.TABLE_NAME, NO_NULL_COLUMN_HACK, values);
            getContext().getContentResolver().notifyChange(Contract.Maintenance.CONTENT_URI, NO_CONTENT_OBSERVER);
            return Uri.withAppendedPath(Contract.Maintenance.CONTENT_URI, String.valueOf(id));
        }else if(uri.toString().equals(Contract.PhotoDiary.CONTENT_URI.toString())) {
            long id = db.insert(Contract.PhotoDiary.TABLE_NAME, NO_NULL_COLUMN_HACK, values);
            getContext().getContentResolver().notifyChange(Contract.PhotoDiary.CONTENT_URI, NO_CONTENT_OBSERVER);
            return Uri.withAppendedPath(Contract.PhotoDiary.CONTENT_URI, String.valueOf(id));
        }else if(uri.toString().equals(Contract.Weather.CONTENT_URI.toString())) {
            long id = db.insert(Contract.Weather.TABLE_NAME, NO_NULL_COLUMN_HACK, values);
            getContext().getContentResolver().notifyChange(Contract.Weather.CONTENT_URI, NO_CONTENT_OBSERVER);
            return Uri.withAppendedPath(Contract.Weather.CONTENT_URI, String.valueOf(id));
        }else if(uri.toString().equals(Contract.Settings.CONTENT_URI.toString())) {
            long id = db.insert(Contract.Settings.TABLE_NAME, NO_NULL_COLUMN_HACK, values);
            getContext().getContentResolver().notifyChange(Contract.Settings.CONTENT_URI, NO_CONTENT_OBSERVER);
            return Uri.withAppendedPath(Contract.Settings.CONTENT_URI, String.valueOf(id));
        }

        return null;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
        int lastIndexOfSlash =  uri.toString().lastIndexOf("/");
        String uriWithouId =uri.toString().substring(0,lastIndexOfSlash);

        if(uriWithouId.equals(Contract.Maintenance.CONTENT_URI.toString())) {
            String id = uri.getLastPathSegment();
            String[] whereArgs = {id};
            int affectedRows = db.delete(Contract.Maintenance.TABLE_NAME, Contract.Maintenance._ID + "=?", whereArgs);
            getContext().getContentResolver().notifyChange(Contract.Maintenance.CONTENT_URI, NO_CONTENT_OBSERVER);
            return affectedRows;
        }else if(uriWithouId.equals(Contract.PhotoDiary.CONTENT_URI.toString())) {
            String id = uri.getLastPathSegment();
            String[] whereArgs = {id};
            int affectedRows = db.delete(Contract.PhotoDiary.TABLE_NAME, Contract.PhotoDiary._ID + "=?", whereArgs);
            getContext().getContentResolver().notifyChange(Contract.PhotoDiary.CONTENT_URI, NO_CONTENT_OBSERVER);
            return affectedRows;
        }

        return 0;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
        int lastIndexOfSlash =  uri.toString().lastIndexOf("/");
        String uriWithouId =uri.toString().substring(0,lastIndexOfSlash);

        if(uriWithouId.equals(Contract.Maintenance.CONTENT_URI.toString())) {
            String id = uri.getLastPathSegment();
            String[] whereArgs = { id };
            selection = "_id = ?";
            int affectedRows = db.update(Contract.Maintenance.TABLE_NAME,values,selection,whereArgs);
            getContext().getContentResolver().notifyChange(Contract.Maintenance.CONTENT_URI,NO_CONTENT_OBSERVER);
            return affectedRows;
        }else if(uriWithouId.equals(Contract.PhotoDiary.CONTENT_URI.toString())) {
            String id = uri.getLastPathSegment();
            String[] whereArgs = { id };
            selection = "_id = ?";
            int affectedRows = db.update(Contract.PhotoDiary.TABLE_NAME,values,selection,whereArgs);
            getContext().getContentResolver().notifyChange(Contract.PhotoDiary.CONTENT_URI,NO_CONTENT_OBSERVER);
            return affectedRows;
        }else if(uriWithouId.equals(Contract.Weather.CONTENT_URI.toString())) {
            String id = uri.getLastPathSegment();
            String[] whereArgs = { id };
            selection = "_id = ?";
            int affectedRows = db.update(Contract.Weather.TABLE_NAME,values,selection,whereArgs);
            getContext().getContentResolver().notifyChange(Contract.Weather.CONTENT_URI,NO_CONTENT_OBSERVER);
            return affectedRows;
        }else if(uriWithouId.equals(Contract.Settings.CONTENT_URI.toString())) {
            String id = uri.getLastPathSegment();
            String[] whereArgs = { id };
            selection = "_id = ?";
            int affectedRows = db.update(Contract.Settings.TABLE_NAME,values,selection,whereArgs);
            getContext().getContentResolver().notifyChange(Contract.Settings.CONTENT_URI,NO_CONTENT_OBSERVER);
            return affectedRows;
        }

        return 0;
    }
    
    @Override
    public String getType(Uri uri) {
        return null;
    }



}