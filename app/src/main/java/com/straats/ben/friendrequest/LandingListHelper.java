package com.straats.ben.friendrequest;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LandingListHelper {

    private Context c;
    private TableLayout mainList;
    private ProgressBar progressBar;

    private int limit = 49;

    private int pendingTotal;
    private int pendingSkip;
    private boolean fullyDoneLoadingPending;
    private boolean currentlyLoadingPending;

    private int friendSkip;
    private boolean fullyDoneLoadingFriends;
    private boolean currentlyLoadingFriends;

    private ArrayList<CustomRow> rowList;
    private int numRequests;

    public LandingListHelper(Context c, TableLayout mainList, ProgressBar progressBar) {
        this.c = c;
        this.mainList = mainList;
        this.progressBar = progressBar;

        pendingTotal = -1;
        friendSkip = 0;
        numRequests = 0;

        rowList = new ArrayList<>();

        wipeList();
        initialLoad();
    }

    public void wipeList() {
        mainList.removeAllViews();
        rowList.clear();
        numRequests = 0;

        HeadingRow pendingHeader = new HeadingRow(0,"Pending Friends", "Tap to expand");
        rowList.add(0, pendingHeader);
    }

    public void initialLoad() {
        fullyDoneLoadingPending = false;
        pendingSkip = 0;
        numRequests = 0;
        getRequests(pendingSkip, limit);

        fullyDoneLoadingFriends = false;
        friendSkip = 0;
        getFriends(friendSkip, limit);
    }

    private void getRequests(final int skip, final int limit) {

        showLoading();
        currentlyLoadingPending = true;

        final VolleyWrapper vw = VolleyWrapper.getInstance(c);

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {

                try {
                    int total = Integer.parseInt(response.getString("total"));

                    int numUsers = Math.min((total-skip), limit);

                    JSONArray requestedUsers = response.getJSONArray("data");
                    JSONObject userInfo = response.getJSONObject("userInfo");

                    for (int i=skip; i<(skip+numUsers); i++) {
                        String id = requestedUsers.getJSONObject(i).getString("_id");
                        String requester = requestedUsers.getJSONObject(i).getString("requester");

                        numRequests++;
                        int index = numRequests;

                        rowList.add(index, new PendingFriendRow(index, id, requester,
                                getUserName(userInfo, requester),
                                getUserUsername(userInfo, requester)));
                    }

                    if (numUsers < limit || (skip + numUsers) == total) {
                        fullyDoneLoadingPending = true;
                    }

                    //Initial load
                    if (skip == 0) {
                        rowList.get(0).onRowClick(null);
                    }

                } catch (JSONException e) {
                    Toast.makeText(c, R.string.bad_response, Toast.LENGTH_SHORT).show();
                }

                currentlyLoadingPending = false;
                hideLoading();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(c, R.string.failed_server_call, Toast.LENGTH_SHORT).show();
                currentlyLoadingPending = false;
                hideLoading();
            }
        };

        String url = Utils.requestsURL + "?requestee=" + Utils.userID + "&$limit=" + limit +
                "&$skip=" + skip;

        vw.request(c, url, Utils.getRequestsTAG, Request.Method.GET, null, callback);
    }

    private void getFriends(final int skip, final int limit) {

        showLoading();
        currentlyLoadingFriends = true;

        final VolleyWrapper vw = VolleyWrapper.getInstance(c);

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {

                try {

                    int total = Integer.parseInt(response.getString("total"));

                    int numUsers = Math.min((total-skip), limit);

                    JSONArray friendUsers = response.getJSONArray("data");
                    JSONObject userInfo = response.getJSONObject("userInfo");

                    for (int i=skip; i<(skip+numUsers); i++) {
                        String id = friendUsers.getJSONObject(i).getString("_id");
                        String user1 = friendUsers.getJSONObject(i).getString("user1");
                        String user2 = friendUsers.getJSONObject(i).getString("user2");

                        String otherUserID = user1;

                        if (user1.equals(Utils.userID)) {
                            otherUserID = user2;
                        }

                        int index = rowList.size();

                        rowList.add(index, new AddedFriendRow(index, id, otherUserID,
                                getUserName(userInfo, otherUserID),
                                getUserUsername(userInfo, otherUserID)));
                    }

                    if (numUsers < limit || (skip + numUsers) == total) {
                        fullyDoneLoadingFriends = true;
                    }

                } catch (JSONException e) {
                    Toast.makeText(c, R.string.bad_response, Toast.LENGTH_SHORT).show();
                }

                currentlyLoadingFriends = false;
                hideLoading();
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(c, R.string.failed_server_call, Toast.LENGTH_SHORT).show();
                currentlyLoadingFriends = false;
                hideLoading();
            }
        };

        String url = Utils.friendsURL + "?$limit=" + limit + "&$skip=" + skip;
        vw.request(c, url, Utils.getFriendsTAG, Request.Method.GET, null, callback);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private String getUserUsername(JSONObject userInfo, String userID) {

        try {
            JSONObject data = userInfo.getJSONObject("data");
            int total = data.getInt("total");
            int limit = data.getInt("limit");

            JSONArray userData = data.getJSONArray("data");

            int num = Math.min(total, limit);

            for (int i=0; i<num; i++) {
                if (userData.getJSONObject(i).getString("_id").equals(userID)) {
                    JSONObject correctUser = userData.getJSONObject(i);
                    return correctUser.getString("email");
                }
            }
        } catch (JSONException e) {
            return "Bad Response";
        }
        return "Bad Response";
    }

    private String getUserName(JSONObject userInfo, String userID) {
        try {
            JSONObject data = userInfo.getJSONObject("data");
            int total = data.getInt("total");
            int limit = data.getInt("limit");

            JSONArray userData = data.getJSONArray("data");

            int num = Math.min(total, limit);

            for (int i=0; i<num; i++) {
                if (userData.getJSONObject(i).getString("_id").equals(userID)) {
                    JSONObject correctUser = userData.getJSONObject(i);
                    return correctUser.getString("name");
                }
            }
        } catch (JSONException e) {
            return "Bad Response";
        }
        return "Bad Response";
    }

    private abstract class CustomRow {

        TableRow row;
        int index;

        public void showRow() {
            row.setVisibility(View.VISIBLE);
        }

        public void hideRow() {
            row.setVisibility(View.GONE);
        }

        public boolean isVisisble() {
            return row.getVisibility() == View.VISIBLE;
        }

        public int getIndex() {
            return index;
        }

        public void destroy() {
            mainList.removeViewAt(index);
            //Does this work?
            rowList.remove(this);
        }

        abstract String rowType();
        abstract void onRowClick(View v);
    }

    private class HeadingRow extends CustomRow {

        private String headingText;
        private String subText;

        public HeadingRow(int index, String headingText, String subText) {
            this.headingText = headingText;
            this.subText = subText;
            this.index = index;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_heading_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(headingText);
            subTextView.setText(subText);

            mainList.addView(row, index);
        }

        public void onRowClick(View v) {
            //Temp hardcoding of minimizing all pending friend rows
            for (CustomRow item : rowList) {
                if (item.rowType().equals("pending")) {
                    if (item.isVisisble()) {
                        item.hideRow();
                    } else {
                        item.showRow();
                    }
                }
            }
        }

        public String rowType() {
            return "heading";
        }
    }

    private class PendingFriendRow extends CustomRow {

        private String requestID;
        private String requesterID;
        private String requesterName;
        private String requesterUsername;

        public PendingFriendRow(int index, String requestID, String requesterID, String requesterName, String requesterUsername) {
            this.requestID = requestID;
            this.requesterID = requesterID;
            this.requesterName = requesterName;
            this.requesterUsername = requesterUsername;
            this.index = index;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_pending_friend_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(requesterName);
            subTextView.setText(requesterUsername);

            ImageButton deleteButton = (ImageButton) cl.getChildAt(2);
            ImageButton addButton = (ImageButton) cl.getChildAt(3);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, "Deleting user", Toast.LENGTH_SHORT).show();
                }
            });

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, "Adding user", Toast.LENGTH_SHORT).show();
                }
            });

            mainList.addView(row, index);
        }

        public String rowType() {
            return "pending";
        }

        public void onRowClick(View v) {
            Toast.makeText(c, "You clicked a pending row", Toast.LENGTH_SHORT).show();
        }
    }

    private class AddedFriendRow extends CustomRow {

        private String friendID;
        private String friendUserID;
        private String friendName;
        private String friendUsername;

        public AddedFriendRow(int index, String friendID, String friendUserID, String friendName, String friendUsername) {
            this.friendID = friendID;
            this.friendUserID = friendUserID;
            this.friendName = friendName;
            this.friendUsername = friendUsername;
            this.index = index;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_friend_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });
            //TODO: add onclick for the whole row

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(friendName);
            subTextView.setText(friendUsername);

            mainList.addView(row, index);
        }

        public String rowType() {
            return "friend";
        }

        public void onRowClick(View v) {
            Intent intent = new Intent(c, Profile.class);
            intent.putExtra("friendUserID", friendUserID);
            intent.putExtra("friendID", friendID);
            c.startActivity(intent);
        }
    }
}
