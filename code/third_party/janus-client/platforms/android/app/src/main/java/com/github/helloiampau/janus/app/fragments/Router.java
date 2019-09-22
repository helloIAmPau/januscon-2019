package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.helloiampau.janus.app.Status;

import java.util.HashMap;

import helloiampau.github.com.janus.R;

public class Router extends Fragment {

  private HashMap<String, Fragment> _locations;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    this._locations = new HashMap<>();
    this._locations.put("settings", new Settings());
    this._locations.put("janus.plugin.echotest", new Echotest());
    this._locations.put("janus.plugin.streaming", new Streaming());
    this._locations.put("janus.plugin.videoroom", new Videoroom());


    return inflater.inflate(R.layout.router, container, false);
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("location").observe(this, l -> {
      Fragment newDestination = null;

      if(this._locations.containsKey(l)) {
        newDestination = this._locations.get(l);
      }

      if(newDestination == null) {
        return;
      }

      FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
      transaction.replace(R.id.main_content, newDestination);
      transaction.commit();
    });
  }


}
