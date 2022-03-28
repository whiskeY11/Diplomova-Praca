package com.example.geoapp.database;

import android.provider.BaseColumns;

public class LocalDatabaseModels {

    public static class Areas {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "areas";
            public static final String JSON = "json";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.JSON + " LONGTEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Points {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "points";
            public static final String JSON = "json";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.JSON + " LONGTEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Streets {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "streets";
            public static final String JSON = "json";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.JSON + " LONGTEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Bounds {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "bounds";
            public static final String ID = "id";
            public static final String LAT_NORTH = "lat_north";
            public static final String LAT_SOUTH = "lat_south";
            public static final String LNG_WEST = "lng_west";
            public static final String LNG_EAST = "lng_east";
            public static final String LAST_SYNC = "last_sync";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.ID + " INTEGER," +
                        FeedEntry.LAT_NORTH + " DOUBLE," +
                        FeedEntry.LAT_SOUTH + " DOUBLE," +
                        FeedEntry.LNG_WEST + " DOUBLE," +
                        FeedEntry.LNG_EAST + " DOUBLE," +
                        FeedEntry.LAST_SYNC + " BIGINT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Icons {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "icons";
            public static final String NAME = "name";
            public static final String PATH = "path";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.NAME + " VARCHAR," +
                        FeedEntry.PATH + " VARCHAR)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Cities {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "cities";
            public static final String JSON = "json";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.JSON + " LONGTEXT)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class AttributeHeaders {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "attribute_headers";
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String LEGEND_ID = "legend_id";
            public static final String CITY_ID = "city_id";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.ID + " INTEGER," +
                        FeedEntry.LEGEND_ID + " INTEGER," +
                        FeedEntry.CITY_ID + " INTEGER," +
                        FeedEntry.NAME + " VARCHAR)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Attributes {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "attributes";
            public static final String CSV_ID = "csv_id";
            public static final String LIST_ID = "list_id";
            public static final String VALUE = "value";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.CSV_ID + " INTEGER," +
                        FeedEntry.LIST_ID + " INTEGER," +
                        FeedEntry.VALUE + " INTEGER)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }

    public static class Legends {
        /* Inner class that defines the table contents */
        public static class FeedEntry implements BaseColumns {
            public static final String TABLE_NAME = "legend";
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String ZERO = "zero";
            public static final String FIRST = "first";
            public static final String SECOND = "second";
            public static final String THIRD = "third";
            public static final String FOURTH = "fourth";
            public static final String FIFTH = "fifth";
            public static final String SIXTH = "sixth";
            public static final String SEVENTH = "seventh";
            public static final String EIGHT = "eight";
            public static final String NINTH = "ninth";
        }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        FeedEntry._ID + " INTEGER PRIMARY KEY," +
                        FeedEntry.ID + " INTEGER," +
                        FeedEntry.NAME + " VARCHAR," +
                        FeedEntry.ZERO + " VARCHAR," +
                        FeedEntry.FIRST + " VARCHAR," +
                        FeedEntry.SECOND + " VARCHAR," +
                        FeedEntry.THIRD + " VARCHAR," +
                        FeedEntry.FOURTH + " VARCHAR," +
                        FeedEntry.FIFTH + " VARCHAR," +
                        FeedEntry.SIXTH + " VARCHAR," +
                        FeedEntry.SEVENTH + " VARCHAR," +
                        FeedEntry.EIGHT + " VARCHAR," +
                        FeedEntry.NINTH + " VARCHAR)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    }
}
