package com.github.helloiampau.janus.rtc;

import org.junit.Test;
import org.webrtc.PeerConnectionFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LocalAudioTrackFactoryTest {

  @Test
  public void itShouldCreateALocalAudioTrack() {
    PeerConnectionFactory peerConnectionFactory = mock(PeerConnectionFactory.class);

    LocalAudioTrackFactory factory = new LocalAudioTrackFactory(peerConnectionFactory);
    assertNotNull(factory.create());
  }

}