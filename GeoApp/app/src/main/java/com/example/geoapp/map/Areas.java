package com.example.geoapp.map;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import android.graphics.Color;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import java.util.HashMap;

public class Areas extends MapEntity {

    private final String source_id = "area-source";

    @Override
    public void setupSourceAndLayer(HashMap<MapHelper.mapLayers, String> layerMap, Style style) {
        newGeoJsonSource(source_id);
        if (featureCollection() != null) {
            geoJsonSource().setGeoJson(featureCollection());
        }

        style.addSource(geoJsonSource());

        FillLayer layer = new FillLayer(layerMap.get(MapHelper.mapLayers.area), source_id);
        style.addLayerBelow(layer,"water");


        layer.withProperties(
                fillColor(match(Expression.toString(get("selected")),
                        Expression.color(Color.parseColor("#3bb2d0")),
                        stop("true", Expression.color(Color.RED)))),
                fillOpacity(0.5f));


        LineLayer outlineLayer = new LineLayer(layerMap.get(MapHelper.mapLayers.areaOutline), source_id);
        style.addLayerBelow(outlineLayer,"road_major_label");

        outlineLayer.withProperties(
                lineColor(Color.BLACK),
                lineWidth(2f));
    }
}
