package com.github.helloiampau.petsapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.helloiampau.petsapp.Commands;
import com.github.helloiampau.petsapp.R;
import com.github.helloiampau.petsapp.Status;

public class LoadingFragment extends Fragment {
  private Button _retryButton;
  private ProgressBar _spinner;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Status.get().error().observe(this, (message) -> {
      if(message == null) {
        return;
      }

      this._spinner.setVisibility(View.GONE);
      this._retryButton.setVisibility(View.VISIBLE);

      Toast.makeText(this.getContext(), message, Toast.LENGTH_LONG).show();
    });

    Status.get().session().observe(this, (janus) -> {
      if(janus == null) {
        return;
      }

      Commands.route("login");
    });

    View view = inflater.inflate(R.layout.loading, container, false);
    this._retryButton = view.findViewById(R.id.retry);
    this._retryButton.setOnClickListener((v) -> {
      this._retryButton.setVisibility(View.GONE);
      this._spinner.setVisibility(View.VISIBLE);

      Commands.init();
    });

    this._spinner = view.findViewById(R.id.loading);

    Commands.init();

    return view;
  }

}
