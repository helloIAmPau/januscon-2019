package com.github.helloiampau.janus.rtc;

import com.github.helloiampau.janus.generated.LocalAudioTrack;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;

import java.util.UUID;

public class LocalAudioTrackImpl extends LocalAudioTrack {

  private PeerConnectionFactory _peerConnectionFactory = null;

  // current session
  private AudioSource _audioSource = null;
  private AudioTrack _audioTrack = null;

  public LocalAudioTrackImpl(PeerConnectionFactory peerConnectionFactory) {
    this._peerConnectionFactory = peerConnectionFactory;
  }

  @Override
  public void stop() {
    if(this._audioTrack == null) {
      return;
    }

    this._audioTrack.setEnabled(false);
    this._audioTrack.dispose();
    this._audioTrack = null;

    this._audioSource.dispose();
    this._audioSource = null;
  }

  @Override
  public void start() {
    if(this._audioTrack != null) {
      return;
    }

    this._audioSource = this._peerConnectionFactory.createAudioSource(new MediaConstraints());
    this._audioTrack = this._peerConnectionFactory.createAudioTrack(UUID.randomUUID().toString(), this._audioSource);
    this._audioTrack.setEnabled(true);
  }

  public AudioTrack getNativeTrack() {
    return this._audioTrack;
  }

}
