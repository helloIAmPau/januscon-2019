#include "janus/plugins/streaming.h"
#include "janus/json.h"
#include "janus/jsep.hpp"

namespace Janus {

  void Streaming::dispatch(const std::string& name, const std::shared_ptr<ArgBundle>& data) {
    if(name == "list") {
      auto list = std::make_shared<JSON>();
      list->string("request", "list");
      this->_signaling->message(list->serialize(), data);

      return;
    }

    if(name == "watch") {
      auto watch = std::make_shared<JSON>();
      watch->string("request", "watch");
      watch->integer("id", data->getInt("id"));
      watch->boolean("offer_audio", data->getBool("offer_audio"));
      watch->boolean("offer_video", data->getBool("offer_video"));
      watch->boolean("offer_data", false);

      this->_signaling->message(watch->serialize(), data);
    }
  }

  void Streaming::onEvent(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context) {
    auto evt = std::make_shared<JSON>(data->data());

    if(evt->string("streaming") == "list") {
      this->_delegate->onEvent(data, context);

      return;
    }

    auto result = evt->object("result");

    if(result->string("status") == "preparing") {
      auto jsep = data->jsep();
      auto parsed = Jsep::create(jsep->type(), this->_delegate->onOffer(jsep->sdp()));
      this->_peer->setRemoteDescription(parsed);

      Constraints constraints(false, false, parsed->hasMedia("audio"), parsed->hasMedia("video"));
      this->_peer->createAnswer(constraints, context);

      return;
    }
  }

  void Streaming::onOffer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {}

  void Streaming::onAnswer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {
    auto answer = this->_delegate->onAnswer(sdp);

    auto body = std::make_shared<JSON>();
    body->string("request", "start");
    std::shared_ptr<Jsep> jsep = Jsep::create("answer", answer);

    this->_signaling->jsep(body->serialize(), jsep, context);
    this->_peer->setLocalDescription(jsep);
  }

  std::shared_ptr<Plugin> StreamingFactory::create() {
    return std::make_shared<Streaming>();
  }

}
