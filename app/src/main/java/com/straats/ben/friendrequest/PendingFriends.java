package com.straats.ben.friendrequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PendingFriends extends AppCompatActivity {

    private ListView mainListView;
    private ProgressBar searchLoadingBar;

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private JSONArray requestedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_friends);

        mainListView = findViewById(R.id.PendingList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);

        searchLoadingBar = findViewById(R.id.searchLoadingBar);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                acceptRequest(position);
            }
        });

        getRequests();
    }

    private void getRequests() {

        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());
        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideSearchLoading();
                try {
                    users.clear();

                    int total = Integer.parseInt(response.getString("total"));
                    int limit = Integer.parseInt(response.getString("limit"));

                    int num = Math.min(total, limit);

                    requestedUsers = response.getJSONArray("data");
                    JSONObject userInfo = response.getJSONObject("userInfo");

                    for (int i=0; i<num; i++) {
                        users.add(Utils.getUserInfo(requestedUsers.getJSONObject(i).getString("requester"), userInfo));
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Response",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                hideSearchLoading();
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = vw.requestsURL + "?requestee=" + Utils.userID + "&$limit=49";

        showSearchLoading();
        vw.request(getApplication(), url, vw.getRequestsTAG,
                Request.Method.GET, null, callback);
    }

    private void acceptRequest(int position) {

        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());
        String requestID;
        try {
            requestID = requestedUsers.getJSONObject(position).getString("_id");
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Failed to parse requested user",Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalRequestID = requestID;

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Successfully added user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = vw.friendsURL;
        JSONObject body = new JSONObject();
        try {
            body.put("requestID", finalRequestID);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to sign up",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        vw.request(getApplication(), url, vw.acceptRequestTAG,
                Request.Method.POST, body, callback);
    }

    private void showSearchLoading() {
        searchLoadingBar.setVisibility(View.VISIBLE);
    }

    private void hideSearchLoading() {
        searchLoadingBar.setVisibility(View.INVISIBLE);
    }
}
