package org.xiaoxingqi.alarmService.persistance;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import org.xiaoxingqi.alarmService.utils.Preconditions;
import org.xiaoxingqi.shengxi.BuildConfig;

public class AlarmProvider extends ContentProvider {
    private AlarmDatabaseHelper mOpenHelper;


    private static final int ALARMS = 1;
    private static final int ALARMS_ID = 2;
    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI(BuildConfig.APPLICATION_ID + ".model", "alarm", ALARMS);
        sURLMatcher.addURI(BuildConfig.APPLICATION_ID + ".model", "alarm/#", ALARMS_ID);
    }

    @Override
    public boolean onCreate() {
        //log.addLogWriter(LogcatLogWriter.create());
        mOpenHelper = new AlarmDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        Preconditions.checkArgument(sURLMatcher.match(url) == ALARMS, "Invalid URL %s", url);
        qb.setTables("alarms");

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret;
        try {
            ret = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort);
        } catch (SQLException e) {
            db.execSQL("DROP TABLE IF EXISTS alarms");
            // I know this is not nice to call onCreate() by ourselves :-)
            mOpenHelper.onCreate(db);
            ret = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort);
        }
        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }
        return ret;
    }

    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case ALARMS:
                return "vnd.android.cursor.dir/alarms";
            case ALARMS_ID:
                return "vnd.android.cursor.item/alarms";
            default:
                throw new IllegalArgumentException("Invalid URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        Preconditions.checkArgument(sURLMatcher.match(url) == ALARMS_ID, "Invalid URL %s", url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String segment = url.getPathSegments().get(1);
        long rowId = Long.parseLong(segment);
        int count = db.update("alarms", values, "_id=" + rowId, null);
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        Preconditions.checkArgument(sURLMatcher.match(url) == ALARMS, "Invalid URL %s", url);
        Uri newUrl = mOpenHelper.commonInsert(initialValues);
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        Preconditions.checkArgument(sURLMatcher.match(url) == ALARMS_ID, "Invalid URL %s", url);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int count;
        String segment = url.getPathSegments().get(1);
        if (TextUtils.isEmpty(where)) {
            count = db.delete("alarms", "_id=" + segment, whereArgs);
        } else {
            count = db.delete("alarms", "_id=" + segment + " AND (" + where + ")", whereArgs);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }
}
