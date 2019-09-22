package com.github.helloiampau.customapi;

import android.app.Activity;

import com.github.helloiampau.janus.JanusConfImpl;
import com.github.helloiampau.janus.JanusFactory;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.JanusDelegate;
import com.github.helloiampau.janus.generated.JanusError;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Platform;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.PluginDelegate;

public class CustomWrapper extends JanusDelegate {

  private Janus _service;
  private CustomWrapperDelegate _delegate;
  private Activity _context;

  CustomWrapper(Activity context, CustomWrapperDelegate delegate) {
    this._delegate = delegate;
    this._context = context;

    Platform platform = JanusFactory.platform();
    platform.registerProtocolFactory(new CustomProtocolFactory());
    this._service = JanusFactory.create(context, platform);
  }

  public void start(String host) {
    JanusConfImpl conf = new JanusConfImpl();
    conf.url(host);

    this._service.init(conf, this);
  }

  public void stop() {
    this._service.close();
  }

  @Override
  public void onJanusError(JanusError janusError) {

  }

  @Override
  public void onPluginEnabled(String s, Plugin plugin, ArgBundle argBundle) {
    if(!s.equals("custom-plugin")) {
      return;
    }

    CustomWrapper self = this;
    plugin.setDelegate(new PluginDelegate() {
      @Override
      public void onEvent(JanusEvent data, ArgBundle context) {}

      @Override
      public void onMediaChanged(Media media) {
        self._context.runOnUiThread(() -> {
          self._delegate.onMedia(media);
        });
      }

      @Override
      public void onHangup() {
        self._context.runOnUiThread(() -> {
          self._delegate.onHangup();
        });
      }

      @Override
      public void onDetach() {

      }
    });
  }

}
