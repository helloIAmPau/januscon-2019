package com.github.helloiampau.janus.app.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.github.helloiampau.janus.app.Status;
import com.github.helloiampau.janus.generated.ArgBundle;

import java.util.List;

import helloiampau.github.com.janus.R;

public class FeedList extends Fragment {

  private RecyclerView _feedList;

  class FeedViewHolder extends RecyclerView.ViewHolder {

    private Status _status;

    public FeedViewHolder(@NonNull View itemView, Status status) {
      super(itemView);

      this._status = status;
    }

    public void draw(Status.Feed feed) {
      this.itemView.setOnClickListener(v -> {
        Status.Payload payload = this._status.payload();
        payload.value("feed", feed);

        this._status.dispatch("select-feed", payload);
      });

      TextView typeView = this.itemView.findViewById(R.id.type);
      typeView.setText(feed.type());

      TextView descriptionView = this.itemView.findViewById(R.id.description);
      descriptionView.setText(feed.description());
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
    View layout = inflater.inflate(R.layout.feed_list, container, false);

    this._feedList = layout.findViewById(R.id.feed_list);

    return layout;
  }

  @Override
  public void onActivityCreated(Bundle state) {
    super.onActivityCreated(state);

    Status status = ViewModelProviders.of(this.getActivity()).get(Status.class);

    status.get("feedList").observe(this, l -> {
      if(l != null) {
        return;
      }

      status.dispatch("get-feed-list");
    });

    status.get("feedList").observe(this, l -> {
      if(l == null) {
        return;
      }

      List<Status.Feed> feeds = (List<Status.Feed>) l;
      FeedList self = this;

      this._feedList.setLayoutManager(new LinearLayoutManager(this.getActivity()));
      this._feedList.setAdapter(new RecyclerView.Adapter() {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View item = inflater.inflate(R.layout.feed_item, parent, false);

            return new FeedViewHolder(item, status);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
          ((FeedViewHolder) viewHolder).draw(feeds.get(i));
        }

        @Override
        public int getItemCount() {
          return feeds.size();
        }
      });
    });

  }

}
