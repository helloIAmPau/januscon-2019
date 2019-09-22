#include "janus/plugins/videoroom.h"
#include "janus/jsep_impl.h"
#include "janus/json.h"

namespace Janus {
  void Videoroom::dispatch(const std::string& name, const std::shared_ptr<ArgBundle>& data) {
    if(name == "list") {
      auto msg = std::make_shared<JSON>();
      msg->string("request", "list");

      data->setString("request", "list");

      this->_signaling->message(msg->serialize(), data);

      return;
    }

    if(name == "join") {
      auto msg = std::make_shared<JSON>();
      msg->string("request", "join");
      msg->string("ptype", "publisher");
      msg->integer("room", data->getInt("room"));
      msg->string("display", data->getString("display"));

      this->_signaling->message(msg->serialize(), data);

      return;
    }

    if(name == "publish") {
      Constraints constraints(data->getBool("audio"), data->getBool("video"), false, false);
      this->_peer->createOffer(constraints, data);

      return;
    }

    if(name == "subscribe") {
      data->setString("bind", "janus.plugin.videoroom.subscriber");
      this->_signaling->attach("janus.plugin.videoroom", data);

      return;
    }
  }

  void Videoroom::onEvent(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context) {
    auto event = std::make_shared<JSON>(data->data());

    // Received an answer for publisher
    if(event->string("videoroom") == "event" && event->string("configured") == "ok") {
      auto jsep = data->jsep();
      auto parsed = Jsep::create(jsep->type(), this->_delegate->onAnswer(jsep->sdp()));
      this->_peer->setRemoteDescription(parsed);

      return;
    }

    this->_delegate->onEvent(data, context);
  }

  void Videoroom::onOffer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {
    auto offer = this->_delegate->onOffer(sdp);

    auto body = std::make_unique<JSON>();
    body->string("request", "publish");
    body->boolean("audio", context->getBool("audio"));
    body->boolean("video", context->getBool("video"));
    body->boolean("data", context->getBool("data"));

    std::shared_ptr<Jsep> jsep = Jsep::create("offer", offer);

    this->_peer->setLocalDescription(jsep);
    this->_signaling->jsep(body->serialize(), jsep, context);
  }

  void Videoroom::onAnswer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {}

  std::shared_ptr<Plugin> VideoroomFactory::create() {
    return std::make_shared<Videoroom>();
  }

}  // namespace Janus
