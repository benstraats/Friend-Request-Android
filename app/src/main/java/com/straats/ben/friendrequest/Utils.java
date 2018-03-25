package com.straats.ben.friendrequest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ben_s on 2018-03-18.
 */

public class Utils {

    protected static String userName;

    //VOLLEY/HTTP call section
    //URLs section
    protected static final String baseURL = "http://friendrequest.ca/";
    protected static final String usersURL = baseURL + "users";
    protected static final String authenticationURL = baseURL + "authentication";
    protected static final String friendsURL = baseURL + "friends";
    protected static final String requestsURL = baseURL + "requests";
    protected static final String profileURL = baseURL + "profile";

    //TAGs section
    protected static final String signUpTAG = "Sign Up";
    protected static final String loginTAG = "Log In";
    protected static final String getFriendsTAG = "Get My Friends";
    protected static final String searchUsersTAG = "Search Users";
    protected static final String requestUserTAG = "Request User";
    protected static final String getRequestsTAG = "Get My Requests";
    protected static final String acceptRequestTAG = "Accept Request";
    protected static final String viewProfileTAG = "View Profile";
    protected static final String saveProfileTAG = "Save Profile";

    protected static void volleyRequest(final Context c, final String url, final String tag, int requestMethod, final HashMap<String, String> body, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(c);//maybe make singleton?

        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        if (!(tag.equals(signUpTAG) || tag.equals(loginTAG))) {
            headers.put("Authorization", "Bearer " + accessToken);
        }

        JSONObject jsonBody = null;

        if (body != null) {
            jsonBody = new JSONObject(body);
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(requestMethod, url, jsonBody,
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

    protected static String accessToken = null;

    protected static String decodeError(VolleyError error) {
        return "Generic Error";
    }

}
