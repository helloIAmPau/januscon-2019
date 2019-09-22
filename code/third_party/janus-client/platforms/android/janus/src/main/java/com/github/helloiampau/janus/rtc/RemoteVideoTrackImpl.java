package com.github.helloiampau.janus.rtc;

import com.github.helloiampau.janus.generated.RemoteVideoTrack;

import org.webrtc.VideoTrack;

public class RemoteVideoTrackImpl extends RemoteVideoTrack implements JanusVideoTrack {

  private VideoTrack _videoTrack = null;

  protected RemoteVideoTrackImpl(VideoTrack videoTrack) {
    this._videoTrack = videoTrack;
  }

  @Override
  public VideoTrack getNativeTrack() {
    return this._videoTrack;
  }

}
