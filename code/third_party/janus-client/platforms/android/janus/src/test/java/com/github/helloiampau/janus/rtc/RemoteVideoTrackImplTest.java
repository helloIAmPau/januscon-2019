package com.github.helloiampau.janus.rtc;

import org.junit.Test;
import org.webrtc.VideoTrack;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class RemoteVideoTrackImplTest {

  @Test
  public void itShouldWrapARemoteVideoTrack() {
    VideoTrack track = mock(VideoTrack.class);

    RemoteVideoTrackImpl remoteVideoTrack = new RemoteVideoTrackImpl(track);
    assertEquals(remoteVideoTrack.getNativeTrack(), track);
  }

}