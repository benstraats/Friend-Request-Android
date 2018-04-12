package com.straats.ben.friendrequest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewProfile extends AppCompatActivity {

    private TextView profileText;
    private ProgressBar loadingBar;
    private String friendID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profileText = findViewById(R.id.profileText);
        loadingBar = findViewById(R.id.loadingBar);

        String friendUserID = getIntent().getExtras().getString("friendUserID");
        friendID = getIntent().getExtras().getString("friendID");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setTitle(R.string.view_profile_activity_delete_title);
                builder.setMessage(R.string.view_profile_activity_delete_message);

                builder.setPositiveButton(R.string.view_profile_activity_delete_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFriend(friendID);
                    }
                }).setNegativeButton(R.string.view_profile_activity_delete_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });

                builder.show();
            }
        });
        fab.setImageResource(android.R.drawable.ic_delete);

        getProfile(friendUserID);
    }

    private void getProfile(final String friendUserID) {
        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());
        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideLoading();
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
                    profileText.setText(R.string.view_profile_activity_load_empty);
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                hideLoading();
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = vw.profileURL + "?userID=" + friendUserID;

        showLoading();
        vw.request(getApplication(), url, vw.viewProfileTAG,
                Request.Method.GET, null, callback);
    }

    private void deleteFriend(final String friendID) {
        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());
        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getApplicationContext(), R.string.view_profile_activity_delete_success,
                        Toast.LENGTH_SHORT).show();
                //TODO: transport back to landing activity
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = vw.friendsURL + "/" + friendID;

        vw.request(getApplication(), url, vw.viewProfileTAG,
                Request.Method.DELETE, null, callback);
    }

    private void showLoading() {
        loadingBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingBar.setVisibility(View.INVISIBLE);
    }
}
