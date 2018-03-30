package com.straats.ben.friendrequest;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewProfile extends AppCompatActivity {

    TextView profileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

        fab.setVisibility(View.INVISIBLE);

        profileText = findViewById(R.id.profileText);

        String friendID = getIntent().getExtras().getString("friendID");

        getProfile(friendID);
    }

    private void getProfile(final String friendID) {
        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String text = "";

                    JSONArray profile = response.getJSONArray("data").getJSONObject(0)
                            .getJSONArray("profile");

                    //This assumes rows are in order
                    for (int i=0; i<profile.length(); i++) {
                        String key = profile.getJSONObject(i).getString("key");
                        String value = profile.getJSONObject(i).getString("value");
                        text = text + key + ": " + value + "\n";
                    }

                    profileText.setText(text);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Bad Response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.profileURL + "?userID=" + friendID;

        Utils.volleyRequest(getApplication(), url, Utils.viewProfileTAG,
                Request.Method.GET, null, callback);
    }
}
