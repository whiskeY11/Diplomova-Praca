package com.example.geoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geoapp.attribute.AttributeHeader;
import com.example.geoapp.database.DatabaseCalls;
import com.example.geoapp.database.LocalDatabase;
import com.example.geoapp.map.MapHelper;
import com.example.geoapp.misc.Helper;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final double lat = 48.99839;
    private final double lng = 21.23393;
    private final double zoom = 8;

    private LocalDatabase localDb;
    private DatabaseCalls dbCalls;

    private boolean partialDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createSingletonInstances();

        dbCalls = DatabaseCalls.getInstance();

        dbCalls.SetupLocalDatabase(this);
        localDb = dbCalls.getLocalDb();

        DatabaseCalls.getInstance().setCallbackHandler(mainHandler);

        Helper.Companion.getInstance().Setup(null, this);
        Helper.Companion.getInstance().createProgressDialog("Sťahovanie dát...");
        findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
    }

    private void createSingletonInstances() {
        if(!DatabaseCalls.createInstance()){
            System.out.println("Cannot create dbCalls Instance.");
        }
        if(!MapHelper.createInstance()) {
            System.out.println("Cannot create mapHelper Instance.");
        }
        if(!Helper.createInstance()) {
            System.out.println("Cannot create helper Instance.");
        }
    }

    private void onLoad() {
        Toast.makeText(MainActivity.this, "Prihlásenie úspešné!", Toast.LENGTH_LONG).show();
        Intent mapActivity = new Intent(MainActivity.this, MapActivity.class);
        mapActivity.putExtra("lat", lat);
        mapActivity.putExtra("lng", lng);
        mapActivity.putExtra("zoom", zoom);
        startActivity(mapActivity);
    }

    private void onCheckUserParamsResponse(boolean download) {
        findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
        if(download) {
            downloadAllData();
        } else {
            String points = localDb.getPoints();
            String streets = localDb.getStreets();
            String areas = localDb.getAreas();
            String cities = localDb.getCities();
            HashMap<Integer, LatLngBounds> boundsHashMap = localDb.getBounds();
            HashMap<Integer, Long> syncHashMap = localDb.getBoundsLastSyncTime();
            ArrayList<AttributeHeader> atributeHeaders = localDb.getAttributeHeaders();

            if(points == null || streets == null || areas == null || cities == null ||
                    boundsHashMap.isEmpty() || syncHashMap.isEmpty() || atributeHeaders.isEmpty()) {
                downloadAllData();
            } else {
                MapHelper.getInstance().setBoundsHashMap(boundsHashMap);
                MapHelper.getInstance().setSyncHashMap(syncHashMap);
                MapHelper.getInstance().setAttributeHeaders(atributeHeaders);
                MapHelper.getInstance().getPoints().newFeatureCollection(points);
                MapHelper.getInstance().getStreets().newFeatureCollection(streets);
                MapHelper.getInstance().getAreas().newFeatureCollection(areas);
                MapHelper.getInstance().getCities().newFeatureCollection(cities);

                onLoad();
            }
        }
    }

    private void onUserNeedsPartialDownload() {
        partialDownload = true;
        findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
        Objects.requireNonNull(Helper.Companion.getInstance()).setProgressDialogActive(true);
        DatabaseCalls.getInstance().getNeededData();
    }

    private void downloadAllData() {
        Objects.requireNonNull(Helper.Companion.getInstance()).setProgressDialogText("Sťahovanie dát...");
        Helper.Companion.getInstance().setProgressDialogActive(true);
        DatabaseCalls.getInstance().getAllData();
    }

    private void onLoginSuccessful() {
        DatabaseCalls.getInstance().execCheckUserParams();
    }

    public void onLoginClick(View v) {
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        EditText email = findViewById(R.id.emailLogin);
        EditText password = findViewById(R.id.passwordLogin);
        DatabaseCalls.getInstance().setCallbackHandler(mainHandler);
        DatabaseCalls.getInstance().execLogin(email.getText().toString(), password.getText().toString());
    }

    public void onRegisterClick(View v) {
        Intent mapActivity = new Intent(MainActivity.this, RegisterActivity.class);
        startActivityForResult(mapActivity, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(MainActivity.this, "Registrácia bola úspešná!", Toast.LENGTH_LONG).show();
            }
        }
    }

    Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -2:
                    findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    findViewById(R.id.login_progress).setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Problém so sieťou.", Toast.LENGTH_LONG).show();
                    break;
                case 3: //LOGIN
                    onLoginSuccessful();
                    break;
                case 4: //Needs download
                    onCheckUserParamsResponse(true);
                    break;
                case 5: //doesnt need download
                    onCheckUserParamsResponse(false);
                    break;
                case 6: //Needs partial download
                    onUserNeedsPartialDownload();
                    break;
                case 10: //something was downloaded
                    if(!partialDownload) {
                        Helper.Companion.getInstance().appendProgress((int)((1.0f/(float)DatabaseCalls.getInstance().NUMBER_OF_COLLECTIONS)*100));
                        if(DatabaseCalls.getInstance().isEverythingLoaded()) {
                            onLoad();
                            DatabaseCalls.getInstance().execDisableForceDownload();
                        }
                    } else {
                        Helper.Companion.getInstance().appendProgress((int)((1.0f/(float)DatabaseCalls.getInstance().NUMBER_OF_COLLECTIONS_NEEDED)*100));
                        if(DatabaseCalls.getInstance().isEverythingLoaded()) {
                            Log.i("download", "Download finished");
                            partialDownload = false;
                            DatabaseCalls.getInstance().ResetNumberOfNeededCollections();
                            onCheckUserParamsResponse(false);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
