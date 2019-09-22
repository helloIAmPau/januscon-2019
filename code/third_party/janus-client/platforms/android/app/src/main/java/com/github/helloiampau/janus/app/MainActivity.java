package com.github.helloiampau.janus.app;

import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import helloiampau.github.com.janus.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Status status = ViewModelProviders.of(this).get(Status.class);
    Actions.register(status, this);

    this.setContentView(R.layout.main_activity);

    boolean isDebuggable =  ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
    if(isDebuggable) {
      ImageButton inspectButton = this.findViewById(R.id.inspect_button);
      inspectButton.setVisibility(View.VISIBLE);
      inspectButton.setOnClickListener(v -> {
        status.dispatch("inspect");
      });
    }

    status.dispatch("init");
  }

  @Override
  public void onBackPressed() {
    Status status = ViewModelProviders.of(this).get(Status.class);
    status.dispatch("go-back");
  }

}