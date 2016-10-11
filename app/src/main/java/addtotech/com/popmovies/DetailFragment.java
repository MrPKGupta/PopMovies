package addtotech.com.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailFragment extends Fragment implements View.OnClickListener {

    static final String ARG_PARAM_TITLE = "original_title";
    static final String ARG_PARAM_SYNOPSIS = "overview";
    static final String ARG_PARAM_RATING = "vote_average";
    static final String ARG_PARAM_RELEASE = "release_date";
    static final String ARG_PARAM_POSTER_URL = "poster_path";
    static final String ARG_PARAM_ID = "id";

    private TextView tvTitle;
    private TextView tvSynopsis;
    private TextView tvRating;
    private TextView tvRelease;
    private ImageView poster;
    private CheckBox checkFavorite;
    private LinearLayout reviewLayout;
    private LinearLayout trailerLayout;

    private boolean mIsDualPane;
    private String movieId;

    private FetchReviewTask fetchReviewTask = null;
    private FetchTrailerTask fetchTrailerTask = null;

    //store pixel values of the padding to be set for views programmatically
    private int leftPadding;
    private int rightPadding;

    private MovieDataSource movieDataSource;
    private boolean isFavorite;
    private JSONObject movieItem;
    private Bitmap currentBitmap;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsDualPane = getResources().getBoolean(R.bool.has_two_panes);

        MovieDbHelper movieDbHelper = MovieDbHelper.getInstance(getActivity());
        movieDataSource = new MovieDataSource(movieDbHelper);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvSynopsis = (TextView) view.findViewById(R.id.tvSynopsis);
        tvRating = (TextView) view.findViewById(R.id.tvUserRating);
        tvRelease = (TextView) view.findViewById(R.id.tvReleaseDate);
        poster = (ImageView) view.findViewById(R.id.imageViewThumbNail);
        checkFavorite = (CheckBox) view.findViewById(R.id.checkFavorite);
        reviewLayout = (LinearLayout) view.findViewById(R.id.reviewLayout);
        trailerLayout = (LinearLayout) view.findViewById(R.id.trailerLayout);

        checkFavorite.setOnClickListener(this);

        if (!mIsDualPane && getActivity().getIntent() != null && getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT) != null) {
            displayMovie(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkFavorite:
                try {
                    File file = new File(getContext().getFilesDir(), movieItem.getString(ARG_PARAM_POSTER_URL));
                    if (isFavorite) {
                        movieDataSource.deleteItem(movieId);
                        file.delete();
                        isFavorite = false;
                        checkFavorite.setChecked(false);
                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.favorite_remove, Snackbar.LENGTH_SHORT)
                                .show();
                    } else if (movieItem != null) {
                        try {
                            FileOutputStream oStream = new FileOutputStream(file);
                            currentBitmap.compress(Bitmap.CompressFormat.JPEG, 75, oStream);
                            oStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ContentValues values = new ContentValues();
                        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieItem.getString(ARG_PARAM_ID));
                        values.put(MovieContract.MovieEntry.COLUMN_TITLE, movieItem.getString(ARG_PARAM_TITLE));
                        values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movieItem.getString(ARG_PARAM_SYNOPSIS));
                        values.put(MovieContract.MovieEntry.COLUMN_RATING, movieItem.getString(ARG_PARAM_RATING));
                        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movieItem.getString(ARG_PARAM_RELEASE));
                        values.put(MovieContract.MovieEntry.COLUMN_POSTER, movieItem.getString(ARG_PARAM_POSTER_URL));

                        movieDataSource.insertItem(values);
                        isFavorite = true;
                        checkFavorite.setChecked(true);
                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.favorite_add, Snackbar.LENGTH_SHORT)
                                .show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    public void displayMovie(String movie) {
        if (!movie.equals("")) {
            try {
                movieItem = new JSONObject(movie);
                tvTitle.setText(movieItem.getString(ARG_PARAM_TITLE));
                tvSynopsis.setText(movieItem.getString(ARG_PARAM_SYNOPSIS));
                tvRating.setText(movieItem.getString(ARG_PARAM_RATING));
                tvRelease.setText(movieItem.getString(ARG_PARAM_RELEASE));
                String posterUrl = movieItem.getString(ARG_PARAM_POSTER_URL);
                movieId = movieItem.getString(ARG_PARAM_ID);

                //Show movie as favorite if it's stored in the database
                if (movieDataSource.checkItem(movieId)) {
                    isFavorite = true;
                    checkFavorite.setChecked(true);
                } else {
                    isFavorite = false;
                    checkFavorite.setChecked(false);
                }

                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        poster.setImageBitmap(bitmap);
                        currentBitmap = bitmap;
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        return;
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        return;
                    }
                };

                if (posterUrl != null && !posterUrl.equals("")) {
                    if (isFavorite) {
                        String path = "file:" + getContext().getFilesDir().getAbsolutePath() + posterUrl;
                        Picasso.with(getContext())
                                .load(path)
                                .placeholder(R.drawable.placeholder)
                                .resize(Utils.convertToPx(getActivity(), 120), 0)
                                .into(target);
                    } else {
                        Picasso.with(getContext())
                                .load(getString(R.string.poster_base_url) + posterUrl)
                                .placeholder(R.drawable.placeholder)
                                .resize(Utils.convertToPx(getActivity(), 120), 0)
                                .into(target);
                    }
                }
                fetchReviewTask = new FetchReviewTask();
                fetchTrailerTask = new FetchTrailerTask();

                //Parallel execution of async task from SDK 11
                if (Build.VERSION.SDK_INT >= 11) {
                    fetchReviewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, movieId);
                    fetchTrailerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, movieId);
                } else {
                    fetchReviewTask.execute(movieId);
                    fetchTrailerTask.execute(movieId);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        if (fetchReviewTask != null && fetchReviewTask.getStatus() == AsyncTask.Status.RUNNING) {
            fetchReviewTask.cancel(true);
        }
        if (fetchTrailerTask != null && fetchTrailerTask.getStatus() == AsyncTask.Status.RUNNING) {
            fetchTrailerTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private class FetchReviewTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!Utils.isNetworkAvailable(getContext())) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.no_network, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new FetchReviewTask().execute(movieId);
                            }
                        })
                        .show();
                cancel(true);
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;
            String api_key = getString(R.string.api_key);
            String resultString;

            try {
                final String MOVIE_BASE_URL = getString(R.string.movie_base_url);
                final String ADD_URL = strings[0];
                final String QUERY_URL = getString(R.string.review_url_query);
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL + ADD_URL + QUERY_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                if (stringBuilder.length() == 0) {
                    return null;
                }
                resultString = stringBuilder.toString();
                Log.v("ListFragment", resultString);

            } catch (IOException e) {
                Log.e("ListFragment", "Error", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ListFragment", "Error closing stream", e);
                    }
                }
            }
            return resultString;
        }


        @Override
        protected void onPostExecute(String string) {
            int count = 1;
            //Getting the list of image urls to be passed to the list adapter
            if (string == null || string.isEmpty())
                return;
            try {
                JSONObject jsonObject = new JSONObject(string);
                JSONArray results = jsonObject.getJSONArray("results");
                count = results.length();

                //Remove the review added from the last set of data
                reviewLayout.removeAllViews();

                //Adding row separator
                View separatorView = new View(getContext());
                separatorView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        Utils.convertToPx(getContext(), 1));
                separatorView.setLayoutParams(layoutParams);
                reviewLayout.addView(separatorView);

                //Adding review heading
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView reviewHeadingTextView = new TextView(getContext());
                reviewHeadingTextView.setLayoutParams(layoutParams1);
                reviewHeadingTextView.setText(R.string.review_heading);
                reviewLayout.addView(reviewHeadingTextView);

                //Adding review list
                for (int i = 0; i < count; i++) {
                    JSONObject object = results.getJSONObject(i);
                    String authorName = object.getString("author");
                    String reviewText = object.getString("content");

                    View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, null);
                    TextView authorTextView = (TextView) view.findViewById(R.id.tvAuthor);
                    TextView reviewTextView = (TextView) view.findViewById(R.id.tvReview);
                    authorTextView.setText(authorName);
                    reviewTextView.setText(reviewText);
                    reviewLayout.addView(view);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchTrailerTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!Utils.isNetworkAvailable(getContext())) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.no_network, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new FetchTrailerTask().execute(movieId);
                            }
                        })
                        .show();
                cancel(true);
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader reader = null;
            String api_key = getString(R.string.api_key);
            String resultString;

            try {
                final String MOVIE_BASE_URL = getString(R.string.movie_base_url);
                final String ADD_URL = strings[0];
                final String QUERY_URL = getString(R.string.trailer_url_query);
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL + ADD_URL + QUERY_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                if (stringBuilder.length() == 0) {
                    return null;
                }
                resultString = stringBuilder.toString();
                Log.v("ListFragment", resultString);

            } catch (IOException e) {
                Log.e("ListFragment", "Error", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ListFragment", "Error closing stream", e);
                    }
                }
            }
            return resultString;
        }


        @Override
        protected void onPostExecute(String string) {
            int count = 1;
            //Getting the list of image urls to be passed to the list adapter
            if (string == null || string.isEmpty())
                return;

            try {
                JSONObject jsonObject = new JSONObject(string);
                JSONArray results = jsonObject.getJSONArray("results");
                count = results.length();

                //Remove the trailer added from the last set of data
                trailerLayout.removeAllViews();

                //Adding row separator
                View separatorView = new View(getContext());
                separatorView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        Utils.convertToPx(getContext(), 1));
                separatorView.setLayoutParams(layoutParams);
                trailerLayout.addView(separatorView);

                //Adding trailer heading
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView trailerTextView = new TextView(getContext());
                trailerTextView.setLayoutParams(layoutParams1);
                trailerTextView.setText(R.string.trailer_heading);
                trailerLayout.addView(trailerTextView);

                //Adding trailers list
                for (int i = 1; i <= count; i++) {
                    JSONObject object = results.getJSONObject(i - 1);
                    final String trailerUrlKey = object.getString("key");
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, null);
                    ImageView trailerImageView = (ImageView) view.findViewById(R.id.imageViewThumbNail);
                    TextView titleTextView = (TextView) view.findViewById(R.id.tvTrailerTitle);
                    ImageButton shareButton = (ImageButton) view.findViewById(R.id.shareBtn);

                    titleTextView.setText("Trailer " + i);

                    String url = getContext().getString(R.string.trailer_thumbnail_base_url) + trailerUrlKey +
                            getContext().getString(R.string.trailer_thumbnail_query);

                    Picasso.with(getContext()).load(url)
                            //.resize(screenWidthPixels, 0)
                            //.placeholder(R.drawable.placeholder)
                            .into(trailerImageView);

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri = Uri.parse(getString(R.string.youtube_base_url) + trailerUrlKey);
                            switch (view.getId()) {
                                case R.id.imageViewThumbNail:
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setData(uri);
                                    startActivity(intent);
                                    break;
                                case R.id.shareBtn:
                                    Intent shareIntent = new Intent();
                                    shareIntent.setAction(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
                                    shareIntent.setType("text/plain");
                                    startActivity(Intent.createChooser(shareIntent, getText(R.string.share_chooser_title)));
                                    break;
                            }
                        }
                    };

                    trailerImageView.setOnClickListener(onClickListener);
                    shareButton.setOnClickListener(onClickListener);

                    trailerLayout.addView(view);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
