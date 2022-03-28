package com.example.geoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geoapp.attribute.AttributeHeader;
import com.example.geoapp.attribute.AttributeValues;
import com.example.geoapp.map.Cities;
import com.example.geoapp.map.MapHelper;
import com.example.geoapp.misc.Helper;
import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private ArrayList<Map<String, Object>> rData = new ArrayList<>();

    private boolean attributeChanged = false;
    private boolean firstChange = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        MapHelper.getInstance().SetSettingsValues(mainHandler, SettingsActivity.this);
        SetupAll();
    }

    private void SetupAll() {
        setupSpinner();
        setupLegend();
    }

    @SuppressLint("SetTextI18n")
    private void setupLegend() {
        AttributeValues selectedAttribute = MapHelper.getInstance().getSelectedAttribute();
        if(selectedAttribute != null) {
            ArrayList<String> colors = selectedAttribute.Legend().Colors();
            ArrayList<View> colorViews = new ArrayList<>();

            colorViews.add(findViewById(R.id.color0));
            colorViews.add(findViewById(R.id.color1));
            colorViews.add(findViewById(R.id.color2));
            colorViews.add(findViewById(R.id.color3));
            colorViews.add(findViewById(R.id.color4));
            colorViews.add(findViewById(R.id.color5));
            colorViews.add(findViewById(R.id.color6));
            colorViews.add(findViewById(R.id.color7));
            colorViews.add(findViewById(R.id.color8));
            colorViews.add(findViewById(R.id.color9));

            for (int i = 0; i < colorViews.size(); i++) {
                colorViews.get(i).setBackgroundColor(Color.parseColor(colors.get(i)));
            }

            ((TextView) findViewById(R.id.legendHeaderValues)).setText(": Min. 0, Max. "+selectedAttribute.MaxValue());

            findViewById(R.id.legendParentOfRows).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.legendParentOfRows).setVisibility(View.INVISIBLE);
        }
    }

    private void setupSpinner() {
        parseAttributeHeaders();

        SimpleAdapter arrayAdapter = new SimpleAdapter(this, rData,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"name"}, new int[]{android.R.id.text1});
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinner = findViewById(R.id.attributeSpinner);
        spinner.setAdapter(arrayAdapter);
        setSpinnerSelection(spinner);

        attributeChanged = false;

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!firstChange) {
                    MapHelper.getInstance().execApplyAttribute(attributeIndex(spinner.getSelectedItemPosition()), false);
                    attributeChanged = true;
                    findViewById(R.id.attribute_progress).setVisibility(View.VISIBLE);
                } else {
                    firstChange = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }

        });
    }

    private void parseAttributeHeaders() {
        rData.clear();

        Map<String, Object> none = new HashMap<>();
        none.put("id", -1);
        none.put("name", "Žiadna");
        rData.add(none);

        for (AttributeHeader header : MapHelper.getInstance().getAttributeHeaders()) {
            Feature city = ((Cities) MapHelper.getInstance().getCities()).citySuggestion(header.CityID());
            Map<String, Object> item = new HashMap<>();
            item.put("id", header.ID());
            item.put("name", city != null ? header.Name()+" - "+city.getStringProperty("name") : header.Name());
            rData.add(item);
        }
    }

    private void setSpinnerSelection(Spinner spinner) {
        int index = 0;
        if(MapHelper.getInstance().getSelectedAttribute() != null) {
            for (Map<String, Object> item : rData) {
                if ((int) Objects.requireNonNull(item.get("id")) == MapHelper.getInstance().getSelectedAttribute().Header().ID()) {
                    spinner.setSelection(index);
                    break;
                }
                index++;
            }
        }
    }

    private int attributeIndex(int position) {
        Map<String, Object> selectedItem = rData.get(position);
        return (int) Objects.requireNonNull(selectedItem.get("id"));
    }

    public void returnToMapActivity(View v) {
        if(attributeChanged) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        MapHelper.getInstance().SetSettingsValues(null, null);
        finish();
    }

    Handler mainHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //Selected attribute was changed - with toast
                    Toast.makeText(SettingsActivity.this, "Výbraný atribút bol upravený.", Toast.LENGTH_LONG).show();
                    firstChange = true;
                    SetupAll();
                    break;
                case 2: //Selected attribute was changed - without toast
                    firstChange = true;
                    SetupAll();
                    break;
                case 3: //User action was conflicting with synchronization
                    findViewById(R.id.attribute_progress).setVisibility(View.INVISIBLE);
                    Helper.Companion.getInstance().instantiateInfoDialog(SettingsActivity.this,"Synchronizácia",
                            "Vaše zariadenie sa momentálne synchronizuje so serverom, počkajte prosím.", -1, true);
                    break;
                case 4: //Attribute is applied
                    findViewById(R.id.attribute_progress).setVisibility(View.INVISIBLE);
                    setupLegend();
                    break;
                default:
                    break;
            }
        }
    };
}