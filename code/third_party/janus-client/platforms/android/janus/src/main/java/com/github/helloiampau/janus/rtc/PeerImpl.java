package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.CameraDevice;
import com.github.helloiampau.janus.generated.Candidate;
import com.github.helloiampau.janus.generated.Constraints;
import com.github.helloiampau.janus.generated.IceServer;
import com.github.helloiampau.janus.generated.JanusConf;
import com.github.helloiampau.janus.generated.Jsep;
import com.github.helloiampau.janus.generated.Peer;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.List;

public class PeerImpl extends Peer implements PeerConnection.Observer {
  private PeerConnectionFactory _peerConnectionFactory = null;
  private UserMediaFactory _userMediaFactory = null;
  private EglBaseFactory _eglBaseFactory = null;

  private Plugin _delegate = null;
  private Activity _context = null;

  private MediaImpl _media = null;
  private PeerConnection _peerConnection = null;

  private JanusConf _conf = null;

  protected PeerImpl(JanusConf conf, PeerConnectionFactory peerConnectionFactory, UserMediaFactory userMediaFactory, EglBaseFactory eglBaseFactory, Activity context, Plugin delegate) {
    this._peerConnectionFactory = peerConnectionFactory;
    this._userMediaFactory = userMediaFactory;
    this._eglBaseFactory = eglBaseFactory;

    this._delegate = delegate;
    this._context = context;

    this._conf = conf;
  }

