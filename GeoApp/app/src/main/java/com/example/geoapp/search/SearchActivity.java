package com.example.geoapp.search;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.geoapp.SettingsActivity;
import com.example.geoapp.map.MapHelper;
import com.example.geoapp.R;
import com.example.geoapp.misc.Helper;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            finish();
            Feature feature = (Feature) view.getTag();
            MapHelper.getInstance().deselectFeatures();
            MapHelper.getInstance().selectFeature(feature);
        });

        handleSearch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSearch();
    }

    private void handleSearch() {
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            MapHelper.getInstance().execFilterData(searchQuery, mainHandler);
            findViewById(R.id.search_progress).setVisibility(View.VISIBLE);
        } else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
            finish();
            Feature feature = Feature.fromJson(Objects.requireNonNull(intent.getDataString()));
            MapHelper.getInstance().deselectFeatures();
            MapHelper.getInstance().selectFeature(feature);
        }
    }

    Handler mainHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //Data filtering was finished
                    CustomSearchAdapter adapter = new CustomSearchAdapter(SearchActivity.this,
                            android.R.layout.simple_list_item_2,
                            (ArrayList<Feature>) msg.obj);
                    listView.setAdapter(adapter);
                    findViewById(R.id.search_progress).setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
}