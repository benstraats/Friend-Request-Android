package com.straats.ben.friendrequest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Profile extends AppCompatActivity {

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
                //TODO: send correct params
            saveProfile(null, null, true, view);
            }
        });

        fab.setImageResource(android.R.drawable.ic_menu_save);

        getProfile(Utils.userID);
    }

    private void getProfile(final String currUserID) {
        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                //TODO: Parse the profile here
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.profileURL + "?userID=" + currUserID;

        Utils.volleyRequest(getApplication(), url, Utils.viewProfileTAG,
                Request.Method.GET, null, callback);
    }

    private void saveProfile(final String profileID, final HashMap<String, String> profile, final boolean creating, final View v) {
        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Snackbar.make(v, "Saved your profile", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.profileURL;
        int method = Request.Method.POST;

        if (!creating) {
            url = url + "/" + profileID;
            method = Request.Method.PUT;
        }

        Utils.volleyRequest(getApplication(), url, Utils.saveProfileTAG,
                method, profile, callback);
    }
}
