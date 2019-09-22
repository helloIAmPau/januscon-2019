package com.github.helloiampau.janus;

import android.app.Activity;

import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.Platform;
import com.github.helloiampau.janus.rtc.EglBaseFactory;
import com.github.helloiampau.janus.rtc.PeerFactoryImpl;

public class JanusFactory {
  private static native void init();

  static {
    System.loadLibrary("janus-android-sdk");
    JanusFactory.init();
  }

  public static Janus create(Activity context, Platform platform) {
    EglBaseFactory eglBaseFactory = new EglBaseFactory();

    if(platform == null) {
      platform = Platform.create();
    }

    platform.registerPeerFactory(new PeerFactoryImpl(eglBaseFactory, context));

    return Janus.create(platform);
  }

  public static Janus create(Activity context) {
    return JanusFactory.create(context, null);
  }

  public static Platform platform() {
    return Platform.create();
  }
}