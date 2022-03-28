package com.example.geoapp.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.geoapp.map.Cities;
import com.example.geoapp.map.MapHelper;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.Objects;

public class CustomSearchAdapter extends ArrayAdapter {

    private ArrayList<Feature> dataList;
    private int searchResultItemLayout;

    public CustomSearchAdapter(Context context, int resource, ArrayList<Feature> features) {
        super(context, resource, features);
        dataList = features;
        searchResultItemLayout = resource;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Feature getItem(int position) {
        return dataList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(searchResultItemLayout, parent, false);
        }

        Feature feature = dataList.get(position);

        view.setTag(feature);
        TextView resultItem = (TextView) view.findViewById(android.R.id.text1);
        resultItem.setText(feature.getStringProperty("name"));

        boolean isCity = feature.hasProperty("city");

        StringBuilder sb = new StringBuilder();
        switch(Objects.requireNonNull(feature.geometry()).type()) {
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
            Feature citySuggestion = ((Cities) MapHelper.getInstance().getCities())
                                    .citySuggestion(feature.getNumberProperty("cityid").intValue());
            if (citySuggestion != null) {
                sb.append(" v meste ");
                sb.append(citySuggestion.getStringProperty("name"));
            }
        }

        TextView description = (TextView) view.findViewById(android.R.id.text2);
        description.setText(sb.toString());
        return view;
    }
}
