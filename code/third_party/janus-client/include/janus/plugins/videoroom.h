#pragma once

#include "janus/janus_plugin.h"
#include "janus/arg_bundle_impl.h"
#include "janus/plugin_factory.hpp"
#include "janus/constraints.hpp"
#include "janus/janus_event.hpp"
#include "janus/janus_plugin_info.hpp"

namespace Janus {

  class Videoroom : public JanusPlugin {
   public:
     void dispatch(const std::string& name, const std::shared_ptr<ArgBundle>& data);
     void onEvent(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context);
     void onOffer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context);
     void onAnswer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context);
  };

  class VideoroomFactory : public PluginFactory {
   public:
     JanusPluginInfo info() {
       return JanusPluginInfo("janus.plugin.videoroom", "Videoroom plugin", 0);
     }

     std::shared_ptr<Plugin> create();
  };

}  // namespace Janus
