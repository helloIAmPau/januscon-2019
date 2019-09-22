package com.github.helloiampau.janus.rtc;

import org.junit.Before;
import org.junit.Test;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocalAudioTrackTest {

  private PeerConnectionFactory peerConnectionFactory = null;
  private AudioSource audioSource = null;
  private AudioTrack audioTrack = null;

  @Before
  public void SetUp() {
    this.audioSource = mock(AudioSource.class);
    this.audioTrack = mock(AudioTrack.class);

    this.peerConnectionFactory = mock(PeerConnectionFactory.class);
    when(peerConnectionFactory.createAudioSource(any(MediaConstraints.class))).thenReturn(this.audioSource);
    when(peerConnectionFactory.createAudioTrack(any(String.class), eq(this.audioSource))).thenReturn(this.audioTrack);
  }

  @Test
  public void itShouldCreateAnAudioTrack() {
    LocalAudioTrackImpl localAudioTrack = new LocalAudioTrackImpl(this.peerConnectionFactory);
    localAudioTrack.start();

    verify(this.audioTrack).setEnabled(true);
  }

  @Test
  public void itShouldStopTheAudioTrackOnStop() {
    LocalAudioTrackImpl localAudioTrack = new LocalAudioTrackImpl(this.peerConnectionFactory);
    localAudioTrack.start();
    localAudioTrack.stop();

    verify(this.audioSource).dispose();
    verify(this.audioTrack).setEnabled(false);
    verify(this.audioTrack).dispose();
  }

  @Test
  public void itShouldSkipStopIfTrackIsNotEnabled() {
    LocalAudioTrackImpl localAudioTrack = new LocalAudioTrackImpl(this.peerConnectionFactory);
    localAudioTrack.stop();
  }

  @Test
  public void itShouldStartAudioTrackOnce() {
    LocalAudioTrackImpl localAudioTrack = new LocalAudioTrackImpl(this.peerConnectionFactory);
    localAudioTrack.start();
    localAudioTrack.start();

    verify(this.audioTrack, times(1)).setEnabled(true);
  }

  @Test
  public void itShouldResetAudioTrackOnStop() {
    LocalAudioTrackImpl localAudioTrack = new LocalAudioTrackImpl(this.peerConnectionFactory);
    localAudioTrack.start();
    localAudioTrack.stop();
    localAudioTrack.start();

    verify(this.audioTrack, times(2)).setEnabled(true);
  }

  @Test
  public void itShouldGetTheNativeVideoTrack() {
    LocalAudioTrackImpl localAudioTrack = new LocalAudioTrackImpl(this.peerConnectionFactory);
    localAudioTrack.start();

    assertEquals(localAudioTrack.getNativeTrack(), this.audioTrack);
  }

}