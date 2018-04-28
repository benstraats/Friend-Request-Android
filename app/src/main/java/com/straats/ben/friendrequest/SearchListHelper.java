package com.straats.ben.friendrequest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
    private ScrollView scrollView;
    private final TableLayout mainList;
    private EditText searchEditText;
    private Button searchButton;
    private ProgressBar progressBar;

    private int limit = 50;
    private int skip = 0;
    private String currentSearchText;

    private boolean currentlySearching;
    private boolean fullyDoneSearching;

    private ArrayList<SearchRow> rowList;

    private final String friendStatus = "Friends";
    private final String notFriendStatus = "Add User";
    private final String requesteeStatus = "Accept/Decline Request";
    private final String requesterStatus = "Cancel Request";

    public SearchListHelper(Context context, ScrollView sv, TableLayout list, EditText text, Button search, ProgressBar pgBar) {
        this.c = context;
        this.scrollView = sv;
        this.mainList = list;
        this.searchEditText = text;
        this.searchButton = search;
        this.progressBar = pgBar;

        currentlySearching = false;
        fullyDoneSearching = true;

        rowList = new ArrayList<>();

        final VolleyWrapper.VolleyCallback requestCallback = new VolleyWrapper.VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                hideSearchLoading();

                if (skip == 0) {
                    wipeList();
                }

                try {
                    JSONObject usersSection = response.getJSONObject("users");
                    JSONObject friendsSection = response.getJSONObject("friends");
                    JSONObject requestsSection = response.getJSONObject("requests");

                    int total = Integer.parseInt(usersSection.getString("total"));

                    int numUsers = Math.min((total-skip), limit);

                    if (numUsers < limit || (skip + numUsers) == total) {
                        fullyDoneSearching = true;
                    }

                    int numFriends = Integer.parseInt(friendsSection.getString("total"));
                    int numRequests = Integer.parseInt(requestsSection.getString("total"));

                    for (int i=0; i<numUsers; i++) {

                        boolean statusFound = false;
                        String searchedUserID = usersSection.getJSONArray("data").getJSONObject(i).getString("_id");
                        String usersUsername = usersSection.getJSONArray("data").getJSONObject(i).getString("email");
                        String usersName = usersSection.getJSONArray("data").getJSONObject(i).getString("name");

                        for (int j=0; j<numFriends; j++) {
                            String user1 = friendsSection.getJSONArray("data").getJSONObject(j).getString("user1");
                            String user2 = friendsSection.getJSONArray("data").getJSONObject(j).getString("user2");

                            if (user1.equals(searchedUserID)) {
                                String friendID = friendsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                rowList.add(new SearchRow(rowList.size(), friendID, user1, usersUsername, usersName, friendStatus));

                                statusFound = true;
                                break;
                            } else if (user2.equals(searchedUserID)) {
                                String friendID = friendsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                rowList.add(new SearchRow(rowList.size(), friendID, user2, usersUsername, usersName, friendStatus));

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
                                    rowList.add(new SearchRow(rowList.size(), requestID, searchedUserID, usersUsername, usersName, requesteeStatus));

                                    statusFound = true;
                                    break;
                                } else if (requestee.equals(searchedUserID)) {
                                    String requestID = requestsSection.getJSONArray("data").getJSONObject(j).getString("_id");
                                    rowList.add(new SearchRow(rowList.size(), requestID, searchedUserID, usersUsername, usersName, requesterStatus));

                                    statusFound = true;
                                    break;
                                }
                            }
                        }

                        if (!statusFound) {
                            rowList.add(new SearchRow(rowList.size(), null, searchedUserID, usersUsername, usersName, notFriendStatus));
                        }
                    }
                    currentlySearching = false;
                }

                catch (JSONException e) {
                    currentlySearching = false;
                    Toast.makeText(c, R.string.parse_failure, Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(VolleyError error) {
                hideSearchLoading();
                currentlySearching = false;
                Toast.makeText(c, R.string.bad_response, Toast.LENGTH_SHORT);
            }
        };

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                showSearchLoading();

                /* enter keyboard */
                searchEditText.setOnKeyListener(new View.OnKeyListener()
                {
                    public boolean onKey(View v, int keyCode, KeyEvent event)
                    {
                        if (event.getAction() == KeyEvent.ACTION_DOWN)
                        {
                            switch (keyCode)
                            {
                                case KeyEvent.KEYCODE_DPAD_CENTER:
                                case KeyEvent.KEYCODE_ENTER:
                                    searchButton.callOnClick();
                                    return true;
                                default:
                                    break;
                            }
                        }
                        return false;
                    }
                });


                currentSearchText = searchEditText.getText().toString();
                currentlySearching = true;
                fullyDoneSearching = false;
                skip = 0;
                Utils.searchUsers(v.getContext(), currentSearchText, 0, limit,
                        requestCallback);
            }
        });


        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                if (!currentlySearching && !fullyDoneSearching) {
                    double currentPosition = Utils.getPercentScrolled(scrollView);

                    if (currentPosition >= 50) {
                        showSearchLoading();
                        currentlySearching = true;
                        fullyDoneSearching = false;
                        skip += limit;
                        Utils.searchUsers(c, currentSearchText, skip, limit,
                                requestCallback);
                    }
                }
            }
        });
    }

    private void wipeList() {
        mainList.removeAllViews();
        rowList.clear();
    }

    private void showSearchLoading() {
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.INVISIBLE);
    }

    private void hideSearchLoading() {
        progressBar.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.VISIBLE);
    }

    private class SearchRow{

        private TableRow row;
        private TextView mainTextView;
        private TextView subTextView;
        private TextView statusText;
        private ProgressBar loadingBar;

        private String itemID;
        private String otherUserID;
        private String status;
        private String otherUserName;

        private boolean loading;

        public SearchRow(int index, String itemID, String otherUserID, String otherUserUsername, String otherUserName, String status) {
            this.itemID = itemID;
            this.otherUserID = otherUserID;
            this.status = status;
            this.otherUserName = otherUserName;
            loading = false;

            row = (TableRow) LayoutInflater.from(c).inflate(R.layout.search_row, null);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            ConstraintLayout cl = (ConstraintLayout) row.getChildAt(0);

            mainTextView = (TextView) cl.getChildAt(0);
            subTextView = (TextView) cl.getChildAt(1);
            statusText = (TextView) cl.getChildAt(2);

            mainTextView.setText(otherUserUsername);
            subTextView.setText(otherUserName);
            statusText.setText(status);

            loadingBar = (ProgressBar) cl.getChildAt(3);
            hideLoad();

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick(v);
                }
            });

            mainList.addView(row, index);
        }

        public void showRow() {
            row.setVisibility(View.VISIBLE);
        }

        public void hideRow() {
            row.setVisibility(View.GONE);
        }

        public boolean isVisisble() {
            return row.getVisibility() == View.VISIBLE;
        }

        private void setStatus(String newStatus) {
            status = newStatus;
            statusText.setText(newStatus);
        }

        public void showLoad() {
            loading = true;
            loadingBar.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.INVISIBLE);
        }

        public void hideLoad() {
            loading = false;
            loadingBar.setVisibility(View.INVISIBLE);
            statusText.setVisibility(View.VISIBLE);
        }

        public void onRowClick(View v) {

            if (!loading) {
                if (status.equals(friendStatus)) {
                    Intent intent = new Intent(c, Profile.class);
                    intent.putExtra("friendUserID", otherUserID);
                    intent.putExtra("friendID", itemID);
                    c.startActivity(intent);
                } else {

                    final String title;
                    final String message;
                    final String positiveText;
                    final String negativeText;

                    if (status.equals(requesteeStatus)) {
                        title = "Accept Request?";
                        message = "Are you sure you want to accept the friend request from " + otherUserName;
                        positiveText = "Accept";
                        negativeText = "Reject";
                    } else if (status.equals(requesterStatus)) {
                        title = "Cancel Request?";
                        message = "Are you sure you want to cancel the friend request to " + otherUserName;
                        positiveText = "Cancel";
                        negativeText = "Don\'t Cancel";
                    } else {
                        title = "Request User?";
                        message = "Are you sure you want to request " + otherUserName + " to be your friend?";
                        positiveText = "Request";
                        negativeText = "Cancel";
                    }

                    AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(c);

                    builder.setTitle(title);
                    builder.setMessage(message);

                    builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (status.equals(requesteeStatus)) {
                                acceptRequest();
                            } else if (status.equals(requesterStatus)) {
                                rejectRequest();
                            } else {
                                requestUser();
                            }
                        }
                    }).setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (status.equals(requesteeStatus)) {
                                rejectRequest();
                            } else if (status.equals(requesterStatus)) {
                                //Do Nothing
                            } else {
                                //Do Nothing
                            }
                        }
                    });

                    builder.show();
                }
            }
        }

        private void acceptRequest() {
            showLoad();

            VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    hideLoad();
                    try {
                        itemID = response.getString("_id");
                        setStatus(friendStatus);
                    } catch (JSONException e) {
                        Toast.makeText(c, R.string.bad_response, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    hideLoad();
                    Toast.makeText(c, Utils.decodeError(error), Toast.LENGTH_SHORT).show();
                }
            };

            Utils.acceptRequest(c, itemID, callback);
        }

        private void rejectRequest() {
            showLoad();

            VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    hideLoad();
                    itemID = null;
                    setStatus(notFriendStatus);
                }

                @Override
                public void onFailure(VolleyError error) {
                    hideLoad();
                    Toast.makeText(c, Utils.decodeError(error), Toast.LENGTH_SHORT).show();
                }
            };

            Utils.rejectRequest(c, itemID, callback);
        }

        private void requestUser() {
            showLoad();

            VolleyWrapper.VolleyCallback callback = new VolleyWrapper.VolleyCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    hideLoad();

                    try {
                        itemID = response.getString("_id");
                        setStatus(requesterStatus);
                    } catch (JSONException e) {
                        Toast.makeText(c, R.string.parse_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(VolleyError error) {
                    hideLoad();
                    Toast.makeText(c, Utils.decodeError(error), Toast.LENGTH_SHORT).show();
                }
            };

            Utils.requestUser(c, otherUserID, callback);
        }
    }
}
