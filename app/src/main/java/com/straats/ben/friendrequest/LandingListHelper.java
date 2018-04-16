package com.straats.ben.friendrequest;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;

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
    private ArrayList<CustomRow> pendingFriends;

    private int friendSkip;
    private boolean fullyDoneLoadingFriends;
    private boolean currentlyLoadingFriends;
    private ArrayList<CustomRow> addedFriends;

    public LandingListHelper(Context c, TableLayout mainList, ProgressBar progressBar) {
        this.c = c;
        this.mainList = mainList;
        this.progressBar = progressBar;

        pendingTotal = -1;
        pendingSkip = 0;
        fullyDoneLoadingPending = false;
        currentlyLoadingPending = false;
        pendingFriends = new ArrayList<>();

        friendSkip = 0;
        fullyDoneLoadingFriends = false;
        currentlyLoadingFriends = false;
        addedFriends = new ArrayList<>();

        HeadingRow pendingHeader = new HeadingRow(0,"Pending Friends", "Tap to expand");
        addedFriends.add(0, pendingHeader);
    }

    private void getRequests(final int skip, final int limit) {

        showLoading();
        final VolleyWrapper vw = VolleyWrapper.getInstance(c);

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {

                hideLoading();
            }

            @Override
            public void onFailure(VolleyError error) {

                hideLoading();
            }
        };

        String url = Utils.requestsURL + "?requestee=" + Utils.userID + "&$limit=" + limit +
                "&$skip=" + skip;

        vw.request(c, url, Utils.getRequestsTAG, Request.Method.GET, null, callback);
    }

    private void getFriends(final int skip, final int limit) {

        showLoading();
        final VolleyWrapper vw = VolleyWrapper.getInstance(c);

        VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {

                hideLoading();
            }

            @Override
            public void onFailure(VolleyError error) {

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

    private abstract class CustomRow {

        TableRow row;
        int index;

        public void showRow() {
            row.setVisibility(View.VISIBLE);
        }

        public void hideRow() {
            row.setVisibility(View.GONE);
        }

        public int getIndex() {
            return index;
        }

        public void destroy() {
            mainList.removeViewAt(index);
        }

        abstract String rowType();
    }

    private class HeadingRow extends CustomRow {

        private String headingText;
        private String subText;

        public HeadingRow(int index, String headingText, String subText) {
            this.headingText = headingText;
            this.subText = subText;
            this.index = index;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_heading_row, null);

            //TODO: add onclick for the whole row

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(headingText);
            subTextView.setText(subText);

            mainList.addView(row, index);
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

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_heading_row, null);

            //TODO: add onclick for the whole row

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(requesterName);
            subTextView.setText(requesterUsername);

            ImageButton deleteButton = (ImageButton) cl.getChildAt(2);
            ImageButton addButton = (ImageButton) cl.getChildAt(3);

            //TODO: add onclick methods for these two

            mainList.addView(row, index);
        }

        public String rowType() {
            return "pending";
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

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.landing_heading_row, null);

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
    }
}
