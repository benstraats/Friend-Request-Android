package com.straats.ben.friendrequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;

public class FindFriends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        ScrollView sv = findViewById(R.id.scrollView);
        TableLayout mainListView = findViewById(R.id.mainList);
        EditText searchEditText = findViewById(R.id.SearchEditText);
        Button searchButton = findViewById(R.id.SearchButton);
        ProgressBar loadingBar = findViewById(R.id.searchLoadingBar);

        new SearchListHelper(this, sv, mainListView, searchEditText, searchButton, loadingBar);
    }

}
