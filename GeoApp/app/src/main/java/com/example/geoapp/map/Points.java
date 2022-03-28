package com.example.geoapp.map;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import java.util.HashMap;

public class Points extends MapEntity {

    private final String source_id = "point-source";

    @Override
    public void setupSourceAndLayer(HashMap<MapHelper.mapLayers, String> layerMap, Style style) {
        newGeoJsonSource(source_id);
        if (featureCollection() != null) {
            geoJsonSource().setGeoJson(featureCollection());
        }

        style.addSource(geoJsonSource());
        SymbolLayer layer = new SymbolLayer(layerMap.get(MapHelper.mapLayers.point), source_id);
        style.addLayer(layer);

        layer.setMinZoom(10f);

        layer.withProperties(iconImage(Expression.get("icon")), iconAllowOverlap(false), PropertyFactory.iconSize(
                match(Expression.toString(get("selected")), literal(1.0f),
                        stop("true", 1.3f))));

        SymbolLayer infoBoxLayer = new SymbolLayer(layerMap.get(MapHelper.mapLayers.pointInfo), source_id);

        style.addLayer(infoBoxLayer);

        infoBoxLayer.withProperties(
                iconIgnorePlacement(true),
                iconImage(Expression.get("id")),
                iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT),
                iconOffset(new Float[] {-20.0f, -30.0f}));
        infoBoxLayer.withFilter(eq(get("selected"), literal(true)));
    }
}
