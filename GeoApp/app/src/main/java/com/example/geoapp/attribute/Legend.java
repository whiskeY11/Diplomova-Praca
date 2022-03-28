package com.example.geoapp.attribute;

import java.util.ArrayList;

public class Legend {
    private ArrayList<String> _colors;
    private int _id;

    public Legend(int id, ArrayList<String> colors) {
        _colors = colors;
        _id = id;
    }

    public int ID() { return _id; }

    public ArrayList<String> Colors() { return _colors; }
}
