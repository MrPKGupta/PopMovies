package addtotech.com.popmovies;

/**
 * Created by MrGupta on 07-Oct-16.
 */

public class MovieItem {
    private String original_title;
    private String overview;
    private String vote_average;
    private String release_date;
    private String poster_path;
    private String id;

    public String getOriginal_title() {
        return original_title;
    }

    public String getOverview() {
        return overview;
    }

    public String getVote_average() {
        return vote_average;
    }

    public String getPoster() {
        return poster_path;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getId() {
        return id;
    }
}
