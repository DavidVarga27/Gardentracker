package sk.upjs.ics.android.gardentracker;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class GardenMaintenance extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter listViewAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_maintenance);

        listView = (ListView) findViewById(R.id.listView);


            Log.d("nacitava sa", "z databazy");
            String[] from = {Contract.Maintenance.NAME, Contract.Maintenance.NEXT_CHECK};
            int[] to = {R.id.maintenanceTitleTextView, R.id.maintenanceDaysLeftTextView};
            listViewAdapter = new SimpleCursorAdapter(this, R.layout.one_maintenance_layout, Defaults.NO_CURSOR, from, to, Defaults.NO_FLAGS);
            listViewAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                    TextView textView = (TextView) view;
                    final View parent = (View) view.getParent();//jedno policko maintenance
                    final int position = cursor.getPosition();

                    if (columnIndex == cursor.getColumnIndex(Contract.Maintenance.NAME)) {
                        //Log.d("nacitalo sa", "z databazy");
                        String name = cursor.getString(columnIndex);
                        textView.setText(name);
                        return true;
                    } else if (columnIndex == cursor.getColumnIndex(Contract.Maintenance.NEXT_CHECK)) {
                        //Log.d("nacitalo sa", "z databazy");
                        long nextCheck = cursor.getLong(columnIndex);
                        long actualDate = System.currentTimeMillis();
                        long daysLeft = (nextCheck - actualDate) / (24 * 3600 * 1000)+1;
                        Log.d("days",String.valueOf(daysLeft));
                        String days = String.format(getResources().getString(R.string.days_left), daysLeft);
                        textView.setText(days);

                        parent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(GardenMaintenance.this, GardenMaintenanceFormDetail.class);
                                intent.putExtra(getResources().getString(R.string.position), position);
                                startActivity(intent);
                            }
                        });

                        parent.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                int position = listView.getPositionForView(parent);
                                Cursor cursor = getContentResolver().query(Contract.Maintenance.CONTENT_URI, null, null, null, null);
                                cursor.moveToPosition(position);
                                final int id = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance._ID));

                                final AlertDialog dialog = new AlertDialog.Builder(GardenMaintenance.this).setMessage(getResources().getString(R.string.delete_maintenance_question))
                                        .setTitle(getResources().getString(R.string.warning)).setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                                    // no specific behavior is required after DELETE is completed
                                                };
                                                queryHandler.startDelete(0, null, Uri.withAppendedPath(Contract.Maintenance.CONTENT_URI, String.valueOf(id)), Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);

                                            }
                                        })
                                        .setNegativeButton(getResources().getString(R.string.close), null)
                                        .show();
                                doKeepDialog(dialog);

                                //  Uri selectedMaintenanceUri = ContentUris.withAppendedId(Contract.Maintenance.CONTENT_URI, id);

                                return true;
                            }
                        });

                        Button doneButton = (Button) parent.findViewById(R.id.doneButton);
                        doneButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final AlertDialog dialog = new AlertDialog.Builder(GardenMaintenance.this).setMessage(getResources().getString(R.string.done_maintenance_question))
                                        .setTitle(getResources().getString(R.string.warning)).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                int position = listView.getPositionForView(parent);
                                                Cursor cursor = getContentResolver().query(Contract.Maintenance.CONTENT_URI, null, null, null, null);
                                                cursor.moveToPosition(position);
                                                final int id = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance._ID));



                                                long intervalInDays = cursor.getLong(cursor.getColumnIndex(Contract.Maintenance.INTERVAL_IN_DAYS));




                                                long last_check = System.currentTimeMillis();
                                                long next_check = last_check + intervalInDays*24*3600*1000;
                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put(Contract.Maintenance.LAST_CHECK, last_check);
                                                contentValues.put(Contract.Maintenance.NEXT_CHECK, next_check);

                                                AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                                    @Override
                                                    protected void onUpdateComplete(int token, Object cookie, int result) {
                                                        Toast.makeText(GardenMaintenance.this, "Updated!", Toast.LENGTH_LONG).show();

                                                    }
                                                };
                                                Uri selectedMaintenanceUri = ContentUris.withAppendedId(Contract.Maintenance.CONTENT_URI, id);
                                                queryHandler.startUpdate(0,Defaults.NO_COOKIE,selectedMaintenanceUri,contentValues,null,null);
                                            }
                                        })
                                        .setNegativeButton(getResources().getString(R.string.no), null)
                                        .show();
                                doKeepDialog(dialog);
                            }
                        });
                        return true;
                    }
                    return false;
                }
            });
            listView.setAdapter(listViewAdapter);
            getLoaderManager().initLoader(Defaults.DEFAULT_LOADER_ID, Bundle.EMPTY, this);

    }


    //metoda na to aby ked je v Alert Dialogu pri otazke vymazania - pri obrateni telefonu ostane otvorene AlertDialog(lebo pred tym sa
    // zmizlo a do konzoly vypisovalo bludy)
    private static void doKeepDialog(Dialog dialog){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != Defaults.DEFAULT_LOADER_ID) {
            throw new IllegalStateException("Invalid loader ID " + id);
        }

        CursorLoader cursorLoader = new CursorLoader(this);
        cursorLoader.setUri(Contract.Maintenance.CONTENT_URI);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.setNotificationUri(getContentResolver(), Contract.Maintenance.CONTENT_URI);
        listViewAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listViewAdapter.swapCursor(Defaults.NO_CURSOR);
    }

    public void onFabClick(View view) {
        Intent intent = new Intent(GardenMaintenance.this, GardenMaintenanceForm.class);
        startActivity(intent);
    }


    /*  ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Maintenance.NAME, "Test this");
        contentValues.put(Contract.Maintenance.DESCRIPTION, "Test description");
        contentValues.put(Contract.Maintenance.LAST_CHECK, System.currentTimeMillis());
        contentValues.put(Contract.Maintenance.NEXT_CHECK, System.currentTimeMillis()+5*24*3600*1000);
        contentValues.put(Contract.Maintenance.INTERVAL_IN_DAYS, 5*24*3600*1000);

        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                Toast.makeText(GardenMaintenance.this, "Note was saved", Toast.LENGTH_LONG).show();
            }
        };
        queryHandler.startInsert(0, Defaults.NO_COOKIE, Contract.Maintenance.CONTENT_URI, contentValues);
    }



   @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
            // no specific behavior is required after DELETE is completed
        };
        queryHandler.startDelete(0, null, Uri.withAppendedPath(Contract.Maintenance.CONTENT_URI, String.valueOf(id)), Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);

        return true;
    }*/
}
