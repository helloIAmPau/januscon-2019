package com.github.helloiampau.customapi;

import com.github.helloiampau.janus.generated.JanusConf;
import com.github.helloiampau.janus.generated.JanusDelegate;
import com.github.helloiampau.janus.generated.Platform;
import com.github.helloiampau.janus.generated.Protocol;
import com.github.helloiampau.janus.generated.ProtocolFactory;

public class CustomProtocolFactory extends ProtocolFactory {

  @Override
  public Protocol bootstrap(JanusConf conf, Platform platform, JanusDelegate janusDelegate) {
    return new CustomProtocol(conf, platform, janusDelegate);
  }

}
