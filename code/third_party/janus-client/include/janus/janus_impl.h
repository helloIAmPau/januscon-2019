#pragma once

#include <string>
#include <vector>
#include <memory>
#include "janus/janus.hpp"
#include "janus/platform.hpp"
#include "janus/ready_state.hpp"
#include "janus/conf.h"
#include "janus/protocol.hpp"
#include "janus/janus_delegate.hpp"
#include "janus/janus_plugin_info.hpp"

namespace Janus {

  class JanusImpl : public Janus {
   public:
     explicit JanusImpl(const std::shared_ptr<Platform>& platform);

     ReadyState init(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<JanusDelegate>& delegate);
     std::vector<JanusPluginInfo> plugins();
     void attach(const std::string& pluginId, const std::shared_ptr<ArgBundle>& context);
     ReadyState close();
     ReadyState readyState();

   private:
     std::shared_ptr<Protocol> _session = nullptr;
     std::shared_ptr<Platform> _platform = nullptr;
  };

}  // namespace Janus
