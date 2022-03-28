package com.example.geoapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.geoapp.attribute.AttributeHeader;
import com.example.geoapp.database.DatabaseCalls;
import com.example.geoapp.map.MapHelper;
import com.example.geoapp.misc.Helper;
import com.example.geoapp.search.SearchSuggestions;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.Style.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements LifecycleObserver {

    private enum MENUSTATE
    {
        DEFAULT,
        SEARCH,
        ADDING,
        SELECTED,
        HIDDEN
    }

    private final String jsonStyleLocation = "/mapboxStyle/style.json";

    private MapView mapView;
    private MapboxMap mapBoxMap;
    private Style savedStyle;
    private Menu menu;

    private DatabaseCalls dbCalls;
    private Helper helper;
    private MapHelper mapHelper;

    private MENUSTATE currentMenuState = MENUSTATE.DEFAULT;
    private SearchView searchView;
    private MenuItem delete;
    private MenuItem edit;
    private MenuItem editLocation;
    private MenuItem cancel;
    private MenuItem search;
    private MenuItem add;
    private MenuItem sync;
    private MenuItem settings;

    private ActivityResultLauncher<Intent> settingsActivityResultLauncher;

    private boolean isAdding = false;
    private boolean isEditing = false;
    private boolean iconClick = false;

    private boolean imagesInitialized = false;
    private boolean needFirstSync = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        registerSettingsActivityResultListener();

        dbCalls = DatabaseCalls.getInstance();
        dbCalls.SetupLocalDatabase(this);
        dbCalls.setCallbackHandler(mapActivityHandler);

        helper = Helper.Companion.getInstance();
        helper.setProgressDialogActive(false);
        helper.Setup(mapActivityHandler, MapActivity.this);

        mapHelper = MapHelper.getInstance();
        mapHelper.setupImages(MapActivity.this, mapActivityHandler); //create images first

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        loadMap(savedInstanceState);
    }

    public void loadMap(Bundle savedInstanceState) {
        final Intent intent = getIntent();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(new Builder().fromUri(dbCalls.getUrlPrefix()+jsonStyleLocation), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) { //Style.MAPBOX_STREETS new Builder().fromUri(jsonStyleLocation)
                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(intent.getDoubleExtra("lat", 48.99839), intent.getDoubleExtra("lng", 21.23393))) // Sets the new camera position
                                .zoom(intent.getDoubleExtra("zoom", 11)) // Sets the zoom
                                .build(); // Creates a CameraPosition from the builder

                        savedStyle = mapboxMap.getStyle();
                        mapBoxMap = mapboxMap;

                        mapHelper.Setup(savedStyle, mapBoxMap,MapActivity.this, mapActivityHandler);

                        mapHelper.setupAllSources();
                        mapHelper.deselectAllFeatures();

                        mapBoxMap.moveCamera(CameraUpdateFactory
                                .newCameraPosition(position));

                        //if images were loaded before sources were set up, we will sync now
                        if(needFirstSync) {
                            synchronize();
                        }

                        mapBoxMap.addOnMapClickListener(point -> {
                            handleOnMapClick(point);
                            return true;
                        });

                        mapBoxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
                            @Override
                            public void onMoveBegin(@NonNull MoveGestureDetector detector) {}
                            @Override
                            public void onMove(MoveGestureDetector detector) {}
                            @Override
                            public void onMoveEnd(MoveGestureDetector detector) {
                                if(imagesInitialized) {
                                    synchronize();
                                }
                            }
                        });

                        mapBoxMap.addOnFlingListener(() -> {
                            if(imagesInitialized) {
                                synchronize();
                            }
                        });
                    }
                });
            }
        });
    }

    private void onDownload() {
        setCurrentMenuState(MENUSTATE.DEFAULT);
        mapHelper.refreshAllSources();
        helper.setProgressDialogActive(false);
        Toast.makeText(MapActivity.this, "Dáta boli aktualizované.", Toast.LENGTH_LONG).show();
    }

    private void setupMenuRefsAndListeners() {
        delete = menu.findItem(R.id.action_delete);
        edit = menu.findItem(R.id.action_edit);
        editLocation = menu.findItem(R.id.action_edit_location);
        cancel = menu.findItem(R.id.action_cancel);
        search = menu.findItem(R.id.action_search);
        add = menu.findItem(R.id.action_add);
        sync = menu.findItem(R.id.action_sync);
        settings = menu.findItem(R.id.action_settings);
        searchView = (SearchView)search.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchView.onActionViewCollapsed();
                setCurrentMenuState(MENUSTATE.DEFAULT);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                setCurrentMenuState(MENUSTATE.DEFAULT);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        searchView.setOnCloseListener(() -> {
            setCurrentMenuState(MENUSTATE.DEFAULT);
            return false;
        });
        searchView.setOnSearchClickListener(view -> setCurrentMenuState(MENUSTATE.SEARCH));

        SearchSuggestions.SetCursorAdapter(searchView.getSuggestionsAdapter());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
        setupMenuRefsAndListeners();

        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }

        switch(currentMenuState) {
            case DEFAULT:
                searchView.onActionViewCollapsed();
                search.setVisible(true);
                add.setVisible(true);
                sync.setVisible(true);
                settings.setVisible(true);
                break;
            case SEARCH:
                search.setVisible(true);
                break;
            case ADDING:
                cancel.setVisible(true);
                break;
            case SELECTED:
                delete.setVisible(true);
                edit.setVisible(true);
                editLocation.setVisible(true);
                break;
        }

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                if(mapHelper.isSyncTaskRunning()) {
                    ShowSynchronizationRunningPopup();
                    return true;
                }

                if(mapHelper.getCurrentSquareBoundsIndex() != -1) {
                    setupSpecialMode(false);
                } else {
                    Toast.makeText(MapActivity.this, "Ste mimo dostupných hraníc.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_sync:
                setProgressBarVisible(true);
                synchronize();
                return true;
            case R.id.action_settings:
                Intent mapActivity = new Intent(MapActivity.this, SettingsActivity.class);
                settingsActivityResultLauncher.launch(mapActivity);
                return true;
            case R.id.action_search:
                super.onSearchRequested();
                return true;
            case R.id.action_cancel:
                if(isEditing) {
                    cancelSpecialMode(true);
                } else if (isAdding) {
                    cancelSpecialMode(false);
                }
                return true;
            case R.id.action_delete:
                if(mapHelper.isSyncTaskRunning()) {
                    ShowSynchronizationRunningPopup();
                    return true;
                }

                helper.instantiateConfirmDialog(
                        "Vymazanie",
                        "Ste si istý, že chcete vymazať tento bod?",
                        12,
                        13);
                setCurrentMenuState(MENUSTATE.HIDDEN);
                return true;
            case R.id.action_edit:
                if(mapHelper.isSyncTaskRunning()) {
                    ShowSynchronizationRunningPopup();
                    return true;
                }

                isEditing = true;
                Feature selected = mapHelper.getSelectedFeature();
                helper.setEditPointId(selected.getNumberProperty("id").intValue());
                helper.showEditDialog(selected.getStringProperty("name"), selected.getStringProperty("icon"));
                return true;
            case R.id.action_edit_location:
                if(mapHelper.isSyncTaskRunning()) {
                    ShowSynchronizationRunningPopup();
                    return true;
                }

                setupSpecialMode(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setCurrentMenuState(MENUSTATE menuState) {
        currentMenuState = menuState;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::invalidateOptionsMenu, 100);
    }

    private void registerSettingsActivityResultListener() {
        settingsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ShowUserIsFarFromCityPopupIfNeeded();
                    }
                });
    }

    private void synchronize() {
        if (!isAdding && !isEditing && !dbCalls.isSynchronizing() && !mapHelper.areAsyncTasksRunning()) {
            int boundsID = mapHelper.getCurrentSquareBoundsIndex();
            if(boundsID != -1) {
                dbCalls.synchronize(boundsID, mapHelper.getSyncHashMap().get(boundsID));
            } else {
                System.out.println("You are not in any known bound, synchronization aborted.");
            }
        }
    }

    private void handleOnMapClick(@NonNull LatLng point) {
        if(!isAdding && !isEditing) {
            PointF screenPoint = mapBoxMap.getProjection().toScreenLocation(point);
            List<Feature> features = mapBoxMap.queryRenderedFeatures(screenPoint, "1");

            if (!features.isEmpty() && !iconClick) {
                Feature selectedFeature = features.get(0);
                mapHelper.selectFeature(selectedFeature);
            }

            if(iconClick) iconClick = false;

            if (features.isEmpty()) {
                mapHelper.deselectFeatures();
                setCurrentMenuState(MENUSTATE.DEFAULT);
                setCenterLocationButtonVisible(false);
            }
        }
    }

    public void centerLocationIconClick(View v) {
        iconClick = true;
        mapHelper.animateCameraToSelection(mapHelper.getSelectedFeature());
    }

    public void editSaveLocationClick(View v) {
        final LatLng position = mapHelper.getCurrentCameraPosition();
        if(isAdding) {
            if(mapHelper.getCurrentSquareBoundsIndex() != -1) {
                helper.setAddingLatLng(position);
                helper.showAddDialog();
            } else {
                Toast.makeText(MapActivity.this, "Ste mimo dostupných hraníc.", Toast.LENGTH_LONG).show();
            }
        } else if(isEditing) {
            if(dbCalls.isSynchronizing() || mapHelper.isSyncTaskRunning()) {
                ShowSynchronizationRunningPopup();
                return;
            }

            dbCalls.execEditLocation(mapHelper.getSelectedFeature().getNumberProperty("id").intValue() + "", position.getLongitude() + "", position.getLatitude() + "");
        }
    }

    private void setCenterLocationButtonVisible(boolean visible) {
        if(visible){
            findViewById(R.id.actionAvailable_container).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.actionAvailable_container).setVisibility(View.INVISIBLE);
        }
    }

    private void setProgressBarVisible(boolean visible) {
        if(visible){
            findViewById(R.id.map_progress).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.map_progress).setVisibility(View.INVISIBLE);
        }
    }

    private void setSaveLocationButtonVisible(boolean visible) {
        if(visible){
            findViewById(R.id.saveLocationButton).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.saveLocationButton).setVisibility(View.INVISIBLE);
        }
    }

    private void addPoint() {
        if(dbCalls.isSynchronizing() || mapHelper.isSyncTaskRunning()) {
            ShowSynchronizationRunningPopup();
            return;
        }

        dbCalls.execAdd(
                helper.getEditTextDialogInput(),
                "Point",
                helper.getSelectedIcon(),
                helper.getAddingLatLng().getLongitude()+"",
                helper.getAddingLatLng().getLatitude()+"",
                "-1");
    }

    private void editPoint() {
        if(dbCalls.isSynchronizing() || mapHelper.isSyncTaskRunning()) {
            ShowSynchronizationRunningPopup();
            return;
        }

        dbCalls.execEdit(
                helper.getEditPointId()+"",
                helper.getEditTextDialogInput(),
                helper.getSelectedIcon(),
                "-1");
    }

    private void setupSpecialMode(boolean isEdit) {
        setSaveLocationButtonVisible(true);
        mapHelper.hideAllLayers();
        findViewById(R.id.editMarker).setVisibility(View.VISIBLE);
        setCurrentMenuState(MENUSTATE.ADDING);

        if(isEdit) {
            isEditing = true;
            mapHelper.moveCameraToSelection(mapHelper.getSelectedFeature());
            mapHelper.showEditLocationBounds();
        } else {
            isAdding = true;
        }
    }

    private void cancelSpecialMode(boolean isEdit) {
        setSaveLocationButtonVisible(false);
        mapHelper.unhideAllLayers();
        findViewById(R.id.editMarker).setVisibility(View.INVISIBLE);
        synchronize();

        if(mapHelper.getSelectedFeature() != null) {
            setCurrentMenuState(MENUSTATE.SELECTED);
        } else {
            setCurrentMenuState(MENUSTATE.DEFAULT);
        }

        if(isEdit) {
            isEditing = false;
            mapHelper.moveCameraToSelection(mapHelper.getSelectedFeature());
            mapHelper.hideEditLocationBounds();
        } else {
            isAdding = false;
        }
    }

    private void handleSelectedFeatureUpdated() {
        if(isEditing) {
            setSaveLocationButtonVisible(false);
            mapHelper.unhideAllLayers();
            findViewById(R.id.editMarker).setVisibility(View.INVISIBLE);

            if (mapHelper.getSelectedFeature() != null) {
                setCurrentMenuState(MENUSTATE.SELECTED);
                mapHelper.moveCameraToSelection(mapHelper.getSelectedFeature());
                mapHelper.hideEditLocationBounds();
            } else {
                setCurrentMenuState(MENUSTATE.DEFAULT);
            }

            isEditing = false;
        }
    }

    private void ShowSynchronizationRunningPopup() {
        helper.instantiateInfoDialog(MapActivity.this,"Synchronizácia",
                "Vaše zariadenie sa momentálne synchronizuje so serverom, počkajte prosím.", -1, true);
    }

    private void ShowUserIsFarFromCityPopupIfNeeded() {
        if(mapHelper.getSelectedAttribute() != null && !mapHelper.isNearCityWithAttributes()) {
            AttributeHeader header = mapHelper.getSelectedAttribute().Header();
            if(header != null)
            {
                Feature city = mapHelper.getSelectedAttribute().City();
                String city_name = city != null ? " - " + city.getStringProperty("name") : "";
                helper.instantiateConfirmDialog("Atribút",
                        "V oblasti v ktorej sa momentálne nachádzate nemáme žiadne dáta o atribúte " +
                                header.Name() + city_name + ", želáte si presun na príslušnú oblasť?",
                        18, -1);
            }
        }
    }

    Handler mapActivityHandler = new Handler() {

        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -2:
                    Toast.makeText(MapActivity.this, "Problém s lokálnou databázou.", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    Toast.makeText(MapActivity.this, "Problém so sieťou.", Toast.LENGTH_LONG).show();
                    setCurrentMenuState(MENUSTATE.DEFAULT);
                    break;
                case 0: //map images are loaded
                    if(mapHelper.areSourcesInitialized()) {
                        synchronize(); //first sync
                    } else {
                        needFirstSync = true;
                    }
                    helper.createAllDialogs();
                    imagesInitialized = true;
                    break;
                case 1: //add point name entered
                    addPoint();
                    break;
                case 2: //edit point name entered
                    editPoint();
                    break;
                case 3: //feature added
                    cancelSpecialMode(false);
                    break;
                case 4: //feature edit location saved
                    cancelSpecialMode(true);
                    break;
                case 5: //selected feature was deleted
                    setCenterLocationButtonVisible(false);
                    setCurrentMenuState(MENUSTATE.DEFAULT);
                    handleSelectedFeatureUpdated();
                    Toast.makeText(MapActivity.this, "Výber bol vymazaný.", Toast.LENGTH_LONG).show();
                    break;
                case 6: //selected feature was edited
                    setCenterLocationButtonVisible(false);
                    handleSelectedFeatureUpdated();
                    Toast.makeText(MapActivity.this, "Výber bol upravený.", Toast.LENGTH_LONG).show();
                    break;
                case 7: //user closed edit dialog
                    isEditing = false;
                    break;
                case 8: //user has opened dialog and icons changed
                    Toast.makeText(MapActivity.this, "Ikony boli aktualizované\nOtvorte dialóg znovu", Toast.LENGTH_LONG).show();
                    break;
                case 9: //user has clicked ok on info dialog
                    helper.setProgressDialogText("Sťahovanie dát...");
                    helper.setProgressDialogActive(true);
                    dbCalls.getAllData();
                    break;
                case 10: //something was downloaded
                    helper.appendProgress((int)((1.0f/(float)DatabaseCalls.getInstance().NUMBER_OF_COLLECTIONS)*100));
                    if(DatabaseCalls.getInstance().isEverythingLoaded()){
                        helper.setProgressDialogActive(false);
                        mapHelper.refreshImages(() -> onDownload());
                        DatabaseCalls.getInstance().execDisableForceDownload();
                    }
                    break;
                case 11: //download is needed
                    setCurrentMenuState(MENUSTATE.HIDDEN);
                    mapHelper.deselectFeatures();
                    setCenterLocationButtonVisible(false);
                    helper.instantiateInfoDialog(MapActivity.this,"Dáta", "Vyžaduje sa aktualizácia dát.", 9, false);
                    break;
                case 12: //confirm dialog - delete clicked ok
                    int id = mapHelper.getSelectedFeature().getNumberProperty("id").intValue();
                    dbCalls.execDelete(id+"");
                    break;
                case 13: //confirm dialog - delete clicked cancel
                    setCurrentMenuState(MENUSTATE.SELECTED);
                    break;
                case 14: //feature was deleted
                    setCurrentMenuState(MENUSTATE.DEFAULT);
                    setCenterLocationButtonVisible(false);
                    break;
                case 15: //Feature was selected
                    if(mapHelper.isSelectedFeatureCreatedByUser()) {
                        setCurrentMenuState(MENUSTATE.SELECTED);
                    } else {
                        setCurrentMenuState(MENUSTATE.DEFAULT);
                    }
                    setCenterLocationButtonVisible(true);
                    break;
                case 16: //Attributes were updated
                    Toast.makeText(MapActivity.this, "Atribúty boli aktualizované.", Toast.LENGTH_LONG).show();
                    ShowUserIsFarFromCityPopupIfNeeded();
                    break;
                case 17: //User action was conflicting with synchronization
                    ShowSynchronizationRunningPopup();
                    break;
                case 18: //User clicked ok on confirm dialog - Set position near city with attributes
                    MapHelper.getInstance().animateCameraToSelection(MapHelper.getInstance().getSelectedAttribute().City(), 14);
                    break;
                case 19: //Synchronization completed
                    setProgressBarVisible(false);
                    break;
                default:
                    break;
            }
        }
    };

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        DatabaseCalls.getInstance().execUpdateLocalDb("false");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() { }

    @Override
    public void onBackPressed() {
        if(isAdding) {
            cancelSpecialMode(false);
        } else if (isEditing) {
            cancelSpecialMode(true);
        } else if(mapHelper.getSelectedFeature() != null) {
            mapHelper.deselectFeatures();
            setCurrentMenuState(MENUSTATE.DEFAULT);
            setCenterLocationButtonVisible(false);
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
