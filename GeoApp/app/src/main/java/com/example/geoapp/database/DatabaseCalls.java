package com.example.geoapp.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.geoapp.misc.Compression;
import com.example.geoapp.misc.Connect;
import com.example.geoapp.map.MapHelper;
import com.example.geoapp.misc.Helper;
import com.example.geoapp.misc.ImageHelper;
import com.mapbox.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DatabaseCalls {

    private final String urlPrefix = "http://192.168.241.236";
    private final String serverPath = "/GeoAppServer/public/";
    private final String storagePath = "/GeoAppServer/storage/app/";

    private final String CLIENT_ID = "2";
    private final String GRANT_TYPE = "password";
    private final String CLIENT_SECRET = "1tYN2QHpFiR3gUDTAbDyOzsFyMfUJR9YbBehRW0j";
    private int userID;

    private String iconsPath = "";
    private boolean calledFromSynchronize = false;

    private boolean needsIconDownload = false;
    private boolean needsAttributeDownload = false;
    private boolean needsCityDownload = false;
    private boolean needsLegendDownload = false;
    public int NUMBER_OF_COLLECTIONS_NEEDED = 0;
    public final int NUMBER_OF_COLLECTIONS = 7;
    private ArrayList<Boolean> dataLoaded = new ArrayList<>();

    private Handler callbackHandler = null;

    private boolean synchronize = false;

    private static DatabaseCalls Instance = null;

    private LocalDatabase localDb = null;
    private Connect connect;

    public Context context;

    public void SetupLocalDatabase(Context context) {
        if(localDb != null) {
            localDb.close();
        }

        localDb = new LocalDatabase(context);
        this.context = context;
    }

    private DatabaseCalls() {
        for(int i = 0; i < NUMBER_OF_COLLECTIONS; i++) {
            dataLoaded.add(false);
        }
        connect = new Connect();
    }

    public static boolean createInstance() {
        if(Instance == null) {
            Instance = new DatabaseCalls();
            return true;
        } else return false;
    }

    public static DatabaseCalls getInstance() {
        return Instance;
    }

    public LocalDatabase getLocalDb() { return localDb; }

    public void setCallbackHandler(Handler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public void ResetNumberOfNeededCollections() { NUMBER_OF_COLLECTIONS_NEEDED = 0; }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public int getUserID() { return userID; }

    public void execLogin(String email, String password) {
        new login().execute(email, GRANT_TYPE, CLIENT_ID, CLIENT_SECRET, email, password);
    }

    public void execRegister(String email, String password) { new register().execute(email, password); }

    public void execCheckUserParams() { new checkUserParams().execute(); }

    public void execDisableForceDownload() { new disableForceDownload().execute(); }

    public void execDisableIconDownload() { new disableIconDownload().execute(); }

    public void execDisableAttributesDownload() { new disableAttributeDownload().execute(); }

    public void execDisableCitiesDownload() { new disableCityDownload().execute(); }

    public void execDisableLegendsDownload() { new disableLegendDownload().execute(); }

    public void execGetPointData() {
        new getData().execute("Point");
    }

    public void execGetStreetData() {
        new getData().execute("LineString");
    }

    public void execGetAreaData() {
        new getData().execute("Polygon");
    }

    public void execGetBoundsData() { new getBoundsData().execute(); }

    public void execGetIcons() {
        new getIconList().execute();
    }

    public void execGetCityData() {
        new getCityData().execute();
    }

    public void execGetAttributeData(boolean include_attributes, boolean include_legends) {
        String param = "0";
        String param2 = "0";
        if(include_attributes) param = "1";
        if(include_legends) param2 = "1";

        new getAttributeData().execute(param, param2);
    }

    //Gets all data from database
    public void getAllData() {
        calledFromSynchronize = false;

        Log.i("download", "Download started");

        dataLoaded.clear();
        for(int i = 0; i < NUMBER_OF_COLLECTIONS; i++) {
            dataLoaded.add(false);
        }

        needsLegendDownload = true;
        needsAttributeDownload = true;

        execGetBoundsData();
        execGetIcons();
        execGetPointData();
        execGetStreetData();
        execGetAreaData();
        execGetCityData();
        execGetAttributeData(true,true);
    }

    public void getNeededData() {
        calledFromSynchronize = false;

        dataLoaded.clear();
        for(int i = 0; i < NUMBER_OF_COLLECTIONS_NEEDED; i++) {
            dataLoaded.add(false);
        }

        if(needsCityDownload) {
            execGetCityData();
        }
        if(needsAttributeDownload || needsLegendDownload) {
            execGetAttributeData(needsAttributeDownload, needsLegendDownload);
        }
        if(needsIconDownload) {
            execGetIcons();
        }
    }

    public void execDelete(String id) {
        new deleteFeature().execute(id);
    }

    public void execAdd(String name, String type, String icon, String lng, String lat, String city_id) {
        new addFeature().execute(name, type, icon, lng, lat, city_id);
    }

    public void execEdit(String id, String name, String icon, String city_id) {
        new editFeature().execute(id, name, icon, city_id);
    }

    public void execEditLocation(String id, String lng, String lat) { new editFeatureLocation().execute(id, lng, lat); }

    public void execUpdateLocalDb(String pointsOnly) { new updateLocalDatabase().execute(pointsOnly); }

    public boolean isEverythingLoaded() { return !dataLoaded.contains(false); }

    public boolean isSynchronizing() {
        return synchronize;
    }

    public void synchronize(int boundsID, long boundsLastSync) {
        if(!synchronize) {
            synchronize = true;
            Log.i("synchronization", "Sync started, boundsID = "+boundsID);
            new sendSyncRequest().execute(boundsID+"", boundsLastSync+"");
        } else {
            Log.i("synchronization", "Synchronization is still running, ignoring request for next");
        }
    }

    private class sendSyncRequest extends AsyncTask<String, String, String> {
        private int boundsID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            boundsID = Integer.parseInt(params[0]);
            String[] parametre = {"boundsID", "lastSync"};
            return connect.makeRequest(getUrlPrefix() + serverPath, "POST", "synchronizeUser",
                    parametre, params, true, new int[]{40000,40000});
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    String decompressed = Compression.decompress(Compression.decodeBase64(result));
                    JSONObject jObj = new JSONObject(decompressed);
                    Log.i("synchronization", jObj.toString());
                    int force_download = jObj.getInt("force_download");

                    if(force_download == 1) {
                        if (callbackHandler != null)
                            callbackHandler.sendEmptyMessage(11);
                    } else {
                        JSONArray deleted = jObj.getJSONArray("deleted");
                        JSONArray added = jObj.getJSONArray("added");
                        JSONArray updated = jObj.getJSONArray("updated");
                        long syncTime = jObj.getLong("syncTime");
                        int icon_download = jObj.getInt("icon_download");
                        int city_download = jObj.getInt("city_download");
                        int attribute_download = jObj.getInt("attribute_download");
                        int legend_download = jObj.getInt("legend_download");

                        calledFromSynchronize = true;

                        if (icon_download == 1) {
                            needsIconDownload = true;
                            execGetIcons();
                        }
                        if (city_download == 1) {
                            needsCityDownload = true;
                            execGetCityData();
                        }

                        needsAttributeDownload = attribute_download == 1;
                        needsLegendDownload = legend_download == 1;

                        MapHelper.getInstance().getSyncHashMap().put(boundsID, syncTime);
                        MapHelper.getInstance().execSync(deleted, updated, added, needsAttributeDownload, needsLegendDownload);
                    }

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }

            synchronize = false;
        }
    }

    private class login extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String[] parametre = {"email", "grant_type", "client_id", "client_secret", "username", "password"};
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "oauth/token", parametre, params);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                    connect.setToken(jObj.getString("access_token"));
                    if(callbackHandler != null)
                        callbackHandler.sendEmptyMessage(3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null) {
                    if(result.equals(Connect.REQUEST_RETURNED_ERROR)) {
                        try {
                            String error = connect.getErrorMessage();
                            JSONObject jObj = new JSONObject(error);
                            String message = "";
                            switch(jObj.getString("error")) {
                                case "invalid_grant":
                                    message = "Nesprávne prihlasovacie údaje!";
                                    break;
                                case "invalid_client":
                                    message = "Nesprávne OAuth prístupové dáta!";
                                    break;
                            }
                            Message msg = Message.obtain();
                            msg.obj = message;
                            msg.what = -2;
                            callbackHandler.dispatchMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Message msg = Message.obtain();
                        msg.obj = "Problém s internetovým pripojením.";
                        msg.what = -2;
                        callbackHandler.dispatchMessage(msg);
                    }
                }
            }
        }
    }

    private class register extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String[] parametre = {"email", "password"};
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "registerUser", parametre, params);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                    if(callbackHandler != null) {
                        if(jObj.getString("result").equals("1")) callbackHandler.sendEmptyMessage(1);
                        else callbackHandler.sendEmptyMessage(0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null) {
                    callbackHandler.sendEmptyMessage(-1);
                }
            }
        }
    }

    private class checkUserParams extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "checkUserParams", null, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());

                    userID = jObj.getInt("user_id");

                    if(jObj.getString("force_download").equals("1")) {
                        if(callbackHandler != null)
                            callbackHandler.sendEmptyMessage(4);
                    } else {
                        if(callbackHandler != null) {
                            if(jObj.getString("icon_download").equals("1")) {
                                needsIconDownload = true;
                                NUMBER_OF_COLLECTIONS_NEEDED++;
                            }
                            if(jObj.getString("attribute_download").equals("1") ||
                                    jObj.getString("legend_download").equals("1")) {
                                needsAttributeDownload = jObj.getString("attribute_download").equals("1");
                                needsLegendDownload = jObj.getString("legend_download").equals("1");
                                NUMBER_OF_COLLECTIONS_NEEDED++;
                            }
                            if(jObj.getString("city_download").equals("1")) {
                                needsCityDownload = true;
                                NUMBER_OF_COLLECTIONS_NEEDED++;
                            }

                            if(NUMBER_OF_COLLECTIONS_NEEDED > 0)
                                callbackHandler.sendEmptyMessage(6);
                            else {
                                callbackHandler.sendEmptyMessage(5);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class disableForceDownload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "disableForceDownload", null, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class disableIconDownload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "disableIconDownload", null, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                    needsIconDownload = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class disableAttributeDownload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "disableAttributeDownload", null, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                    needsAttributeDownload = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class disableCityDownload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "disableCityDownload", null, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                    needsCityDownload = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class disableLegendDownload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "disableLegendDownload", null, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());
                    needsLegendDownload = false;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class getData extends AsyncTask<String, String, String> {

        private String type = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String[] parametre = {"type"};
            type = params[0];
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST",
                    "getList", parametre, params, true, new int[]{40000,40000});
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    String decompressed = Compression.decompress(Compression.decodeBase64(result));
                    JSONObject jObj = new JSONObject(decompressed);
                    System.out.println(jObj.toString());

                    for (Boolean next : dataLoaded) {
                        if (!next) {
                            dataLoaded.set(dataLoaded.indexOf(next), true);
                            break;
                        }
                    }

                    switch(type) {
                        case "Point":
                            MapHelper.getInstance().getPoints().newFeatureCollection(jObj.toString());
                            if(localDb.getPoints() == null) {
                                localDb.insertPoints(jObj.toString());
                            } else {
                                localDb.updatePoints(jObj.toString());
                            }
                            break;
                        case "LineString":
                            MapHelper.getInstance().getStreets().newFeatureCollection(jObj.toString());
                            if(localDb.getStreets() == null) {
                                localDb.insertStreets(jObj.toString());
                            } else {
                                localDb.updateStreets(jObj.toString());
                            }
                            break;
                        case "Polygon":
                            MapHelper.getInstance().getAreas().newFeatureCollection(jObj.toString());
                            if(localDb.getAreas() == null) {
                                localDb.insertAreas(jObj.toString());
                            } else {
                                localDb.updateAreas(jObj.toString());
                            }
                            break;
                    }

                    if(callbackHandler != null)
                        callbackHandler.sendEmptyMessage(10);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class getBoundsData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST",
                    "getBounds", null, null, true, new int[]{40000,40000});
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    String decompressed = Compression.decompress(Compression.decodeBase64(result));
                    JSONObject jObj = new JSONObject(decompressed);
                    System.out.println(jObj.toString());

                    JSONArray bounds = jObj.getJSONArray("bounds");
                    localDb.insertBounds(bounds);

                    for (Boolean next : dataLoaded) {
                        if (!next) {
                            dataLoaded.set(dataLoaded.indexOf(next), true);
                            break;
                        }
                    }

                    MapHelper.getInstance().setBoundsHashMap(localDb.getBounds());
                    MapHelper.getInstance().setSyncHashMap(localDb.getBoundsLastSyncTime());

                    if(callbackHandler != null)
                        callbackHandler.sendEmptyMessage(10);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class getIconList extends AsyncTask<String, String, String> implements ImageHelper.OnImageLoaderListener, ImageHelper.OnBitmapSaveListener {

        private int count = 0;
        private int savedCount = 0;

        private ImageHelper imageHelper;
        private HashMap<String, String> images = new HashMap<>();

        private final float dpx = 30;
        private final float dpy = 30;

        private int xsize;
        private int ysize;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST",
                    "getIcons", null, null, true, new int[]{40000,40000});
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    String decompressed = Compression.decompress(Compression.decodeBase64(result));
                    JSONObject jObj = new JSONObject(decompressed);
                    System.out.println(jObj.toString());
                    JSONArray list = jObj.getJSONArray("icons");

                    xsize = Helper.Companion.getInstance().dpToPx(dpx);
                    ysize = Helper.Companion.getInstance().dpToPx(dpy);

                    count = list.length();
                    iconsPath = Objects.requireNonNull(context.getExternalCacheDir()).getPath()+"/icons/";

                    imageHelper = new ImageHelper(this, context);

                    imageHelper.deleteRecursive(new File(iconsPath));

                    for (int i = 0; i < list.length(); i++) {
                        try {
                            JSONObject jsonObject = list.getJSONObject(i);
                            ImageHelper.IMAGETYPE imagetype = ImageHelper.IMAGETYPE.valueOf(jsonObject.getString("type"));
                            imageHelper.download(
                                            imagetype,
                                    getUrlPrefix()+storagePath+jsonObject.getString("url"),
                                            jsonObject.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }

        private void imagesSaved() {
            localDb.insertIcons(images);

            if(!calledFromSynchronize) {
                for (Boolean next : dataLoaded) {
                    if (!next) {
                        dataLoaded.set(dataLoaded.indexOf(next), true);
                        break;
                    }
                }
            }

            if(callbackHandler != null) {
                if(calledFromSynchronize) {
                    MapHelper.getInstance().mapImages.refreshIcons();
                } else {
                    callbackHandler.sendEmptyMessage(10);
                }
            }

            execDisableIconDownload();
        }

        @Override
        public void onError(ImageHelper.ImageError error) {
            System.out.println(error.getMessage());
        }

        @Override
        public void onComplete(String name, Bitmap bitmap) {
            bitmap = Helper.Companion.getInstance().getResizedBitmap(bitmap, xsize, ysize);
            ImageHelper.writeToDisk(new File(iconsPath+name+".png"), bitmap, name,
                    this, Bitmap.CompressFormat.PNG, true);
        }

        @Override
        public void onBitmapSaved(String path, String name) {
            savedCount++;
            images.put(name, path);
            System.out.println("Icons saved count: " + savedCount);

            if(savedCount == count) {
                imagesSaved();
            }
        }

        @Override
        public void onBitmapSaveError(ImageHelper.ImageError error) {
            System.out.println(error.getMessage());
        }
    }

    private class getCityData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST",
                    "getCities", null, null, true, new int[]{40000,40000});
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    String decompressed = Compression.decompress(Compression.decodeBase64(result));
                    JSONObject jObj = new JSONObject(decompressed);
                    System.out.println(jObj.toString());

                    MapHelper.getInstance().getCities().newFeatureCollection(jObj.toString());
                    if(localDb.getCities() == null) {
                        localDb.insertCities(jObj.toString());
                    } else {
                        localDb.updateCities(jObj.toString());
                    }

                    if(!calledFromSynchronize) {
                        for (Boolean next : dataLoaded) {
                            if (!next) {
                                dataLoaded.set(dataLoaded.indexOf(next), true);
                                break;
                            }
                        }

                        if(callbackHandler != null)
                            callbackHandler.sendEmptyMessage(10);
                    }

                    execDisableCitiesDownload();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class getAttributeData extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String[] parametre = {"attribute_download", "legend_download"};
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST",
                    "getAttributes", parametre, params, true, new int[]{40000,40000});
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals(Connect.REQUEST_RETURNED_ERROR)
                    && !result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                try {
                    String decompressed = Compression.decompress(Compression.decodeBase64(result));
                    JSONObject jObj = new JSONObject(decompressed);
                    System.out.println(jObj.toString());

                    if(needsAttributeDownload) {
                        JSONArray headers = jObj.getJSONArray("csv");
                        JSONArray attributes = jObj.getJSONArray("attributes");
                        localDb.insertAttributes(headers, attributes);

                        MapHelper.getInstance().setAttributeHeaders(localDb.getAttributeHeaders());

                        execDisableAttributesDownload();
                    }
                    if(needsLegendDownload) {
                        JSONArray legends = jObj.getJSONArray("legends");
                        localDb.insertLegends(legends);
                        execDisableLegendsDownload();
                    }

                    if(!calledFromSynchronize) {
                        for (Boolean next : dataLoaded) {
                            if (!next) {
                                dataLoaded.set(dataLoaded.indexOf(next), true);
                                break;
                            }
                        }

                        if(callbackHandler != null)
                            callbackHandler.sendEmptyMessage(10);
                    } else {
                        if(MapHelper.getInstance().getSelectedAttribute() != null) {
                            MapHelper.getInstance().execApplyAttribute(MapHelper.getInstance().getSelectedAttribute().Header().ID(), true);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            }
        }
    }

    private class deleteFeature extends AsyncTask<String, String, String> {

        private int id = -1;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            id = Integer.parseInt(params[0]);
            String[] parametre = {"id"};
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "removeItemAndCords", parametre, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(Connect.REQUEST_RETURNED_ERROR)
                    || result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            } else {
                MapHelper.getInstance().removeFeature(id,  true);
                callbackHandler.sendEmptyMessage(14);
            }
        }
    }

    private class addFeature extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String[] parametre = {"name", "type", "icon", "lng", "lat", "city_id"};
            return connect.makeRequest(getUrlPrefix()+serverPath,"POST", "addItemAndCords", parametre, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(Connect.REQUEST_RETURNED_ERROR)
                    || result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                if(callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            } else {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());

                    Feature feature = Feature.fromJson(jObj.toString());

                    MapHelper.getInstance().addFeature(feature, 3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class editFeature extends AsyncTask<String, String, String> {

        private int id = -1;
        private String name = "";
        private String icon = "";
        private String city_id = "";

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected String doInBackground(String... params) {
            id = Integer.parseInt(params[0]);
            name = params[1];
            icon = params[2];
            city_id = params[3];
            String[] parametre = {"id", "name", "icon", "city_id"};
            return connect.makeRequest(getUrlPrefix() + serverPath, "POST", "editItem", parametre, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(Connect.REQUEST_RETURNED_ERROR)
                    || result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                if (callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            } else {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());

                    Feature feature = Feature.fromJson(jObj.toString());
                    MapHelper.getInstance().editFeature(id, name, icon, city_id, feature.getNumberProperty("updated").longValue());

                    callbackHandler.sendEmptyMessage(7);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class editFeatureLocation extends AsyncTask<String, String, String> {

        private int id = -1;

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected String doInBackground(String... params) {
            id = Integer.parseInt(params[0]);
            String[] parametre = {"id", "lng", "lat"};
            return connect.makeRequest(getUrlPrefix() + serverPath, "POST", "editItemLocation", parametre, params, true);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(Connect.REQUEST_RETURNED_ERROR)
                    || result.equals(Connect.REQUEST_UNABLE_TO_EXEC)) {
                if (callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-1);
            } else {
                try {
                    JSONObject jObj = new JSONObject(result);
                    System.out.println(jObj.toString());

                    Feature feature = Feature.fromJson(jObj.toString());
                    MapHelper.getInstance().removeFeature(id,  false);
                    MapHelper.getInstance().addFeature(feature, 4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class updateLocalDatabase extends AsyncTask<String, Boolean, Boolean> {

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            if(!params[0].equals("points_only")) {
                if(localDb.updateAreas(MapHelper.getInstance().getAreas().featureCollection().toJson()) == 0) {
                    System.out.println("Cannot save areas.");
                    result = false;
                }
                if(localDb.updateStreets(MapHelper.getInstance().getStreets().featureCollection().toJson()) == 0) {
                    System.out.println("Cannot save streets.");
                    result = false;
                }
            }

            if(localDb.updateBoundSyncTimes(MapHelper.getInstance().getSyncHashMap()) == 0) {
                System.out.println("Cannot save bound sync times.");
                result = false;
            }
            if(localDb.updatePoints(MapHelper.getInstance().getPoints().featureCollection().toJson()) == 0) {
                System.out.println("Cannot save points.");
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result) {
                if (callbackHandler != null)
                    callbackHandler.sendEmptyMessage(-2);
            }
        }
    }
}
