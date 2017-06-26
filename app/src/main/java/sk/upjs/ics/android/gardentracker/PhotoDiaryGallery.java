package sk.upjs.ics.android.gardentracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class PhotoDiaryGallery extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int CAMERA_REQUEST = 1888;
    private SimpleCursorAdapter gridViewAdapter;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_diary_gallery);

        String[] from = {Contract.PhotoDiary.PHOTO};
        int[] to = {R.id.onePhotoImageView};

        gridView = (GridView) findViewById(R.id.photoGridView);
        gridViewAdapter = new SimpleCursorAdapter(this, R.layout.one_photo_layout, Defaults.NO_CURSOR, from, to, Defaults.NO_FLAGS);

        gridViewAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                Log.d("index",String.valueOf(columnIndex));
                if(columnIndex == cursor.getColumnIndex(Contract.PhotoDiary.PHOTO)){
                    ImageView imageView = (ImageView)view;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                   // options.inDither = false;
                   // options.inTempStorage = new byte[1024 *32];
                    byte[] pic = cursor.getBlob(cursor.getColumnIndex(Contract.PhotoDiary.PHOTO));

                    Bitmap bm = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
                    imageView.setImageBitmap(bm);

                    final View parent = (View) view.getParent();
                    final int position = cursor.getPosition();
                    parent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(PhotoDiaryGallery.this,PhotoDiaryDetail.class);
                            intent.putExtra(getResources().getString(R.string.position), position);
                            startActivity(intent);
                        }
                    });

                    parent.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int position = gridView.getPositionForView(parent);
                            Cursor cursor = getContentResolver().query(Contract.PhotoDiary.CONTENT_URI, null, null, null, null);
                            cursor.moveToPosition(position);
                            final int id = cursor.getInt(cursor.getColumnIndex(Contract.PhotoDiary._ID));

                            final AlertDialog dialog = new AlertDialog.Builder(PhotoDiaryGallery.this).setMessage(getResources().getString(R.string.delete_photo_question))
                                    .setTitle(getResources().getString(R.string.warning)).setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                                                // no specific behavior is required after DELETE is completed
                                            };
                                            queryHandler.startDelete(0, null, Uri.withAppendedPath(Contract.PhotoDiary.CONTENT_URI, String.valueOf(id)), Defaults.NO_SELECTION, Defaults.NO_SELECTION_ARGS);

                                        }
                                    })
                                    .setNegativeButton(getResources().getString(R.string.close), null)
                                    .show();
                            doKeepDialog(dialog);
                            return true;
                        }
                    });

                    return true;
                }
                return false;
            }
        });
        gridView.setAdapter(gridViewAdapter);
        getLoaderManager().initLoader(Defaults.DEFAULT_LOADER_ID, Bundle.EMPTY, this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(checkCameraHardware(this)){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA_REQUEST);
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.camera_toast),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            fab.setVisibility(View.INVISIBLE);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            byte[] photoBlob = getBytesFromBitmap(photo);
            insertPhotoInDB(photoBlob);
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
// this device has a camera
            return true;
        } else {
// no camera on this device
            return false;
        }
    }

    private  byte[] getBytesFromBitmap(Bitmap photo) {
        if (photo!=null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, bos);
            return bos.toByteArray();
        }
        return null;
    }

    private void insertPhotoInDB(byte[] photo){
        long time = System.currentTimeMillis();
        String name = String.format(getResources().getString(R.string.jpg),time);

        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.PhotoDiary.NAME, name);
        contentValues.put(Contract.PhotoDiary.DESCRIPTION, "");
        contentValues.put(Contract.PhotoDiary.DATE, time);
        contentValues.put(Contract.PhotoDiary.PHOTO, photo);

        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()){
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                Toast.makeText(PhotoDiaryGallery.this, "Saved!", Toast.LENGTH_LONG).show();

            }
        };
        queryHandler.startInsert(0, Defaults.NO_COOKIE, Contract.PhotoDiary.CONTENT_URI, contentValues);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != Defaults.DEFAULT_LOADER_ID) {
            throw new IllegalStateException("Invalid loader ID " + id);
        }

        CursorLoader cursorLoader = new CursorLoader(this);
        cursorLoader.setUri(Contract.PhotoDiary.CONTENT_URI);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.setNotificationUri(getContentResolver(), Contract.PhotoDiary.CONTENT_URI);
        Log.d("pocet riadkov",String.valueOf(data.getCount()));
        data.moveToFirst();
        while (data.moveToNext()) {
            Log.d("foto",data.getBlob(data.getColumnIndex(Contract.PhotoDiary.PHOTO)).toString());
        }
        gridViewAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        gridViewAdapter.swapCursor(Defaults.NO_CURSOR);
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
}
