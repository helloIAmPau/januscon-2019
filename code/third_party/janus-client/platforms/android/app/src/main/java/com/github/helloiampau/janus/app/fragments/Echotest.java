package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.CameraDevice;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.rtc.LocalVideoTrackImpl;
import com.github.helloiampau.janus.rtc.MediaImpl;
import com.github.helloiampau.janus.rtc.MediaView;

import helloiampau.github.com.janus.R;

public class Echotest extends PluginFragment {

  private ImageButton _callButton;
  private ImageButton _cameraButton;
  private Switch _audioSwitch;
  private Switch _videoSwitch;
  private MediaView _mainView;
  private MediaView _pip;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    View layout = inflater.inflate(R.layout.echotest, container, false);

    this._callButton = layout.findViewById(R.id.call_button);

    this._cameraButton = layout.findViewById(R.id.camera_button);
    this._cameraButton.setOnClickListener(l -> {
      status.dispatch("switch-camera");
    });

    this._audioSwitch = layout.findViewById(R.id.audio_switch);
    this._videoSwitch = layout.findViewById(R.id.video_switch);

    this._mainView = layout.findViewById(R.id.main_view);
    this._mainView.scaling(MediaView.Scaling.FILL);

    this._pip = layout.findViewById(R.id.pip);
    this._pip.scaling(MediaView.Scaling.FIT);

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("media").observe(this, m -> {
      if (m == null) {
        return;
      }

      this._callButton.setImageResource(android.R.drawable.sym_call_missed);
      this._callButton.setOnClickListener(v -> {
        status.dispatch("hangup");
      });

      MediaImpl media = (MediaImpl) m;

      if(media.localVideoTrack() != null) {
        this._cameraButton.setEnabled(true);
        media.addLocalTrackSink(this._pip);
      }

      if(media.remoteVideoTrack() != null) {
        media.addRemoteTrackSink(this._mainView);
      }
    });

    status.get("media").observe(this, m -> {
      if (m != null) {
        return;
      }

      this._callButton.setImageResource(android.R.drawable.ic_menu_call);
      this._callButton.setOnClickListener(v -> {
        ArgBundle bundle = ArgBundle.create();
        bundle.setBool("audio", this._audioSwitch.isChecked());
        bundle.setBool("video", this._videoSwitch.isChecked());
        bundle.setInt("camera", CameraDevice.FRONT.ordinal());
        bundle.setInt("height", LocalVideoTrackImpl.DEFAULT_WIDTH);
        bundle.setInt("width", LocalVideoTrackImpl.DEFAULT_HEIGHT);
        bundle.setInt("fps", LocalVideoTrackImpl.DEFAULT_FPS);

        Status.Payload payload = status.payload();
        payload.put("name", "connect");
        payload.put("bundle", bundle);

        status.dispatch("dispatch", payload);
      });

      this._cameraButton.setEnabled(false);
    });
  }

  @Override
  protected void onEvent(JanusEvent data, ArgBundle context) {

  }
}
