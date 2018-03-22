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

    private Button searchButton;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mainListView = findViewById(R.id.SearchList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);
        populateList();

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Sent friend request to user " + position, Toast.LENGTH_LONG).show();
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

                    JSONArray searchedUsers = response.getJSONArray("data");

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

        String url = Utils.usersURL + "?search=" + search;

        Utils.volleyRequest(getApplication(), url, Utils.searchUsersTAG,
                Request.Method.GET, null, callback);
    }

    private void populateList() {
        for (int i=0; i<50; i++) {
            users.add("Searched User " + i);
        }
        adapter.notifyDataSetChanged();
    }
}
