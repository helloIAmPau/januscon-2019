#include "janus/janus_signaling.h"

namespace Janus {

  JanusSignaling::JanusSignaling(const std::string& handleId, const std::shared_ptr<Transport>& transport, const std::shared_ptr<Protocol>& protocol) {
    this->_id = handleId;
    this->_transport = transport;
    this->_protocol = protocol;
  }

  void JanusSignaling::message(const std::string& body, const std::shared_ptr<ArgBundle>& context) {
    auto data = std::make_shared<JSON>(body);

    PluginMessage::request(this->_transport, data, this->_id, context);
  }

  void JanusSignaling::jsep(const std::string& body, const std::shared_ptr<Jsep>& jsep, const std::shared_ptr<ArgBundle>& context) {
    auto data = std::make_shared<JSON>(body);

    JsepMessage::request(this->_transport, data, jsep, this->_id, context);
  }

  void JanusSignaling::trickle(const Candidate& candidate) {
    Trickle::request(this->_transport, candidate, this->_id);
  }

  void JanusSignaling::detach() {
    Detach::request(this->_transport, this->_id);
  }

  void JanusSignaling::attach(const std::string& id, const std::shared_ptr<ArgBundle>& context) {
    this->_protocol->attach(id, context);
  }

  void JanusSignaling::hangup() {
    Hangup::request(this->_transport, this->_id);
  }

  std::shared_ptr<Signaling> JanusSignalingFactoryImpl::create(const std::string& handleId, const std::shared_ptr<Transport>& transport, const std::shared_ptr<Protocol>& protocol) {
    return std::make_shared<JanusSignaling>(handleId, transport, protocol);
  }

}  // namespace Janus
