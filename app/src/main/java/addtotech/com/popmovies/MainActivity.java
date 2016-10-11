package addtotech.com.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

/**
 * This is the main activity of the application.
 * For tablets layout will be dual pane. For phones layout will be single pane.
 * If dual pane is displayed, Details of first movie in the list will be shown by default in detail view
 */
public class MainActivity extends AppCompatActivity implements MoviesFragment.OnMovieSelectedListener {

    public static final String SORT_QUERY = "sort_query";
    private static final String SELECTION_INDEX = "selection_index";

    public static int mSelectIndex = 0;

    //Whether or not we are in dual-pane mode
    boolean mIsDualPane;

    //Fragment containing list of movies
    MoviesFragment moviesFragment;

    //Fragment containing detail of movie
    DetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find out fragments
        moviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movies);
        detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.movie_detail);
        MoviesFragment moviesFragment = new MoviesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        View detailView = findViewById(R.id.movie_detail);
        mIsDualPane = detailView != null && detailView.getVisibility() == View.VISIBLE;

        //Register for the MoviesList fragment events
        moviesFragment.setOnMovieSelectedListener(this);

        restoreSelection(savedInstanceState);
    }

    void restoreSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (mIsDualPane) {
                mSelectIndex = savedInstanceState.getInt(SELECTION_INDEX, 0);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        restoreSelection(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTION_INDEX, mSelectIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMovieSelected(String movie, int position) {
        mSelectIndex = position;
        if(mIsDualPane) {
            detailFragment.displayMovie(movie);
        } else {
            Intent intent = new Intent(MainActivity.this, addtotech.com.popmovies.DetailActivity.class);

            intent.putExtra(Intent.EXTRA_TEXT, movie);
            Log.d("MainActivity", "intent movie" + movie);
            startActivity(intent);
        }
    }
}
