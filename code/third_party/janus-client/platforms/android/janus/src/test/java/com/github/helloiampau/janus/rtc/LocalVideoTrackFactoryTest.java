package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import org.junit.Test;
import org.webrtc.PeerConnectionFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LocalVideoTrackFactoryTest {

  @Test
  public void itShouldCreateANewLocalVideoTrack() {
    PeerConnectionFactory peerConnectionFactory = mock(PeerConnectionFactory.class);
    Activity context = mock(Activity.class);
    EglBaseFactory eglBaseFactory = mock(EglBaseFactory.class);

    LocalVideoTrackFactory factory = new LocalVideoTrackFactory(peerConnectionFactory, eglBaseFactory, context);
    assertNotNull(factory.create());
  }

}