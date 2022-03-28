package com.example.geoapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import com.example.geoapp.attribute.AttributeHeader;
import com.example.geoapp.attribute.AttributeValues;
import com.example.geoapp.attribute.Legend;
import com.example.geoapp.map.MapHelper;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocalDatabase extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "geoapp-local.db";

    private SQLiteDatabase dbWrite = null, dbRead = null;

    public LocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbWrite = getWritableDatabase();
        dbRead = getReadableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LocalDatabaseModels.Areas.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Streets.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Points.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Bounds.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Icons.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Cities.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Attributes.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.AttributeHeaders.SQL_CREATE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Legends.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(LocalDatabaseModels.Areas.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Streets.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Points.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Bounds.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Icons.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Cities.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Attributes.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.AttributeHeaders.SQL_DELETE_ENTRIES);
        db.execSQL(LocalDatabaseModels.Legends.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void insertPoints(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Points.FeedEntry.JSON, json);

        long newRowId = dbWrite.insert(LocalDatabaseModels.Points.FeedEntry.TABLE_NAME, null, values);
    }

    public void insertStreets(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Streets.FeedEntry.JSON, json);

        long newRowId = dbWrite.insert(LocalDatabaseModels.Streets.FeedEntry.TABLE_NAME, null, values);
    }

    public void insertAreas(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Areas.FeedEntry.JSON, json);

        long newRowId = dbWrite.insert(LocalDatabaseModels.Areas.FeedEntry.TABLE_NAME, null, values);
    }

    public void insertBounds(JSONArray bounds) {
        dbWrite.execSQL("delete from "+ LocalDatabaseModels.Bounds.FeedEntry.TABLE_NAME);

        if(bounds != null) {
            for (int i = 0; i < bounds.length(); i++) {
                try {
                    JSONObject jsonObject = bounds.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put(LocalDatabaseModels.Bounds.FeedEntry.ID, jsonObject.getDouble(LocalDatabaseModels.Bounds.FeedEntry.ID));
                    values.put(LocalDatabaseModels.Bounds.FeedEntry.LAT_NORTH, jsonObject.getDouble(LocalDatabaseModels.Bounds.FeedEntry.LAT_NORTH));
                    values.put(LocalDatabaseModels.Bounds.FeedEntry.LAT_SOUTH, jsonObject.getDouble(LocalDatabaseModels.Bounds.FeedEntry.LAT_SOUTH));
                    values.put(LocalDatabaseModels.Bounds.FeedEntry.LNG_WEST, jsonObject.getDouble(LocalDatabaseModels.Bounds.FeedEntry.LNG_WEST));
                    values.put(LocalDatabaseModels.Bounds.FeedEntry.LNG_EAST, jsonObject.getDouble(LocalDatabaseModels.Bounds.FeedEntry.LNG_EAST));
                    values.put(LocalDatabaseModels.Bounds.FeedEntry.LAST_SYNC, jsonObject.getLong(LocalDatabaseModels.Bounds.FeedEntry.LAST_SYNC));

                    dbWrite.insert(LocalDatabaseModels.Bounds.FeedEntry.TABLE_NAME, null, values);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void insertIcons(HashMap<String, String> icons) {
        dbWrite.execSQL("delete from "+ LocalDatabaseModels.Icons.FeedEntry.TABLE_NAME);

        if(!icons.isEmpty()) {
            for(Map.Entry<String, String> entry : icons.entrySet()) {
                ContentValues values = new ContentValues();
                values.put(LocalDatabaseModels.Icons.FeedEntry.NAME, entry.getKey());
                values.put(LocalDatabaseModels.Icons.FeedEntry.PATH, entry.getValue());

                dbWrite.insert(LocalDatabaseModels.Icons.FeedEntry.TABLE_NAME, null, values);
            }
        }
    }

    public void insertCities(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Cities.FeedEntry.JSON, json);

        dbWrite.insert(LocalDatabaseModels.Cities.FeedEntry.TABLE_NAME, null, values);
    }

    public void insertAttributes(JSONArray headers, JSONArray attributes) {
        dbWrite.execSQL("delete from "+ LocalDatabaseModels.AttributeHeaders.FeedEntry.TABLE_NAME);
        dbWrite.execSQL("delete from "+ LocalDatabaseModels.Attributes.FeedEntry.TABLE_NAME);

        if(headers != null) {
            for (int i = 0; i < headers.length(); i++) {
                try {
                    JSONObject jsonObject = headers.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put(
                            LocalDatabaseModels.AttributeHeaders.FeedEntry.ID,
                            jsonObject.getDouble(LocalDatabaseModels.AttributeHeaders.FeedEntry.ID)
                    );
                    values.put(
                            LocalDatabaseModels.AttributeHeaders.FeedEntry.LEGEND_ID,
                            jsonObject.getDouble(LocalDatabaseModels.AttributeHeaders.FeedEntry.LEGEND_ID)
                    );
                    values.put(
                            LocalDatabaseModels.AttributeHeaders.FeedEntry.CITY_ID,
                            jsonObject.getDouble(LocalDatabaseModels.AttributeHeaders.FeedEntry.CITY_ID)
                    );
                    values.put(
                            LocalDatabaseModels.AttributeHeaders.FeedEntry.NAME,
                            jsonObject.getString(LocalDatabaseModels.AttributeHeaders.FeedEntry.NAME)
                    );

                    dbWrite.insert(LocalDatabaseModels.AttributeHeaders.FeedEntry.TABLE_NAME, null, values);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if(attributes != null) {
            for (int i = 0; i < attributes.length(); i++) {
                try {
                    JSONObject jsonObject = attributes.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put(
                            LocalDatabaseModels.Attributes.FeedEntry.CSV_ID,
                            jsonObject.getDouble(LocalDatabaseModels.Attributes.FeedEntry.CSV_ID)
                    );
                    values.put(
                            LocalDatabaseModels.Attributes.FeedEntry.LIST_ID,
                            jsonObject.getDouble(LocalDatabaseModels.Attributes.FeedEntry.LIST_ID)
                    );
                    values.put(
                            LocalDatabaseModels.Attributes.FeedEntry.VALUE,
                            jsonObject.getDouble(LocalDatabaseModels.Attributes.FeedEntry.VALUE)
                    );

                    dbWrite.insert(LocalDatabaseModels.Attributes.FeedEntry.TABLE_NAME, null, values);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void insertLegends(JSONArray legends) {
        dbWrite.execSQL("delete from "+ LocalDatabaseModels.Legends.FeedEntry.TABLE_NAME);

        if(legends != null) {
            for (int i = 0; i < legends.length(); i++) {
                try {
                    JSONObject jsonObject = legends.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.ID,
                            jsonObject.getDouble(LocalDatabaseModels.Legends.FeedEntry.ID)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.NAME,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.NAME)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.ZERO,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.ZERO)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.FIRST,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.FIRST)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.SECOND,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.SECOND)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.THIRD,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.THIRD)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.FOURTH,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.FOURTH)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.FIFTH,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.FIFTH)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.SIXTH,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.SIXTH)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.SEVENTH,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.SEVENTH)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.EIGHT,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.EIGHT)
                    );
                    values.put(
                            LocalDatabaseModels.Legends.FeedEntry.NINTH,
                            jsonObject.getString(LocalDatabaseModels.Legends.FeedEntry.NINTH)
                    );

                    dbWrite.insert(LocalDatabaseModels.Legends.FeedEntry.TABLE_NAME, null, values);
                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public int updateAreas(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Areas.FeedEntry.JSON, json);

        String selection = LocalDatabaseModels.Areas.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        return dbWrite.update(
                LocalDatabaseModels.Areas.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public int updateStreets(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Streets.FeedEntry.JSON, json);

        String selection = LocalDatabaseModels.Streets.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        return dbWrite.update(
                LocalDatabaseModels.Streets.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public int updatePoints(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Points.FeedEntry.JSON, json);

        // Which row to update, based on the title
        String selection = LocalDatabaseModels.Points.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        return dbWrite.update(
                LocalDatabaseModels.Points.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public int updateBoundSyncTimes(HashMap<Integer, Long> syncHashMap) {
        int rows = 0;
        for(Map.Entry<Integer, Long> entry : syncHashMap.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(LocalDatabaseModels.Bounds.FeedEntry.LAST_SYNC, entry.getValue());

            String selection = LocalDatabaseModels.Bounds.FeedEntry.ID + " = ?";
            String[] selectionArgs = { entry.getKey()+"" };

            int count = dbWrite.update(
                    LocalDatabaseModels.Bounds.FeedEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            rows += count;
        }
        return rows;
    }

    public int updateCities(String json) {
        ContentValues values = new ContentValues();
        values.put(LocalDatabaseModels.Cities.FeedEntry.JSON, json);

        String selection = LocalDatabaseModels.Cities.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        return dbWrite.update(
                LocalDatabaseModels.Cities.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public String getPoints() {
        String[] projection = {
                LocalDatabaseModels.Points.FeedEntry.JSON
        };

        String selection = LocalDatabaseModels.Points.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Points.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        if(cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Points.FeedEntry.JSON));
        }

        cursor.close();
        return null;
    }

    public String getStreets() {
        String[] projection = {
                LocalDatabaseModels.Streets.FeedEntry.JSON
        };

        String selection = LocalDatabaseModels.Streets.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Streets.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        if(cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Streets.FeedEntry.JSON));
        }

        cursor.close();
        return null;
    }

    public String getAreas() {
        String[] projection = {
                LocalDatabaseModels.Areas.FeedEntry.JSON
        };

        String selection = LocalDatabaseModels.Areas.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Areas.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        if(cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Areas.FeedEntry.JSON));
        }

        cursor.close();
        return null;
    }

    public HashMap<Integer, LatLngBounds> getBounds() {
        String[] projection = {
                LocalDatabaseModels.Bounds.FeedEntry.ID,
                LocalDatabaseModels.Bounds.FeedEntry.LAT_NORTH,
                LocalDatabaseModels.Bounds.FeedEntry.LAT_SOUTH,
                LocalDatabaseModels.Bounds.FeedEntry.LNG_EAST,
                LocalDatabaseModels.Bounds.FeedEntry.LNG_WEST,
        };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Bounds.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        HashMap<Integer, LatLngBounds> boundsHashMap = new HashMap<>();
        while(cursor.moveToNext()) {
            double latNorth = cursor.getDouble(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.LAT_NORTH));
            double latSouth = cursor.getDouble(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.LAT_SOUTH));
            double lngWest = cursor.getDouble(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.LNG_WEST));
            double lngEast = cursor.getDouble(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.LNG_EAST));
            int ID = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.ID));
            boundsHashMap.put(ID, LatLngBounds.from(latNorth, lngEast, latSouth, lngWest));
        }

        cursor.close();
        return boundsHashMap;
    }

    public HashMap<Integer, Long> getBoundsLastSyncTime() {
        String[] projection = {
                LocalDatabaseModels.Bounds.FeedEntry.LAST_SYNC,
                LocalDatabaseModels.Bounds.FeedEntry.ID
        };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Bounds.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        HashMap<Integer, Long> syncHashMap = new HashMap<>();
        while(cursor.moveToNext()) {
            long lastSync = cursor.getLong(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.LAST_SYNC));
            int ID = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Bounds.FeedEntry.ID));
            syncHashMap.put(ID, lastSync);
        }

        cursor.close();
        return syncHashMap;
    }

    public HashMap<String, String> getIcons() {
        String[] projection = {
                LocalDatabaseModels.Icons.FeedEntry.NAME,
                LocalDatabaseModels.Icons.FeedEntry.PATH
        };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Icons.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        HashMap<String, String> icons = new HashMap<>();
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Icons.FeedEntry.NAME));
            String path = cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Icons.FeedEntry.PATH));
            icons.put(name, path);
        }

        cursor.close();
        return icons;
    }

    public String getCities() {
        String[] projection = {
                LocalDatabaseModels.Cities.FeedEntry.JSON
        };

        String selection = LocalDatabaseModels.Cities.FeedEntry._ID + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Cities.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        String cities = null;
        if(cursor.moveToNext()) {
            cities = cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Cities.FeedEntry.JSON));
        }

        cursor.close();
        return cities;
    }

    public ArrayList<AttributeHeader> getAttributeHeaders() {
        String[] projection = {
                LocalDatabaseModels.AttributeHeaders.FeedEntry.ID,
                LocalDatabaseModels.AttributeHeaders.FeedEntry.NAME,
                LocalDatabaseModels.AttributeHeaders.FeedEntry.LEGEND_ID,
                LocalDatabaseModels.AttributeHeaders.FeedEntry.CITY_ID,
        };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.AttributeHeaders.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        ArrayList<AttributeHeader> headers = new ArrayList<>();
        while(cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(LocalDatabaseModels.AttributeHeaders.FeedEntry.NAME));
            int ID = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.AttributeHeaders.FeedEntry.ID));
            int idLegend = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.AttributeHeaders.FeedEntry.LEGEND_ID));
            int idCity = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.AttributeHeaders.FeedEntry.CITY_ID));
            headers.add(new AttributeHeader(ID, name, idLegend, idCity));
        }

        cursor.close();

        return headers;
    }

    public AttributeValues getAttributes(AttributeHeader header) {
        String[] projection = {
                LocalDatabaseModels.Attributes.FeedEntry.LIST_ID,
                LocalDatabaseModels.Attributes.FeedEntry.VALUE,
        };

        String selection = LocalDatabaseModels.Attributes.FeedEntry.CSV_ID + " = ?";
        String[] selectionArgs = { header.ID() + "" };

        Cursor cursor = dbRead.query(
                LocalDatabaseModels.Attributes.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        HashMap<Integer, Integer> attributesHashMap = new HashMap<>();
        while(cursor.moveToNext()) {
            int value = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Attributes.FeedEntry.VALUE));
            int list_id = cursor.getInt(cursor.getColumnIndexOrThrow(LocalDatabaseModels.Attributes.FeedEntry.LIST_ID));
            attributesHashMap.put(list_id, value);
        }

        String selectionLegend = LocalDatabaseModels.Legends.FeedEntry.ID + " = ?";
        String[] selectionArgsLegend = { header.LegendID() + "" };

        Cursor cursorLegend = dbRead.query(
                LocalDatabaseModels.Legends.FeedEntry.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selectionLegend,              // The columns for the WHERE
                selectionArgsLegend,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );

        Legend legend = null;
        if(cursorLegend.moveToNext()) {
            int id = cursorLegend.getInt(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.ID));
            ArrayList<String> colors = new ArrayList<>();
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.ZERO)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.FIRST)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.SECOND)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.THIRD)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.FOURTH)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.FIFTH)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.SIXTH)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.SEVENTH)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.EIGHT)));
            colors.add(cursorLegend.getString(cursorLegend.getColumnIndexOrThrow(LocalDatabaseModels.Legends.FeedEntry.NINTH)));
            legend = new Legend(id, colors);
        }

        cursor.close();
        cursorLegend.close();
        return new AttributeValues(header, attributesHashMap, legend);
    }
}
