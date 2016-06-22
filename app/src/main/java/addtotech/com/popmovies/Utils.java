package addtotech.com.popmovies;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by MrGupta on 21-Jun-16.
 */
public class Utils {

    public static int convertToPx(Context context, int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
}


