package com.straats.ben.friendrequest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.VolleyError;

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

}
