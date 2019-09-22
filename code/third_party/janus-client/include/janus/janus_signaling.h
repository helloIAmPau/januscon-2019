#pragma once

#include <string>
#include <memory>
#include "janus/transport.h"
#include "janus/protocol.hpp"
#include "janus/signaling.hpp"

namespace Janus {

  class JanusSignaling : public Signaling {
   public:
     JanusSignaling(const std::string& handleId, const std::shared_ptr<Transport>& transport, const std::shared_ptr<Protocol>& protocol);

     void message(const std::string& body, const std::shared_ptr<ArgBundle>& context);
     void jsep(const std::string& body, const std::shared_ptr<Jsep>& jsep, const std::shared_ptr<ArgBundle>& context);
     void trickle(const Candidate& candidate);
     void attach(const std::string& id, const std::shared_ptr<ArgBundle>& context);
     void detach();
     void hangup();

   private:
     std::string _id = "";
     std::shared_ptr<Transport> _transport = nullptr;
     std::shared_ptr<Protocol> _protocol = nullptr;
  };

  class JanusSignalingFactory {
   public:
     virtual std::shared_ptr<Signaling> create(const std::string& handleId, const std::shared_ptr<Transport>& transport, const std::shared_ptr<Protocol>& protocol) = 0;
  };

  class JanusSignalingFactoryImpl : public JanusSignalingFactory {
   public:
     std::shared_ptr<Signaling> create(const std::string& handleId, const std::shared_ptr<Transport>& transport, const std::shared_ptr<Protocol>& protocol);
  };

}  // namespace Janus
