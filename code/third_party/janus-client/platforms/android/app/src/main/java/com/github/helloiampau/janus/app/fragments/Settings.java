package com.github.helloiampau.janus.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import helloiampau.github.com.janus.R;

public class Settings extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    return inflater.inflate(R.layout.settings, container, false);
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);
  }

}
