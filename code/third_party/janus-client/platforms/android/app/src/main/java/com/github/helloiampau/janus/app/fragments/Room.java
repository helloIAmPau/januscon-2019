package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.CameraDevice;
import com.github.helloiampau.janus.rtc.LocalVideoTrackImpl;
import com.github.helloiampau.janus.rtc.MediaImpl;
import com.github.helloiampau.janus.rtc.MediaView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import helloiampau.github.com.janus.R;

public class Room extends Fragment {

  private GridView _grid;
  private ImageButton _publish;
  private Switch _audioSwitch;
  private Switch _videoSwitch;
  private ImageButton _cameraButton;

  private View _local;

  private HashMap<Long, View> _remotes;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.room, container, false);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    this._grid = layout.findViewById(R.id.grid);
    this._publish = layout.findViewById(R.id.publish_button);
    this._audioSwitch = layout.findViewById(R.id.audio_switch);
    this._videoSwitch = layout.findViewById(R.id.video_switch);
    this._cameraButton = layout.findViewById(R.id.camera_button);
    this._cameraButton.setOnClickListener(l -> {
      status.dispatch("switch-camera");
    });

    this._local = inflater.inflate(R.layout.user, null, false);

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("room").observe(this, r -> {
      if(r == null) {
        return;
      }

      status.dispatch("join-room");
    });

    status.get("media").observe(this, m -> {
      if (m == null) {
        return;
      }

      this._publish.setImageResource(android.R.drawable.sym_call_missed);
      this._publish.setOnClickListener(v -> {
        status.dispatch("hangup");
      });

      this._cameraButton.setEnabled(true);

      MediaView view = this._local.findViewById(R.id.video_container);

      MediaImpl media = (MediaImpl) m;
      media.addLocalTrackSink(view);
    });

    status.get("media").observe(this, m -> {
      if (m != null) {
        return;
      }

      this._publish.setImageResource(android.R.drawable.ic_menu_call);
      this._publish.setOnClickListener(v -> {
        ArgBundle bundle = ArgBundle.create();
        bundle.setBool("audio", this._audioSwitch.isChecked());
        bundle.setBool("video", this._videoSwitch.isChecked());
        bundle.setInt("camera", CameraDevice.FRONT.ordinal());
        bundle.setInt("height", LocalVideoTrackImpl.DEFAULT_WIDTH);
        bundle.setInt("width", LocalVideoTrackImpl.DEFAULT_HEIGHT);
        bundle.setInt("fps", LocalVideoTrackImpl.DEFAULT_FPS);

        Status.Payload payload = status.payload();
        payload.put("name", "publish");
        payload.put("bundle", bundle);

        status.dispatch("dispatch", payload);
      });

      this._cameraButton.setEnabled(false);
    });

    status.get("users").observe(this, l -> {
      if (l == null) {
        return;
      }

      HashMap<Long, Status.User> users = (HashMap<Long, Status.User>) l;
      List<Long> keys = new ArrayList<>(users.keySet());
      LayoutInflater inflater = this.getLayoutInflater();

      HashMap<Long, View> views = new HashMap<>();
      for(Long id : users.keySet()) {
        if(this._remotes.containsKey(id)) {
          views.put(id, this._remotes.get(id));
        }
      }
      this._remotes = views;

      Room self =  this;

      this._grid.setAdapter(new BaseAdapter() {
        @Override
        public int getCount() {
          return keys.size() + 1;
        }

        @Override
        public Status.User getItem(int i) {
          if(i == 0) {
            return status.user(-999, "me", true);
          }

          return users.get(this.getItemId(i));
        }

        @Override
        public long getItemId(int i) {
          if(i == 0) {
            return -999;
          }

          return keys.get(i - 1);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
          if(i == 0) {
            return self._local;
          }

          Status.User user = this.getItem(i);

          if(self._remotes.containsKey(user.id())) {
            return self._remotes.get(user.id());
          }

          View layout = inflater.inflate(R.layout.user, null, false);
          TextView display = layout.findViewById(R.id.display_container);
          display.setText(user.display());

          if(user.isPublisher() == false) {
            user.media().observe(self, m -> {
              MediaView mediaView = layout.findViewById(R.id.video_container);
              m.addRemoteTrackSink(mediaView);
            });

            Status.Payload payload = status.payload();
            payload.value("user", user);
            status.dispatch("subscribe", payload);
          }

          self._remotes.put(user.id(), layout);

          return layout;
        }
      });
    });

  }

}
