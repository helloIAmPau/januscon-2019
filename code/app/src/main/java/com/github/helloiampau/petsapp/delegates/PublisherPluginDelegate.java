package com.github.helloiampau.petsapp.delegates;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.PluginDelegate;
import com.github.helloiampau.petsapp.Commands;

import org.json.JSONObject;

public class PublisherPluginDelegate extends PluginDelegate {
  @Override
  public void onEvent(JanusEvent data, ArgBundle context) {
    try {
      JSONObject event = new JSONObject(data.data());

      if(event.getString("videoroom").equals("joined")) {
        Commands.updatePets(event.getJSONArray("publishers"));

        Commands.route("room");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onMediaChanged(Media media) {}

  @Override
  public void onHangup() {}

  @Override
  public void onDetach() {}

  @Override
  public String onOffer(String sdp) {
    return sdp;
  }

  @Override
  public String onAnswer(String sdp) {
    return sdp;
  }
}
