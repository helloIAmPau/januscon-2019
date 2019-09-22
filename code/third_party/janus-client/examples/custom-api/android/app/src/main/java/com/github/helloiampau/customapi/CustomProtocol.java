package com.github.helloiampau.customapi;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.Info;
import com.github.helloiampau.janus.generated.JanusConf;
import com.github.helloiampau.janus.generated.JanusDelegate;
import com.github.helloiampau.janus.generated.JanusPluginInfo;
import com.github.helloiampau.janus.generated.Peer;
import com.github.helloiampau.janus.generated.Platform;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.Protocol;
import com.github.helloiampau.janus.generated.ReadyState;

import java.util.ArrayList;

public class CustomProtocol extends Protocol {

  private JanusDelegate _delegate;
  private Plugin _session;

  public CustomProtocol(JanusConf conf, Platform platform, JanusDelegate janusDelegate) {
    this._delegate = janusDelegate;

    this._session = new CustomPlugin(conf.url());
    this._delegate.onPluginEnabled("custom-plugin", this._session, null);

    Peer peer = platform.createPeer(conf, this._session);
    this._session.init(null, peer);
  }

  @Override
  public Info info() {
    return new Info("Custom Protocol", 0);
  }

  @Override
  public ReadyState readyState() {
    return ReadyState.READY;
  }

  @Override
  public void close() {
    this._session.hangup();
    this._session = null;
  }

  @Override
  public ArrayList<JanusPluginInfo> plugins() {
    return new ArrayList<>();
  }

  @Override
  public void attach(String s, ArgBundle argBundle) {}
}
