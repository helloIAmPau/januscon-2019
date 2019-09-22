#pragma once

#include <memory>
#include <string>
#include "janus/janus_error.hpp"
#include "janus/janus_event.hpp"
#include "janus/candidate.hpp"
#include "janus/jsep_impl.h"
#include "janus/uuid.h"
#include "janus/json.h"
#include "janus/arg_bundle_impl.h"
#include "janus/http.h"

namespace Janus {

  class Transport;

  class Message {
   public:
     Message(const std::string& type, const std::shared_ptr<Uuid>& uuidFactory);
     explicit Message(const std::string& serialized);
     explicit Message(const std::shared_ptr<JSON>& content);

     std::string janus();
     std::string transaction();
     JanusError error();

     std::string serialize();

     template <typename T>
     std::shared_ptr<T> as() {
       return std::make_shared<T>(this->_content);
     }
   protected:
     std::shared_ptr<JSON> _content = nullptr;
  };

  class Event : public Message, public JanusEvent {
   public:
     using Message::Message;

     std::string sender();
     std::string data();
     std::shared_ptr<Jsep> jsep();
  };

  class Handle : public Message {
   public:
     using Message::Message;

     Handle(const std::string& type, const std::string& handleId);

     std::string handleId();
  };

  class PluginMessage : public Handle {
   public:
     using Handle::Handle;

     PluginMessage(const std::shared_ptr<JSON>& data, const std::string& handleId);

     std::shared_ptr<JSON> body();

     static void request(const std::shared_ptr<Transport>& transport, const std::shared_ptr<JSON>& data, const std::string& handleId, const std::shared_ptr<ArgBundle>& context);
  };

  class JsepMessage : public PluginMessage {
   public:
     using PluginMessage::PluginMessage;

     JsepMessage(const std::shared_ptr<JSON>& data, const std::shared_ptr<Jsep>& jsep, const std::string& handleId);

     std::shared_ptr<Jsep> jsep();

     static void request(const std::shared_ptr<Transport>& transport, const std::shared_ptr<JSON>& data, const std::shared_ptr<Jsep>& jsep, const std::string& handleId, const std::shared_ptr<ArgBundle>& context);
  };

  class Success : public Message {
   public:
     using Message::Message;

     std::string id();
  };

  class Session : public Success {
   public:
     using Success::Success;

     static std::shared_ptr<Session> request(const std::shared_ptr<Http>& http);
  };

  class Attach : public Message {
   public:
     explicit Attach(const std::string& pluginId);
     explicit Attach(const std::shared_ptr<JSON>& content);

     std::string plugin();

     static void request(const std::shared_ptr<Transport>& transport, const std::string& id, const std::shared_ptr<ArgBundle>& context);
  };

  class Trickle : public Handle {
   public:
     using Handle::Handle;

     Trickle(const Candidate& candidate, const std::string& handleId);

     Candidate candidate();
     bool completed();

     static void request(const std::shared_ptr<Transport>& transport, const Candidate& candidate, const std::string& handleId);
  };

  class Detach : public Handle {
   public:
     explicit Detach(const std::string& handleId);

     static void request(const std::shared_ptr<Transport> transport, const std::string& handleId);
  };

  class Hangup : public Handle {
   public:
     explicit Hangup(const std::string& handleId);

     static void request(const std::shared_ptr<Transport> transport, const std::string& handleId);
  };

}  // namespace Janus
