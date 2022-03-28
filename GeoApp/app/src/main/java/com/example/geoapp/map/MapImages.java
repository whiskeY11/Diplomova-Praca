package com.example.geoapp.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoapp.MapActivity;
import com.example.geoapp.R;
import com.example.geoapp.database.DatabaseCalls;
import com.example.geoapp.misc.Helper;
import com.example.geoapp.misc.ImageHelper;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapImages {

    private final String DEFAULT_ICON_NAME = "defaultIcon";

    private Context context;
    private Style savedStyle;
    private Handler callbackHandler;
    private Runnable action = null;

    private HashMap<String, Bitmap> iconImagesMap = new HashMap();
    private HashMap imagesMap = new HashMap();
    private HashMap<Integer, View> viewMap = new HashMap<>();

    private final int NUMBER_OF_COLLECTIONS = 1;
    private ArrayList<Boolean> imagesLoaded = new ArrayList<>();

    private boolean addingNeeded = false;
    private boolean setupFinished = false;
    private boolean isInitialized = false;

    public void Setup(Context _context, Style style, Handler callback) {
        savedStyle = style;
        callbackHandler = callback;
        context = _context;
        isInitialized = true;
    }

    public MapImages(Context _context, Handler callback) {
        callbackHandler = callback;
        context = _context;

        for(int i = 0; i < NUMBER_OF_COLLECTIONS; i++) {
            imagesLoaded.add(false);
        }
    }

    public void refreshIcons() {
        HashMap<String, String> savedIconsPath = DatabaseCalls.getInstance().getLocalDb().getIcons();

        for(Map.Entry<String, Bitmap> entry : iconImagesMap.entrySet()) {
            savedStyle.removeImage(entry.getKey());
        }

        iconImagesMap.clear();

        for(Map.Entry<String, String> entry : savedIconsPath.entrySet()) {
            Bitmap bitmap = ImageHelper.readFromDisk(new File(entry.getValue()));
            iconImagesMap.put(entry.getKey(), bitmap);
        }

        savedStyle.addImages(iconImagesMap);

        MapHelper.getInstance().refreshEntitySource(MapHelper.getInstance().getPoints());
        Helper.Companion.getInstance().onIconDataChanged();
    }

    public void refreshImages(Context context, Runnable callback) {
        action = callback;

        for(int i = 0; i < NUMBER_OF_COLLECTIONS; i++) {
            imagesLoaded.add(false);
        }

        ((Activity) context).runOnUiThread(() -> {
            for(Object id : imagesMap.keySet()) {
                if(id instanceof String) {
                    removeImageAndView(Integer.parseInt((String) id), context);
                }
            }

            for(Map.Entry<String, Bitmap> entry : iconImagesMap.entrySet()) {
                savedStyle.removeImage(entry.getKey());
            }
        });

        addingNeeded = true;
        execSetupImages("Point");
    }

    public void removeImageAndView(final int key, Context context) {
        ((Activity) context).runOnUiThread(() -> {
            savedStyle.removeImage(key + "");
            imagesMap.remove(key);
            viewMap.remove(key);
        });
    }

    public void addImageAndView(Feature feature) {
        final View view = LayoutInflater.from(context).inflate(R.layout.infobox, null);

        String title = feature.getStringProperty("name");
        final int id = feature.getNumberProperty("id").intValue();
        TextView titleTv = (TextView) view.findViewById(R.id.title);
        titleTv.setText(title);

        final Bitmap bitmap = Helper.Companion.getInstance().generate(view);
        imagesMap.put(id+"", bitmap);
        viewMap.put(id, view);

        savedStyle.addImage(id+"", bitmap);
    }

    public String[] iconNames() {
        String[] names = new String[iconImagesMap.size()];

        names[0] = DEFAULT_ICON_NAME;

        int index = 1;
        for(Map.Entry<String, Bitmap> entry : iconImagesMap.entrySet()) {
            if(!entry.getKey().equals(DEFAULT_ICON_NAME)) {
                names[index] = entry.getKey();
                index++;
            }
        }
        return names;
    }

    public Bitmap getIcon(String key) {
        if(iconImagesMap.containsKey(key)) {
            return iconImagesMap.get(key);
        }
        return null;
    }

    public void execSetupImages(String value) {
        new setupImages(context).execute(value);
    }

    public void execAddImages(boolean value) {
        new addImages(value).execute();
    }

    public void addImagesIfNeeded() {
        if(!setupFinished) {
            addingNeeded = true;
        } else {
            addingNeeded = false;
            execAddImages(true);
        }
    }

    public void execSetupMultipleImages(Collection<Feature> features, boolean value) {
        new setupMultipleImages(context, features, value).execute();
    }

    private class setupImages extends AsyncTask<String, String, String> {
        private WeakReference<Context> activity;

        setupImages(Context context) {
            this.activity = new WeakReference<>(context);;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected String doInBackground(String... params) {
            Context context = activity.get();
            if(params[0].equals("Point")) {
                HashMap<String, String> savedIconsPath = DatabaseCalls.getInstance().getLocalDb().getIcons();

                for(Map.Entry<String, String> entry : savedIconsPath.entrySet()) {
                    Bitmap bitmap = ImageHelper.readFromDisk(new File(entry.getValue()));
                    iconImagesMap.put(entry.getKey(), bitmap);
                }
            }

            List<Feature> featuresToAdd = null;
            switch(params[0]) {
                case "Point":
                    featuresToAdd = MapHelper.getInstance().getPoints().featureCollection().features();
                    break;
                case "LineString":
                    featuresToAdd = MapHelper.getInstance().getStreets().featureCollection().features();
                    break;
                case "Polygon":
                    featuresToAdd = MapHelper.getInstance().getAreas().featureCollection().features();
                    break;
            }

            if(featuresToAdd == null) {
                return "Nesprávny parameter";
            }

            for(Feature f : new ArrayList<>(featuresToAdd)) {
                final View view = LayoutInflater.from(context).inflate(R.layout.infobox, null);

                final String title = f.getStringProperty("name");
                final int id = f.getNumberProperty("id").intValue();
                TextView titleTv = (TextView) view.findViewById(R.id.title);
                titleTv.setText(title);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Bitmap bitmap = Helper.Companion.getInstance().generate(view);
                        imagesMap.put(id+"", bitmap);
                        viewMap.put(id, view);
                    }
                }, 80);   //0.05 seconds
            }

            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("done")) {
                Iterator<Boolean> iterator = imagesLoaded.iterator();

                while(iterator.hasNext()) {
                    Boolean next = iterator.next();
                    if(!next) {
                        imagesLoaded.set(imagesLoaded.indexOf(next), true);
                        break;
                    }
                }

                if(!imagesLoaded.contains(false)) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            callbackHandler.sendEmptyMessage(0);
                            setupFinished = true;
                            if(addingNeeded) {
                                addingNeeded = false;
                                execAddImages(true);
                            }
                        }
                    }, 200);   //0.05 seconds
                }
            } else {
                System.out.println(result);
            }
        }
    }

    private class setupMultipleImages extends AsyncTask<String, String, String> {

        private HashMap currentImagesMap = new HashMap();
        private boolean refreshSource;
        private WeakReference<Context> activity;
        private Collection<Feature> featuresToAdd;

        setupMultipleImages(Context context, Collection<Feature> featuresToAdd, boolean refreshSource) {
            this.activity = new WeakReference<>(context);
            this.refreshSource = refreshSource;
            this.featuresToAdd = featuresToAdd;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected String doInBackground(String... params) {
            Context context = activity.get();

            for (Feature f : featuresToAdd) {
                final View view = LayoutInflater.from(context).inflate(R.layout.infobox, null);

                final String title = f.getStringProperty("name");
                final int id = f.getNumberProperty("id").intValue();
                TextView titleTv = view.findViewById(R.id.title);
                titleTv.setText(title);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    Bitmap bitmap = Helper.Companion.getInstance().generate(view);
                    imagesMap.put(id+"", bitmap);
                    currentImagesMap.put(id+"", bitmap);
                    viewMap.put(id, view);
                }, 80);   //0.05 seconds
            }

            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("done")) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    savedStyle.addImages(currentImagesMap);
                    if(refreshSource) {
                        MapHelper.getInstance().refreshEntitySource(MapHelper.getInstance().getPoints());
                    }
                }, 80);
            } else {
                System.out.println(result);
            }
        }
    }

    private class addImages extends AsyncTask<String, String, String> {
        private boolean refreshSource;

        addImages(boolean refreshSource) {
            this.refreshSource = refreshSource;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected String doInBackground(String... params) {
            savedStyle.addImagesAsync(imagesMap);
            savedStyle.addImagesAsync(iconImagesMap);

            return "done";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("done")) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if(refreshSource) {
                        MapHelper.getInstance().refreshEntitySource(MapHelper.getInstance().getPoints());

                        if(action != null) {
                            action.run();
                            action = null;
                        }
                    }
                }, 80);
            } else {
                System.out.println(result);
            }
        }
    }
}
