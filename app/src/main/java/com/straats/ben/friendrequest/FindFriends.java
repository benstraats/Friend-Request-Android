package com.straats.ben.friendrequest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FindFriends extends AppCompatActivity {

    private ListView mainListView;

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private JSONArray searchedUsers;

    private Button searchButton;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mainListView = findViewById(R.id.SearchList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String userEmail = searchedUsers.getJSONObject(position).getString("email");
                    requestUser(userEmail);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Request",Toast.LENGTH_LONG).show();
                }
            }
        });

        searchButton = findViewById(R.id.SearchButton);
        searchText = findViewById(R.id.SearchEditText);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUsers(searchText.getText().toString());
            }
        });
    }

    private void searchUsers(final String search) {

        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    users.clear();

                    int total = Integer.parseInt(response.getString("total"));
                    int limit = Integer.parseInt(response.getString("limit"));

                    int num = Math.min(total, limit);

                    searchedUsers = response.getJSONArray("data");

                    for (int i=0; i<num; i++) {
                        users.add(searchedUsers.getJSONObject(i).getString("name") + " (" + searchedUsers.getJSONObject(i).getString("email") + ")");
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Response",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Failed to get searched users",Toast.LENGTH_LONG).show();
            }
        };

        String url = Utils.usersURL + "?$search=" + search + "&$limit=50";

        Utils.volleyRequest(getApplication(), url, Utils.searchUsersTAG,
                Request.Method.GET, null, callback);
    }

    private void requestUser(final String userEmail) {

        Utils.VolleyCallback callback = new Utils.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Successfully added user",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_LONG).show();
            }
        };

        String url = Utils.requestsURL;
        final HashMap<String, String> body = new HashMap<>();
        body.put("requestee", userEmail);

        Utils.volleyRequest(getApplication(), url, Utils.requestUserTAG,
                Request.Method.POST, body, callback);
    }
}
