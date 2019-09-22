package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import com.github.helloiampau.janus.generated.CameraDevice;
import com.github.helloiampau.janus.generated.LocalVideoTrack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.CapturerObserver;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SurfaceTextureHelper.class, UUID.class})
public class LocalVideoTrackTest {

  private PeerConnectionFactory peerConnectionFactory = null;
  private CameraEnumerator enumerator = null;
  private CameraEnumeratorFactory cameraEnumeratorFactory = null;
  private CameraVideoCapturer capturer = null;
  private VideoSource videoSource = null;
  private CapturerObserver capturerObserver = null;
  private VideoTrack videoTrack = null;
  private EglBaseFactory eglBaseFactory = null;
  private Activity context = null;

  @Before
  public void SetUp() {
    EglBase.Context eglContext = mock(EglBase.Context.class);

    EglBase eglBase = mock(EglBase.class);
    when(eglBase.getEglBaseContext()).thenReturn(eglContext);

    this.eglBaseFactory = mock(EglBaseFactory.class);
    when(this.eglBaseFactory.create()).thenReturn(eglBase);

    SurfaceTextureHelper surfaceTextureHelper = mock(SurfaceTextureHelper.class);

    mockStatic(SurfaceTextureHelper.class);
    when(SurfaceTextureHelper.create(any(String.class), eq(eglContext))).thenReturn(surfaceTextureHelper);

    this.capturerObserver = mock(CapturerObserver.class);

    this.videoSource = mock(VideoSource.class);
    when(this.videoSource.getCapturerObserver()).thenReturn(capturerObserver);

    this.videoTrack = mock(VideoTrack.class);

    this.peerConnectionFactory = mock(PeerConnectionFactory.class);
    when(this.peerConnectionFactory.createVideoSource(false)).thenReturn(this.videoSource);
    when(this.peerConnectionFactory.createVideoTrack(any(String.class), eq(videoSource))).thenReturn(this.videoTrack);

    this.capturer = mock(CameraVideoCapturer.class);

    this.enumerator = mock(CameraEnumerator.class);
    when(this.enumerator.getDeviceNames()).thenReturn(new String[] { "front", "back" });
    when(this.enumerator.isFrontFacing("back")).thenReturn(false);
    when(this.enumerator.isFrontFacing("front")).thenReturn(true);
    when(this.enumerator.createCapturer(any(String.class), eq(null))).thenReturn(this.capturer);

    this.cameraEnumeratorFactory = mock(CameraEnumeratorFactory.class);
    when(this.cameraEnumeratorFactory.create()).thenReturn(this.enumerator);

    this.context = mock(Activity.class);
  }

  @Test
  public void itShouldCreateANativeVideoTrackFromFrontCamera() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.FRONT);

    verify(this.enumerator).createCapturer("front", null);
    verify(this.videoTrack).setEnabled(true);
    verify(this.capturer).startCapture(1280, 720, 30);
  }

  @Test
  public void itShouldCreateANativeVideoTrackFromBackCamera() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR);

    verify(this.enumerator).createCapturer("back", null);
    verify(this.videoTrack).setEnabled(true);
    verify(this.capturer).startCapture(1280, 720, 30);
  }

  @Test
  public void itShouldStartCaptureWithACustomResolution() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR, 999, 998, 997);

    verify(this.capturer).startCapture(999, 998, 997);
  }

  @Test
  public void itShouldDoNothingIfCameraIsNotFound() {
    when(this.enumerator.isFrontFacing("front")).thenReturn(false);

    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.FRONT);
  }

  @Test
  public void itShouldChangeTheCurrentCaptureFormat() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR);
    localVideoTrack.changeResolution(999, 998, 997);

    verify(this.capturer).changeCaptureFormat(999, 998, 997);
  }

  @Test
  public void itShouldNotChangeTheCurrentCaptureFormatIfCapturerIsNotDefined() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.changeResolution(999, 998, 997);
  }

  @Test
  public void itShouldStopCapturing() throws InterruptedException {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR);
    localVideoTrack.stop();

    verify(this.capturer).stopCapture();
    verify(this.videoTrack).setEnabled(false);
    verify(this.videoTrack).dispose();
    verify(this.capturer).dispose();
  }

  @Test
  public void itShouldNotStopCapturingIfTrackIsNotEnabled() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.stop();
  }

  @Test
  public void itShouldStartOnce() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR);
    localVideoTrack.start(CameraDevice.REAR);

    verify(this.videoTrack, times(1)).setEnabled(true);
  }

  @Test
  public void itShouldResetVideotrackOnStop() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR);
    localVideoTrack.stop();
    localVideoTrack.start(CameraDevice.REAR);

    verify(this.videoTrack, times(2)).setEnabled(true);
  }

  @Test
  public void itShouldGetTheNativeVideoTrack() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.REAR);

    assertEquals(localVideoTrack.getNativeTrack(), this.videoTrack);
  }

  @Test
  public void itShouldSwitchTheCapturingCamera() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.FRONT);
    localVideoTrack.switchCamera();

    verify(this.capturer).switchCamera(null);
  }

  @Test
  public void itShouldSkipSwitchIfTrackIsNotStarted() {
    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.switchCamera();
  }

  @Test
  public void itShouldSelectTheFirstAvailableCameraIfSelectedIsNotPresent() {
    when(this.enumerator.getDeviceNames()).thenReturn(new String[] { "first", "front", "back" });

    when(this.enumerator.isFrontFacing("front")).thenReturn(false);
    when(this.enumerator.isFrontFacing("first")).thenReturn(false);

    LocalVideoTrackImpl localVideoTrack = new LocalVideoTrackImpl(this.peerConnectionFactory, this.cameraEnumeratorFactory, this.eglBaseFactory, this.context);
    localVideoTrack.start(CameraDevice.FRONT);

    verify(this.enumerator).createCapturer("first", null);
  }

}