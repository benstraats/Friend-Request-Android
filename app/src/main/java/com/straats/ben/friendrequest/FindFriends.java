package com.straats.ben.friendrequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FindFriends extends AppCompatActivity {

    private ListView mainListView;

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private JSONArray searchedUsers;

    private Button searchButton;
    private EditText searchText;
    private ProgressBar searchLoadingBar;

    private boolean doneCurrentSearch = true;
    private boolean currentlySearching = false;
    private int currentSearchSkip = 0;
    private int currentSearchLimit = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mainListView = findViewById(R.id.SearchList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String userID = searchedUsers.getJSONObject(position).getString("_id");
                    requestUser(userID);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Response", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mainListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                final int lastItem = firstVisibleItem + visibleItemCount;

                if(lastItem == totalItemCount && !doneCurrentSearch && !currentlySearching) {
                    currentSearchSkip += currentSearchLimit;
                    searchUsers(searchText.getText().toString(), currentSearchSkip, currentSearchLimit);
                }
            }
        });

        searchButton = findViewById(R.id.SearchButton);
        searchText = findViewById(R.id.SearchEditText);
        searchLoadingBar = findViewById(R.id.searchLoadingBar);
        hideSearchLoading();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                users.clear();
                adapter.notifyDataSetChanged();
                currentSearchSkip = 0;
                searchUsers(searchText.getText().toString(), currentSearchSkip, currentSearchLimit);
            }
        });
    }

    private void searchUsers(final String search, final int searchSkip, final int searchLimit) {

        currentlySearching = true;
        if (searchSkip == 0) {
            doneCurrentSearch = false;
        }

        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideSearchLoading();
                try {
                    int total = Integer.parseInt(response.getString("total"));

                    int numUsers = Math.min((total-searchSkip), searchLimit);

                    searchedUsers = response.getJSONArray("data");

                    for (int i=0; i<numUsers ; i++) {
                        users.add(searchedUsers.getJSONObject(i).getString("name") + " (" +
                                searchedUsers.getJSONObject(i).getString("email") + ")");
                    }

                    currentlySearching = false;

                    if (numUsers  < searchLimit || (searchSkip + numUsers) == total) {
                        doneCurrentSearch = true;
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Response", Toast.LENGTH_SHORT)
                            .show();
                    currentlySearching = false;
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(),Utils.decodeError(error), Toast.LENGTH_SHORT)
                        .show();
                hideSearchLoading();
                currentlySearching = false;
            }
        };

        String url = vw.usersURL + "?$search=" + search + "&$limit=" + searchLimit + "&$skip=" + searchSkip;

        showSearchLoading();
        vw.request(getApplication(), url, vw.searchUsersTAG, Request.Method.GET, null,
                callback);
    }

    private void requestUser(final String userID) {

        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Successfully added " + userID,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error),
                        Toast.LENGTH_SHORT).show();
            }
        };

        String url = vw.requestsURL;

        JSONObject body = new JSONObject();
        try {
            body.put("requesteeID", userID);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to request user",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        vw.request(getApplication(), url, vw.requestUserTAG, Request.Method.POST, body, callback);
    }

    private void showSearchLoading() {
        searchLoadingBar.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.INVISIBLE);
    }

    private void hideSearchLoading() {
        searchLoadingBar.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.VISIBLE);
    }
}
