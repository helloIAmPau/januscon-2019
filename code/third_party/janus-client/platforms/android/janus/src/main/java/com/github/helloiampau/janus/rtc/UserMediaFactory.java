package com.github.helloiampau.janus.rtc;

import android.Manifest;
import android.app.Activity;

import com.github.helloiampau.janus.generated.Constraints;
import com.github.helloiampau.janus.generated.JanusError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class UserMedia {
  private LocalVideoTrackImpl _video;
  private LocalAudioTrackImpl _audio;

  UserMedia(LocalVideoTrackImpl video, LocalAudioTrackImpl audio) {
    this._video = video;
    this._audio = audio;
  }

  LocalVideoTrackImpl video() {
    return this._video;
  }

  LocalAudioTrackImpl audio() {
    return this._audio;
  }
}

public class UserMediaFactory {

  private LocalVideoTrackFactory _localVideoTrackFactory = null;
  private LocalAudioTrackFactory _localAudioTrackFactory = null;
  private EglBaseFactory _eglBaseFactory = null;
  private Activity _context = null;

  protected UserMediaFactory(LocalVideoTrackFactory localVideoTrackFactory, LocalAudioTrackFactory localAudioTrackFactory, EglBaseFactory eglBaseFactory, Activity context) {
    this._localVideoTrackFactory = localVideoTrackFactory;
    this._localAudioTrackFactory = localAudioTrackFactory;
    this._context = context;
  }

  protected void getUserMedia(Constraints constraints, Consumer<UserMedia> onMedia, Consumer<JanusError> onError) {
    List<String> permissionArray = new ArrayList<>();

    if(constraints.getVideo() == true) {
      permissionArray.add(Manifest.permission.CAMERA);
    }

    if(constraints.getAudio() == true) {
      permissionArray.add(Manifest.permission.RECORD_AUDIO);
    }

    if(permissionArray.size() == 0) {
      onMedia.accept(new UserMedia(null, null));

      return;
    }

    UserMediaFactory self = this;

    Dexter.withActivity(this._context).withPermissions(permissionArray).withListener(new MultiplePermissionsListener() {
      @Override
      public void onPermissionsChecked(MultiplePermissionsReport report) {
        if(report.areAllPermissionsGranted() == false) {
          onError.accept(new JanusError(999, "You MUST grant all the permissions"));

          return;
        }

        LocalVideoTrackImpl videoTrack = null;
        if(constraints.getVideo() == true) {
          videoTrack = self._localVideoTrackFactory.create();
        }

        LocalAudioTrackImpl audioTrack = null;
        if(constraints.getAudio() == true) {
          audioTrack = self._localAudioTrackFactory.create();
        }

        onMedia.accept(new UserMedia(videoTrack, audioTrack));
      }

      @Override
      public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

      }
    }).check();
  }

}
