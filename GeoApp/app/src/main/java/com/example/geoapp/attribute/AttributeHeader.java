package com.example.geoapp.attribute;

public class AttributeHeader {
    private int _id;
    private String _name;
    private int _idLegend;
    private int _idCity;

    public AttributeHeader(int id, String name, int idLegend, int idCity) {
        _id = id;
        _name = name;
        _idLegend = idLegend;
        _idCity = idCity;
    }

    public int ID() { return _id; }

    public String Name() { return _name; }

    public int LegendID() { return _idLegend; }

    public int CityID() { return _idCity; }
}
