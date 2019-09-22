package com.github.helloiampau.janus.rtc;

import android.Manifest;
import android.app.Activity;

import com.github.helloiampau.janus.generated.Constraints;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Dexter.class, MultiplePermissionsReport.class })
public class UserMediaFactoryTest {

  LocalAudioTrackFactory localAudioTrackFactory = null;
  LocalAudioTrackImpl localAudioTrack = null;

  LocalVideoTrackFactory localVideoTrackFactory = null;
  LocalVideoTrackImpl localVideoTrack = null;

  EglBaseFactory eglBaseFactory = null;

  Activity context = null;
  DexterBuilder.Permission permission = null;
  DexterBuilder.MultiPermissionListener listener = null;
  DexterBuilder builder = null;

  @Before
  public void SetUp() {
    this.eglBaseFactory = mock(EglBaseFactory.class);

    this.localAudioTrack = mock(LocalAudioTrackImpl.class);
    this.localAudioTrackFactory = mock(LocalAudioTrackFactory.class);
    when(this.localAudioTrackFactory.create()).thenReturn(this.localAudioTrack);

    this.localVideoTrack = mock(LocalVideoTrackImpl.class);
    this.localVideoTrackFactory = mock(LocalVideoTrackFactory.class);
    when(this.localVideoTrackFactory.create()).thenReturn(this.localVideoTrack);

    this.context = mock(Activity.class);

    this.permission = mock(DexterBuilder.Permission.class);
    mockStatic(Dexter.class);
    when(Dexter.withActivity(this.context)).thenReturn(this.permission);

    this.listener = mock(DexterBuilder.MultiPermissionListener.class);
    when(this.permission.withPermissions(any(Collection.class))).thenReturn(this.listener);

    this.builder = mock(DexterBuilder.class);
    when(this.listener.withListener(any(MultiplePermissionsListener.class))).thenReturn(this.builder);
  }

  @Test
  public void itShouldCheckPermissionsAndCreateTracks() {
    UserMediaFactory factory = new UserMediaFactory(this.localVideoTrackFactory, this.localAudioTrackFactory, this.eglBaseFactory, this.context);

    Constraints constraints = new Constraints(true, true, true, true);
    factory.getUserMedia(constraints, m -> {
      assertEquals(this.localVideoTrack, m.video());
      assertEquals(this.localAudioTrack, m.audio());
    }, e -> {
      throw new Error("Not here");
    });

    ArgumentCaptor<List<String>> permissionArray = ArgumentCaptor.forClass(List.class);
    verify(this.permission).withPermissions(permissionArray.capture());

    assertEquals(permissionArray.getValue().get(0), Manifest.permission.CAMERA);
    assertEquals(permissionArray.getValue().get(1), Manifest.permission.RECORD_AUDIO);

    ArgumentCaptor<MultiplePermissionsListener> handler = ArgumentCaptor.forClass(MultiplePermissionsListener.class);
    verify(this.listener).withListener(handler.capture());

    MultiplePermissionsReport report = PowerMockito.mock(MultiplePermissionsReport.class);
    when(report.areAllPermissionsGranted()).thenReturn(true);

    handler.getValue().onPermissionsChecked(report);

    verify(this.builder).check();
  }

  @Test
  public void itShouldCreateLocaAudioOnly() {
    UserMediaFactory factory = new UserMediaFactory(this.localVideoTrackFactory, this.localAudioTrackFactory, this.eglBaseFactory, this.context);

    Constraints constraints = new Constraints(true, false, true, true);
    factory.getUserMedia(constraints, m -> {
      assertEquals(m.video(), null);
      assertEquals(m.audio(), this.localAudioTrack);
    }, e -> {
      throw new Error("Not here");
    });

    ArgumentCaptor<List<String>> permissionArray = ArgumentCaptor.forClass(List.class);
    verify(this.permission).withPermissions(permissionArray.capture());

    assertEquals(permissionArray.getValue().size(), 1);
    assertEquals(permissionArray.getValue().get(0), Manifest.permission.RECORD_AUDIO);

    ArgumentCaptor<MultiplePermissionsListener> handler = ArgumentCaptor.forClass(MultiplePermissionsListener.class);
    verify(this.listener).withListener(handler.capture());

    MultiplePermissionsReport report = PowerMockito.mock(MultiplePermissionsReport.class);
    when(report.areAllPermissionsGranted()).thenReturn(true);

    handler.getValue().onPermissionsChecked(report);
  }

  @Test
  public void itShouldCreateLocaVideoOnly() {
    UserMediaFactory factory = new UserMediaFactory(this.localVideoTrackFactory, this.localAudioTrackFactory, this.eglBaseFactory, this.context);

    Constraints constraints = new Constraints(false, true, true, true);
    factory.getUserMedia(constraints, m -> {
      assertEquals(m.video(), this.localVideoTrack);
      assertEquals(m.audio(), null);
    }, e -> {
      throw new Error("Not here");
    });

    ArgumentCaptor<List<String>> permissionArray = ArgumentCaptor.forClass(List.class);
    verify(this.permission).withPermissions(permissionArray.capture());

    assertEquals(permissionArray.getValue().size(), 1);
    assertEquals(permissionArray.getValue().get(0), Manifest.permission.CAMERA);

    ArgumentCaptor<MultiplePermissionsListener> handler = ArgumentCaptor.forClass(MultiplePermissionsListener.class);
    verify(this.listener).withListener(handler.capture());

    MultiplePermissionsReport report = PowerMockito.mock(MultiplePermissionsReport.class);
    when(report.areAllPermissionsGranted()).thenReturn(true);

    handler.getValue().onPermissionsChecked(report);
  }

  @Test
  public void itShouldHandleNoMedia() {
    UserMediaFactory factory = new UserMediaFactory(this.localVideoTrackFactory, this.localAudioTrackFactory, this.eglBaseFactory, this.context);
    Constraints constraints = new Constraints(false, false, true, true);
    when(this.permission.withPermissions(any(Collection.class))).thenReturn(null);

    factory.getUserMedia(constraints, m -> {
      assertEquals(m.audio(), null);
      assertEquals(m.video(), null);
    }, e -> {
      throw new Error("Not here");
    });
  }

  @Test
  public void itShouldRaiseErrorCallbackIfPermissionsAreNotGranted() {
    UserMediaFactory factory = new UserMediaFactory(this.localVideoTrackFactory, this.localAudioTrackFactory, this.eglBaseFactory, this.context);

    Constraints constraints = new Constraints(true, true, true, true);
    factory.getUserMedia(constraints, m -> {
      throw new Error("Not here");
    }, e -> {
      assertEquals(e.getReason(), "You MUST grant all the permissions");
      assertEquals(e.getCode(), 999);
    });

    ArgumentCaptor<MultiplePermissionsListener> handler = ArgumentCaptor.forClass(MultiplePermissionsListener.class);
    verify(this.listener).withListener(handler.capture());

    MultiplePermissionsReport report = PowerMockito.mock(MultiplePermissionsReport.class);
    when(report.areAllPermissionsGranted()).thenReturn(false);

    handler.getValue().onPermissionsChecked(report);
  }

}