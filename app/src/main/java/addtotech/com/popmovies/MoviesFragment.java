package addtotech.com.popmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Fragment displaying list of movies
 */
public class MoviesFragment extends Fragment {
    private static final String LOG_TAG = MoviesFragment.class.getSimpleName();
    private fetchImageTask fetchTask = null;
    private JSONArray results;
    private ArrayList<String> imageUrlList = new ArrayList<>();
    ListAdapter listAdapter;
    private GridView gridView;

    OnMovieSelectedListener onMovieSelectedListener = null;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new ListAdapter(getActivity(), imageUrlList);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView)view.findViewById(R.id.gridMovie);

        gridView.setAdapter(listAdapter);

        //Load the movies by popularity or rating
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortQuery = sharedPreferences.getString(MainActivity.SORT_QUERY, getString(R.string.popular_url_query));
        fetchTask = new fetchImageTask();
        fetchTask.execute(sortQuery);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(onMovieSelectedListener != null) {
                    try {
                        JSONObject movie = results.getJSONObject(i);
                        onMovieSelectedListener.onMovieSelected(movie.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        if(fetchTask != null && fetchTask.getStatus() == AsyncTask.Status.RUNNING) {
            fetchTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMovieSelectedListener) {
            onMovieSelectedListener = (OnMovieSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMovieSelectedListener");
        }
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(R.string.app_name);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onMovieSelectedListener = null;
    }

    public void setOnMovieSelectedListener(OnMovieSelectedListener listener) {
        onMovieSelectedListener = listener;
    }

    private class fetchImageTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL + ADD_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                StringBuilder stringBuilder = new StringBuilder();
                if(inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                if(stringBuilder.length() == 0) {
                    return null;
                }
                resultString = stringBuilder.toString();
                Log.v("ListFragment", resultString);

            }catch(IOException e) {
                Log.e("ListFragment", "Error", e);
                return null;
            } finally {
                if(httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if(reader != null) {
                    try{
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
            //Getting the list of image urls to be passed to the list adapter
            imageUrlList.clear();
            try {
                JSONObject jsonObject = new JSONObject(string);
                results = jsonObject.getJSONArray("results");
                for(int i=0; i<results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);
                    String posterPath = object.getString("poster_path");
                    imageUrlList.add(posterPath);
                }

                //Setting list adapter to gridView
                listAdapter.notifyDataSetChanged();

                if(getResources().getBoolean(R.bool.has_two_panes) && !imageUrlList.isEmpty()) {
                    gridView.performItemClick(listAdapter.getView(0, null, null), 0, listAdapter.getItemId(0));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list_fragment, menu);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortQuery = sharedPreferences.getString(MainActivity.SORT_QUERY, getString(R.string.popular_url_query));
        MenuItem menuPopular = menu.findItem(R.id.sortByPopularity);
        MenuItem menuRating = menu.findItem(R.id.sortByRating);
        if(sortQuery.equals(getString(R.string.popular_url_query))) {
            menuRating.setChecked(false);
            menuPopular.setChecked(true);
        } else {
            menuPopular.setChecked(false);
            menuRating.setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String sortQuery;
        if(id == R.id.sortByPopularity) {
            sortQuery = getString(R.string.popular_url_query);
            new fetchImageTask().execute(sortQuery);
            editor.putString(MainActivity.SORT_QUERY, sortQuery);
            editor.apply();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        }
        if(id == R.id.sortByRating) {
            sortQuery = getString(R.string.rating_url_query);
            new fetchImageTask().execute(sortQuery);
            editor.putString(MainActivity.SORT_QUERY, sortQuery);
            editor.apply();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(String movie);
    }
}
