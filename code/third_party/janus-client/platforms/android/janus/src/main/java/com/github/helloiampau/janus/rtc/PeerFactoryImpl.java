package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import com.github.helloiampau.janus.generated.JanusConf;
import com.github.helloiampau.janus.generated.Peer;
import com.github.helloiampau.janus.generated.PeerFactory;
import com.github.helloiampau.janus.generated.Plugin;

import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;

public class PeerFactoryImpl extends PeerFactory {

  private Activity _context = null;
  private EglBaseFactory _eglBaseFactory = null;

  public PeerFactoryImpl(EglBaseFactory eglBaseFactory, Activity context) {
    PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory
        .InitializationOptions
        .builder(context.getApplicationContext())
        .createInitializationOptions();
    PeerConnectionFactory.initialize(options);

    this._eglBaseFactory = eglBaseFactory;
    this._context = context;
  }

  @Override
  public Peer create(JanusConf conf, Plugin delegate) {
    VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(this._eglBaseFactory.create().getEglBaseContext());
    VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(this._eglBaseFactory.create().getEglBaseContext(), true, true);

    PeerConnectionFactory peerConnectionFactory = PeerConnectionFactory.builder()
        .setVideoDecoderFactory(decoderFactory)
        .setVideoEncoderFactory(encoderFactory)
        .createPeerConnectionFactory();

    LocalVideoTrackFactory localVideoTrackFactory = new LocalVideoTrackFactory(peerConnectionFactory, this._eglBaseFactory, this._context);
    LocalAudioTrackFactory localAudioTrackFactory = new LocalAudioTrackFactory(peerConnectionFactory);
    UserMediaFactory userMediaFactory = new UserMediaFactory(localVideoTrackFactory, localAudioTrackFactory, this._eglBaseFactory, this._context);

    return new PeerImpl(conf, peerConnectionFactory, userMediaFactory, this._eglBaseFactory, this._context, delegate);
  }

}
