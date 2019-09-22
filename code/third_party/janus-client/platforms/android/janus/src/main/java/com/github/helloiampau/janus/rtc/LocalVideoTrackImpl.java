package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import com.github.helloiampau.janus.generated.CameraDevice;
import com.github.helloiampau.janus.generated.LocalVideoTrack;

import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LocalVideoTrackImpl extends LocalVideoTrack implements JanusVideoTrack {

  // starting in Full-HD
  public static int DEFAULT_WIDTH = 1280;
  public static int DEFAULT_HEIGHT = 720;
  public static int DEFAULT_FPS = 30;

  private CameraEnumerator _cameraEnumerator = null;
  private PeerConnectionFactory _peerConnectionFactory = null;
  private EglBase _eglBase = null;
  private Activity _context = null;

  // current session
  private VideoTrack _videoTrack = null;
  private CameraVideoCapturer _capturer = null;

  public LocalVideoTrackImpl(PeerConnectionFactory peerConnectionFactory, CameraEnumeratorFactory cameraEnumeratorFactory, EglBaseFactory eglBaseFactory, Activity context) {
    this._cameraEnumerator = cameraEnumeratorFactory.create();
    this._peerConnectionFactory = peerConnectionFactory;
    this._eglBase = eglBaseFactory.create();
    this._context = context;
  }

  @Override
  public void stop() {
    if(this._videoTrack == null) {
      return;
    }

    try {
      this._capturer.stopCapture();
    } catch (Exception e) {}
    this._capturer.dispose();
    this._capturer = null;

    this._videoTrack.setEnabled(false);
    this._videoTrack.dispose();
    this._videoTrack = null;
  }

  @Override
  public void start(CameraDevice camera, int width, int height, int fps) {
    if(this._videoTrack != null) {
      return;
    }

    boolean isFrontCamera = camera == CameraDevice.FRONT;

    String[] devices = this._cameraEnumerator.getDeviceNames();
    String cameraName = Arrays.stream(devices).filter(name -> {
      return this._cameraEnumerator.isFrontFacing(name) == isFrontCamera;
    }).findFirst().orElse(null);

    if(cameraName == null) {
      cameraName = devices[0];
    }

    this._capturer = this._cameraEnumerator.createCapturer(cameraName, null);
    VideoSource source = this._peerConnectionFactory.createVideoSource(false);
    SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create(UUID.randomUUID().toString(), this._eglBase.getEglBaseContext());

    _capturer.initialize(surfaceTextureHelper, this._context, source.getCapturerObserver());
    _capturer.startCapture(width, height, fps);

    this._videoTrack = this._peerConnectionFactory.createVideoTrack(UUID.randomUUID().toString(), source);
    this._videoTrack.setEnabled(true);
  }

  public void start(CameraDevice camera) {
    this.start(camera, LocalVideoTrackImpl.DEFAULT_WIDTH, LocalVideoTrackImpl.DEFAULT_HEIGHT, LocalVideoTrackImpl.DEFAULT_FPS);
  }

  @Override
  public void changeResolution(int width, int height, int fps) {
    if(this._capturer == null) {
      return;
    }

    this._capturer.changeCaptureFormat(width, height, fps);
  }

  @Override
  public void switchCamera() {
    if(this._capturer == null) {
      return;
    }

    this._capturer.switchCamera(null);
  }

  @Override
  public VideoTrack getNativeTrack() {
    return this._videoTrack;
  }

}
