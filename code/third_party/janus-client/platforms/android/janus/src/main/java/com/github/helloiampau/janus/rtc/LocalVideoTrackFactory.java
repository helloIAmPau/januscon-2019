package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import org.webrtc.PeerConnectionFactory;

public class LocalVideoTrackFactory {

  private PeerConnectionFactory _peerConnectionFactory = null;
  private Activity _context = null;
  private EglBaseFactory _eglBaseFactory = null;

  public LocalVideoTrackFactory(PeerConnectionFactory peerConnectionFactory, EglBaseFactory eglBaseFactory, Activity context) {
    this._peerConnectionFactory = peerConnectionFactory;
    this._context = context;
    this._eglBaseFactory = eglBaseFactory;
  }

  public LocalVideoTrackImpl create() {
    CameraEnumeratorFactory cameraEnumeratorFactory = new CameraEnumeratorFactory(this._context);

    return new LocalVideoTrackImpl(this._peerConnectionFactory, cameraEnumeratorFactory, this._eglBaseFactory, this._context);
  }

}
