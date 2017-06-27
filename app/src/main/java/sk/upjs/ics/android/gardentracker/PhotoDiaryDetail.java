package sk.upjs.ics.android.gardentracker;

import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import sk.upjs.ics.android.gardentracker.provider.Contract;
import sk.upjs.ics.android.util.Defaults;

public class PhotoDiaryDetail extends AppCompatActivity {

    private Button changeSaveButton;
    private int position, id;
    private TextView timeTextView;
    private EditText nameEditText, descriptionEditText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);// vypne zobrazovanie klavesnice
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_diary_detail);


        fillOutForm(savedInstanceState);
    }





    private void fillOutForm(Bundle savedInstanceState){
        Intent intent = getIntent();
        position = intent.getIntExtra(getResources().getString(R.string.position),0);
        nameEditText = (EditText) findViewById(R.id.photoNameTextView);
        timeTextView = (TextView)findViewById(R.id.photoTimeTextView);
        descriptionEditText = (EditText)findViewById(R.id.photoDescriptionEditText);
        imageView = (ImageView)findViewById(R.id.photoImageView);

        if (savedInstanceState == null) {
            String name, description;
            Cursor cursor = getContentResolver().query(Contract.PhotoDiary.CONTENT_URI, null, null, null, null);
            cursor.moveToPosition(position);
            id = cursor.getInt(cursor.getColumnIndex(Contract.PhotoDiary._ID));

            name = cursor.getString(cursor.getColumnIndex(Contract.PhotoDiary.NAME));
            description = cursor.getString(cursor.getColumnIndex(Contract.PhotoDiary.DESCRIPTION));
            long timeInMillis = cursor.getLong(cursor.getColumnIndex(Contract.PhotoDiary.DATE));

            BitmapFactory.Options options = new BitmapFactory.Options();
            byte[] pic = cursor.getBlob(cursor.getColumnIndex(Contract.PhotoDiary.PHOTO));
            Bitmap bm = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
            imageView.setImageBitmap(bm);

            nameEditText.setText(name);
            timeTextView.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(timeInMillis)));
            descriptionEditText.setText(description);
            nameEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);
        }

        ((Button) findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeSaveButton = (Button) findViewById(R.id.changeSaveButton);
        changeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getResources().getString(R.string.save).equals(changeSaveButton.getText().toString())){
                    String name = nameEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();
                    if(name.length() == 0){
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.fill_form_warning_toast),Toast.LENGTH_LONG).show();
                    }else {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Contract.PhotoDiary.NAME,name);
                        contentValues.put(Contract.PhotoDiary.DESCRIPTION, description);
                        AsyncQueryHandler queryHandler = new AsyncQueryHandler(getContentResolver()) {
                            @Override
                            protected void onUpdateComplete(int token, Object cookie, int result) {
                                Toast.makeText(PhotoDiaryDetail.this, getResources().getString(R.string.updated), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        };
                        Uri selectedUri = ContentUris.withAppendedId(Contract.PhotoDiary.CONTENT_URI, id);
                        queryHandler.startUpdate(0, Defaults.NO_COOKIE,selectedUri,contentValues,null,null);
                        nameEditText.setEnabled(false);
                        descriptionEditText.setEnabled(false);
                    }
                }else{
                    changeSaveButton.setText(getResources().getString(R.string.save));
                    nameEditText.setEnabled(true);
                    descriptionEditText.setEnabled(true);
                }
            }
        });
    }

}
