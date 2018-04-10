package com.straats.ben.friendrequest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyWrapper {

    private static VolleyWrapper instance = null;
    private static RequestQueue queue;

    //Not sure if these constants should be in Utils or VolleyWrapper
    //URLs Section
    protected final String baseURL = "http://api.friendrequest.ca/";
    protected final String usersURL = baseURL + "users";
    protected final String authenticationURL = baseURL + "authentication";
    protected final String friendsURL = baseURL + "friends";
    protected final String requestsURL = baseURL + "requests";
    protected final String profileURL = baseURL + "profile";

    //TAGs section
    protected final String signUpTAG = "Sign Up";
    protected final String loginTAG = "Log In";
    protected final String getFriendsTAG = "Get My Friends";
    protected final String searchUsersTAG = "Search Users";
    protected final String requestUserTAG = "Request User";
    protected final String getRequestsTAG = "Get My Requests";
    protected final String acceptRequestTAG = "Accept Request";
    protected final String rejectRequestTAG = "Reject Request";
    protected final String viewProfileTAG = "View Profile";
    protected final String saveProfileTAG = "Save Profile";

    private VolleyWrapper(Context c) {
        queue = Volley.newRequestQueue(c);
    }

    public static VolleyWrapper getInstance(Context c) {
        if (instance == null) {
            instance = new VolleyWrapper(c);
        }
        return instance;
    }

    protected void request(final Context c, final String url, final String tag, int requestMethod,
                           final JSONObject body, final VolleyWrapper.VolleyCallback callback) {

        if (!Utils.isNetworkAvailable(c)) {
            Toast.makeText(c, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        if (!(tag.equals(signUpTAG) || tag.equals(loginTAG))) {
            headers.put("Authorization", "Bearer " + Utils.accessToken);
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(requestMethod, url, body,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        jsonObjReq.setTag(tag);
        // Adding request to request queue
        queue.add(jsonObjReq);
    }

    protected interface VolleyCallback {
        void onSuccess(JSONObject response);
        void onFailure(VolleyError error);
    }
}
