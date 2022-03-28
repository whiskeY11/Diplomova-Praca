package com.example.geoapp.map;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.HashMap;
import java.util.List;

public class Cities extends MapEntity {

    private HashMap<Integer, Feature> citiesHashMap = new HashMap<>();

    @Override
    public void setupSourceAndLayer(HashMap<MapHelper.mapLayers, String> layerMap, Style style) { }

    @Override
    public void newFeatureCollection(String json) {
        super.newFeatureCollection(json);

        citiesHashMap.clear();
        List<Feature> features = featureCollection().features();
        assert features != null;
        for(Feature f : features) {
            citiesHashMap.put(
                    f.getNumberProperty("id").intValue(),
                    f
            );
        }
    }

    public Feature citySuggestion(int cityID) {
        if(!citiesHashMap.isEmpty() && citiesHashMap.containsKey(cityID)) {
            return citiesHashMap.get(cityID);
        }
        return null;
    }
}
