package sk.upjs.ics.android.gardentracker.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import static android.content.ContentResolver.SCHEME_CONTENT;

public interface Contract {
    interface Maintenance extends BaseColumns {
        String TABLE_NAME = "maintenance";
        String NAME = "name";
        String DESCRIPTION = "description";
        String LAST_CHECK = "last_check";
        String NEXT_CHECK = "next_check";
        String INTERVAL_IN_DAYS = "interval";
        String AUTHORITY = "sk.upjs.ics.android.gardentracker";

        Uri CONTENT_URI = new Uri.Builder()
                .scheme(SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build();
    }

    interface PhotoDiary extends BaseColumns{
        String TABLE_NAME = "photo_diary";
        String NAME = "name";
        String DESCRIPTION = "description";
        String PHOTO = "photo";
        String DATE = "date";
        String AUTHORITY = "sk.upjs.ics.android.gardentracker";

        Uri CONTENT_URI = new Uri.Builder()
                .scheme(SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build();
    }

    interface Weather extends BaseColumns {
        String TABLE_NAME = "weather";
        String CITY = "city";
        String AUTHORITY = "sk.upjs.ics.android.gardentracker";

        Uri CONTENT_URI = new Uri.Builder()
                .scheme(SCHEME_CONTENT)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build();
    }
}
