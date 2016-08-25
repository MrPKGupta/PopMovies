package addtotech.com.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by MrGupta on 17-Jul-16.
 */
public class TrailerListAdapter extends ArrayAdapter<String> {

    public TrailerListAdapter(Context context, List<String> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int screenWidthPixels;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent, false);
        }

        String url = getContext().getString(R.string.trailer_thumbnail_base_url) + getItem(position) +
                getContext().getString(R.string.trailer_thumbnail_query);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewThumbNail);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.tvTrailerTitle);
        titleTextView.setText("Trailer "+ position+1);
        /*DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        //int minSize = Utils.convertToPx(getContext(), (int)getContext().getResources().getDimension(R.dimen.imageview_min_size));
        if(getContext().getResources().getBoolean(R.bool.has_two_panes)) {
            //screenWidthPixels = (int) (displayMetrics.widthPixels * ((float)4/7));
            screenWidthPixels = displayMetrics.widthPixels /4;
        } else {
            screenWidthPixels = displayMetrics.widthPixels /2;
        }*/
        Picasso.with(getContext()).load(url)
                //.resize(screenWidthPixels, 0)
                .into(imageView);
        return convertView;
    }
}
