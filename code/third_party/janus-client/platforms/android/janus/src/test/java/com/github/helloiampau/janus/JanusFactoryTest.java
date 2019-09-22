package com.github.helloiampau.janus;

//import com.github.helloiampau.janus.generated.Janus;
//import com.github.helloiampau.janus.generated.Platform;
//
//import org.junit.runner.RunWith;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ Janus.class, Platform.class, JanusFactory.class, System.class })
//public class JanusFactoryTest {
//
//  private Platform platform = null;
//  private Janus janus = null;
//  private Activity context = null;
//
//  @Test
//  public void itShouldCreateAJanusInstance() {
//    this.platform = mock(Platform.class);
//
//    mockStatic(Platform.class);
//    when(Platform.create()).thenReturn(this.platform);
//
//    this.janus = mock(Janus.class);
//
//    mockStatic(Janus.class);
//    when(Janus.create(this.platform)).thenReturn(this.janus);
//
//    this.context = mock(Activity.class);
//
//    mockStatic(JanusFactory.class);
//
//    mockStatic(System.class);
//
//    JanusFactory factory = new JanusFactory();
//    assertEquals(factory.create(this.context), this.janus);
//
//    verify(this.platform).registerPeerFactory(any(PeerFactoryImpl.class));
//
//    verifyStatic(System.class);
//    System.loadLibrary("janus-android-sdk");
//  }
//
//}