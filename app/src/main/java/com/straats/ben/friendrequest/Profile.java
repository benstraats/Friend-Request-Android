package com.straats.ben.friendrequest;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Profile extends AppCompatActivity {

    Button addRowButton;
    TableLayout mainTable;
    TableRow firstRow;

    boolean creatingProfile = true;
    String profileID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            saveProfile(view);
            }
        });

        fab.setImageResource(android.R.drawable.ic_menu_save);

        addRowButton = findViewById(R.id.addRowButton);
        mainTable = findViewById(R.id.profileTable);

        addRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow("","");
            }
        });

        getProfile(Utils.userID);
    }

    private void getProfile(final String currUserID) {
        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray("data");
                    JSONArray profile = data.getJSONObject(0).getJSONArray("profile");

                    //This assumes rows are in order
                    for (int i=0; i<profile.length(); i++) {
                        String key = profile.getJSONObject(i).getString("key");
                        String value = profile.getJSONObject(i).getString("value");

                        addRow(key, value);
                    }

                    creatingProfile = false;
                    profileID = data.getJSONObject(0).getString("_id");

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "No profile found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error),
                        Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.profileURL + "?userID=" + currUserID;

        Utils.volleyRequest(getApplication(), url, Utils.viewProfileTAG,
                Request.Method.GET, null, callback);
    }

    private void saveProfile(final View v) {

        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {

                //get the profile id
                try {
                    profileID = response.getString("_id");

                    Snackbar.make(v, "Saved your profile", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                    creatingProfile = false;
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Failed to save profile",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error),
                        Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.profileURL;
        int method = Request.Method.POST;

        if (!creatingProfile) {
            url = url + "/" + profileID;
            method = Request.Method.PUT;
        }

        Utils.volleyRequest(getApplication(), url, Utils.saveProfileTAG, method,
                getProfileJSON(), callback);
    }

    private void addRow(String key, String value) {

        TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.profile_row, null);

        ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);
        LinearLayout ll = (LinearLayout) cl.getChildAt(0);

        EditText keyText = (EditText) ll.getChildAt(0);
        EditText valueText = (EditText) ll.getChildAt(1);
        ImageButton editRowButton = (ImageButton) ll.getChildAt(2);

        keyText.setText(key);
        valueText.setText(value);

        Button deleteRow = (Button) cl.getChildAt(1);

        int numRows = mainTable.getChildCount();

        deleteRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View newV = (View) v.getParent().getParent();
                mainTable.removeView(newV);
            }
        });

        editRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstraintLayout cl = (ConstraintLayout) v.getParent().getParent();

                Button deleteButton = (Button) cl.getChildAt(1);
                CheckBox publicCheckbox = (CheckBox) cl.getChildAt(2);

                if (deleteButton.getVisibility() == View.VISIBLE) {
                    deleteButton.setVisibility(View.GONE);
                    publicCheckbox.setVisibility(View.GONE);
                } else {
                    deleteButton.setVisibility(View.VISIBLE);
                    publicCheckbox.setVisibility(View.VISIBLE);
                }
            }
        });

        mainTable.addView(row, numRows-1);
    }

    private void setRow(int rowIndex, String key, String value) {
        TableRow row = (TableRow) mainTable.getChildAt(rowIndex);

        ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);
        LinearLayout ll = (LinearLayout) cl.getChildAt(0);

        EditText keyText = (EditText) ll.getChildAt(0);
        keyText.setText(key);

        EditText valueText = (EditText) ll.getChildAt(1);
        valueText.setText(value);
    }

    private JSONObject getProfileJSON() {
        JSONObject body = new JSONObject();

        for (int i=0; i<mainTable.getChildCount()-1; i++) {
            TableRow row = (TableRow) mainTable.getChildAt(i);

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);
            LinearLayout ll = (LinearLayout) cl.getChildAt(0);

            EditText keyText = (EditText) ll.getChildAt(0);
            String keyString = keyText.getText().toString();

            EditText valueText = (EditText) ll.getChildAt(1);
            String valueString = valueText.getText().toString();
            try {
                if (i==0) {
                    JSONArray rowArray = new JSONArray();
                    JSONObject jsonRow = new JSONObject();
                    jsonRow.put("row", i);
                    jsonRow.put("key", keyString);
                    jsonRow.put("value", valueString);
                    rowArray.put(jsonRow);
                    body.put("profile", rowArray);
                } else {
                    JSONObject jsonRow = new JSONObject();
                    jsonRow.put("row", i);
                    jsonRow.put("key", keyString);
                    jsonRow.put("value", valueString);
                    body.accumulate("profile", jsonRow);
                }
            } catch(JSONException e) {
                Toast.makeText(getApplicationContext(), "Failed to parse profile",
                        Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return body;
    }
}
