package addtotech.com.popmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment {

    private static final String ARG_PARAM_TITLE = "original_title";
    private static final String ARG_PARAM_SYNOPSIS = "overview";
    private static final String ARG_PARAM_RATING = "vote_average";
    private static final String ARG_PARAM_RELEASE = "release_date";
    private static final String ARG_PARAM_POSTER_URL = "poster_path";

    private TextView tvTitle;
    private TextView tvSynopsis;
    private TextView tvRating;
    private TextView tvRelease;
    private ImageView poster;

    private boolean mIsDualPane;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsDualPane = getResources().getBoolean(R.bool.has_two_panes);
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

        if(!mIsDualPane && getActivity().getIntent() != null && getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT) != null) {
            displayMovie(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
        }
        return view;
    }

    public void displayMovie(String movie) {
        if(!movie.equals("")) {
            try {
                JSONObject movieItem = new JSONObject(movie);
                tvTitle.setText(movieItem.getString(ARG_PARAM_TITLE));
                tvSynopsis.setText(movieItem.getString(ARG_PARAM_SYNOPSIS));
                tvRating.setText(movieItem.getString(ARG_PARAM_RATING));
                tvRelease.setText(movieItem.getString(ARG_PARAM_RELEASE));
                String posterUrl = movieItem.getString(ARG_PARAM_POSTER_URL);

                if(posterUrl != null && !posterUrl.equals("")) {
                    Picasso.with(getContext()).load(getString(R.string.poster_base_url) + posterUrl)
                            .resize(Utils.convertToPx(getActivity(),120), 0)
                            .into(poster);
                }
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
