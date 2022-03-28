package com.example.geoapp.attribute;

import com.example.geoapp.misc.Helper;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AttributeValues {
    private AttributeHeader _header;
    private HashMap<Integer, Integer> _values;
    private Legend _legend;
    private int _maxValue;
    private Feature _city;

    public AttributeValues(AttributeHeader header, HashMap<Integer, Integer> values, Legend legend) {
        _header = header;
        _values = values;
        _legend = legend;

        int max = 0;
        for (Map.Entry<Integer, Integer> entry : _values.entrySet()) {
            if (entry.getValue() > max) max = entry.getValue();
        }
        _maxValue = max;
    }

    public void SetCity(Feature city) { _city = city; }

    public Feature City() { return _city; }

    public int MaxValue() { return _maxValue; }

    public AttributeHeader Header() { return _header; }

    public Legend Legend() { return _legend; }

    public HashMap<Integer, Integer> Values() { return _values; }

    public String ColorFromValue(int value) {
        ArrayList<String> colors = _legend.Colors();
        int colorIndex = (int) Helper.Companion.getInstance().lerp(0, colors.size()-1, (double) value / _maxValue);
        return colors.get(colorIndex);
    }
}
