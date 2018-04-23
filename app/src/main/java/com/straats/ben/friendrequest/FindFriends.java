package com.straats.ben.friendrequest;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FindFriends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        TableLayout mainListView = findViewById(R.id.mainList);
        EditText searchEditText = findViewById(R.id.SearchEditText);
        Button searchButton = findViewById(R.id.SearchButton);
        ProgressBar loadingBar = findViewById(R.id.searchLoadingBar);

        new SearchListHelper(this, mainListView, searchEditText, searchButton, loadingBar);
    }

}
