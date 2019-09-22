package com.github.helloiampau.janus.rtc;

import android.app.Activity;
import android.content.Context;

import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.JanusConf;
import com.github.helloiampau.janus.generated.Plugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.PeerConnectionFactory;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PeerConnectionFactory.class, PeerConnectionFactory.InitializationOptions.class})
public class PeerFactoryImplTest {

  private EglBaseFactory eglBaseFactory = null;
  private EglBase egl = null;
  private EglBase.Context eglContext = null;

  private Activity context = null;
  private Context appContext = null;

  private Plugin delegate = null;

  private PeerConnectionFactory.InitializationOptions.Builder builder = null;
  private PeerConnectionFactory.InitializationOptions options = null;

  private PeerConnectionFactory.Builder factoryBuilder = null;
  private PeerConnectionFactory peerConnectionFactory = null;

  @Before
  public void setUp() {
    this.delegate = mock(Plugin.class);

    this.context = mock(Activity.class);
    when(this.context.getApplicationContext()).thenReturn(this.appContext);

    this.eglContext = mock(EglBase.Context.class);

    this.egl = mock(EglBase.class);
    when(this.egl.getEglBaseContext()).thenReturn(this.eglContext);

    this.eglBaseFactory = mock(EglBaseFactory.class);
    when(this.eglBaseFactory.create()).thenReturn(this.egl);

    this.options = mock(PeerConnectionFactory.InitializationOptions.class);

    this.builder = mock(PeerConnectionFactory.InitializationOptions.Builder.class);
    when(this.builder.createInitializationOptions()).thenReturn(this.options);

    mockStatic(PeerConnectionFactory.InitializationOptions.class);
    when(PeerConnectionFactory.InitializationOptions.builder(this.appContext)).thenReturn(this.builder);

    this.peerConnectionFactory = mock(PeerConnectionFactory.class);

    this.factoryBuilder = mock(PeerConnectionFactory.Builder.class);
    when(this.factoryBuilder.setVideoDecoderFactory(any(DefaultVideoDecoderFactory.class))).thenReturn(this.factoryBuilder);
    when(this.factoryBuilder.setVideoEncoderFactory(any(DefaultVideoEncoderFactory.class))).thenReturn(this.factoryBuilder);
    when(this.factoryBuilder.createPeerConnectionFactory()).thenReturn(this.peerConnectionFactory);

    mockStatic(PeerConnectionFactory.class);
    when(PeerConnectionFactory.builder()).thenReturn(this.factoryBuilder);
  }

  @Test
  public void itShouldInitializePeerConnectionFactory() {
    PeerFactoryImpl peerFactory = new PeerFactoryImpl(this.eglBaseFactory, this.context);
    verifyStatic(PeerConnectionFactory.class);
    PeerConnectionFactory.initialize(this.options);
  }

  @Test
  public void itShouldCreateANewPeer() {
    PeerFactoryImpl peerFactory = new PeerFactoryImpl(this.eglBaseFactory, this.context);
    JanusConf conf = mock(JanusConf.class);
    assertNotNull(peerFactory.create(conf, this.delegate));
  }

}