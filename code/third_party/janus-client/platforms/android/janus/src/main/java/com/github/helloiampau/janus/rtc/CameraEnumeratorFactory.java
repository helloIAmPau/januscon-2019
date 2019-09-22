package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;

public class CameraEnumeratorFactory {

  private Activity _context = null;

  public CameraEnumeratorFactory(Activity context) {
    this._context = context;
  }

  public CameraEnumerator create() {
    if(Camera2Enumerator.isSupported(this._context)) {
      return new Camera2Enumerator(this._context);
    }

    return new Camera1Enumerator(false);
  }

}
