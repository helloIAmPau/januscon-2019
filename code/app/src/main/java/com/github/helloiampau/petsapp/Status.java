package com.github.helloiampau.petsapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;

import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.Plugin;

import java.util.HashMap;

public class Status extends ViewModel {

  private MutableLiveData<Janus> _session = new MutableLiveData<>();
  public LiveData<Janus> session() {
    return _session;
  }
  public void session(Janus janusSession) {
    this._session.postValue(janusSession);
  }

  private MutableLiveData<Plugin> _plugin = new MutableLiveData<>();
  public LiveData<Plugin> plugin() {
    return _plugin;
  }
  public void plugin(Plugin plugin) {
    this._plugin.postValue(plugin);
  }

  private MutableLiveData<String> _route = new MutableLiveData<>();
  public LiveData<String> route() {
    return _route;
  }
  public void route(String route) {
    this._route.postValue(route);
  }

  private MutableLiveData<String> _error = new MutableLiveData<>();
  public LiveData<String> error() {
    return _error;
  }
  public void error(String message) {
    this._error.postValue(message);
  }

  private MutableLiveData<HashMap<String, Pet>> _pets = new MutableLiveData<>();
  public LiveData<HashMap<String, Pet>> pets() {
    return _pets;
  }
  public void pets(HashMap<String, Pet> users) {
    this._pets.postValue(users);
  }

  private static AppCompatActivity _context;
  public static void registerContext(AppCompatActivity context) {
    Status._context = context;
  }

  public static Status get() {
    return ViewModelProviders.of(Status._context).get(Status.class);
  }
}
