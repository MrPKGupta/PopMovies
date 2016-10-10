package addtotech.com.popmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MrGupta on 06-Oct-16.
 */

public class MovieDataSource {
    private MovieDbHelper movieDbHelper;

    public MovieDataSource(MovieDbHelper movieDbHelper) {
        this.movieDbHelper = movieDbHelper;
    }

    public void insertItem(ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
        db.close();
    }

    public boolean checkItem(String id) {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        String[] projection = {MovieContract.MovieEntry._ID};
        String whereId = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = \"" + id + "\"";
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME, projection, whereId, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void deleteItem(String id) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        String whereId = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = \"" + id + "\"";
        db.delete(MovieContract.MovieEntry.TABLE_NAME, whereId, null);
    }

    public JSONArray readItems() {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME, null, null, null, null, null,null);
        cursor.moveToFirst();
        JSONArray movieList = new JSONArray();
        while(!cursor.isAfterLast()) {
            try {
                JSONObject movie = new JSONObject();
                movie.put(DetailFragment.ARG_PARAM_ID, cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
                movie.put(DetailFragment.ARG_PARAM_TITLE, cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                movie.put(DetailFragment.ARG_PARAM_RATING, cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING)));
                movie.put(DetailFragment.ARG_PARAM_RELEASE, cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                movie.put(DetailFragment.ARG_PARAM_SYNOPSIS, cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS)));
                movie.put(DetailFragment.ARG_PARAM_POSTER_URL, cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER)));
                movieList.put(movie);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
        return movieList;
    }
}
