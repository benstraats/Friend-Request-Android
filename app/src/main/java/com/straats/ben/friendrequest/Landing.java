package com.straats.ben.friendrequest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Landing extends AppCompatActivity {

    private LandingListHelper listHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FindFriends.class);
                startActivity(intent);
            }
        });
        fab.setImageResource(android.R.drawable.ic_menu_search);

        Button myProfileButton = findViewById(R.id.myProfileButton);

        myProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
            }
        });

        TableLayout mainList = findViewById(R.id.mainTable);
        ScrollView listScroll = findViewById(R.id.listScroll);
        ProgressBar loadingBar = findViewById(R.id.loadingBar);

        listHelper = new LandingListHelper(this, mainList, listScroll, loadingBar);
    }

    @Override
    public void onResume() {
        super.onResume();
        listHelper.initialLoad();
    }

}
