package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.PluginDelegate;

public abstract class PluginFragment extends Fragment {

  private PluginDelegate _delegate;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    PluginFragment self = this;

    this._delegate = new PluginDelegate() {
      @Override
      public void onEvent(JanusEvent data, ArgBundle context) {
        self.onEvent(data, context);
      }

      @Override
      public void onMediaChanged(Media media) {
        Status.Payload payload = status.payload();
        payload.value("media", media);

        status.dispatch("enable-media", payload);
      }

      @Override
      public void onHangup() {
        status.dispatch("disable-media");
      }

      @Override
      public void onDetach() {

      }

      @Override
      public String onOffer(String sdp) {
        return sdp;
      }

      @Override
      public String onAnswer(String sdp) {
        return sdp;
      }
    };
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("session").observe(this, s -> {
      if(s == null) {
        return;
      }

      Plugin session = (Plugin) s;
      session.setDelegate(this._delegate);
    });
  }

  protected abstract void onEvent(JanusEvent data, ArgBundle context);

}
