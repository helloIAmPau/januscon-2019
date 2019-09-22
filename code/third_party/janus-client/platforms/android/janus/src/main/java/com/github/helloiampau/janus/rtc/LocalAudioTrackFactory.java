package com.github.helloiampau.janus.rtc;

import org.webrtc.PeerConnectionFactory;

public class LocalAudioTrackFactory {

  private PeerConnectionFactory _peerConnectionFactory = null;

  public LocalAudioTrackFactory(PeerConnectionFactory peerConnectionFactory) {
    this._peerConnectionFactory = peerConnectionFactory;
  }

  public LocalAudioTrackImpl create() {
    return new LocalAudioTrackImpl(this._peerConnectionFactory);
  }
}
