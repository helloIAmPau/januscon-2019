package com.github.helloiampau.janus.rtc;

import com.github.helloiampau.janus.generated.Plugin;

import org.junit.Before;
import org.junit.Test;
import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.PeerConnection;
import org.webrtc.VideoTrack;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaImplTest {

  private EglBaseFactory eglBaseFactory = null;
  private EglBase eglBase = null;
  private EglBase.Context eglContext = null;
  private Plugin delegate = null;
  private PeerConnection peerConnection = null;

  @Before
  public void setUp() {
    this.eglContext = mock(EglBase.Context.class);

    this.eglBase = mock(EglBase.class);
    when(this.eglBase.getEglBaseContext()).thenReturn(this.eglContext);

    this.eglBaseFactory = mock(EglBaseFactory.class);
    when(this.eglBaseFactory.create()).thenReturn(this.eglBase);

    this.delegate = mock(Plugin.class);

    this.peerConnection = mock(PeerConnection.class);
  }

  @Test
  public void itShouldHoldALocalVideoObject() {
    LocalVideoTrackImpl track = mock(LocalVideoTrackImpl.class);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.localVideoTrack(track);

    assertEquals(media.localVideoTrack(), track);
    verify(this.delegate).onMediaChanged(media);
  }

  @Test
  public void itShouldHoldALocalAudioObject() {
    AudioTrack nativeAudioTrack = mock(AudioTrack.class);
    LocalAudioTrackImpl track = mock(LocalAudioTrackImpl.class);
    when(track.getNativeTrack()).thenReturn(nativeAudioTrack);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.localAudioTrack(track);

    assertEquals(media.localAudioTrack(), track);
    verify(this.delegate).onMediaChanged(media);
  }

  @Test
  public void itShouldHoldARemoteVideoObject() {
    RemoteVideoTrackImpl track = mock(RemoteVideoTrackImpl.class);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.remoteVideoTrack(track);

    assertEquals(media.remoteVideoTrack(), track);
    verify(this.delegate).onMediaChanged(media);
  }

  @Test
  public void itShouldSetandUnsetASinkToRemoteVideoTrack() {
    RemoteVideoTrackImpl videoTrack = mock(RemoteVideoTrackImpl.class);

    MediaView view = mock(MediaView.class);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.remoteVideoTrack(videoTrack);
    media.addRemoteTrackSink(view);

    verify(view).addTrack(videoTrack, this.eglContext);

    media.removeRemoteTrackSink();

    verify(view).removeTrack(videoTrack);
  }

  @Test
  public void itShouldSetandUnsetASinkToLocalVideoTrack() {
    LocalVideoTrackImpl videoTrack = mock(LocalVideoTrackImpl.class);
    VideoTrack nativeVideoTrack = mock(VideoTrack.class);
    when(videoTrack.getNativeTrack()).thenReturn(nativeVideoTrack);

    MediaView view = mock(MediaView.class);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.localVideoTrack(videoTrack);
    media.addLocalTrackSink(view);

    verify(view).addTrack(videoTrack, this.eglContext);

    media.removeLocalTrackSink();

    verify(view).removeTrack(videoTrack);
  }

  @Test
  public void itShouldSkipSinkSettingIfRemoteTrackIsNotSet() {
    MediaView view = mock(MediaView.class);
    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.addRemoteTrackSink(view);
    media.removeRemoteTrackSink();

    verify(view, times(0)).addTrack(null, this.eglContext);
    verify(view, times(0)).removeTrack(null);
  }

  @Test
  public void itShouldSkipSinkSettingIfLocalTrackIsNotSet() {
    MediaView view = mock(MediaView.class);
    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.addLocalTrackSink(view);
    media.removeLocalTrackSink();

    verify(view, times(0)).addTrack(null, this.eglContext);
    verify(view, times(0)).removeTrack(null);
  }

  @Test
  public void itShouldSkipSinkRemoveIfSinkIsNotSet() {
    LocalVideoTrackImpl localVideoTrack = mock(LocalVideoTrackImpl.class);
    RemoteVideoTrackImpl remoteVideoTrack = mock(RemoteVideoTrackImpl.class);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.localVideoTrack(localVideoTrack);
    media.remoteVideoTrack(remoteVideoTrack);

    media.removeRemoteTrackSink();
    media.removeLocalTrackSink();
  }

  @Test
  public void itShouldRegisterARemoteAudioTrack() {
    RemoteAudioTrackImpl audio = mock(RemoteAudioTrackImpl.class);

    MediaImpl media = new MediaImpl(this.eglBaseFactory, this.peerConnection, this.delegate);
    media.remoteAudioTrack(audio);

    assertEquals(media.remoteAudioTrack(), audio);
    verify(this.delegate).onMediaChanged(media);
  }

}