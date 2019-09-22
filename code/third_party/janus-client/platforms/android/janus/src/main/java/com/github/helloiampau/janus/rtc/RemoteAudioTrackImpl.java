package com.github.helloiampau.janus.rtc;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.github.helloiampau.janus.generated.AudioDevice;
import com.github.helloiampau.janus.generated.RemoteAudioTrack;

public class RemoteAudioTrackImpl extends RemoteAudioTrack {

  private AudioManager _manager = null;

  protected RemoteAudioTrackImpl(Activity context) {
    this._manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    this.setOutputDevice(AudioDevice.EARPIECE);
  }

  @Override
  public void setOutputDevice(AudioDevice device) {
    this._manager.setSpeakerphoneOn(device == AudioDevice.SPEAKER);
  }

}
