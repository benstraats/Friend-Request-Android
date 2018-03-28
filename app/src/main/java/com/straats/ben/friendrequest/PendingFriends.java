package com.straats.ben.friendrequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PendingFriends extends AppCompatActivity {

    private ListView mainListView;

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

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                acceptRequest(position);
            }
        });

        getRequests();
    }

    private void getRequests() {

        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
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
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.requestsURL + "?requestee=" + Utils.userID + "&$limit=49";

        Utils.volleyRequest(getApplication(), url, Utils.getRequestsTAG,
                Request.Method.GET, null, callback);
    }

    private void acceptRequest(int position) {
        String requestID;
        try {
            requestID = requestedUsers.getJSONObject(position).getString("_id");
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Failed to parse requested user",Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalRequestID = requestID;

        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Successfully added user", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = Utils.friendsURL;
        final HashMap<String, String> body = new HashMap<>();
        body.put("requestID", finalRequestID);

        Utils.volleyRequest(getApplication(), url, Utils.acceptRequestTAG,
                Request.Method.POST, body, callback);
    }
}
