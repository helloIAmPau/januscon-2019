package com.github.helloiampau.petsapp.delegates;

import com.github.helloiampau.janus.generated.ArgBundle;
import com.github.helloiampau.janus.generated.Janus;
import com.github.helloiampau.janus.generated.JanusDelegate;
import com.github.helloiampau.janus.generated.JanusError;
import com.github.helloiampau.janus.generated.Plugin;
import com.github.helloiampau.petsapp.Commands;
import com.github.helloiampau.petsapp.Pet;
import com.github.helloiampau.petsapp.Status;

public class PetsAppJanusDelegate extends JanusDelegate {
  private Janus _janus;

  public PetsAppJanusDelegate(Janus janus) {
    this._janus = janus;
  }

  @Override
  public void onJanusError(JanusError error) {
    Status.get().error(error.getReason());
  }

  @Override
  public void onPluginEnabled(String id, Plugin handle, ArgBundle context) {
    if(id.equals("janus.plugin.videoroom")) {
      Status status = Status.get();

      status.loggedIn(false);

      handle.setDelegate(new PublisherPluginDelegate());
      status.plugin(handle);

      status.session(this._janus);

      return;
    }

    if(id.equals("janus.plugin.videoroom.subscriber")) {
      Status status = Status.get();

      String subscriberid = context.getString("subscriber-id");
      handle.setDelegate(new SubscriberPluginDelegate(subscriberid));

      Pet pet = status.pets().getValue().get(subscriberid);
      pet.setHandle(handle);

      return;
    }
  }
}
