package com.straats.ben.friendrequest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private final TableLayout mainList;
    private ProgressBar progressBar;

    private int limit = 49;

    private boolean pendingCollapsed = false;
    private int pendingTotal;
    private int absoluteTotal;
    private int pendingSkip;
    private boolean fullyDoneLoadingPending;
    private boolean currentlyLoadingPending;

    private int friendSkip;
    private boolean fullyDoneLoadingFriends;
    private boolean currentlyLoadingFriends;

    private ArrayList<CustomRow> rowList;
    private int numRequests;

    public LandingListHelper(Context c, TableLayout list, final ScrollView scrollView, ProgressBar progressBar) {
        this.c = c;
        this.mainList = list;
        this.progressBar = progressBar;

        pendingTotal = 0;
        absoluteTotal = 0;
        friendSkip = 0;
        numRequests = 0;

        rowList = new ArrayList<>();

        wipeList();
        initialLoad();

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                double currentPosition = 0.0;

                if (!pendingCollapsed && !currentlyLoadingPending && !fullyDoneLoadingPending) {
                    //check if at last 20 pending friends then load next bunch
                    currentPosition = getPercentScrolled(scrollView);

                    double pendingPercent = (pendingTotal / ((double) mainList.getChildCount())) * 100;
                    if (currentPosition >= (pendingPercent-25)) {
                        pendingSkip += limit;
                        getRequests(pendingSkip, limit);
                    }
                }

                if (!currentlyLoadingFriends && !fullyDoneLoadingFriends) {
                    //Dont want to calc again if we dont have to
                    if (currentPosition == 0.0) {
                        currentPosition = getPercentScrolled(scrollView);
                    }

                    if (currentPosition >= 30) {
                        friendSkip += limit;
                        getFriends(friendSkip, limit);
                    }
                }
            }
        });
    }

    private double getPercentScrolled(ScrollView sv) {
        //This eqn is fucked
        float scrollY = sv.getScrollY();
        int totalHeight = sv.getChildAt(0).getHeight();
        return (scrollY/totalHeight) * 100;
    }

    public void wipeList() {
        mainList.removeAllViews();
        rowList.clear();
        numRequests = 0;

        PendingFriendHeaderRow pendingHeader = new PendingFriendHeaderRow(0,"Pending Friends", "Tap to expand");
        pendingHeader.hideRow();
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

                    absoluteTotal = total;
                    ((PendingFriendHeaderRow) rowList.get(0)).setHeadingText("Pending Friends (" + total + ")");

                    if (total == 0) {
                        rowList.get(0).hideRow();
                    } else {
                        rowList.get(0).showRow();
                    }

                    int numUsers = Math.min((total-skip), limit);

                    JSONArray requestedUsers = response.getJSONArray("data");
                    JSONObject userInfo = response.getJSONObject("userInfo");

                    for (int i=0; i<numUsers; i++) {
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

                    for (int i=0; i<numUsers; i++) {
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

    private void acceptRequest(final String requestID, VolleyWrapper.VolleyCallback callback) {

        final VolleyWrapper vw = VolleyWrapper.getInstance(c);

        String url = Utils.friendsURL;
        JSONObject body = new JSONObject();
        try {
            body.put("requestID", requestID);
        } catch (JSONException e) {
            Toast.makeText(c, R.string.parse_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        vw.request(c, url, Utils.acceptRequestTAG, Request.Method.POST, body, callback);
    }

    private void rejectRequest(final String requestID, VolleyWrapper.VolleyCallback callback) {
        final VolleyWrapper vw = VolleyWrapper.getInstance(c);

        String url = Utils.requestsURL + "/" + requestID;

        vw.request(c, url, Utils.rejectRequestTAG, Request.Method.DELETE, null, callback);
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

        public void showRow() {
            row.setVisibility(View.VISIBLE);
        }

        public void hideRow() {
            row.setVisibility(View.GONE);
        }

        public boolean isVisisble() {
            return row.getVisibility() == View.VISIBLE;
        }

        public void destroy() {
            mainList.removeViewAt(rowList.indexOf(this));
            rowList.remove(this);
        }

        abstract String rowType();
        abstract void onRowClick(View v);
    }

    private class HeadingRow extends CustomRow {

        private String headingText;
        private String subText;

        TextView mainTextView;
        TextView subTextView;

        public HeadingRow(int index, String headingText, String subText) {
            this.headingText = headingText;
            this.subText = subText;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_heading_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            mainTextView = (TextView) cl.getChildAt(0);
            subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(headingText);
            subTextView.setText(subText);

            mainList.addView(row, index);
        }

        public void onRowClick(View v) {
        }

        public String rowType() {
            return "heading";
        }

        public void setHeadingText(String headingText) {
            this.headingText = headingText;
            mainTextView.setText(headingText);
        }

        public void setSubText(String subText) {
            this.subText = subText;
            subTextView.setText(subText);
        }
    }

    public class PendingFriendHeaderRow extends HeadingRow {

        public PendingFriendHeaderRow(int index, String headingText, String subText) {
            super(index, headingText, subText);

            //Unsure if the pending friends heading row should be grey aswell
            //row.setBackgroundColor(c.getResources().getColor(R.color.lightGrey));
        }

        @Override
        public void onRowClick(View v) {
            if (pendingCollapsed) {
                pendingCollapsed = false;
                setSubText("Tap to collapse");

            } else {
                pendingCollapsed = true;
                setSubText("Tap to expand");
            }

            for (CustomRow item : rowList) {
                if (item.rowType().equals("pending")) {
                    if (pendingCollapsed) {
                        item.hideRow();
                    } else {
                        item.showRow();
                    }
                }
            }
        }
    }

    private class PendingFriendRow extends CustomRow {

        private final String requestID;
        private String requesterID;
        private final String requesterName;
        private final String requesterUsername;

        private final ImageButton deleteButton;
        private final ImageButton addButton;
        private final ProgressBar progressBar;

        public PendingFriendRow(int index, String requestRESTID, String requesterID, String requesterNameHold, String requesterUsernameHold) {
            this.requestID = requestRESTID;
            this.requesterID = requesterID;
            this.requesterName = requesterNameHold;
            this.requesterUsername = requesterUsernameHold;

            pendingTotal += 1;

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

            deleteButton = (ImageButton) cl.getChildAt(2);
            addButton = (ImageButton) cl.getChildAt(3);

            progressBar = (ProgressBar) cl.getChildAt(4);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            hideLoading();
                            destroy();
                            if (rowList.get(1).rowType().equals("friend")) {
                                rowList.get(0).hideRow();
                            }
                            absoluteTotal--;
                            ((PendingFriendHeaderRow) rowList.get(0)).setHeadingText("Pending Friends (" + absoluteTotal + ")");
                        }

                        @Override
                        public void onFailure(VolleyError error) {
                            Toast.makeText(c, Utils.decodeError(error), Toast.LENGTH_SHORT).show();
                            hideLoading();
                        }
                    };

                    showLoading();
                    rejectRequest(requestID, callback);
                }
            });

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            hideLoading();
                            if (fullyDoneLoadingFriends) {
                                try {
                                    String id = response.getString("_id");
                                    String user1 = response.getString("user1");
                                    String user2 = response.getString("user2");

                                    String otherUserID = user1;

                                    if (user1.equals(Utils.userID)) {
                                        otherUserID = user2;
                                    }

                                    int size = rowList.size();
                                    rowList.add(size, new AddedFriendRow(size, id, otherUserID, requesterName, requesterUsername));
                                } catch (JSONException e) {
                                    Toast.makeText(c, R.string.bad_response, Toast.LENGTH_SHORT).show();
                                }
                            }

                            absoluteTotal--;
                            ((PendingFriendHeaderRow) rowList.get(0)).setHeadingText("Pending Friends (" + absoluteTotal + ")");
                            destroy();
                            if (rowList.get(1).rowType().equals("friend")) {
                                rowList.get(0).hideRow();
                            }
                        }

                        @Override
                        public void onFailure(VolleyError error) {
                            hideLoading();
                            Toast.makeText(c, Utils.decodeError(error), Toast.LENGTH_SHORT).show();
                        }
                    };

                    showLoading();
                    acceptRequest(requestID, callback);
                }
            });

            mainList.addView(row, index);
        }

        private void hideLoading() {
            progressBar.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
        }

        private void showLoading() {
            progressBar.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            addButton.setVisibility(View.INVISIBLE);
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

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_friend_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

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
