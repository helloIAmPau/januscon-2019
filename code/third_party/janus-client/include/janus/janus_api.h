#pragma once

#include <vector>
#include <memory>
#include <string>
#include <unordered_map>
#include "janus/protocol_factory.hpp"
#include "janus/platform.hpp"
#include "janus/janus_plugin_info.hpp"
#include "janus/protocol.hpp"
#include "janus/janus_conf.hpp"
#include "janus/janus_delegate.hpp"
#include "janus/plugin.hpp"
#include "janus/info.hpp"
#include "janus/ready_state.hpp"
#include "janus/async.h"
#include "janus/arg_bundle_impl.h"
#include "janus/transport.h"
#include "janus/messages.h"
#include "janus/janus_signaling.h"

namespace Janus {

  class JanusApi : public Protocol, public TransportDelegate, public Task, public std::enable_shared_from_this<JanusApi> {
   public:
     explicit JanusApi(const std::shared_ptr<Platform>& platform);
     void init(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<HttpFactory>& httpFactory, const std::shared_ptr<TransportFactory>& transportFactory, const std::shared_ptr<JanusSignalingFactory>& signalingFactory, const std::shared_ptr<Async>& async, const std::shared_ptr<JanusDelegate>& delegate);
     Info info();
     ReadyState readyState();
     void close();
     std::vector<JanusPluginInfo> plugins();
     void attach(const std::string& pluginId, const std::shared_ptr<ArgBundle>& context);
     void onMessage(const std::string& response, const std::shared_ptr<ArgBundle>& context);

     void action();

   private:
     std::shared_ptr<JanusConf> _conf;

     void readyState(const ReadyState& readyState);
     ReadyState _readyState = ReadyState::OFF;
     std::mutex _readyStateMutex;

     std::shared_ptr<Transport> _transport = nullptr;
     std::shared_ptr<Async> _async = nullptr;

     std::shared_ptr<Platform> _platform = nullptr;
     std::shared_ptr<JanusSignalingFactory> _signalingFactory = nullptr;

     std::unordered_map<std::string, std::shared_ptr<Plugin>> _handles;
     std::mutex _handlesMutex;
     std::shared_ptr<Plugin> _handle(const std::string& handleId);

     std::shared_ptr<JanusDelegate> _delegate = nullptr;
  };

  class JanusApiFactory : public ProtocolFactory {
   public:
     std::shared_ptr<Protocol> bootstrap(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Platform>& platform, const std::shared_ptr<JanusDelegate>& delegate);
  };

}  // namespace Janus

