package com.example.geoapp.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.geoapp.MapActivity;
import com.example.geoapp.R;
import com.example.geoapp.attribute.AttributeHeader;
import com.example.geoapp.attribute.AttributeValues;
import com.example.geoapp.database.DatabaseCalls;
import com.example.geoapp.misc.Helper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import org.json.JSONArray;
import org.json.JSONException;

public class MapHelper {

    enum mapLayers {
        point,
        pointInfo,
        street,
        streetAttributes,
        area,
        areaOutline
    }

    private final int NUMBER_OF_ENTITIES = 4;
    private final int BOUNDSLAYERINDEX = mapLayers.values().length+1;
    private final MapEntity[] mapEntities = new MapEntity[NUMBER_OF_ENTITIES];
    private MapEntity points;
    private MapEntity streets;
    private MapEntity areas;
    private MapEntity cities;
    public MapImages mapImages;
    private GeoJsonSource boundsSource;

    private Feature selectedFeature = null;
    private AttributeValues selectedAttribute = null;

    private final HashMap<mapLayers, String> layerMap = new HashMap<>();
    private HashMap<Integer, LatLngBounds> boundsHashMap = new HashMap<>();
    private HashMap<Integer, Long> syncHashMap = new HashMap<>();
    private ArrayList<AttributeHeader> attributeHeaders = new ArrayList<>();

    private MapboxMap mapboxMap;
    private Style savedStyle;
    private Handler callbackHandler;
    private Handler settingsCallbackHandler;

    private Context mapContext;
    private Context settingsContext;

    private filterData currentFilterDataTask;
    private applyAttribute currentApplyAttributeTask;
    private Sync currentSyncTask;

    @SuppressLint("StaticFieldLeak")
    private static MapHelper Instance = null;

    private boolean isInitialized = false;
    private boolean sourcesInitialized = false;

    public void Setup(Style savedStyle, MapboxMap mapboxMap, Context context, Handler callbackHandler) {
        if(Instance != null) {
            this.savedStyle = savedStyle;
            this.mapboxMap = mapboxMap;
            this.callbackHandler = callbackHandler;

            int id = 1;
            for (mapLayers layer : mapLayers.values()) {
                layerMap.put(layer, id+"");
                id++;
            }

            mapEntities[0] = points;
            mapEntities[1] = streets;
            mapEntities[2] = areas;
            mapEntities[3] = cities;

            mapContext = context;

            mapImages.Setup(context, savedStyle, callbackHandler);

            isInitialized = true;
        }
    }

    public static boolean createInstance() {
        if(Instance == null) {
            Instance = new MapHelper();
            Instance.points = new Points();
            Instance.streets = new Streets();
            Instance.areas = new Areas();
            Instance.cities = new Cities();
            return true;
        } else {
            return false;
        }
    }

    public static MapHelper getInstance() {
        return Instance;
    }

    //this method is called before initialization => so before map loading
    public void setupImages(Context context, Handler callbackHandler) {
        this.callbackHandler = callbackHandler;
        mapImages = new MapImages(context, callbackHandler);
        mapImages.execSetupImages("Point");
    }

    public void refreshImages(Runnable action) {
        mapImages.refreshImages(mapContext, action);
    }

    public boolean areSourcesInitialized() { return sourcesInitialized; }

    public void SetSettingsValues(Handler callbackHandler, Context settingsContext) {
        settingsCallbackHandler = callbackHandler;
        this.settingsContext = settingsContext;
    }

    public HashMap<Integer, LatLngBounds> getBoundsHashMap() { return boundsHashMap; }
    public void setBoundsHashMap(HashMap<Integer, LatLngBounds> boundsHashMap) { this.boundsHashMap = boundsHashMap; }

    public HashMap<Integer, Long> getSyncHashMap() { return syncHashMap; }
    public void setSyncHashMap(HashMap<Integer, Long> syncHashMap) { this.syncHashMap = syncHashMap;}

    public ArrayList<AttributeHeader> getAttributeHeaders() { return attributeHeaders; }
    public void setAttributeHeaders(ArrayList<AttributeHeader> attributeHeaders) { this.attributeHeaders = attributeHeaders; }

