package com.github.helloiampau.janus.rtc;

import com.github.helloiampau.janus.generated.LocalAudioTrack;
import com.github.helloiampau.janus.generated.LocalVideoTrack;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.RemoteAudioTrack;
import com.github.helloiampau.janus.generated.RemoteVideoTrack;

import org.webrtc.PeerConnection;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MediaImpl extends Media {

  private LocalVideoTrackImpl _localVideoTrack = null;
  private MediaView _localTrackSink = null;
  private LocalAudioTrackImpl _localAudioTrack = null;
  private RemoteVideoTrackImpl _remoteVideoTrack = null;
  private MediaView _remoteTrackSink = null;
  private RemoteAudioTrack _remoteAudioTrack = null;

  private EglBaseFactory _eglBaseFactory = null;

  private Plugin _delegate = null;
  private PeerConnection _peerConnection = null;

  public MediaImpl(EglBaseFactory eglBaseFactory, PeerConnection peerConnection, Plugin delegate) {
    this._eglBaseFactory = eglBaseFactory;
    this._delegate = delegate;
    this._peerConnection = peerConnection;
  }

  @Override
  public LocalVideoTrack localVideoTrack() {
    return _localVideoTrack;
  }

  @Override
  public LocalAudioTrack localAudioTrack() {
    return _localAudioTrack;
  }

  @Override
  public RemoteVideoTrack remoteVideoTrack() {
    return this._remoteVideoTrack;
  }

  @Override
  public RemoteAudioTrack remoteAudioTrack() {
    return this._remoteAudioTrack;
  }

  protected void localAudioTrack(LocalAudioTrackImpl localAudioTrack) {
    List<String> labels = Collections.singletonList(UUID.randomUUID().toString());
    this._peerConnection.addTrack(localAudioTrack.getNativeTrack(), labels);

    this._localAudioTrack = localAudioTrack;

    this._delegate.onMediaChanged(this);
  }

  protected void remoteVideoTrack(RemoteVideoTrackImpl remoteVideoTrack) {
    this._remoteVideoTrack = remoteVideoTrack;

    this._delegate.onMediaChanged(this);
  }

  public void localVideoTrack(LocalVideoTrackImpl localVideoTrack) {
    List<String> labels = Collections.singletonList(UUID.randomUUID().toString());
    this._peerConnection.addTrack(localVideoTrack.getNativeTrack(), labels);

    this._localVideoTrack = localVideoTrack;

    this._delegate.onMediaChanged(this);
  }

  public void remoteAudioTrack(RemoteAudioTrack remoteAudioTrack) {
    this._remoteAudioTrack = remoteAudioTrack;

    this._delegate.onMediaChanged(this);
  }

  public void addLocalTrackSink(MediaView view) {
    this._localTrackSink = view;

    this._addTrackSink(this._localVideoTrack, this._localTrackSink);
  }

  public void removeLocalTrackSink() {
    this._removeTrackSink(this._localVideoTrack, this._localTrackSink);
  }

  public void addRemoteTrackSink(MediaView view) {
    this._remoteTrackSink = view;

    this._addTrackSink(this._remoteVideoTrack, this._remoteTrackSink);
  }

  public void removeRemoteTrackSink() {
    this._removeTrackSink(this._remoteVideoTrack, this._remoteTrackSink);
  }

  private void _addTrackSink(JanusVideoTrack track, MediaView view) {
    if(track == null) {
      return;
    }

    view.addTrack(track, this._eglBaseFactory.create().getEglBaseContext());
  }

  private void _removeTrackSink(JanusVideoTrack track, MediaView view) {
    if(track == null || view == null) {
      return;
    }

    view.removeTrack(track);
  }

}
