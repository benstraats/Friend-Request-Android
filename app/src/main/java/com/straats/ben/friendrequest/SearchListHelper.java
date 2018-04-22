package com.straats.ben.friendrequest;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchListHelper {

    private Context c;
    private final TableLayout mainList;
    private EditText searchEditText;
    private Button searchButton;
    private ProgressBar progressBar;

    private int limit = 50;

    private ArrayList<CustomRow> rowList;

    public SearchListHelper(Context context, TableLayout list, EditText text, Button search, ProgressBar pgBar) {
        this.c = context;
        this.mainList = list;
        this.searchEditText = text;
        this.searchButton = search;
        this.progressBar = pgBar;

        rowList = new ArrayList<>();

        final VolleyWrapper.VolleyCallback requestCallback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONObject usersSection = response.getJSONObject("users");
                    JSONObject friendsSection = response.getJSONObject("friends");
                    JSONObject requestsSection = response.getJSONObject("requests");

                    int total = Integer.parseInt(usersSection.getString("total"));

                    int skip = 0;
                    int numUsers = Math.min((total-skip), limit);

                    int numFriends = Integer.parseInt(friendsSection.getString("total"));
                    int numRequests = Integer.parseInt(requestsSection.getString("total"));

                    for (int i=0; i<numUsers; i++) {

                        boolean statusFound = false;
                        String searchedUserID = usersSection.getJSONArray("data").getJSONObject(i).getString("_id");

                        for (int j=0; j<numFriends; j++) {
                            String user1 = friendsSection.getJSONArray("data").getJSONObject(j).getString("user1");
                            String user2 = friendsSection.getJSONArray("data").getJSONObject(j).getString("user2");

                            if (user1.equals(searchedUserID)) {
                                String friendID = friendsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                String friendUsername = usersSection.getJSONArray("data").getJSONObject(i).getString("email");
                                String friendName = usersSection.getJSONArray("data").getJSONObject(i).getString("name");

                                rowList.add(new FriendRow(rowList.size(), friendID, user1, friendUsername, friendName));

                                statusFound = true;
                                break;
                            } else if (user2.equals(searchedUserID)) {
                                String friendID = friendsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                String friendUsername = usersSection.getJSONArray("data").getJSONObject(i).getString("email");
                                String friendName = usersSection.getJSONArray("data").getJSONObject(i).getString("name");

                                rowList.add(new FriendRow(rowList.size(), friendID, user2, friendUsername, friendName));

                                statusFound = true;
                                break;
                            }
                        }

                        if (!statusFound) {
                            for (int j=0; j<numRequests; j++) {
                                String requester = requestsSection.getJSONArray("data").getJSONObject(j).getString("requester");
                                String requestee = requestsSection.getJSONArray("data").getJSONObject(j).getString("requestee");

                                if (requester.equals(searchedUserID)) {
                                    String requestID = requestsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                    String usersUsername = usersSection.getJSONArray("data").getJSONObject(i).getString("email");
                                    String usersName = usersSection.getJSONArray("data").getJSONObject(i).getString("name");
                                    rowList.add(new RequestedFriendRow(rowList.size(), requestID, searchedUserID, usersUsername, usersName));

                                    statusFound = true;
                                    break;
                                } else if (requestee.equals(searchedUserID)) {
                                    String requestID = requestsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                    String usersUsername = usersSection.getJSONArray("data").getJSONObject(i).getString("email");
                                    String usersName = usersSection.getJSONArray("data").getJSONObject(i).getString("name");
                                    rowList.add(new PendingFriendRow(rowList.size(), requestID, searchedUserID, usersUsername, usersName));

                                    statusFound = true;
                                    break;
                                }
                            }
                        }

                        if (!statusFound) {
                            String usersUsername = usersSection.getJSONArray("data").getJSONObject(i).getString("email");
                            String usersName = usersSection.getJSONArray("data").getJSONObject(i).getString("name");

                            rowList.add(new AddUserRow(rowList.size(), searchedUserID, usersUsername, usersName));
                        }
                    }
                }

                catch (JSONException e) {
                    Toast.makeText(c, R.string.parse_failure, Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                Toast.makeText(c, R.string.bad_response, Toast.LENGTH_SHORT);
            }
        };

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.searchUsers(v.getContext(), searchEditText.getText().toString(), 0,
                        limit, requestCallback);
            }
        });
    }

    private void showSearchLoading() {
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.INVISIBLE);
    }

    private void hideSearchLoading() {
        progressBar.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.VISIBLE);
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

    private class FriendRow extends CustomRow{

        private String friendID;
        private String friendUserID;

        public FriendRow(int index, String friendID, String friendUserID, String friendUsername, String friendName) {
            this.friendID = friendID;
            this.friendUserID = friendUserID;

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

    private class AddUserRow extends CustomRow{

        private String usersID;

        public AddUserRow(int index, String usersID, String usersUsername, String usersName) {
            this.usersID = usersID;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.add_friend_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(usersUsername);
            subTextView.setText(usersName);

            mainList.addView(row, index);
        }

        public String rowType() {
            return "add";
        }

        public void onRowClick(View v) {
            Toast.makeText(c, "Show confirmation to add " + usersID, Toast.LENGTH_SHORT).show();
        }
    }

    private class RequestedFriendRow extends CustomRow{

        private final String requestID;
        private String requestUserID;

        public RequestedFriendRow(int index, final String requestID, String requestUserID, String usersUsername, String usersName) {
            this.requestID = requestID;
            this.requestUserID = requestUserID;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.requested_friend_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            TextView mainTextView = (TextView) cl.getChildAt(0);
            TextView subTextView = (TextView) cl.getChildAt(1);

            mainTextView.setText(usersUsername);
            subTextView.setText(usersName);

            Button cancelButton = (Button) cl.getChildAt(2);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, "Cancel the request " + requestID, Toast.LENGTH_SHORT).show();
                }
            });

            mainList.addView(row, index);
        }

        public String rowType() {
            return "requested";
        }

        public void onRowClick(View v) {
            Toast.makeText(c, "Show confirmation to cancel request " + requestID, Toast.LENGTH_SHORT).show();
        }
    }

    private class PendingFriendRow extends CustomRow {

        private final String requestID;
        private String requestUserID;

        public PendingFriendRow(int index, final String requestID, String requestUserID, String usersUsername, String usersName) {
            this.requestID = requestID;
            this.requestUserID = requestUserID;

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

            mainTextView.setText(usersUsername);
            subTextView.setText(usersName);

            ImageButton declineButton = (ImageButton) cl.getChildAt(2);

            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, "decline the request " + requestID, Toast.LENGTH_SHORT).show();
                }
            });

            ImageButton acceptButton = (ImageButton) cl.getChildAt(3);

            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, "accept the request " + requestID, Toast.LENGTH_SHORT).show();
                }
            });

            mainList.addView(row, index);
        }

        public String rowType() {
            return "pending";
        }

        public void onRowClick(View v) {
            Toast.makeText(c, "Show confirmation to decline/accept request " + requestID, Toast.LENGTH_SHORT).show();
        }
    }
}