    public Feature getSelectedFeature() { return selectedFeature; }
    public void setSelectedFeature(Feature selectedFeature) { this.selectedFeature = selectedFeature; }

    public AttributeValues getSelectedAttribute() { return selectedAttribute; }
    public void setSelectedAttribute(AttributeValues selectedAttribute) { this.selectedAttribute = selectedAttribute; }

    public MapEntity getPoints() { return points; }
    public MapEntity getStreets() { return streets; }
    public MapEntity getAreas() { return areas; }
    public MapEntity getCities() { return cities; }

    public void setupAllSources() {
        mapImages.addImagesIfNeeded();
        setupBoundsLayersAndSources();

        for(MapEntity mapEntity : mapEntities) {
            mapEntity.setupSourceAndLayer(layerMap, savedStyle);
        }
        sourcesInitialized = true;
    }

    private void setupBoundsLayersAndSources() {
        final String BOUNDSOURCEID = "bound-source";

        boundsSource = new GeoJsonSource(BOUNDSOURCEID);
        savedStyle.addSource(boundsSource);

        LineLayer outlineLayer = new LineLayer(BOUNDSLAYERINDEX+"", BOUNDSOURCEID);
        savedStyle.addLayer(outlineLayer);

        outlineLayer.withProperties(
                lineColor(Color.BLUE),
                lineWidth(3f),
                visibility(NONE));
    }

    public void refreshAllSources() {
        for(MapEntity entity : mapEntities) {
            entity.refreshSource();
        }
    }

    public void refreshEntitySource(MapEntity mapEntity) {
        mapEntity.refreshSource();
    }

    public void hideAllLayers() {
        if(isInitialized) {
            for (mapLayers s : mapLayers.values()) {
                Layer layer = savedStyle.getLayer(Objects.requireNonNull(layerMap.get(s)));
                if (layer != null)
                    layer.setProperties(visibility(NONE));
            }
        }
    }

    public void unhideAllLayers() {
        if (isInitialized) {
            for (mapLayers s : mapLayers.values()) {
                Layer layer = savedStyle.getLayer(Objects.requireNonNull(layerMap.get(s)));
                if (layer != null)
                    layer.setProperties(visibility(VISIBLE));
            }
        }
    }

