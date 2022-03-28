package com.example.geoapp.map;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public abstract class MapEntity {
    private GeoJsonSource geoJsonSource;
    private FeatureCollection featureCollection;

    public abstract void setupSourceAndLayer(HashMap<MapHelper.mapLayers, String> layerMap, Style style);

    public void refreshSource() {
        if(geoJsonSource != null) {
            geoJsonSource.setGeoJson(featureCollection);
        }
    }

    public void newFeatureCollection(String json) {
        featureCollection = FeatureCollection.fromJson(json);
    }

    public void newFeatureCollection(ArrayList<Feature> features) {
        featureCollection = FeatureCollection.fromFeatures(features);
    }

    public FeatureCollection featureCollection() {
        return featureCollection;
    }

    public HashMap<Integer, Feature> featureHashMap() {
        HashMap<Integer, Feature> featureHashMap = new HashMap<>();
        if(featureCollection != null) {
            assert featureCollection.features() != null;
            if (!featureCollection.features().isEmpty()) {
                for (Feature f : featureCollection.features()) {
                    featureHashMap.put(f.getNumberProperty("id").intValue(), f);
                }
            }
        }
        return featureHashMap;
    }

    public void newGeoJsonSource(String id) {
        geoJsonSource = new GeoJsonSource(id);
    }

    public GeoJsonSource geoJsonSource() {
        return geoJsonSource;
    }
}
