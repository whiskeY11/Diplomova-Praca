package com.example.geoapp.search;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.CursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.geoapp.SettingsActivity;
import com.example.geoapp.map.Cities;
import com.example.geoapp.map.MapHelper;
import com.example.geoapp.misc.Helper;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.Objects;

public class SearchSuggestions extends ContentProvider {

    private static final String ENTITIES = "entities/"+SearchManager.SUGGEST_URI_PATH_QUERY+"/*";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI("com.example.geoapp.search", ENTITIES, 1);
    }

    private static String[] matrixCursorColumns = {"_id",
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA };

    private ArrayList<Feature> featureArrayList = new ArrayList<>();
    private static CursorAdapter searchCursorAdapter = null;

    public static void SetCursorAdapter(CursorAdapter adapter) { searchCursorAdapter = adapter; }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        switch(uriMatcher.match(uri)){
            case 1 :
                String query = uri.getLastPathSegment().toLowerCase();
                MapHelper.getInstance().execFilterData(query, mainHandler);
                return getSearchResultsCursor(featureArrayList);
            default:
                return null;
        }
    }

    private MatrixCursor getSearchResultsCursor(ArrayList<Feature> features){
        MatrixCursor searchResults =  new MatrixCursor(matrixCursorColumns);
        Object[] mRow = new Object[matrixCursorColumns.length];
        int counterId = 0;
        if(features != null && !features.isEmpty()){
            for(Feature f : features){
                boolean isCity = f.hasProperty("city");

                mRow[0] = ""+counterId++;
                mRow[1] = f.getStringProperty("name");

                StringBuilder sb = new StringBuilder();
                switch(Objects.requireNonNull(f.geometry()).type()) {
                    case "Point":
                        if(isCity) {
                            sb.append("Mesto");
                        } else {
                            sb.append("Bod");
                        }
                        break;
                    case "LineString":
                        sb.append("Ulica");
                        break;
                    case "Polygon":
                        sb.append("Oblasť");
                        break;
                }

                if(!isCity) {
                    Feature citySuggestion =
                            ((Cities) MapHelper.getInstance().getCities())
                                    .citySuggestion(f.getNumberProperty("cityid").intValue());
                    if (citySuggestion != null) {
                        sb.append(" v meste ");
                        sb.append(citySuggestion.getStringProperty("name"));
                    }
                }

                mRow[2] = sb.toString();
                mRow[3] = f.toJson();

                searchResults.addRow(mRow);
            }
        }
        return searchResults;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    Handler mainHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //Data filtering was finished
                    featureArrayList = (ArrayList<Feature>) msg.obj;
                    searchCursorAdapter.changeCursor(getSearchResultsCursor(featureArrayList));
                    searchCursorAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };
}