    public int getCurrentSquareBoundsIndex() {
        for(Map.Entry<Integer, LatLngBounds> entry : getBoundsHashMap().entrySet()) {
            if(entry.getValue().contains(getCurrentCameraPosition())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void showEditLocationBounds() {
        LatLng selectedFeatureCords = Helper.Companion.getInstance().convertToLatLng(getSelectedFeature());
        LatLngBounds RESTRICTED_BOUNDS_AREA = null;
        for(Map.Entry<Integer, LatLngBounds> entry : getBoundsHashMap().entrySet()) {
            if(entry.getValue().contains(selectedFeatureCords)) {
                RESTRICTED_BOUNDS_AREA = entry.getValue();
                break;
            }
        }

        if(RESTRICTED_BOUNDS_AREA != null) {
            final double padding = 0.001f;
            LatLngBounds paddedBounds = LatLngBounds.from(
                    RESTRICTED_BOUNDS_AREA.getLatNorth()-padding,
                    RESTRICTED_BOUNDS_AREA.getLonEast()-padding,
                    RESTRICTED_BOUNDS_AREA.getLatSouth()+padding,
                    RESTRICTED_BOUNDS_AREA.getLonWest()+padding);

            mapboxMap.setLatLngBoundsForCameraTarget(paddedBounds);

            final List<List<Point>> points = new ArrayList<>();
            final List<Point> outerPoints = new ArrayList<>();

            outerPoints.add(Point.fromLngLat(paddedBounds.getNorthWest().getLongitude(),
                    paddedBounds.getNorthWest().getLatitude()));
            outerPoints.add(Point.fromLngLat(paddedBounds.getNorthEast().getLongitude(),
                    paddedBounds.getNorthEast().getLatitude()));
            outerPoints.add(Point.fromLngLat(paddedBounds.getSouthEast().getLongitude(),
                    paddedBounds.getSouthEast().getLatitude()));
            outerPoints.add(Point.fromLngLat(paddedBounds.getSouthWest().getLongitude(),
                    paddedBounds.getSouthWest().getLatitude()));
            outerPoints.add(Point.fromLngLat(paddedBounds.getNorthWest().getLongitude(),
                    paddedBounds.getNorthWest().getLatitude()));
            points.add(outerPoints);

            boundsSource.setGeoJson(Polygon.fromLngLats(points));

            Objects.requireNonNull(savedStyle.getLayer(BOUNDSLAYERINDEX + "")).setProperties(visibility(VISIBLE));
        } else {
            System.out.println("Internal error - bounds not found when trying to show bounds outline.");
        }
    }

    public void hideEditLocationBounds() {
        mapboxMap.setLatLngBoundsForCameraTarget(null);
        Objects.requireNonNull(savedStyle.getLayer(BOUNDSLAYERINDEX + "")).setProperties(visibility(NONE));
    }

    public void removeFeature(int id, boolean refreshSource) {
        if(isInitialized) {
            mapImages.removeImageAndView(id, settingsContext != null ? settingsContext : mapContext);

            Iterator<Feature> iterator = getPoints().featureCollection().features().iterator();

            while (iterator.hasNext()) {
                if (iterator.next().getNumberProperty("id").intValue() == id) {
                    iterator.remove();
                    break;
                }
            }

            if(refreshSource) {
                refreshEntitySource(getPoints());
            }
        }
    }

    public void removeMultipleFeatures(Collection<Integer> featuresID, boolean checkSelected) {
        if(isInitialized) {
            for(MapEntity mapEntity : mapEntities) {
                if(!featuresID.isEmpty()) {
                    iterateAndRemoveFeatures(
                            mapEntity,
                            featuresID,
                            mapEntity instanceof Points,
                            checkSelected
                    );
                } else {
                    break;
                }
            }
        }
    }

    private void iterateAndRemoveFeatures(MapEntity mapEntity,
                                          Collection<Integer> featuresID,
                                          boolean removeImage,
                                          boolean checkSelected) {
        HashMap<Integer, Feature> featureHashMap = mapEntity.featureHashMap();
        Iterator<Integer> removeIterator = featuresID.iterator();
        while (removeIterator.hasNext()) {
            int featureID = removeIterator.next();
            if(featureHashMap.containsKey(featureID)) {
                if(checkSelected && selectedFeature != null && selectedFeature.getNumberProperty("id").intValue() == featureID) {
                    setSelectedFeature(null);
                    callbackHandler.sendEmptyMessage(5);
                }

                if(removeImage) {
                    mapImages.removeImageAndView(featureID, settingsContext != null ? settingsContext : mapContext);
                }

                featureHashMap.remove(featureID);
                removeIterator.remove();
            }
        }

        mapEntity.newFeatureCollection(new ArrayList<>(featureHashMap.values()));

       /* while (featureIterator.hasNext()) {
            Feature next = featureIterator.next();
            Iterator<Integer> removeIterator = featuresID.iterator();
            while(removeIterator.hasNext()) {
                int featureID = removeIterator.next();
                if (next.getNumberProperty("id").intValue() == featureID) {
                    if(checkSelected && selectedFeature != null && selectedFeature.getNumberProperty("id").intValue() == featureID) {
                        setSelectedFeature(null);
                        callbackHandler.sendEmptyMessage(5);
                    }

                    if(removeImage) {
                        mapImages.removeImageAndView(featureID, settingsContext != null ? settingsContext : mapContext);
                    }
                    featureIterator.remove();
                    removeIterator.remove();
                    break;
                }
            }
        }*/
    }

    public void addFeature(Feature feature, int callbackMsgId) {
        if(isInitialized) {
            getPoints().featureCollection().features().add(feature);
            mapImages.addImageAndView(feature);
            selectFeature(feature);
            callbackHandler.sendEmptyMessage(callbackMsgId);
        }
    }

    public void addMultipleFeatures(Collection<Feature> features, boolean checkSelected) {
        if(isInitialized) {
            ArrayList<Feature> pointFeatures = new ArrayList<>();
            ArrayList<Feature> streetFeatures = new ArrayList<>();
            ArrayList<Feature> areaFeatures = new ArrayList<>();

            Feature featureToSelect = null;

            for(Feature f : features) {
                switch (Objects.requireNonNull(f.geometry()).type()) {
                    case "Point":
                        pointFeatures.add(f);
                        break;
                    case "LineString":
                        streetFeatures.add(f);
                        break;
                    case "Polygon":
                        areaFeatures.add(f);
                        break;
                }

                if(checkSelected && featureToSelect == null) {
                    int id = f.getNumberProperty("id").intValue();
                    if (selectedFeature != null && selectedFeature.getNumberProperty("id").intValue() == id) {
                        featureToSelect = f;
                    }
                }
            }

            Objects.requireNonNull(getPoints().featureCollection().features()).addAll(pointFeatures);
            Objects.requireNonNull(getStreets().featureCollection().features()).addAll(streetFeatures);
            Objects.requireNonNull(getAreas().featureCollection().features()).addAll(areaFeatures);

            if(featureToSelect != null) {
                selectFeature(featureToSelect, false);
                callbackHandler.sendEmptyMessage(6);
            }

            mapImages.execSetupMultipleImages(pointFeatures, true);
        }
    }

    public void editFeature(int id, String name, String icon, String city_id, long updated) {
        if(isInitialized) {
            for (Feature f : Objects.requireNonNull(getPoints().featureCollection().features())) {
                if(f.getNumberProperty("id").intValue() == id) {
                    f.addStringProperty("name", name);
                    f.addStringProperty("icon", icon);
                    f.addStringProperty("city_id", city_id);
                    f.addNumberProperty("updated", updated);
                    mapImages.removeImageAndView(id, settingsContext != null ? settingsContext : mapContext);
                    mapImages.addImageAndView(f);
                    break;
                }
            }

            refreshEntitySource(getPoints());
        }
    }

    public void editMultipleFeatures(Collection<Feature> features) {
        if(isInitialized) {
            ArrayList<Integer> featuresIDs = new ArrayList<>();
            for(Feature f : features) {
                featuresIDs.add(f.getNumberProperty("id").intValue());
            }

            removeMultipleFeatures(featuresIDs, false);
            addMultipleFeatures(features, true);
        }
    }

    private void resetFeatureAttributes() {
        if(isInitialized) {
            if(getSelectedAttribute() != null) {
                for (Feature f : Objects.requireNonNull(getStreets().featureCollection().features())) {
                    f.addNumberProperty("value", 0);
                }
                setSelectedAttribute(null);
            }
        }
    }

    public void deselectAllFeatures() {
        if(isInitialized) {
            for (Feature f : Objects.requireNonNull(getPoints().featureCollection().features())) {
                f.addBooleanProperty("selected", false);
                f.addNumberProperty("value", 0);
            }

            for (Feature f : Objects.requireNonNull(getStreets().featureCollection().features())) {
                f.addBooleanProperty("selected", false);
                f.addNumberProperty("value", 0);
            }

            for (Feature f : Objects.requireNonNull(getAreas().featureCollection().features())) {
                f.addBooleanProperty("selected", false);
                f.addNumberProperty("value", 0);
            }

            setSelectedAttribute(null);
            setSelectedFeature(null);
            refreshAllSources();
        }
    }

    public void deselectFeatures() {
        if(isInitialized) {
            if(getSelectedFeature() != null && !getSelectedFeature().hasProperty("city")) {
                MapEntity mapEntity = null;
                switch (Objects.requireNonNull(getSelectedFeature().geometry()).type()) {
                    case "Point":
                        mapEntity = getPoints();
                        break;
                    case "LineString":
                        mapEntity = getStreets();
                        break;
                    case "Polygon":
                        mapEntity = getAreas();
                        break;
                }

                assert mapEntity != null;
                for (Feature f : Objects.requireNonNull(mapEntity.featureCollection().features())) {
                    f.addBooleanProperty("selected", false);
                }

                refreshEntitySource(mapEntity);
            }

            setSelectedFeature(null);
        }
    }

    public void selectFeature(Feature selectedFeature, boolean refreshSource) {
        setSelectedFeature(null);

        MapEntity mapEntity = null;
        double zoomValue = 0;
        boolean forceZoom = false;
        switch (Objects.requireNonNull(selectedFeature.geometry()).type()) {
            case "Point":
                if(selectedFeature.hasProperty("city")) {
                    mapEntity = getCities();
                    zoomValue = 11;
                    forceZoom = true;
                } else {
                    mapEntity = getPoints();
                    zoomValue = 14;
                }
                break;
            case "LineString":
                zoomValue = 14;
                forceZoom = true;
                mapEntity = getStreets();
                break;
            case "Polygon":
                zoomValue = 11;
                forceZoom = true;
                mapEntity = getAreas();
                break;
        }

        assert mapEntity != null;

        if(mapEntity instanceof Streets) {
            for (Feature f : Objects.requireNonNull(mapEntity.featureCollection().features())) {
                if ((f.getStringProperty("name")
                        + f.getStringProperty("cityid")
                        + f.getStringProperty("loadid"))
                        .equals((selectedFeature.getStringProperty("name")
                                + selectedFeature.getStringProperty("cityid")
                                + selectedFeature.getStringProperty("loadid"))))
                {
                    f.addBooleanProperty("selected", true);
                    setSelectedFeature(f);
                } else {
                    f.addBooleanProperty("selected", false);
                }
            }
        } else {
            for (Feature f : Objects.requireNonNull(mapEntity.featureCollection().features())) {
                if (f.getNumberProperty("id").intValue() == selectedFeature.getNumberProperty("id").intValue()) {
                    if(!(mapEntity instanceof Cities)) {
                        f.addBooleanProperty("selected", true);
                    }
                    setSelectedFeature(f);
                } else {
                    if(!(mapEntity instanceof Cities)) {
                        f.addBooleanProperty("selected", false);
                    }
                }
            }
        }

        if(refreshSource) {
            refreshEntitySource(mapEntity);
        }

        if(forceZoom || getCurrentCameraZoom() < zoomValue) {
            animateCameraToSelection(selectedFeature, zoomValue);
        } else {
            animateCameraToSelection(selectedFeature);
        }

        if(callbackHandler != null) {
            callbackHandler.sendEmptyMessage(15);
        }
    }

    public void selectFeature(Feature selectedFeature) {
        selectFeature(selectedFeature, true);
    }

    public boolean isSelectedFeatureCreatedByUser() {
        if(getSelectedFeature().hasProperty("userid")) {
            return getSelectedFeature().getNumberProperty("userid").intValue() == DatabaseCalls.getInstance().getUserID();
        }

        return false;
    }

    public void animateCameraToSelection(final Feature feature, double zoom) {
        if(settingsContext == null) {
            ((Activity) mapContext).runOnUiThread(() -> {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Helper.Companion.getInstance().convertToLatLng(feature), zoom));
            });
        }
    }

    public void animateCameraToSelection(final Feature feature) {
        if(settingsContext == null) {
            ((Activity) mapContext).runOnUiThread(() -> {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(Helper.Companion.getInstance().convertToLatLng(feature)));
            });
        }
    }

