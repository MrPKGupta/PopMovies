package addtotech.com.popmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by MrGupta on 21-Jun-16.
 */
public class DetailActivity extends AppCompatActivity {

    int mSelectIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DetailActivity", "extra is " +getIntent().getStringExtra(Intent.EXTRA_TEXT));
        if(getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
            return;
        }

        DetailFragment detailFragment = new DetailFragment();
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, detailFragment).commit();
    }
}
