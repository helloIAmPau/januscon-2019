package com.github.helloiampau.janus.app;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.github.helloiampau.janus.JanusConfImpl;
import com.github.helloiampau.janus.JanusFactory;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.JanusDelegate;
import com.github.helloiampau.janus.generated.JanusError;
import com.github.helloiampau.janus.generated.JanusEvent;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.PluginDelegate;
import com.github.helloiampau.janus.generated.ReadyState;
import com.github.helloiampau.janus.rtc.MediaImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import helloiampau.github.com.janus.R;

public class Actions {

  public static void register(Status status, Activity activity) {

    status.action("inspect", p -> {
      Status.DataHolder data = p.data();
      Log.v("janus-client", "Set a breakpoint on this line in order to inspect current status");
    });

    status.action("init", p -> {
      Janus janus = JanusFactory.create(activity);

      activity.runOnUiThread(() -> {
        String name = "user";

        try {
          JSONObject list = new JSONObject(activity.getResources().getString(R.string.random_names));

          Random r = new Random();
          JSONArray left = list.getJSONArray("left");
          JSONArray right = list.getJSONArray("right");

          name = left.getString(r.nextInt(left.length())) + " " + right.getString(r.nextInt(right.length()));
        } catch (JSONException e) {
          e.printStackTrace();
        }

        Status.DataHolder data = p.data();
        data.location.setValue("settings");
        data.backStack.setValue(new ArrayList<String>());
        data.host.setValue("http://10.0.2.2:8088/janus");
        data.janusService = janus;
        data.readyState.setValue(janus.readyState());
        data.plugins.setValue(data.janusService.plugins());
        data.media.setValue(null);
        data.feedList.setValue(null);
        data.feed.setValue(null);
        data.roomList.setValue(null);
        data.room.setValue(null);
        data.display.setValue(name);
        data.users.setValue(new HashMap<>());

      });
    });

    status.action("route", panel -> {
      String currentLocation = (String) panel.payload().get("current-location");

      if (currentLocation != null) {
        panel.data().backStack.getValue().add(currentLocation);
      }

      activity.runOnUiThread(() -> {
        String location = (String) panel.payload().get("location");
        panel.data().location.setValue(location);
      });
    });

    status.action("go-back", panel -> {
      Status.DataHolder data = panel.data();

      ArrayList<String> backStack = data.backStack.getValue();

      if (backStack.size() == 0) {
        activity.finish();
      }

      String location = backStack.get(backStack.size() - 1);
      backStack.remove(location);

      if (location.equals("settings")) {
        data.session.getValue().detach();

        activity.runOnUiThread(() -> {
          data.pluginId.setValue(null);
          data.session.setValue(null);
          data.room.setValue(null);
        });
      }

      activity.runOnUiThread(() -> {
        data.location.setValue(location);
      });
    });

    status.action("update-host", p -> {
      activity.runOnUiThread(() -> {
        p.data().host.setValue(p.payload().value("host"));
      });
    });

    status.action("subscribe", p -> {
      Status.Payload payload = p.payload();
      Status.User user = payload.value("user");

      ArgBundle bundle = ArgBundle.create();
      bundle.setString("subscriber-id", Long.toString(user.id()));

      payload.value("name", "subscribe");
      payload.value("bundle", bundle);

      status.dispatch("dispatch", payload);
    });

    status.action("connect", p -> {
      Status.DataHolder data = p.data();

      JanusConfImpl conf = new JanusConfImpl();
      conf.url(data.host.getValue());

      ReadyState readyState = data.janusService.init(conf, new JanusDelegate() {
        @Override
        public void onJanusError(JanusError error) {
          Status.Payload payload = status.payload();
          payload.value("janus-errpr", error);

          status.dispatch("error", payload);
        }

        @Override
        public void onPluginEnabled(String id, Plugin handle, ArgBundle context) {
          Status.Payload payload = status.payload();
          payload.value("id", id);
          payload.value("handle", handle);
          payload.value("context", context);

          status.dispatch("enable", payload);
        }
      });

      activity.runOnUiThread(() -> {
        data.readyState.setValue(readyState);
        data.plugins.setValue(data.janusService.plugins());
      });
    });

    status.action("disconnect", p -> {
      Status.DataHolder data = p.data();

      ReadyState readyState = data.janusService.close();

      activity.runOnUiThread(() -> {
        data.readyState.setValue(readyState);
        data.plugins.setValue(data.janusService.plugins());
      });
    });

    status.action("attach", p -> {
      Status.Payload payload = p.payload();

      Status.DataHolder data = p.data();
      data.janusService.attach(payload.value("plugin"), ArgBundle.create());
    });

    status.action("enable-media", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      activity.runOnUiThread(() -> {
        data.media.setValue(payload.value("media"));
      });
    });

