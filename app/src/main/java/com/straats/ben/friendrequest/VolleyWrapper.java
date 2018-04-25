package com.straats.ben.friendrequest;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyWrapper {

    private static VolleyWrapper instance = null;
    private static RequestQueue queue;

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

        if (!(tag.equals(Utils.signUpTAG) || tag.equals(Utils.loginTAG))) {
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
