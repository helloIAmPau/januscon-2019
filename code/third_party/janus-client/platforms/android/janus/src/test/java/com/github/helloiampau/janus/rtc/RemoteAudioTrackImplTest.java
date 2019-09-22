package com.github.helloiampau.janus.rtc;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.github.helloiampau.janus.generated.AudioDevice;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RemoteAudioTrackImplTest {

  private Activity context = null;
  private AudioManager manager = null;

  @Before
  public void setUp() {
    this.manager = mock(AudioManager.class);

    this.context = mock(Activity.class);
    when(this.context.getSystemService(Context.AUDIO_SERVICE)).thenReturn(manager);
  }

  @Test
  public void itShouldStartWithEarPiece() {
    RemoteAudioTrackImpl audioTrack = new RemoteAudioTrackImpl(this.context);
    verify(this.manager).setSpeakerphoneOn(false);
  }

  @Test
  public void itShouldSetTheSpeakerOn() {
    RemoteAudioTrackImpl audioTrack = new RemoteAudioTrackImpl(this.context);
    audioTrack.setOutputDevice(AudioDevice.SPEAKER);
    verify(this.manager).setSpeakerphoneOn(true);
  }

  @Test
  public void itShouldSetTheEarpieceOn() {
    RemoteAudioTrackImpl audioTrack = new RemoteAudioTrackImpl(this.context);
    audioTrack.setOutputDevice(AudioDevice.EARPIECE);
    verify(this.manager, times(2)).setSpeakerphoneOn(false);
  }

}