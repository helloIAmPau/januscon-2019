#pragma once

#include <memory>
#include <string>
#include <vector>
#include <unordered_map>
#include "janus/platform.hpp"
#include "janus/protocol.hpp"
#include "janus/janus_delegate.hpp"
#include "janus/protocol_factory.hpp"
#include "janus/janus_api.h"
#include "janus/conf.h"
#include "janus/peer.hpp"
#include "janus/peer_factory.hpp"
#include "janus/plugin_factory.hpp"

namespace Janus {

  class PlatformImpl : public Platform, public std::enable_shared_from_this<PlatformImpl> {
   public:
     PlatformImpl();

     void registerProtocolFactory(const std::shared_ptr<ProtocolFactory>& factory);
     std::shared_ptr<Protocol> getProtocol(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<JanusDelegate>& delegate);

     void registerPeerFactory(const std::shared_ptr<PeerFactory>& factory);
     std::shared_ptr<Peer> createPeer(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Plugin>& delegate);

     void registerPluginFactory(const std::shared_ptr<PluginFactory>& pluginFactory);
     std::shared_ptr<Plugin> getPlugin(const std::string& id);
     std::vector<JanusPluginInfo> getPlugins();

   private:
     std::shared_ptr<ProtocolFactory> _protocolFactory = nullptr;
     std::shared_ptr<PeerFactory> _peerFactory = nullptr;
     std::unordered_map<std::string, std::shared_ptr<PluginFactory>> _plugins;
  };

}  // namespace Janus
