package com.github.helloiampau.customapi;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class HttpClient {

  private static HttpClient _instance;
  private RequestQueue _requestQueue;

  private static Object semaphore = new Object();

  private HttpClient(Activity context) {
    this._requestQueue = Volley.newRequestQueue(context);
  }

  public void post(String url, Response.Listener<String> onResponse, Response.ErrorListener onError) {
    this.post(url, null, onResponse, onError);
  }

  public void post(String url, JSONObject body, Response.Listener<String> onResponse, Response.ErrorListener onError) {
    this._request(Request.Method.POST, url, body, onResponse, onError);
  }

  private void _request(int method, String url, JSONObject body, Response.Listener<String> onResponse, Response.ErrorListener onError) {
    StringRequest request = new StringRequest(method, url, onResponse, onError) {
      @Override
      public byte[] getBody() {
        if(body == null) {
          return null;
        }

        return body.toString().getBytes();
      }

      @Override
      public String getBodyContentType() {
        return "application/json";
      }
    };

    this._requestQueue.add(request);
  }

  public static void init(Activity context) {
    synchronized (HttpClient.semaphore) {
      if(HttpClient._instance == null) {
        HttpClient._instance = new HttpClient(context);
      }
    }
  }

  synchronized public static HttpClient get() throws Exception {
    synchronized (HttpClient.semaphore) {
      if(HttpClient._instance == null) {
        throw new Exception("Initialize the client first");
      }

      return HttpClient._instance;
    }
  }

}
