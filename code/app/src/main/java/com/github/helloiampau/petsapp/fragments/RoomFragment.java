package com.github.helloiampau.petsapp.fragments;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.github.helloiampau.janus.rtc.MediaView;
import com.github.helloiampau.petsapp.Commands;
import com.github.helloiampau.petsapp.Pet;
import com.github.helloiampau.petsapp.R;
import com.github.helloiampau.petsapp.Status;

import java.util.HashMap;


public class RoomFragment extends Fragment {
  private PetAdapter _currentAdapter;

  class PetAdapter extends BaseAdapter implements LifecycleOwner {
    private LayoutInflater inflater;
    private HashMap<String, Pet> pets;
    private String[] keys;
    private LifecycleRegistry registry;

    public PetAdapter(HashMap<String, Pet> pets, LayoutInflater inflater) {
      this.pets = pets;
      this.keys = this.pets.keySet().toArray(new String[this.pets.size()]);
      this.inflater = inflater;
      this.registry = new LifecycleRegistry(this);
      this.registry.markState(Lifecycle.State.STARTED);
    }

    @Override
    public int getCount() {
      return keys.length;
    }

    @Override
    public Pet getItem(int position) {
      return this.pets.get(keys[position]);
    }

    @Override
    public long getItemId(int position) {
      return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      Pet pet = this.getItem(position);
      View layout = this.inflater.inflate(R.layout.pet, parent, false);

      TextView name = layout.findViewById(R.id.petname);
      name.setText(pet.getName());

      pet.getMedia().observe(this, (m) -> {
        if(m == null) {
          return;
        }

        MediaView mediaView = layout.findViewById(R.id.petvideo);
        m.addRemoteTrackSink(mediaView);
      });

      return layout;
    }

    public void destroy() {
      this.registry.markState(Lifecycle.State.DESTROYED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
      return this.registry;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View room = inflater.inflate(R.layout.room, container, false);

    Status.get().pets().observe(this, p -> {
      if(this._currentAdapter != null) {
        this._currentAdapter.destroy();
      }

      this._currentAdapter = new PetAdapter(p, inflater);

      GridView grid = room.findViewById(R.id.room_layout);
      grid.setAdapter(this._currentAdapter);
    });

    Commands.publish();

    return room;
  }
}
