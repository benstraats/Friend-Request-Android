package com.straats.ben.friendrequest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by ben_s on 2018-03-18.
 */

public class Utils {

    protected static String userEmail;
    protected static String userID;
    protected static String userName;

    protected static String accessToken = null;

    //Not sure if these constants should be in Utils or VolleyWrapper
    //URLs Section
    protected final static String baseURL = "http://api.friendrequest.ca/";
    protected final static String usersURL = baseURL + "users";
    protected final static String authenticationURL = baseURL + "authentication";
    protected final static String friendsURL = baseURL + "friends";
    protected final static String requestsURL = baseURL + "requests";
    protected final static String profileURL = baseURL + "profile";
    protected final static String searchURL = baseURL + "search";
    protected final static String myFriendsURL = baseURL + "myfriends";
    protected final static String myRequestsURL = baseURL + "myrequests";

    //TAGs section
    protected final static String signUpTAG = "Sign Up";
    protected final static String loginTAG = "Log In";
    protected final static String getFriendsTAG = "Get My Friends";
    protected final static String searchUsersTAG = "Search Users";
    protected final static String requestUserTAG = "Request User";
    protected final static String getRequestsTAG = "Get My Requests";
    protected final static String acceptRequestTAG = "Accept Request";
    protected final static String rejectRequestTAG = "Reject Request";
    protected final static String viewProfileTAG = "View Profile";
    protected final static String saveProfileTAG = "Save Profile";

    protected static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected static String decodeError(VolleyError error) {
        if (error.networkResponse == null) {
            return "Server may be down or internet connection lost";
        }

        try {
            JSONObject body = new JSONObject(new String(error.networkResponse.data,"UTF-8"));
            return body.getString("message");
        } catch (UnsupportedEncodingException e) {
            return "Generic Error";
        } catch (JSONException e) {
            return "Generic Error";
        }
    }

    protected static double getPercentScrolled(ScrollView sv) {
        //This eqn is fucked
        float scrollY = sv.getScrollY();
        int totalHeight = sv.getChildAt(0).getHeight();
        return (scrollY/totalHeight) * 100;
    }

    //All http calls section
    protected static void searchUsers(Context c, String search, int searchSkip, int searchLimit, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        String url = Utils.searchURL + "/" + search + "?$limit=" + searchLimit + "&$skip=" +
                searchSkip;
        vw.request(c, url, Utils.searchUsersTAG, Request.Method.GET, null, callback);
    }

    protected static void acceptRequest(Context c, final String requestID, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);

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

    protected static void rejectRequest(Context c, final String requestID, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);

        String url = Utils.requestsURL + "/" + requestID;

        vw.request(c, url, Utils.rejectRequestTAG, Request.Method.DELETE, null, callback);
    }

    protected static void requestUser(Context c, String userID, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);

        JSONObject body = new JSONObject();
        try {
            body.put("requesteeID", userID);
        } catch (JSONException e) {
            Toast.makeText(c, R.string.find_friends_activity_add_user_failure,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        vw.request(c, Utils.requestsURL, Utils.requestUserTAG, Request.Method.POST, body, callback);
    }

    protected static void getRequests(Context c, int skip, int limit, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);

        String url = Utils.myRequestsURL + "?$limit=" + limit + "&$skip=" + skip;
        vw.request(c, url, Utils.getRequestsTAG, Request.Method.GET, null, callback);
    }

    protected static void getFriends(Context c, final int skip, final int limit, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);

        String url = Utils.myFriendsURL + "?$limit=" + limit + "&$skip=" + skip;
        vw.request(c, url, Utils.getFriendsTAG, Request.Method.GET, null, callback);
    }

    protected static void getUserInfo(Context c, String userEmail, VolleyWrapper.VolleyCallback callback) {
        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        String url = Utils.usersURL + "?email=" + userEmail;
        vw.request(c, url, Utils.searchUsersTAG, Request.Method.GET, null,
                callback);
    }

    protected static void signUp(Context c, String username, String name, String password, VolleyWrapper.VolleyCallback callback) {
        VolleyWrapper vw = VolleyWrapper.getInstance(c);

        JSONObject body = new JSONObject();
        try {
            body.put("email", username);
            body.put("name", name);
            body.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(c, R.string.login_activity_failed_to_signup,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        vw.request(c, Utils.usersURL, Utils.signUpTAG,
                Request.Method.POST, body, callback);
    }

    protected static void login(Context c, String username, String password, VolleyWrapper.VolleyCallback callback) {

        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        JSONObject body = new JSONObject();
        try {
            body.put("strategy", "local");
            body.put("email", username);
            body.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(c, R.string.login_activity_failed_to_login,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        vw.request(c, Utils.authenticationURL, Utils.loginTAG, Request.Method.POST, body, callback);
    }

    protected static void deleteFriend(Context c, String friendUserID, VolleyWrapper.VolleyCallback callback) {
        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        String url = Utils.friendsURL + "/" + friendUserID;
        vw.request(c, url, Utils.viewProfileTAG, Request.Method.DELETE, null, callback);
    }

    protected static void getProfile(Context c, String userID, VolleyWrapper.VolleyCallback callback) {
        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        String url = Utils.profileURL + "?userID=" + userID;
        vw.request(c, url, Utils.viewProfileTAG, Request.Method.GET, null, callback);
    }

    protected static void createProfile(Context c, JSONObject profile, VolleyWrapper.VolleyCallback callback) {
        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        vw.request(c, Utils.profileURL, Utils.saveProfileTAG, Request.Method.POST, profile,
                callback);
    }

    protected static void updateProfile(Context c, String profileID, JSONObject profile, VolleyWrapper.VolleyCallback callback) {
        VolleyWrapper vw = VolleyWrapper.getInstance(c);
        String url = Utils.profileURL + "/" + profileID;
        vw.request(c, url, Utils.saveProfileTAG, Request.Method.PUT, profile, callback);
    }
}
