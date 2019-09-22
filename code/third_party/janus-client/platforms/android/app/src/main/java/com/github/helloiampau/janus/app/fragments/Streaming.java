package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.JanusEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import helloiampau.github.com.janus.R;

public class Streaming extends PluginFragment {

  private FeedList _feedList;
  private Theater _theater;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.streaming, container, false);

    this._feedList = new FeedList();
    this._theater = new Theater();

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("feed").observe(this, f -> {
      Fragment content = null;

      if(f == null) {
        content = this._feedList;
      } else {
        content = this._theater;
      }

      FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
      transaction.replace(R.id.content, content);
      transaction.commit();
    });
  }

  @Override
  protected void onEvent(JanusEvent data, ArgBundle context) {
    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    try {
      JSONObject event = new JSONObject(data.data());

      ArrayList<Status.Feed> feedList = new ArrayList<>();
      JSONArray jsonArray = new JSONObject(data.data()).getJSONArray("list");

      for(int index = 0; index < jsonArray.length(); index++) {
        JSONObject obj = jsonArray.getJSONObject(index);
        feedList.add(status.feed(obj));
      }

      Status.Payload payload = status.payload();
      payload.value("list", feedList);

      status.dispatch("update-feeds", payload);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
