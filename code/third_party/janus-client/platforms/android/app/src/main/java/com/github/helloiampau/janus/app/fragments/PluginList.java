package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.JanusPluginInfo;
import com.github.helloiampau.janus.generated.ReadyState;

import java.util.ArrayList;

import helloiampau.github.com.janus.R;

public class PluginList extends Fragment {

  class PluginViewHolder extends RecyclerView.ViewHolder {

    private FragmentActivity context;

    public PluginViewHolder(@NonNull View itemView, FragmentActivity context) {
      super(itemView);

      this.context = context;
    }

    public void draw(JanusPluginInfo info) {
      this.itemView.setOnClickListener(l -> {
        Status status = ViewModelProviders.of(this.context).get(Status.class);
        Status.Payload payload = status.payload();
        payload.put("plugin", info.getId());

        status.dispatch("attach", payload);
      });

      TextView nameView = this.itemView.findViewById(R.id.name_view);
      nameView.setText(info.getName());

      TextView idView = this.itemView.findViewById(R.id.id_view);
      idView.setText(info.getId());

      TextView versionView = this.itemView.findViewById(R.id.version_view);
      versionView.setText(Integer.toString(info.getVersion()));
    }

  }

  private TextView title;
  private RecyclerView pluginList;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.plugin_list, container, false);

    this.title = layout.findViewById(R.id.plugins_title);

    this.pluginList = layout.findViewById(R.id.plugin_list);
    this.pluginList.setLayoutManager(new LinearLayoutManager(this.getActivity()));

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);
    status.get("readyState").observe(this, r -> {
      if(r == ReadyState.READY) {
        this.title.setVisibility(View.VISIBLE);
      } else {
        this.title.setVisibility(View.GONE);
      }
    });

    status.get("pluginId").observe(this, id -> {
      Status.Payload payload = status.payload();

      if(id == null) {
        payload.value("location", "settings");
      } else {
        payload.value("current-location", "settings");
        payload.value("location", id);
      }

      status.dispatch("route", payload);
    });

    status.get("plugins").observe(this, p -> {
      ArrayList<JanusPluginInfo> list = (ArrayList<JanusPluginInfo>) p;

      FragmentActivity context = this.getActivity();

      this.pluginList.setAdapter(new RecyclerView.Adapter() {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int index) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View item = inflater.inflate(R.layout.list_item, parent, false);

            return new PluginViewHolder(item, context);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int index) {
          PluginViewHolder holder = (PluginViewHolder) viewHolder;
          holder.draw(list.get(index));
        }

        @Override
        public int getItemCount() {
          return list.size();
        }
      });
    });
  }

}
