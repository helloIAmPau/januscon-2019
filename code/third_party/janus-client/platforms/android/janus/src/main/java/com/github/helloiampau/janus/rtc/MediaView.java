package com.github.helloiampau.janus.rtc;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MediaView extends ViewGroup {

  public enum Scaling {
    FIT,
    FILL
  }

  private SurfaceViewRenderer _delegate = null;
  private boolean HAS_TRACK = false;

  public MediaView(Context context, boolean isOverlay) {
    super(context);

    this._delegate = new SurfaceViewRenderer(context);

    this.addView(this._delegate);
  }

  public MediaView(Context context, AttributeSet attrs) {
    super(context, attrs);

    this._delegate = new SurfaceViewRenderer(context, attrs);
    this.addView(this._delegate);
  }

  public void mirror(boolean mirror) {
    this._delegate.setMirror(mirror);
  }

  public void scaling(Scaling type) {
    if(type == Scaling.FIT) {
      this._delegate.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    } else if(type == Scaling.FILL) {
      this._delegate.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }
  }

  protected MediaView(SurfaceViewRenderer renderer, Activity context) {
    super(context);

    this._delegate = renderer;
    this.addView(this._delegate);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    this._delegate.layout(0, 0, right - left, bottom - top);
  }

  protected void addTrack(JanusVideoTrack videoTrack, EglBase.Context context) {
    if(this.HAS_TRACK == false) {
      this.HAS_TRACK = true;

      this._delegate.init(context, null);

      videoTrack.getNativeTrack().addSink(this._delegate);
    }
  }

  protected void removeTrack(JanusVideoTrack videoTrack) {
    if(this.HAS_TRACK == true) {
      this.HAS_TRACK = false;

      videoTrack.getNativeTrack().removeSink(this._delegate);
      this._delegate.clearImage();
      this._delegate.release();
    }
  }

}
