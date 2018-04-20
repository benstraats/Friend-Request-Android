package com.straats.ben.friendrequest;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TableLayout;

import java.util.ArrayList;

public class SearchListHelper {

    private Context c;
    private final TableLayout mainList;
    private ProgressBar progressBar;

    private int limit = 50;

    private ArrayList<CustomRow> rowList;

    public SearchListHelper(Context context, TableLayout list, ProgressBar pgBar) {
        this.c = context;
        this.mainList = list;
        this.progressBar = pgBar;

        rowList = new ArrayList<>();
    }

    private class CustomRow {
        //TODO: fill out
    }

}
