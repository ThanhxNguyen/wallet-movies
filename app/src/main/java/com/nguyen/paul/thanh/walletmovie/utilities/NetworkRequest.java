package com.nguyen.paul.thanh.walletmovie.utilities;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * This helper class uses Volley to make network request and it uses singleton pattern
 * to make sure only single Volley instance is being initialized (the purpose is to reduce
 * memory consumption).
 * Reference: https://developer.android.com/training/volley/requestqueue.html
 */
public class NetworkRequest {

    private RequestQueue mRequestQueue;
    private static NetworkRequest mInstance;
    private Context mContext;

    private NetworkRequest(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized NetworkRequest getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new NetworkRequest(context);
        }

        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if(mRequestQueue == null) {
            /**
             * application context is needed for creating new volley request queue. This
             * ensures hat the RequestQueue will last for the lifetime of your app, instead
             * of being recreated every time the activity is recreated (for example, when the user rotates the device).
             */
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag) {
        if(TextUtils.isEmpty(tag)) throw new AssertionError("Tag cannot be empty!");
        //set tag for request object
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests(String tag) {
        if(TextUtils.isEmpty(tag)) {
            throw new AssertionError("Tag cannot be empty!");
        } else if(mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


}
