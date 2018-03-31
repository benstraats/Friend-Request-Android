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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ben_s on 2018-03-18.
 */

public class Utils {

    protected static String userEmail;
    protected static String userID;
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

    protected static void volleyRequest(final Context c, final String url, final String tag, int requestMethod, final JSONObject body, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(c);//maybe make singleton?

        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");

        if (!(tag.equals(signUpTAG) || tag.equals(loginTAG))) {
            headers.put("Authorization", "Bearer " + accessToken);
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

    protected static String accessToken = null;

    protected static String decodeError(VolleyError error) {
        try {
            JSONObject body = new JSONObject(new String(error.networkResponse.data,"UTF-8"));
            return body.getString("message");
        } catch (UnsupportedEncodingException e) {
            return "Generic Error";
        } catch (JSONException e) {
            return "Generic Error";
        }
    }

    protected static String getUserInfo(String usersID, JSONObject users) {
        try {
            JSONObject data = users.getJSONObject("data");
            int total = data.getInt("total");
            int limit = data.getInt("limit");

            JSONArray userData = data.getJSONArray("data");

            int num = Math.min(total, limit);

            for (int i=0; i<num; i++) {
                if (userData.getJSONObject(i).getString("_id").equals(usersID)) {
                    JSONObject correctUser = userData.getJSONObject(i);
                    return correctUser.getString("name") + " (" + correctUser.getString("email") + ")";
                }
            }
        } catch (JSONException e) {
            return "Failed to parse user";
        }
        return "User data not found";
    }

}
