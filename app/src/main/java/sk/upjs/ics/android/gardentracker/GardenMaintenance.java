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

import java.util.Calendar;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class GardenMaintenance extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter listViewAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garden_maintenance);

        //pre lisViewAdapter vytvorim SimpleCursorAdapter a nastavi sa z ktorych stlpcov do ktorych View-ov sa maju ncitavat data-ale je tam uprava
        listView = (ListView) findViewById(R.id.listView);
        String[] from = {Contract.Maintenance.NAME, Contract.Maintenance.NEXT_CHECK};
        int[] to = {R.id.maintenanceTitleTextView, R.id.maintenanceDaysLeftTextView};
        listViewAdapter = new SimpleCursorAdapter(this, R.layout.one_maintenance_layout, Defaults.NO_CURSOR, from, to, Defaults.NO_FLAGS);

        //ak stlpec je NEXT_CHECK, tak zneho sa upravuje Days Left

        listViewAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                TextView textView = (TextView) view;
                final View parent = (View) view.getParent();//jedno policko maintenance
                final int position = cursor.getPosition();

                if (columnIndex == cursor.getColumnIndex(Contract.Maintenance.NAME)) {
                    String name = cursor.getString(columnIndex);
                    textView.setText(name);
                    return true;
                } else if (columnIndex == cursor.getColumnIndex(Contract.Maintenance.NEXT_CHECK)) {
                    long nextCheck = cursor.getLong(columnIndex);
                    long actualDate = System.currentTimeMillis();
                    long daysLeft = getDaysLeft(nextCheck,actualDate);

                    String days = String.format(getResources().getString(R.string.days_left), daysLeft);
                    textView.setText(days);

                    //nastavenie farieb jednotlivych uloh
                    if(daysLeft<0){
                        parent.setBackgroundColor(getResources().getColor(R.color.warning));
                    }else if(daysLeft>0){
                        parent.setBackgroundColor(getResources().getColor(R.color.zelena));
                    }else{
                        parent.setBackgroundColor(getResources().getColor(R.color.oranzova));
                    }

                    //otvorenie detailu
                    parent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(GardenMaintenance.this, GardenMaintenanceFormDetail.class);
                            intent.putExtra(getResources().getString(R.string.position), position);
                            startActivity(intent);
                        }
                    });

                    //vymazanie jednej ulohy
                    parent.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final int position = listView.getPositionForView(parent);//pozicia kliknutia

                            //aby sa vymazalo v inom vlakne
                            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                @Override
                                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                    super.onQueryComplete(token, cookie, cursor);

                                    cursor.moveToPosition(position);//presunie cursor na poziciu a zisti ID ulohy
                                    final int id = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance._ID));

                                    //dialog pre vymazanie
                                    final AlertDialog dialog = new AlertDialog.Builder(GardenMaintenance.this).setMessage(getResources().getString(R.string.delete_maintenance_question))
                                            .setTitle(getResources().getString(R.string.warning)).setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                  //toto je asi blbost takto dalsi Async volat
                                                    //  AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {};
                                                 //   queryHandler.startDelete(0, null, Uri.withAppendedPath(Contract.Maintenance.CONTENT_URI, String.valueOf(id)), Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);
                                                    getContentResolver().delete(Uri.withAppendedPath(Contract.Maintenance.CONTENT_URI, String.valueOf(id)),Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.close), null)
                                            .show();
                                    doKeepDialog(dialog);
                                }
                            };
                            queryHandler.startQuery(0, Defaults.NO_COOKIE,Contract.Maintenance.CONTENT_URI,null,null,null,null);
                            return true;
                        }
                    });

                    //DONE BUTTON next check sa nastavi akt. cas + interval,   last check bude akt. cas
                    Button doneButton = (Button) parent.findViewById(R.id.doneButton);
                    doneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final AlertDialog dialog = new AlertDialog.Builder(GardenMaintenance.this).setMessage(getResources().getString(R.string.done_maintenance_question))
                                    .setTitle(getResources().getString(R.string.warning)).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final int position = listView.getPositionForView(parent);

                                            //najprv sa zisti IDcko a rovno sa aj updateuje
                                            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                                @Override
                                                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                                    super.onQueryComplete(token, cookie, cursor);
                                                    cursor.moveToPosition(position);
                                                    int id = cursor.getInt(cursor.getColumnIndex(Contract.Maintenance._ID));
                                                    long intervalInDays = cursor.getLong(cursor.getColumnIndex(Contract.Maintenance.INTERVAL_IN_DAYS));
                                                    long lastCheck = System.currentTimeMillis();
                                                    long nextCheck = lastCheck + intervalInDays*24*3600*1000;
                                                    ContentValues contentValues = new ContentValues();
                                                    contentValues.put(Contract.Maintenance.LAST_CHECK, lastCheck);
                                                    contentValues.put(Contract.Maintenance.NEXT_CHECK, nextCheck);

                                                    Uri selectedMaintenanceUri = ContentUris.withAppendedId(Contract.Maintenance.CONTENT_URI, id);
                                                    getContentResolver().update(selectedMaintenanceUri,contentValues,null,null);
                                                    Toast.makeText(GardenMaintenance.this, getResources().getString(R.string.updated), Toast.LENGTH_LONG).show();
                                                }
                                            };
                                            queryHandler.startQuery(0, Defaults.NO_COOKIE,Contract.Maintenance.CONTENT_URI,null,null,null,null);

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

    //vytvorenie cursorLoadera
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != Defaults.DEFAULT_LOADER_ID) {
            throw new IllegalStateException(String.format(getResources().getString(R.string.invalid_loader),id));
        }

        CursorLoader cursorLoader = new CursorLoader(this);
        cursorLoader.setUri(Contract.Maintenance.CONTENT_URI);
        cursorLoader.setSortOrder(Contract.Maintenance.NEXT_CHECK);
        return cursorLoader;
    }

    //posle data z cursora do listViewAdaptera, a ten listViewAdapter uz vie s datami pracovat cez ten viewBinder
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.setNotificationUri(getContentResolver(), Contract.Maintenance.CONTENT_URI);
        listViewAdapter.swapCursor(cursor);

    }

    //cursor v listViewAdapteri sa nastavi na null
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        listViewAdapter.swapCursor(Defaults.NO_CURSOR);
    }

    //otvori sa GMForm pre vlozenie novej ulohy
    public void onFabClick(View view) {
        Intent intent = new Intent(GardenMaintenance.this, GardenMaintenanceForm.class);
        startActivity(intent);
    }


    //vrati pocet zostavajucich dni
    private long getDaysLeft(long nextCheck, long actualDate){
        //upravuje sa Days Left, tak, ze do nextCheck dam NEXT_CHECK bez milisekund a do actualDate dam taky isty cas
        //ako v nextCheck ale den,mesiac a rok je aktualny
        //daysLeft je potom nextCheck - actualDate....

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nextCheck);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(Calendar.MILLISECOND);
        nextCheck = nextCheck - milliseconds;



        calendar.setTimeInMillis(actualDate);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        calendar.set(year,month,day,hour,minute,second);
        actualDate = calendar.getTimeInMillis();

        long daysLeft = (long) Math.ceil((nextCheck - actualDate) / (24.0 * 3600 * 1000));

        return daysLeft;
    }
}
