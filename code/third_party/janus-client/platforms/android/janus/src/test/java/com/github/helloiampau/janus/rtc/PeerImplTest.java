package com.github.helloiampau.janus.rtc;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.Candidate;
import com.github.helloiampau.janus.generated.Constraints;
import com.github.helloiampau.janus.generated.IceServer;
import com.github.helloiampau.janus.generated.JanusConf;
import com.github.helloiampau.janus.generated.JanusError;
import com.github.helloiampau.janus.generated.Jsep;
import com.github.helloiampau.janus.generated.LocalAudioTrack;
import com.github.helloiampau.janus.generated.LocalVideoTrack;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.webrtc.AudioTrack;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PeerImplTest {

  private PeerConnectionFactory peerConnectionFactory = null;
  private UserMediaFactory userMediaFactory = null;
  private Plugin delegate = null;

  private PeerConnection peerConnection = null;
  private PeerImpl peer = null;

  private ArgumentCaptor<Consumer<UserMedia>> onMedia = null;
  private ArgumentCaptor<Consumer<JanusError>> onError = null;

  private LocalAudioTrackImpl localAudioTrack = null;
  private LocalVideoTrackImpl localVideoTrack = null;

  private VideoTrack nativeVideoTrack = null;
  private AudioTrack nativeAudioTrack = null;

  private EglBaseFactory eglBaseFactory = null;

  private Activity context = null;
  private AudioManager audioManager = null;

  private JanusConf conf = null;
  private UserMedia userMedia = null;

  @Before
  public void setUp() {
    this.audioManager = mock(AudioManager.class);

    this.context = mock(Activity.class);
    when(this.context.getSystemService(Context.AUDIO_SERVICE)).thenReturn(this.audioManager);

    this.eglBaseFactory = mock(EglBaseFactory.class);

    this.peerConnection = mock(PeerConnection.class);

    ArrayList<IceServer> servers = new ArrayList<>();
    servers.add(new IceServer("test", null, null));
    this.conf = mock(JanusConf.class);
    when(this.conf.iceServers()).thenReturn(servers);

    this.peerConnectionFactory = mock(PeerConnectionFactory.class);
    when(this.peerConnectionFactory.createPeerConnection(any(PeerConnection.RTCConfiguration.class), any(PeerImpl.class))).thenReturn(this.peerConnection);

    this.userMediaFactory = mock(UserMediaFactory.class);
    this.delegate = mock(Plugin.class);

    this.peer = new PeerImpl(this.conf, this.peerConnectionFactory, this.userMediaFactory, this.eglBaseFactory, this.context, this.delegate);

    this.onMedia = ArgumentCaptor.forClass(Consumer.class);
    this.onError = ArgumentCaptor.forClass(Consumer.class);

    this.localAudioTrack = mock(LocalAudioTrackImpl.class);
    this.nativeAudioTrack = mock(AudioTrack.class);
    when(this.localAudioTrack.getNativeTrack()).thenReturn(this.nativeAudioTrack);

    this.localVideoTrack = mock(LocalVideoTrackImpl.class);
    this.nativeVideoTrack = mock(VideoTrack.class);
    when(this.localVideoTrack.getNativeTrack()).thenReturn(this.nativeVideoTrack);

    this.userMedia = mock(UserMedia.class);
    when(this.userMedia.audio()).thenReturn(this.localAudioTrack);
    when(this.userMedia.video()).thenReturn(this.localVideoTrack);
  }

  private void initPeerconnection() {
    Constraints constraints = new Constraints(true, true, true, true);

    ArgBundle context = mock(ArgBundle.class);

    this.peer.createOffer(constraints, context);
  }

  @Test
  public void itShouldGetMediaAndCreateOfferOnCreateOffer() {
    Constraints constraints = new Constraints(true, true, true, true);

    ArgBundle context = mock(ArgBundle.class);

    this.peer.createOffer(constraints, context);

    verify(this.userMediaFactory).getUserMedia(eq(constraints), this.onMedia.capture(), this.onError.capture());
    this.onMedia.getValue().accept(this.userMedia);

    ArgumentCaptor<MediaConstraints> mediaConstraints = ArgumentCaptor.forClass(MediaConstraints.class);
    ArgumentCaptor<SdpObserver> observer = ArgumentCaptor.forClass(SdpObserver.class);
    verify(this.peerConnection).createOffer(observer.capture(), mediaConstraints.capture());

    MediaConstraints.KeyValuePair offerVideo = mediaConstraints.getValue().mandatory.get(0);
    assertEquals(offerVideo.getKey(), "OfferToReceiveVideo");
    assertEquals(offerVideo.getValue(), "true");
    MediaConstraints.KeyValuePair offerAudio = mediaConstraints.getValue().mandatory.get(1);
    assertEquals(offerAudio.getKey(), "OfferToReceiveAudio");
    assertEquals(offerAudio.getValue(), "true");

    SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, "the sdp");
    observer.getValue().onCreateSuccess(sessionDescription);
    verify(this.delegate).onOffer("the sdp", context);
  }

  @Test
  public void itShouldGetMediaAndCreateAnswerOnCreateAnswer() {
    Jsep offer = mock(Jsep.class);
    when(offer.type()).thenReturn("offer");
    when(offer.sdp()).thenReturn("the offer");

    this.peer.setRemoteDescription(offer);

    Constraints constraints = new Constraints(true, true, true, true);

    ArgBundle context = mock(ArgBundle.class);

    this.peer.createAnswer(constraints, context);

    verify(this.userMediaFactory).getUserMedia(eq(constraints), this.onMedia.capture(), this.onError.capture());
    this.onMedia.getValue().accept(this.userMedia);

    ArgumentCaptor<MediaConstraints> mediaConstraints = ArgumentCaptor.forClass(MediaConstraints.class);
    ArgumentCaptor<SdpObserver> observer = ArgumentCaptor.forClass(SdpObserver.class);
    verify(this.peerConnection).createAnswer(observer.capture(), mediaConstraints.capture());

    MediaConstraints.KeyValuePair offerVideo = mediaConstraints.getValue().mandatory.get(0);
    assertEquals(offerVideo.getKey(), "OfferToReceiveVideo");
    assertEquals(offerVideo.getValue(), "true");
    MediaConstraints.KeyValuePair offerAudio = mediaConstraints.getValue().mandatory.get(1);
    assertEquals(offerAudio.getKey(), "OfferToReceiveAudio");
    assertEquals(offerAudio.getValue(), "true");

    SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, "the sdp");
    observer.getValue().onCreateSuccess(sessionDescription);
    verify(this.delegate).onAnswer("the sdp", context);
  }

  @Test
  public void itShouldSetAnOfferAsLocalDescription() {
    this.initPeerconnection();

    Jsep jsep = mock(Jsep.class);
    when(jsep.type()).thenReturn("offer");
    when(jsep.sdp()).thenReturn("the sdp");

    this.peer.setLocalDescription(jsep);

    ArgumentCaptor<SessionDescription> sessionDescription = ArgumentCaptor.forClass(SessionDescription.class);
    verify(this.peerConnection).setLocalDescription(any(SdpObserver.class), sessionDescription.capture());

    assertEquals(sessionDescription.getValue().type, SessionDescription.Type.OFFER);
    assertEquals(sessionDescription.getValue().description, "the sdp");
  }

  @Test
  public void itShouldSetAnAnswerAsLocalDescription() {
    Jsep offer = mock(Jsep.class);
    when(offer.type()).thenReturn("offer");
    when(offer.sdp()).thenReturn("the offer");

    this.peer.setRemoteDescription(offer);

    Jsep jsep = mock(Jsep.class);
    when(jsep.type()).thenReturn("answer");
    when(jsep.sdp()).thenReturn("the sdp");

    this.peer.setLocalDescription(jsep);

    ArgumentCaptor<SessionDescription> sessionDescription = ArgumentCaptor.forClass(SessionDescription.class);
    verify(this.peerConnection).setLocalDescription(any(SdpObserver.class), sessionDescription.capture());

    assertEquals(sessionDescription.getValue().type, SessionDescription.Type.ANSWER);
    assertEquals(sessionDescription.getValue().description, "the sdp");
  }

  @Test
  public void itShouldSetAnOfferAsRemoteDescription() {
    Jsep jsep = mock(Jsep.class);
    when(jsep.type()).thenReturn("offer");
    when(jsep.sdp()).thenReturn("the sdp");

    this.peer.setRemoteDescription(jsep);

    ArgumentCaptor<SessionDescription> sessionDescription = ArgumentCaptor.forClass(SessionDescription.class);
    verify(this.peerConnection).setRemoteDescription(any(SdpObserver.class), sessionDescription.capture());

    assertEquals(sessionDescription.getValue().type, SessionDescription.Type.OFFER);
    assertEquals(sessionDescription.getValue().description, "the sdp");
  }

  @Test
  public void itShouldSetAnAnswerAsRemoteDescription() {
    Jsep jsep = mock(Jsep.class);
    when(jsep.type()).thenReturn("answer");
    when(jsep.sdp()).thenReturn("the sdp");

    this.peer.setRemoteDescription(jsep);

    ArgumentCaptor<SessionDescription> sessionDescription = ArgumentCaptor.forClass(SessionDescription.class);
    verify(this.peerConnection).setRemoteDescription(any(SdpObserver.class), sessionDescription.capture());

    assertEquals(sessionDescription.getValue().type, SessionDescription.Type.ANSWER);
    assertEquals(sessionDescription.getValue().description, "the sdp");
  }

  @Test
  public void itShouldAddARemoteIceCandidate() {
    this.initPeerconnection();

    Candidate candidate = new Candidate("test", 99, "the candidate", false);

    this.peer.addIceCandidate(candidate);

    ArgumentCaptor<IceCandidate> iceCandidate = ArgumentCaptor.forClass(IceCandidate.class);
    verify(this.peerConnection).addIceCandidate(iceCandidate.capture());

    assertEquals(iceCandidate.getValue().sdpMid, "test");
    assertEquals(iceCandidate.getValue().sdp, "the candidate");
    assertEquals(iceCandidate.getValue().sdpMLineIndex, 99);
  }

  @Test
  public void itShouldSendTheLocalIceCandidate() {
    IceCandidate iceCandidate = new IceCandidate("test", 99, "the candidate");

    this.peer.onIceCandidate(iceCandidate);

    ArgumentCaptor<Candidate> candidate = ArgumentCaptor.forClass(Candidate.class);
    verify(this.delegate).onIceCandidate(candidate.capture());

    assertEquals(candidate.getValue().getCompleted(), false);
    assertEquals(candidate.getValue().getSdpMid(), "test");
    assertEquals(candidate.getValue().getSdpMLineIndex(), 99);
    assertEquals(candidate.getValue().getCandidate(), "the candidate");
  }

  @Test
  public void itShouldSendTheCompletedCandidateOnIceCompleted() {
    this.peer.onIceGatheringChange(PeerConnection.IceGatheringState.COMPLETE);

    ArgumentCaptor<Candidate> candidate = ArgumentCaptor.forClass(Candidate.class);
    verify(this.delegate).onIceCandidate(candidate.capture());

    assertEquals(candidate.getValue().getCompleted(), true);
    assertEquals(candidate.getValue().getSdpMid(), "");
    assertEquals(candidate.getValue().getSdpMLineIndex(), -999);
    assertEquals(candidate.getValue().getCandidate(), "");
  }

  @Test
  public void itShouldSkipTheOtherIceEvents() {
    this.peer.onIceGatheringChange(PeerConnection.IceGatheringState.GATHERING);

    verify(this.delegate, times(0)).onIceCandidate(any(Candidate.class));
  }

  @Test
  public void itShouldAddTheRemoteTrackToMedia() {
    this.initPeerconnection();

    VideoTrack track = mock(VideoTrack.class);

    MediaStream mediaStream = new MediaStream(-9);
    mediaStream.videoTracks.add(track);

    this.peer.onAddStream(mediaStream);

    Media media = this.peer.getMedia();
    assertEquals(((RemoteVideoTrackImpl) media.remoteVideoTrack()).getNativeTrack(), track);
  }

  @Test
  public void itShouldSkipRemoteVideoTrackIfVideoIsNotEnabled() {
    this.initPeerconnection();

    MediaStream mediaStream = new MediaStream(-9);

    this.peer.onAddStream(mediaStream);

    Media media = this.peer.getMedia();
    assertEquals(media.remoteVideoTrack(), null);
  }

  @Test
  public void itShouldAddTheRemoteAudioTrackToMedia() {
    this.initPeerconnection();

    AudioTrack track = mock(AudioTrack.class);

    MediaStream mediaStream = new MediaStream(-9);
    mediaStream.audioTracks.add(track);

    this.peer.onAddStream(mediaStream);

    Media media = this.peer.getMedia();
    assertThat(media.remoteAudioTrack(), instanceOf(RemoteAudioTrackImpl.class));
  }

  @Test
  public void itShouldSkipRemoteAudioTrackIfAudioIsNotEnabled() {
    this.initPeerconnection();

    MediaStream mediaStream = new MediaStream(-9);

    this.peer.onAddStream(mediaStream);

    Media media = this.peer.getMedia();
    assertEquals(media.remoteAudioTrack(), null);
  }

  @Test
  public void itShouldCloseThePeerconnection() {
    this.initPeerconnection();

    this.peer.close();

    verify(this.peerConnection).close();
    verify(this.peerConnection).dispose();
  }

  @Test
  public void itShouldCallInitJustOnce() {
    Constraints constraints = new Constraints(true, true, true, true);

    ArgBundle context = mock(ArgBundle.class);

    this.peer.createOffer(constraints, context);
    this.peer.createOffer(constraints, context);

    this.peer.close();

    this.peer.createOffer(constraints, context);

    verify(this.peerConnectionFactory, times(2)).createPeerConnection(any(PeerConnection.RTCConfiguration.class), any(PeerConnection.Observer.class));
  }

  @Test
  public void itShouldSkipCloseOnNullPeerconnection() {
    this.peer.close();
  }

}