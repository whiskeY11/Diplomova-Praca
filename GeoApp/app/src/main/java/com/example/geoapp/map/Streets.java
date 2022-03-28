package com.example.geoapp.map;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.neq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;

import android.graphics.Color;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import java.util.HashMap;
import java.util.Objects;

public class Streets extends MapEntity {

    private final String source_id = "street-source";

    @Override
    public void setupSourceAndLayer(HashMap<MapHelper.mapLayers, String> layerMap, Style style) {
        newGeoJsonSource(source_id);
        if (featureCollection() != null) {
            geoJsonSource().setGeoJson(featureCollection());
        }

        style.addSource(geoJsonSource());
        LineLayer layer = new LineLayer(layerMap.get(MapHelper.mapLayers.street), source_id);
        layer.withProperties(PropertyFactory.lineColor(
                match(Expression.toString(get("selected")),
                        Expression.color(Color.BLACK),
                        stop("true", Expression.color(Color.RED))
                )
        ));
        layer.withFilter(eq(get("value"), literal(0)));

        LineLayer layerAttributes = new LineLayer(layerMap.get(MapHelper.mapLayers.streetAttributes), source_id);
        layerAttributes.withProperties(PropertyFactory.lineColor(
                Expression.toColor(get("value"))), PropertyFactory.lineGapWidth(2f));
        layerAttributes.withFilter(neq(get("value"), literal(0)));

        style.addLayerBelow(layer,"road_major_label");
        style.addLayerAbove(layerAttributes, Objects.requireNonNull(layerMap.get(MapHelper.mapLayers.street)));
    }
}
