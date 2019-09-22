package com.github.helloiampau.janus;

import com.github.helloiampau.janus.generated.IceServer;
import com.github.helloiampau.janus.generated.JanusConf;
import java.util.ArrayList;
import java.util.HashMap;

public class JanusConfImpl extends JanusConf {
  private String _url;
  private ArrayList<IceServer> _iceServers = new ArrayList<IceServer>() {{
    add(new IceServer("stun:stun.l.google.com:19302", null, null));
    add(new IceServer("stun:stun1.l.google.com:19302", null, null));
    add(new IceServer("stun:stun2.l.google.com:19302", null, null));
    add(new IceServer("stun:stun3.l.google.com:19302", null, null));
    add(new IceServer("stun:stun4.l.google.com:19302", null, null));
  }};
  private HashMap<String, String> _webrtc = new HashMap<>();

  @Override
  public String url() {
    return this._url;
  }

  public void url(String url) {
    this._url = url;
  }

  @Override
  public ArrayList<IceServer> iceServers() {
    return this._iceServers;
  }

  public void iceServers(ArrayList<IceServer> iceServers) {
    this._iceServers = iceServers;
  }

  @Override
  public HashMap<String, String> webrtc() {
    return this._webrtc;
  }

  public void webrtc(HashMap<String, String> webrtc) {
    this._webrtc = webrtc;
  }
}
