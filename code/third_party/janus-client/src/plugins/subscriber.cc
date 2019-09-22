#include "janus/plugins/subscriber.h"
#include "janus/json.h"
#include "janus/jsep_impl.h"

namespace Janus {

  void Subscriber::dispatch(const std::string& name, const std::shared_ptr<ArgBundle>& data) {

    if(name == "join") {
      auto msg = std::make_shared<JSON>();
      msg->string("request", "join");
      msg->string("ptype", "subscriber");
      msg->integer("room", data->getLong("room"));
      msg->integer("feed", data->getLong("feed"));

      this->_signaling->message(msg->serialize(), data);

      return;
    }

  }

  void Subscriber::onEvent(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context) {
    auto event = std::make_shared<JSON>(data->data());

    // Received an offer
    if(event->string("videoroom") == "attached") {
      auto jsep = data->jsep();
      auto parsed = Jsep::create(jsep->type(), this->_delegate->onOffer(jsep->sdp()));

      this->_peer->setRemoteDescription(parsed);

      Constraints constraints(false, false, true, true);
      this->_peer->createAnswer(constraints, context);

      return;
    }
  }

  void Subscriber::onOffer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {}

  void Subscriber::onAnswer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {
    auto answer = this->_delegate->onAnswer(sdp);

    auto body = std::make_unique<JSON>();
    body->string("request", "start");

    std::shared_ptr<Jsep> jsep = Jsep::create("answer", answer);

    this->_peer->setLocalDescription(jsep);
    this->_signaling->jsep(body->serialize(), jsep, context);
  }

  std::shared_ptr<Plugin> SubscriberFactory::create() {
    return std::make_shared<Subscriber>();
  }

}
