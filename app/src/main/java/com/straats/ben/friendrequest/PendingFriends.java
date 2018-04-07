package com.straats.ben.friendrequest;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
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

    private boolean doneUserLoad = true;
    private boolean currentlyLoading = false;
    private int userLoadSkip = 0;
    private int userLoadLimit = 49;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_friends);

        mainListView = findViewById(R.id.PendingList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users);
        mainListView.setAdapter(adapter);

        searchLoadingBar = findViewById(R.id.searchLoadingBar);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setMessage("Are you sure you want to accept this friend request?");
                builder.setTitle("Accept request?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        acceptRequest(position);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Send delete request
                        rejectRequest(position);
                    }
                });

                builder.show();
            }
        });

        userLoadSkip = 0;
        users.clear();
        doneUserLoad = false;
        requestedUsers = null;
        getRequests(userLoadSkip, userLoadLimit);

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
                    getRequests(userLoadSkip, userLoadLimit);
                }
            }
        });
    }

    private void getRequests(final int skip, final int limit) {

        currentlyLoading = true;
        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideSearchLoading();
                try {
                    int total = Integer.parseInt(response.getString("total"));

                    int numUsers = Math.min((total-skip), limit);

                    requestedUsers = Utils.combineArray(requestedUsers, response.getJSONArray("data"));
                    JSONObject userInfo = response.getJSONObject("userInfo");

                    for (int i=skip; i<(skip+numUsers); i++) {
                        users.add(Utils.getUserInfo(requestedUsers.getJSONObject(i).getString("requester"), userInfo));
                    }

                    currentlyLoading = false;
                    if (numUsers < limit || (skip + numUsers) == total) {
                        doneUserLoad = true;
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Bad Response",Toast.LENGTH_SHORT).show();
                    currentlyLoading = false;
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                hideSearchLoading();
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
                currentlyLoading = false;
            }
        };

        String url = vw.requestsURL + "?requestee=" + Utils.userID + "&$limit=" + limit + "&$skip="
                + skip;

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

    private void rejectRequest(int position) {
        final VolleyWrapper vw = VolleyWrapper.getInstance(getApplicationContext());
        String requestID;
        try {
            requestID = requestedUsers.getJSONObject(position).getString("_id");
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),"Failed to parse requested user",Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getApplicationContext(),"Successfully deleted request", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(getApplicationContext(), Utils.decodeError(error), Toast.LENGTH_SHORT).show();
            }
        };

        String url = vw.requestsURL + "/" + requestID;

        vw.request(getApplication(), url, vw.rejectRequestTAG,
                Request.Method.DELETE, null, callback);
    }

    private void showSearchLoading() {
        searchLoadingBar.setVisibility(View.VISIBLE);
    }

    private void hideSearchLoading() {
        searchLoadingBar.setVisibility(View.INVISIBLE);
    }
}
