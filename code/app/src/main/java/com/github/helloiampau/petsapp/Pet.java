package com.github.helloiampau.petsapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.github.helloiampau.janus.generated.Media;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.janus.rtc.MediaImpl;

public class Pet {
  private String name;
  private MutableLiveData<MediaImpl> media;
  private Plugin handle;
  private String id;

  public Pet(String id, String name) {
    this.name = name;
    this.media = new MutableLiveData<>();
    this.id = id;

    Commands.subscribe(id);
  }

  public void setHandle(Plugin handle) {
    this.handle = handle;

    Commands.attachFeed(this.id);
  }

  public Plugin getHandle() {
    return this.handle;
  }

  public LiveData<MediaImpl> getMedia() {
    return this.media;
  }

  public void setMedia(Media media) {
    this.media.postValue((MediaImpl) media);
  }

  public String getName() {
    return this.name;
  }
}
