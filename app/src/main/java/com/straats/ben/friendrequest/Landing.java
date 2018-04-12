package com.straats.ben.friendrequest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Landing extends AppCompatActivity {

    private ListView mainListView;

    private ArrayList<String> users = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private JSONArray friendUsers;

    private Button pendingFriendsButton;
    private Button addFriendsButton;

    private ProgressBar loadingBar;

    private boolean doneUserLoad = true;
    private boolean currentlyLoading = false;
    private int userLoadSkip = 0;
    private int userLoadLimit = 49;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mainListView = findViewById(R.id.mainListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {

                    String user1 = friendUsers.getJSONObject(position).getString("user1");
                    String user2 = friendUsers.getJSONObject(position).getString("user2");
                    String friendID = friendUsers.getJSONObject(position).getString("_id");

                    String friendUserID = user1;

                    if (user1.equals(Utils.userID)) {
                        friendUserID = user2;
                    }

                    Intent intent = new Intent(getApplicationContext(), ViewProfile.class);
                    intent.putExtra("friendUserID", friendUserID);
                    intent.putExtra("friendID", friendID);
                    startActivity(intent);
                } catch (JSONException e) {
                    //bad friend yo
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
            }
        });
        fab.setImageResource(android.R.drawable.ic_menu_edit);

        pendingFriendsButton = findViewById(R.id.PendingFriendsButton);
        addFriendsButton = findViewById(R.id.AddFriendsButton);

        loadingBar = findViewById(R.id.loadingBar);

        pendingFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PendingFriends.class);
                startActivity(intent);
            }
        });

        addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FindFriends.class);
                startActivity(intent);
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

                if(lastItem == totalItemCount && !doneUserLoad && !currentlyLoading) {
                    userLoadSkip += userLoadLimit;
                    getFriends(userLoadSkip, userLoadLimit);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        userLoadSkip = 0;
        users.clear();
        friendUsers = null;
        doneUserLoad = false;
        getFriends(userLoadSkip, userLoadLimit);
    }

    private void getFriends(final int skip, final int limit) {

        currentlyLoading = true;
        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideLoading();
                try {
                    int total = Integer.parseInt(response.getString("total"));

                    int numUsers = Math.min((total-skip), limit);

                    friendUsers = Utils.combineArray(friendUsers, response.getJSONArray("data"));

                    for (int i=skip; i<(skip+numUsers); i++) {
                        String user1 = friendUsers.getJSONObject(i).getString("user1");
                        String user2 = friendUsers.getJSONObject(i).getString("user2");

                        JSONObject userInfo = response.getJSONObject("userInfo");

                        if (user1.equals(Utils.userID)) {
                            users.add(Utils.getUserInfo(user2, userInfo));
                        } else {
                            users.add(Utils.getUserInfo(user1, userInfo));
                        }
                    }

                    currentlyLoading = false;

                    if (numUsers < limit || (skip + numUsers) == total) {
                        doneUserLoad = true;
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.bad_response,
                            Toast.LENGTH_SHORT).show();
                    currentlyLoading = false;
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT)
                        .show();
                hideLoading();
                currentlyLoading = false;
            }
        };

        String url = vw.friendsURL + "?$limit=" + limit + "&$skip=" + skip;
        showLoading();
        vw.request(getApplication(), url, vw.getFriendsTAG, Request.Method.GET, null, callback);
    }

    private void showLoading() {
        loadingBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loadingBar.setVisibility(View.INVISIBLE);
    }
}
