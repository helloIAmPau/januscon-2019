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
import com.github.helloiampau.janus.generated.AudioDevice;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.rtc.MediaImpl;
import com.github.helloiampau.janus.rtc.MediaView;

import helloiampau.github.com.janus.R;

public class Theater extends Fragment {

  private MediaView _surface;
  private ImageButton _playButton;
  private Switch _videoSwitch;
  private Switch _audioSwitch;
  private ImageButton _listButton;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.theater, container, false);

    this._playButton = layout.findViewById(R.id.playButton);
    this._surface = layout.findViewById(R.id.surface);

    this._videoSwitch = layout.findViewById(R.id.video_switch);
    this._audioSwitch = layout.findViewById(R.id.audio_switch);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    this._listButton = layout.findViewById(R.id.listButton);
    this._listButton.setOnClickListener(v -> {
      status.dispatch("remove-feed");
    });

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("media").observe(this, m -> {
      if(m != null) {
        return;
      }

      this._playButton.setImageResource(android.R.drawable.ic_media_play);

      this._playButton.setOnClickListener(v -> {
        Status.Payload payload = status.payload();
        payload.value("video", this._videoSwitch.isChecked());
        payload.value("audio", this._audioSwitch.isChecked());

        status.dispatch("play-feed", payload);
      });
    });

    status.get("media").observe(this, m -> {
      if(m == null) {
        return;
      }

      MediaImpl media = (MediaImpl) m;

      if(media.remoteVideoTrack() == null) {
        return;
      }

      this._playButton.setImageResource(android.R.drawable.ic_media_pause);

      this._playButton.setOnClickListener(v -> {
        status.dispatch("hangup");
      });

      media.addRemoteTrackSink(this._surface);

      if(media.remoteAudioTrack() != null) {
        media.remoteAudioTrack().setOutputDevice(AudioDevice.SPEAKER);
      }
    });
  }

}
