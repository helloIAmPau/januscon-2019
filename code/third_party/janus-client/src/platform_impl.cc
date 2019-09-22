#include "janus/platform_impl.h"
#include "janus/janus_plugin_info.hpp"
#include "janus/janus_plugin.h"

namespace Janus {

  std::shared_ptr<Platform> Platform::create() {
    auto platform = std::make_shared<PlatformImpl>();
    JanusPlugin::registerJanusPlugins(platform);

    return platform;
  }

  PlatformImpl::PlatformImpl() {
    this->_protocolFactory = std::make_shared<JanusApiFactory>();
  }

  void PlatformImpl::registerProtocolFactory(const std::shared_ptr<ProtocolFactory>& factory) {
    this->_protocolFactory = factory;
  }

  std::shared_ptr<Protocol> PlatformImpl::getProtocol(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<JanusDelegate>& delegate) {
    return this->_protocolFactory->bootstrap(conf, this->shared_from_this(), delegate);
  }

  void PlatformImpl::registerPeerFactory(const std::shared_ptr<PeerFactory>& factory) {
    this->_peerFactory = factory;
  }

  std::shared_ptr<Peer> PlatformImpl::createPeer(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Plugin>& delegate) {
    return this->_peerFactory->create(conf, delegate);
  }

  void PlatformImpl::registerPluginFactory(const std::shared_ptr<PluginFactory>& pluginFactory) {
    this->_plugins.insert({ pluginFactory->info().id, pluginFactory });
  }

  std::shared_ptr<Plugin> PlatformImpl::getPlugin(const std::string& id) {
    auto iterator = this->_plugins.find(id);

    if(iterator == this->_plugins.end()) {
      return nullptr;
    }

    return iterator->second->create();
  }

  std::vector<JanusPluginInfo> PlatformImpl::getPlugins() {
    std::vector<JanusPluginInfo> plugins;
    auto iterator = this->_plugins.begin();

    while(iterator != this->_plugins.end()) {
      plugins.push_back(iterator->second->info());
      iterator++;
    }

    return plugins;
  }

}  // namespace Janus
