package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MediaViewTest {

  VideoTrack videoTrack = null;
  JanusVideoTrack janusVideoTrack = null;
  Activity context = null;
  SurfaceViewRenderer renderer = null;
  EglBase.Context eglContext = null;

  @Before
  public void setUp() {
    this.videoTrack = mock(VideoTrack.class);
    this.janusVideoTrack = mock(JanusVideoTrack.class);
    when(this.janusVideoTrack.getNativeTrack()).thenReturn(this.videoTrack);

    this.context = mock(Activity.class);
    this.renderer = mock(SurfaceViewRenderer.class);

    this.eglContext = mock(EglBase.Context.class);
  }

  @Test
  public void itShouldAddSinkToANativeTrack() {
    MediaView view = new MediaView(this.renderer, this.context);
    view.addTrack(this.janusVideoTrack, this.eglContext);

    verify(this.renderer).init(this.eglContext, null);
    verify(this.videoTrack).addSink(this.renderer);
  }

  @Test
  public void itShouldRemoveSinkFromANativeTrack() {
    MediaView view = new MediaView(this.renderer, this.context);
    view.addTrack(janusVideoTrack, this.eglContext);
    view.removeTrack(this.janusVideoTrack);

    verify(this.renderer).release();
    verify(this.renderer).clearImage();
    verify(this.videoTrack).removeSink(this.renderer);
  }

  @Test
  public void itShouldSetTheAspectRatioToFit() {
    MediaView view = new MediaView(this.renderer, this.context);
    view.scaling(MediaView.Scaling.FIT);

    verify(this.renderer).setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
  }

  @Test
  public void itShouldSetTheAspectRatioToFill() {
    MediaView view = new MediaView(this.renderer, this.context);
    view.scaling(MediaView.Scaling.FILL);

    verify(this.renderer).setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
  }

  @Test
  public void itShouldSetTheMirrorMode() {
    MediaView view = new MediaView(this.renderer, this.context);

    view.mirror(true);
    verify(this.renderer).setMirror(true);

    view.mirror(false);
    verify(this.renderer).setMirror(false);
  }


}