    public void moveCameraToSelection(Feature feature) {
        if(settingsContext == null) {
            ((Activity) mapContext).runOnUiThread(() -> {
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(Helper.Companion.getInstance().convertToLatLng(feature)));
            });
        }
    }

    public LatLng getCurrentCameraPosition() {
        return mapboxMap.getCameraPosition().target;
    }

    private double getCurrentCameraZoom() {
        return mapboxMap.getCameraPosition().zoom;
    }

    public boolean areAsyncTasksRunning() {
        return currentFilterDataTask != null || currentApplyAttributeTask != null || currentSyncTask != null;
    }

    public boolean isSyncTaskRunning() { return currentSyncTask != null; }

    public void execSync(JSONArray deleted, JSONArray updated, JSONArray added, boolean updateAttributes, boolean updateLegends) {
        if(currentSyncTask != null) currentSyncTask.cancel(true);
        new Sync(deleted, updated, added, updateAttributes, updateLegends).execute();
    }

    private class Sync extends AsyncTask<String, Boolean, Boolean> {

        private JSONArray deleted;
        private JSONArray updated;
        private JSONArray added;

        private boolean updateAttributes;
        private boolean updateLegends;

        private boolean refreshSource = false;

        public Sync(JSONArray deleted, JSONArray updated, JSONArray added, boolean updateAttributes, boolean updateLegends) {
            this.deleted = deleted;
            this.updated = updated;
            this.added = added;
            this.updateAttributes = updateAttributes;
            this.updateLegends = updateLegends;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentSyncTask = this;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                ArrayList<Integer> deletedList = new ArrayList<>();
                if (deleted != null) {
                    for (int i = 0; i < deleted.length(); i++) {
                        deletedList.add(deleted.getInt(i));
                    }
                }

                ArrayList<Feature> addedList = new ArrayList<>();
                if (added != null) {
                    for (int i = 0; i < added.length(); i++) {
                        addedList.add(Feature.fromJson(added.getString(i)));
                    }
                }

                ArrayList<Feature> updatedList = new ArrayList<>();
                if (updated != null) {
                    for (int i = 0; i < updated.length(); i++) {
                        updatedList.add(Feature.fromJson(updated.getString(i)));
                    }
                }

                if (deletedList.size() > 0) {
                    refreshSource = true;
                    removeMultipleFeatures(deletedList, true);
                }
                if (addedList.size() > 0) {
                    refreshSource = true;
                    addMultipleFeatures(addedList, false);
                }
                if (updatedList.size() > 0) {
                    refreshSource = true;
                    editMultipleFeatures(updatedList);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (refreshSource) {
                refreshAllSources();
            }

            if(result) {
                if (updateAttributes || updateLegends) {
                    DatabaseCalls.getInstance().execGetAttributeData(updateAttributes, updateLegends);
                }
                Log.i("synchronization", "Successfully synchronized");
            } else {
                Log.i("synchronization", "Synchronization failed!");
            }

            if(callbackHandler != null) callbackHandler.sendEmptyMessage(19);

            currentSyncTask = null;
        }
    }

    public void execFilterData(String searchString, Handler callback) {
        if(DatabaseCalls.getInstance().isSynchronizing() || isSyncTaskRunning()) {
            if(callbackHandler != null) callbackHandler.sendEmptyMessage(17);
        } else {
            if(currentFilterDataTask != null) currentFilterDataTask.cancel(true);
            new filterData(callback).execute(searchString);
        }
    }

    private class filterData extends AsyncTask<String, Boolean, Boolean> {

        private ArrayList<Feature> searchResults = new ArrayList<>();
        private Handler _callback;
        private final int MaxQueryResults = 10;

        public filterData(Handler callback) {
            _callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentFilterDataTask = this;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String searchString = params[0];
            HashMap<String, String> searchResultsString = new HashMap<>();
            if(searchString != null)
            {
                searchString = Helper.Companion.getInstance().stripAccents(searchString.toLowerCase().trim());

                for(MapEntity mapEntity : mapEntities)
                {
                    List<Feature> features = mapEntity.featureCollection().features();
                    assert features != null;

                    if(mapEntity instanceof Cities) {
                        for (Feature f : features) {
                            if (Helper.Companion.getInstance().stripAccents(f.getStringProperty("name").toLowerCase().trim()).contains(searchString)
                                    && !searchResultsString.containsKey(f.getStringProperty("name")))
                            {
                                searchResults.add(f);
                                searchResultsString.put(f.getStringProperty("name"), null);
                                if(searchResults.size() >= MaxQueryResults) return true;
                            }
                        }
                    } else {
                        for (Feature f : features) {
                            if (Helper.Companion.getInstance().stripAccents(f.getStringProperty("name").toLowerCase().trim()).contains(searchString)
                                    && !searchResultsString.containsKey(
                                    f.getStringProperty("name")
                                            + f.getStringProperty("cityid")
                                            + f.getStringProperty("loadid")))
                            {
                                searchResults.add(f);
                                searchResultsString.put(
                                        f.getStringProperty("name")
                                                + f.getStringProperty("cityid")
                                                + f.getStringProperty("loadid"), null);
                                if(searchResults.size() >= MaxQueryResults) return true;
                            }
                        }
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                Message msg = Message.obtain();
                msg.obj = searchResults;
                msg.what = 1;
                _callback.dispatchMessage(msg);
            } else {
                System.out.println("Failed to filter data for search!");
            }
            currentFilterDataTask = null;
        }
    }

    public boolean isNearCityWithAttributes() {
        if(getSelectedAttribute() != null && getSelectedAttribute().City() != null) {
            final double maxDistance = 0.06;
            LatLng currentCameraPosition = getCurrentCameraPosition();

            Feature city = getSelectedAttribute().City();
            LatLng cityPos = Objects.requireNonNull(Helper.Companion.getInstance()).convertToLatLng(city);
            if(Math.abs(cityPos.getLatitude() - currentCameraPosition.getLatitude()) <= maxDistance &&
                    Math.abs(cityPos.getLongitude() - currentCameraPosition.getLongitude()) <= maxDistance)
            {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public AttributeHeader attributeHeader(int attributeHeaderIndex) {
        for(AttributeHeader header : attributeHeaders) {
            if(header.ID() == attributeHeaderIndex) return header;
        }
        return null;
    }

    public void execApplyAttribute(int headerIndex, boolean calledFromDatabaseCalls) {
        if(!calledFromDatabaseCalls && DatabaseCalls.getInstance().isSynchronizing() || isSyncTaskRunning()) {
            if(settingsCallbackHandler != null) {
                settingsCallbackHandler.sendEmptyMessage(2);
                settingsCallbackHandler.sendEmptyMessage(3);
            }
        } else {
            if(currentApplyAttributeTask != null) currentApplyAttributeTask.cancel(true);
            new applyAttribute().execute(headerIndex, calledFromDatabaseCalls ? 1 : 0);
        }
    }

    private class applyAttribute extends AsyncTask<Integer, Boolean, Boolean> {

        private boolean fireCallback = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentApplyAttributeTask = this;
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            resetFeatureAttributes();
            int headerIndex = params[0];
            if(params[1] == 1) fireCallback = true;

            if(headerIndex != -1) {
                AttributeHeader header = attributeHeader(headerIndex);
                if(header != null) {
                    AttributeValues attribute = DatabaseCalls.getInstance().getLocalDb().getAttributes(attributeHeader(headerIndex));
                    attribute.SetCity(((Cities) getCities()).citySuggestion(header.CityID()));
                    setSelectedAttribute(attribute);

                    HashMap<Integer, Integer> attributeValuesHashMap = attribute.Values();
                    if (!attributeValuesHashMap.isEmpty()) {
                        HashMap<Integer, Feature> featureHashMap = getStreets().featureHashMap();

                        for (Map.Entry<Integer, Integer> entry : attributeValuesHashMap.entrySet()) {
                            if(featureHashMap.containsKey(entry.getKey())) {
                                String color = attribute.ColorFromValue(entry.getValue());
                                Objects.requireNonNull(featureHashMap.get(entry.getKey())).addStringProperty("value", color);
                            }
                        }

                        getStreets().newFeatureCollection(new ArrayList<>(featureHashMap.values()));

                        /*for (Map.Entry<Integer, Integer> entry : attributeValuesHashMap.entrySet()) {
                            for (Feature f : Objects.requireNonNull(getStreets().featureCollection().features())) {
                                if (f.getNumberProperty("id").intValue() == entry.getKey()) {
                                    String color = attribute.ColorFromValue(entry.getValue());
                                    f.addStringProperty("value", color);
                                    break;
                                }
                            }
                        }*/
                    } else {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            refreshEntitySource(getStreets());
            if(fireCallback) {
                if(settingsCallbackHandler != null) settingsCallbackHandler.sendEmptyMessage(1);
                else if(callbackHandler != null) callbackHandler.sendEmptyMessage(16);
            } else {
                if(settingsCallbackHandler != null) settingsCallbackHandler.sendEmptyMessage(4);
            }

            if(!result) {
                System.out.println("Attribute values are empty!");
            }

            currentApplyAttributeTask = null;
        }
    }
}
