package addtotech.com.popmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import addtotech.com.popmovies.MovieContract.MovieEntry;

/**
 * Created by MrGupta on 06-Oct-16.
 */

public class MovieDbHelper extends SQLiteOpenHelper{
    private static MovieDbHelper movieDbHelper;
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MovieDbHelper getInstance(Context context) {
        if(movieDbHelper == null) {
            movieDbHelper = new MovieDbHelper(context.getApplicationContext());
        }
        return movieDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER + " TEXT, " +
                MovieEntry.COLUMN_RATING + " TEXT, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT, " +
                MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