  @Override
  public void createOffer(Constraints constraints, ArgBundle context) {
    this._init();

    this._userMediaFactory.getUserMedia(constraints, m -> {
      this._onUserMedia(m, context);

      PeerImpl self = this;

      this._peerConnection.createOffer(new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
          self._delegate.onOffer(sessionDescription.description, context);
        }

        @Override
        public void onSetSuccess() {}

        @Override
        public void onCreateFailure(String s) {}

        @Override
        public void onSetFailure(String s) {}
      }, this._toMediaConstraints(constraints));
    }, e ->{});
  }

  @Override
  public void createAnswer(Constraints constraints, ArgBundle context) {
    this._userMediaFactory.getUserMedia(constraints, m -> {
      this._onUserMedia(m, context);

      PeerImpl self = this;

      this._peerConnection.createAnswer(new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
          self._delegate.onAnswer(sessionDescription.description, context);
        }

        @Override
        public void onSetSuccess() {}

        @Override
        public void onCreateFailure(String s) {}

        @Override
        public void onSetFailure(String s) {}
      }, this._toMediaConstraints(constraints));
    }, e -> {});
  }

  @Override
  public void setLocalDescription(Jsep jsep) {
    this._peerConnection.setLocalDescription(new SdpObserver() {
      @Override
      public void onCreateSuccess(SessionDescription sessionDescription) {}

      @Override
      public void onSetSuccess() {}

      @Override
      public void onCreateFailure(String s) {}

      @Override
      public void onSetFailure(String s) {}
    }, this._toSessionDescription(jsep));
  }

  @Override
  public void setRemoteDescription(Jsep jsep) {
    this._init();

    this._peerConnection.setRemoteDescription(new SdpObserver() {
      @Override
      public void onCreateSuccess(SessionDescription sessionDescription) {}

      @Override
      public void onSetSuccess() {}

      @Override
      public void onCreateFailure(String s) {}

      @Override
      public void onSetFailure(String s) {}
    }, this._toSessionDescription(jsep));
  }

  @Override
  public void addIceCandidate(Candidate candidate) {
    IceCandidate iceCandidate = new IceCandidate(candidate.getSdpMid(), candidate.getSdpMLineIndex(), candidate.getCandidate());

    this._peerConnection.addIceCandidate(iceCandidate);
  }

  @Override
  public void onIceCandidate(IceCandidate iceCandidate) {
    Candidate candidate = new Candidate(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp, false);

    this._delegate.onIceCandidate(candidate);
  }

  @Override
  public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
    if(iceGatheringState != PeerConnection.IceGatheringState.COMPLETE) {
      return;
    }

    this._delegate.onIceCandidate(new Candidate("", -999, "", true));
  }

  @Override
  public void onAddStream(MediaStream mediaStream) {
    if(mediaStream.videoTracks.size() != 0) {
      RemoteVideoTrackImpl track = new RemoteVideoTrackImpl(mediaStream.videoTracks.get(0));
      this._media.remoteVideoTrack(track);
    }

    if(mediaStream.audioTracks.size() != 0) {
      RemoteAudioTrackImpl track = new RemoteAudioTrackImpl(this._context);
      this._media.remoteAudioTrack(track);
    }
  }

  @Override
  public Media getMedia() {
    return this._media;
  }

  @Override
  public void close() {
    if(this._peerConnection == null) {
      return;
    }

    this._peerConnection.close();
    this._peerConnection.dispose();

    this._peerConnection = null;
    this._media = null;
  }

  private void _onUserMedia(UserMedia media, ArgBundle context) {
    if(media.audio() != null) {
      LocalAudioTrackImpl audio = media.audio();
      audio.start();
      this._media.localAudioTrack(audio);
    }

    if(media.video() != null) {
      LocalVideoTrackImpl video = media.video();

      int height = context.getInt("height") == -999 ? LocalVideoTrackImpl.DEFAULT_HEIGHT : context.getInt("height");
      int width = context.getInt("width") == -999 ? LocalVideoTrackImpl.DEFAULT_WIDTH : context.getInt("width");
      int fps = context.getInt("fps") == -999 ? LocalVideoTrackImpl.DEFAULT_FPS : context.getInt("fps");
      int camera = context.getInt("camera") == -999 ? 0 : context.getInt("camera");
      video.start(CameraDevice.values()[camera], width, height, fps);

      this._media.localVideoTrack(video);
    }
  }

  private MediaConstraints _toMediaConstraints(Constraints constraints) {
    MediaConstraints mediaConstraints = new MediaConstraints();

    if(constraints.getOfferToReceiveVideo() == true) {
      mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    if(constraints.getOfferToReceiveAudio() == true) {
      mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
    }

    return mediaConstraints;
  }

  private SessionDescription _toSessionDescription(Jsep jsep) {
    SessionDescription.Type type;

    if(jsep.type().equals("offer")) {
      type = SessionDescription.Type.OFFER;
    } else {
      type = SessionDescription.Type.ANSWER;
    }

    return new SessionDescription(type, jsep.sdp());
  }

  private void _init() {
    if(this._peerConnection != null) {
      return;
    }

    List<PeerConnection.IceServer> servers = new ArrayList<>();
    for(IceServer server : this._conf.iceServers()) {
      PeerConnection.IceServer.Builder builder = PeerConnection.IceServer.builder(server.getUrl());

      if(server.getUsername() != null) {
        builder = builder.setUsername(server.getUsername());
      }

      if(server.getPassword() != null) {
        builder = builder.setPassword(server.getPassword());
      }

      servers.add(builder.createIceServer());
    }

    PeerConnection.RTCConfiguration rtcConf = new PeerConnection.RTCConfiguration(servers);

    this._peerConnection = this._peerConnectionFactory.createPeerConnection(rtcConf, this);
    this._media = new MediaImpl(this._eglBaseFactory, this._peerConnection, this._delegate);
  }

  @Override
  public void onSignalingChange(PeerConnection.SignalingState signalingState) {

  }

  @Override
  public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

  }

  @Override
  public void onIceConnectionReceivingChange(boolean b) {

  }

  @Override
  public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

  }

  @Override
  public void onRemoveStream(MediaStream mediaStream) {

  }

  @Override
  public void onDataChannel(DataChannel dataChannel) {

  }

  @Override
  public void onRenegotiationNeeded() {

  }

  @Override
  public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

  }

  @Override
  public void onTrack(RtpTransceiver transceiver) {

  }
}
