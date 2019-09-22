package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.JanusEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import helloiampau.github.com.janus.R;

public class Videoroom extends PluginFragment {

  private RoomList _roomList;
  private Room _room;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    this._room = new Room();
    this._roomList = new RoomList();

    return inflater.inflate(R.layout.videoroom, container, false);
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("room").observe(this, r -> {
      Fragment content = null;

      if (r == null) {
        content = this._roomList;
      } else {
        content = this._room;
      }

      FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
      transaction.replace(R.id.vr_content, content);
      transaction.commit();
    });
  }

  @Override
  protected void onEvent(JanusEvent data, ArgBundle context) {
    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    try {
      JSONObject event = new JSONObject(data.data());
      String request = context.getString("request");

      // ToDo(@helloiampau): move this to actions
      if (request.equals("list")) {
        ArrayList<Status.Room> roomList = new ArrayList<>();
        JSONArray jsonArray = event.getJSONArray("list");

        for (int index = 0; index < jsonArray.length(); index++) {
          JSONObject obj = jsonArray.getJSONObject(index);
          roomList.add(status.room(obj));
        }

        Status.Payload payload = status.payload();
        payload.value("list", roomList);

        status.dispatch("update-rooms", payload);

        return;
      }

      if(event.has("publishers")) {
        Status.Payload payload = status.payload();
        payload.value("event", event);
        status.dispatch("update-room", payload);

        return;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

}
