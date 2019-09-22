#pragma once

#include <string>
#include <memory>
#include <mutex>
#include <unordered_map>
#include "janus/plugin.hpp"
#include "janus/signaling.hpp"
#include "janus/peer.hpp"
#include "janus/command.hpp"
#include "janus/platform.hpp"
#include "janus/plugin_delegate.hpp"

namespace Janus {

  class JanusPlugin : public Plugin {
   public:
     void init(const std::shared_ptr<Signaling>& signaling, const std::shared_ptr<Peer>& peer);
     void detach();
     void hangup();

     void setDelegate(const std::shared_ptr<PluginDelegate>& delegate);
     void onMediaChanged(const std::shared_ptr<Media>& media);
     void onHangup();
     void onDetach();
     void onIceCandidate(const Candidate& candidate);

     static void registerJanusPlugins(std::shared_ptr<Platform> platform);
   protected:
     std::shared_ptr<Signaling> _signaling = nullptr;
     std::shared_ptr<Peer> _peer = nullptr;
     std::shared_ptr<PluginDelegate> _delegate = nullptr;
  };

}  // namespace Janus
