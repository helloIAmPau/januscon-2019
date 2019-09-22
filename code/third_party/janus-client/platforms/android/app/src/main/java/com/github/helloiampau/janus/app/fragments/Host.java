package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ReadyState;

import helloiampau.github.com.janus.R;

public class Host extends Fragment {

  private EditText hostEditText;
  private Button connectButton;
  private TextView statusView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.host, container, false);
    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    this.hostEditText = layout.findViewById(R.id.hostEditText);
    this.hostEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        Status.Payload payload = status.payload();
        payload.value("host", editable.toString());

        status.dispatch("update-host", payload);
      }
    });

    this.connectButton = layout.findViewById(R.id.connectButton);
    this.connectButton.setOnClickListener(v -> {
      Button button = (Button) v;

      if(button.getText().toString().equals("Connect")) {
        status.dispatch("connect");
      } else {
        status.dispatch("disconnect");
      }
    });

    this.statusView = layout.findViewById(R.id.statusView);

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("readyState").observe(this, r -> {
      this.statusView.setText(r.toString());

      if(r == ReadyState.READY) {
        this.connectButton.setText("Disconnect");
        this.connectButton.setEnabled(true);
        this.hostEditText.setEnabled(false);
      } else if(r == ReadyState.OFF || r == ReadyState.ERROR) {
        this.connectButton.setText("Connect");
        this.connectButton.setEnabled(true);
        this.hostEditText.setEnabled(true);
      } else {
        this.connectButton.setEnabled(false);
        this.hostEditText.setEnabled(false);
      }
    });

    status.get("host").observe(this, h -> {
      if(this.hostEditText != null && !this.hostEditText.getText().toString().equals(h)) {
        this.hostEditText.setText((String) h);
      }
    });
  }

}
