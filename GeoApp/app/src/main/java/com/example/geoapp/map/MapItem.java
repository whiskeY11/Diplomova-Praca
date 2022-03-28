package com.example.geoapp.map;

import com.mapbox.geojson.Feature;

import java.util.Objects;

public class MapItem {
    private Feature feature;

    public MapItem(Feature feature) {
        this.feature = feature;
    }

    public Feature Feature() { return feature; }

    public int ID() { return feature.getNumberProperty("id").intValue(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapItem mapItem = (MapItem) o;
        return this.ID() == mapItem.ID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature);
    }
}
