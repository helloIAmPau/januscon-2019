package com.github.helloiampau.petsapp.delegates;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.PluginDelegate;
import com.github.helloiampau.petsapp.Commands;

public class SubscriberPluginDelegate extends PluginDelegate {
  private String _id;

  public SubscriberPluginDelegate(String id) {
    this._id = id;
  }

  @Override
  public void onEvent(JanusEvent data, ArgBundle context) {
  }

  @Override
  public void onMediaChanged(Media media) {
    Commands.updatePetMedia(this._id, media);
  }

  @Override
  public void onHangup() {

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
}
