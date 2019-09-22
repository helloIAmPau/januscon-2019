package com.github.helloiampau.janus.app;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.JanusPluginInfo;

import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.generated.ReadyState;
import com.github.helloiampau.janus.rtc.MediaImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Status extends ViewModel {

  public class Panel {
    private DataHolder _data;
    private Payload _payload;

    public Panel(DataHolder data, Payload payload) {
      this._data = data;
      this._payload = payload;
    }

    public DataHolder data() {
      return this._data;
    }

    public Payload payload() {
      return this._payload;
    }
  }

  public class Payload extends HashMap<String, Object> {
    public <T> void value(String key, T value) {
      this.put(key, value);
    }

    public <T> T value(String key) {
      return (T) this.get(key);
    }
  }

  public class Feed {

    private int _id;
    private String _description;
    private String _type;

    public Feed(JSONObject obj) {
      try {
        this._id = obj.getInt("id");
        this._description = obj.getString("description");
        this._type = obj.getString("type");
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    public int id() {
      return this._id;
    }

    public String description() {
      return this._description;
    }

    public String type() {
      return this._type;
    }

  }

  public class Room {

    private int _room;
    private String _description;

    public Room(JSONObject obj) {
      try {
        this._room = obj.getInt("room");
        this._description = obj.getString("description");
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    public int room() {
      return this._room;
    }

    public String description() {
      return this._description;
    }

  }

  public class User {
    private long _id;
    private String _display;
    private boolean _publisher;
    private MutableLiveData<MediaImpl> _media;

    public User(JSONObject obj) throws JSONException {
      this(obj.getLong("id"), obj.getString("display"), false);
    }

    public User(long id, String display, boolean isPublisher) {
      this._id = id;
      this._display = display;
      this._publisher = isPublisher;

      this._media = new MutableLiveData<>();
    }

    public long id() {
      return this._id;
    }

    public String display() {
      return this._display;
    }

    public boolean isPublisher() {
      return this._publisher;
    }

    public MutableLiveData<MediaImpl> media() {
      return this._media;
    }

  }

  public class DataHolder {

    public MutableLiveData<String> location = new MutableLiveData<>();
    public MutableLiveData<ArrayList<String>> backStack = new MutableLiveData<>();
    public MutableLiveData<String> host = new MutableLiveData<>();
    public Janus janusService = null;
    public MutableLiveData<ReadyState> readyState = new MutableLiveData<>();
    public MutableLiveData<ArrayList<JanusPluginInfo>> plugins = new MutableLiveData<>();
    public MutableLiveData<Plugin> session = new MutableLiveData<>();
    public MutableLiveData<String> pluginId = new MutableLiveData<>();
    public MutableLiveData<MediaImpl> media = new MutableLiveData<>();
    public MutableLiveData<ArrayList<Feed>> feedList = new MutableLiveData<>();
    public MutableLiveData<Feed> feed = new MutableLiveData<>();
    public MutableLiveData<ArrayList<Room>> roomList = new MutableLiveData<>();
    public MutableLiveData<Room> room = new MutableLiveData<>();
    public MutableLiveData<String> display = new MutableLiveData<>();
    public MutableLiveData<HashMap<Long, User>> users = new MutableLiveData<>();
  }

  final DataHolder _data = new DataHolder();

  final ExecutorService _actionQueue = Executors.newSingleThreadExecutor();
  final Map<String, Consumer<Panel>> _actions = new HashMap<>();

  public void dispatch(String action, Payload payload) {
    if (this._actions.containsKey(action) == false) {
      return;
    }

    this._actionQueue.submit(() -> {
      this._actions.get(action).accept(new Panel(this._data, payload));
    });
  }

  public void dispatch(String action) {
    this.dispatch(action, this.payload());
  }

  public void action(String name, Consumer<Panel> handler) {
    this._actions.put(name, handler);
  }

  public <T> LiveData<T> get(String key) {
    try {
      Field field = DataHolder.class.getField(key);
      return (LiveData<T>) field.get(this._data);
    } catch (Exception e) {
    }

    return null;
  }

  public Feed feed(JSONObject obj) {
    return new Feed(obj);
  }

  public Payload payload() {
    return new Payload();
  }

  public Room room(JSONObject obj) {
    return new Room(obj);
  }

  public User user(JSONObject obj) {
    try {
      return new User(obj);
    } catch (JSONException e) {
      return null;
    }
  }

  public User user(int id, String display) {
    return this.user(id, display, false);
  }

  public User user(int id, String display, boolean isPublisher) {
    return new User(id, display, isPublisher);
  }

}