    status.action("disable-media", p -> {
      Status.DataHolder data = p.data();

      MediaImpl media = data.media.getValue();

      if (media == null) {
        return;
      }

      activity.runOnUiThread(() -> {
        media.removeLocalTrackSink();
        media.removeRemoteTrackSink();

        if (media.localAudioTrack() != null) {
          media.localAudioTrack().stop();
        }

        if (media.localVideoTrack() != null) {
          media.localVideoTrack().stop();
        }

        data.media.setValue(null);
      });
    });

    status.action("enable", p -> {
      Status.DataHolder data = p.data();

      Status.Payload payload = p.payload();
      Plugin handle = payload.value("handle");
      String pluginId = payload.value("id");

      if (pluginId.equals("janus.plugin.videoroom.subscriber")) {
        ArgBundle context = payload.value("context");
        Long subscriberId = new Long(context.getString("subscriber-id"));

        handle.setDelegate(new PluginDelegate() {
          @Override
          public void onEvent(JanusEvent data, ArgBundle context) {
          }

          @Override
          public void onMediaChanged(Media media) {
            activity.runOnUiThread(() -> {
              data.users.getValue().get(subscriberId).media().setValue((MediaImpl) media);
            });
          }

          @Override
          public void onHangup() {
            HashMap<Long, Status.User> users = data.users.getValue();

            users.get(subscriberId).media().getValue().removeRemoteTrackSink();
            users.remove(subscriberId);
            data.users.postValue(users);
          }

          @Override
          public void onDetach() {

          }

          @Override
          public String onOffer(String sdp) {
            return sdp;
          }

          @Override
          public String onAnswer(String sdp) {
            return sdp;
          }
        });

        ArgBundle arguments = ArgBundle.create();
        arguments.setLong("room", data.room.getValue().room());
        arguments.setLong("feed", subscriberId);

        handle.dispatch("join", arguments);

        return;
      }

      activity.runOnUiThread(() -> {
        data.pluginId.setValue(pluginId);
        data.session.setValue(handle);
      });
    });

    status.action("hangup", p -> {
      Status.DataHolder data = p.data();

      data.session.getValue().hangup();
    });

    status.action("dispatch", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      data.session.getValue().dispatch((String) payload.get("name"), (ArgBundle) payload.get("bundle"));
    });

    status.action("switch-camera", p -> {
      Status.DataHolder data = p.data();

      data.media.getValue().localVideoTrack().switchCamera();
    });

    status.action("get-feed-list", p -> {
      Status.DataHolder data = p.data();

      data.session.getValue().dispatch("list", ArgBundle.create());
    });

    status.action("update-feeds", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      ArrayList<Status.Feed> list = payload.value("list");

      activity.runOnUiThread(() -> {
        data.feedList.setValue(list);
      });
    });

    status.action("get-room-list", p -> {
      Status.DataHolder data = p.data();

      data.session.getValue().dispatch("list", ArgBundle.create());
    });

    status.action("update-rooms", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      ArrayList<Status.Room> list = payload.value("list");

      activity.runOnUiThread(() -> {
        data.roomList.setValue(list);
      });
    });

    status.action("update-room", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      JSONObject event = payload.value("event");

      HashMap<Long, Status.User> users = data.users.getValue();

      try {
        JSONArray jsonArray = event.getJSONArray("publishers");

        for (int index = 0; index < jsonArray.length(); index++) {
          JSONObject obj = jsonArray.getJSONObject(index);

          long id = obj.getLong("id");
          if (users.containsKey(id) == false) {
            users.put(id, status.user(obj));
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      data.users.postValue(users);
    });

    status.action("select-room", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      Status.Room room = payload.value("room");

      activity.runOnUiThread(() -> {
        data.room.setValue(room);
      });
    });

    status.action("join-room", p -> {
      Status.DataHolder data = p.data();

      Status.Payload payload = status.payload();

      ArgBundle bundle = ArgBundle.create();
      bundle.setInt("room", data.room.getValue().room());
      bundle.setString("display", data.display.getValue());

      payload.value("name", "join");
      payload.value("bundle", bundle);

      status.dispatch("dispatch", payload);
    });

    status.action("select-feed", p -> {
      Status.DataHolder data = p.data();
      Status.Payload payload = p.payload();

      activity.runOnUiThread(() -> {
        data.feed.setValue(payload.value("feed"));
      });
    });

    status.action("play-feed", p -> {
      Status.Payload payload = p.payload();
      Status.DataHolder data = p.data();

      ArgBundle bundle = ArgBundle.create();
      bundle.setInt("id", data.feed.getValue().id());
      bundle.setBool("offer_video", payload.value("video"));
      bundle.setBool("offer_audio", payload.value("audio"));

      payload.value("name", "watch");
      payload.value("bundle", bundle);

      status.dispatch("dispatch", payload);
    });

    status.action("remove-feed", p -> {
      Status.DataHolder data = p.data();

      status.dispatch("hangup");

      activity.runOnUiThread(() -> {
        data.feed.setValue(null);
      });
    });

    status.action("update-display", p -> {
      activity.runOnUiThread(() -> {
        p.data().display.setValue(p.payload().value("display"));
      });
    });
  }

}