package com.github.helloiampau.janus.rtc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.webrtc.EglBase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EglBase.class })
public class EglBaseFactoryTest {

  @Test
  public void itShouldCreateAnEglBaseInSingletonWay() {
    EglBase egl = mock(EglBase.class);

    mockStatic(EglBase.class);
    when(EglBase.create()).thenReturn(mock(EglBase.class));
    when(EglBase.create()).thenReturn(egl);

    EglBaseFactory factory = new EglBaseFactory();

    assertEquals(factory.create(), egl);
    assertEquals(factory.create(), egl);
  }

}