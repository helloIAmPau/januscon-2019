package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.helloiampau.janus.app.Status;

import java.util.List;

import helloiampau.github.com.janus.R;

public class RoomList extends Fragment {

  private RecyclerView _roomList;
  private TextView _username;

  class RoomViewHolder extends RecyclerView.ViewHolder {

    private Status _status;

    public RoomViewHolder(@NonNull View itemView, Status status) {
      super(itemView);

      this._status = status;
    }

    public void draw(Status.Room room) {
      this.itemView.setOnClickListener(v -> {
        Status.Payload payload = this._status.payload();
        payload.value("room", room);

        this._status.dispatch("select-room", payload);
      });

      TextView typeView = this.itemView.findViewById(R.id.room_description);
      typeView.setText(room.description());
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.room_list, container, false);

    this._roomList = layout.findViewById(R.id.room_list);
    this._username = layout.findViewById(R.id.display);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);
    this._username.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        Status.Payload payload = status.payload();
        payload.value("display", editable.toString());

        status.dispatch("update-display", payload);
      }
    });

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("display").observe(this, d -> {
      if(this._username != null && !this._username.getText().toString().equals(d)) {
        this._username.setText((String) d);
      }
    });

    status.get("roomList").observe(this, l -> {
      if(l != null) {
        return;
      }

      status.dispatch("get-room-list");
    });

    status.get("roomList").observe(this, l -> {
      if(l == null) {
        return;
      }

      List<Status.Room> rooms = (List<Status.Room>) l;
      RoomList self = this;

      this._roomList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
      this._roomList.setAdapter(new RecyclerView.Adapter() {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
          LayoutInflater inflater = LayoutInflater.from(parent.getContext());
          View item = inflater.inflate(R.layout.room_item, parent, false);

          return new RoomList.RoomViewHolder(item, status);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
          ((RoomList.RoomViewHolder) viewHolder).draw(rooms.get(i));
        }

        @Override
        public int getItemCount() {
          return rooms.size();
        }
      });
    });
  }

}
