package com.github.helloiampau.customapi;

import com.github.helloiampau.janus.generated.Media;

public interface CustomWrapperDelegate {
  void onMedia(Media media);
  void onHangup();
}
