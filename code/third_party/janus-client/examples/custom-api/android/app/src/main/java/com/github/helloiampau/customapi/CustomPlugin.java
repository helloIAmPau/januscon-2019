package com.github.helloiampau.customapi;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.Candidate;
import com.github.helloiampau.janus.generated.Constraints;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Jsep;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Peer;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.PluginDelegate;
import com.github.helloiampau.janus.generated.Signaling;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomPlugin extends Plugin {

  private PluginDelegate _delegate;
  private String _baseUrl;
  private String _id;

  private Peer _peer;

  public CustomPlugin(String baseUrl) {
    this._baseUrl = baseUrl;
  }

  @Override
  public void setDelegate(PluginDelegate pluginDelegate) {
    this._delegate = pluginDelegate;
  }

  @Override
  public void init(Signaling signaling, Peer peer) {
    Constraints constraints = new Constraints(true, true, true, true);
    this._peer = peer;

    peer.createOffer(constraints, ArgBundle.create());
  }

  @Override
  public void onOffer(String s, ArgBundle argBundle) {
    try {
      JSONObject body = new JSONObject();
      body.put("type", "offer");
      body.put("sdp", s);

      HttpClient.get().post(this._baseUrl + "/call", body, response -> {
        try {
          JSONObject content = new JSONObject(response);

          this._id = content.getString("id");
          String answer = content.getJSONObject("jsep").getString("sdp");

          this._peer.setLocalDescription(Jsep.create("offer", s));
          this._peer.setRemoteDescription(Jsep.create("answer", answer));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }, error -> {});
    } catch (Exception e) {}
  }

  @Override
  public void onIceCandidate(Candidate candidate) {
    try {
      JSONObject body = new JSONObject();
      if (candidate.getCompleted() == false) {
        body.put("sdpMid", candidate.getSdpMid());
        body.put("sdpMLineIndex", candidate.getSdpMLineIndex());
        body.put("candidate", candidate.getCandidate());
      } else {
        body.put("completed", true);
      }

      HttpClient.get().post(this._baseUrl + "/trickle/" + this._id, body, response -> {}, error -> {});
    } catch (Exception e) {}
  }

  @Override
  public void hangup() {
    try {
      HttpClient.get().post(this._baseUrl + "/hangup/" + this._id, response -> {
      }, error -> {
      });
    } catch (Exception e) {}
  }

  @Override
  public void onMediaChanged(Media media) {
    this._delegate.onMediaChanged(media);
  }

  @Override
  public void detach() {

  }

  @Override
  public void dispatch(String s, ArgBundle argBundle) {

  }

  @Override
  public void onEvent(JanusEvent janusEvent, ArgBundle argBundle) {

  }

  @Override
  public void onHangup() {

  }

  @Override
  public void onDetach() {

  }

  @Override
  public void onAnswer(String s, ArgBundle argBundle) {

  }
}
