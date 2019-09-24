package com.github.helloiampau.petsapp;

import android.support.v7.app.AppCompatActivity;

import com.github.helloiampau.janus.JanusConfImpl;
import com.github.helloiampau.janus.JanusFactory;
import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.CameraDevice;
import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.ReadyState;
import com.github.helloiampau.petsapp.delegates.PetsAppJanusDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Commands {
  private static AppCompatActivity _context;
  private final static ExecutorService _queue = Executors.newSingleThreadExecutor();

  public static void init() {
    Commands._queue.submit(() -> {
      Janus janus = JanusFactory.create(Commands._context);
      JanusConfImpl conf = new JanusConfImpl();
      conf.url("http://janus.helloiampau.com/janus");

      ReadyState readyState = janus.init(conf, new PetsAppJanusDelegate(janus));

      if(readyState != ReadyState.READY) {
        Status.get().error("Unable to connect");
        return;
      }

      janus.attach("janus.plugin.videoroom", ArgBundle.create());
    });
  }

  public static void route(String destination) {
    Commands._queue.submit(() -> {
      Status.get().route(destination);
    });
  }

  public static void login(String username) {
    Commands._queue.submit(() -> {
      Status status = Status.get();
      Plugin handle = status.plugin().getValue();

      ArgBundle bundle = ArgBundle.create();
      bundle.setString("display", username);
      bundle.setInt("room", 1234);

      handle.dispatch("join", bundle);
    });
  }

  public static void publish() {
    Commands._queue.submit(() -> {
      ArgBundle bundle = ArgBundle.create();
      bundle.setBool("audio", true);
      bundle.setBool("video", true);
      bundle.setInt("camera", CameraDevice.FRONT.ordinal());
      bundle.setInt("height", 240);
      bundle.setInt("width", 320);
      bundle.setInt("fps", 40);

      Plugin handle = Status.get().plugin().getValue();
      handle.dispatch("publish", bundle);
    });
  }

  public static void updatePets(JSONArray jsonArray) {
    Commands._queue.submit(() -> {
      HashMap<String, Pet> pets = new HashMap<>();
      HashMap<String, Pet> oldPets = Status.get().pets().getValue();

      try {
        for (int index = 0; index < jsonArray.length(); index++) {
          JSONObject obj = jsonArray.getJSONObject(index);

          String id = Long.toString(obj.getLong("id"));

          Pet pet;
          if (oldPets == null || !oldPets.containsKey(id)) {
            String name = obj.getString("display");
            pet = new Pet(id, name);
          } else {
            pet = oldPets.get(id);
          }

          pets.put(id, pet);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }

      Status.get().pets(pets);
    });
  }

  public static void subscribe(String id) {
    Commands._queue.submit(() -> {
      Status status = Status.get();
      Plugin plugin = status.plugin().getValue();

      ArgBundle bundle = ArgBundle.create();
      bundle.setString("subscriber-id", id);
      plugin.dispatch("subscribe", bundle);
    });
  }

  public static void attachFeed(String id) {
    Commands._queue.submit(() -> {
      Status status = Status.get();
      Pet pet = status.pets().getValue().get(id);

      ArgBundle bundle = ArgBundle.create();
      bundle.setLong("room", 1234);
      bundle.setLong("feed", Long.parseLong(id));

      pet.getHandle().dispatch("join", bundle);
    });
  }

  public static void updatePetMedia(String id, Media media) {
    Commands._queue.submit(() -> {
      Status status = Status.get();
      Pet pet = status.pets().getValue().get(id);

      pet.setMedia(media);
    });
  }

  public static void registerContex(AppCompatActivity context) {
    Commands._context = context;
  }
}
