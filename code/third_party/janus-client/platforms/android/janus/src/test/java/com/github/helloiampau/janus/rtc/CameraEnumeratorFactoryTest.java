package com.github.helloiampau.janus.rtc;

import android.app.Activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Camera2Enumerator.class})
public class CameraEnumeratorFactoryTest {

  @Test
  public void itShouldCreateACamera2EnumeratorIfItIsSupported() {
    Activity context = mock(Activity.class);

    mockStatic(Camera2Enumerator.class);
    when(Camera2Enumerator.isSupported(context)).thenReturn(true);

    CameraEnumeratorFactory factory = new CameraEnumeratorFactory(context);
    assertThat(factory.create(), instanceOf(Camera2Enumerator.class));
  }

  @Test
  public void itShouldCreateACamera1EnumeratorIf2IsNotSupported() {
    Activity context = mock(Activity.class);

    mockStatic(Camera2Enumerator.class);
    when(Camera2Enumerator.isSupported(context)).thenReturn(false);

    CameraEnumeratorFactory factory = new CameraEnumeratorFactory(context);
    assertThat(factory.create(), instanceOf(Camera1Enumerator.class));
  }

}