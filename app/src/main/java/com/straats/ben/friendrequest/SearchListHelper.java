package com.straats.ben.friendrequest;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableLayout;

import java.util.ArrayList;

public class SearchListHelper {

    private Context c;
    private final TableLayout mainList;
    private EditText searchEditText;
    private Button searchButton;
    private ProgressBar progressBar;

    private int limit = 50;

    private ArrayList<CustomRow> rowList;

    public SearchListHelper(Context context, TableLayout list, EditText text, Button search, ProgressBar pgBar) {
        this.c = context;
        this.mainList = list;
        this.searchEditText = text;
        this.searchButton = search;
        this.progressBar = pgBar;

        rowList = new ArrayList<>();
    }

    private void showSearchLoading() {
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.INVISIBLE);
    }

    private void hideSearchLoading() {
        progressBar.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.VISIBLE);
    }

    private class CustomRow {
        //TODO: fill out
    }

}
