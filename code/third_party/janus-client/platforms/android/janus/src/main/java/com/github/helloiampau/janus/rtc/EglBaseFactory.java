package com.github.helloiampau.janus.rtc;

import org.webrtc.EglBase;

public class EglBaseFactory {

  private EglBase _instance = null;

  public EglBaseFactory() {
    this._instance = EglBase.create();
  }

  public EglBase create() {
    return this._instance;
  }

}
