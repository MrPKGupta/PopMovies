package addtotech.com.popmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MrGupta on 18-Jul-16.
 */
public class ReviewListAdapter extends ArrayAdapter<ArrayList<String>> {
    public ReviewListAdapter(Context context, ArrayList<ArrayList<String>> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }

        TextView authorTextView = (TextView) convertView.findViewById(R.id.tvAuthor);
        TextView reviewTextView = (TextView) convertView.findViewById(R.id.tvReview);
        ArrayList<String> item = getItem(position);

        authorTextView.setText(item.get(0));
        reviewTextView.setText(item.get(1));

        return convertView;
    }
}
