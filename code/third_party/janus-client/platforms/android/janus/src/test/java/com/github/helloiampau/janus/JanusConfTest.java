package com.github.helloiampau.janus;

import com.github.helloiampau.janus.generated.IceServer;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

public class JanusConfTest {
  @Test
  public void itShouldInsertTheBaseUrl() {
    JanusConfImpl uut = new JanusConfImpl();
    uut.url("http://a.base.url");
    assertEquals("http://a.base.url", uut.url());
  }

  @Test
  public void itShouldInitializeIceServers() {
    JanusConfImpl uut = new JanusConfImpl();

    String[] servers = { "stun:stun.l.google.com:19302", "stun:stun1.l.google.com:19302", "stun:stun2.l.google.com:19302", "stun:stun3.l.google.com:19302", "stun:stun4.l.google.com:19302" };
    List<IceServer> defaults = uut.iceServers();

    for(int i = 0; i < defaults.size(); i++) {
      IceServer server = defaults.get(i);
      assertEquals(server.getUrl(), servers[i]);
      assertNull(server.getUsername());
      assertNull(server.getPassword());
    }
  }


}