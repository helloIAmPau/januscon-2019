#include "janus/plugins/echotest.h"
#include "janus/peer.hpp"
#include "janus/signaling.hpp"
#include "janus/command.hpp"
#include "janus/arg_bundle_impl.h"
#include "janus/json.h"
#include "janus/jsep_impl.h"

namespace Janus {

  void Echotest::dispatch(const std::string& name, const std::shared_ptr<ArgBundle>& data) {
    if(name == "connect") {
      Constraints constraints(data->getBool("audio"), data->getBool("video"), data->getBool("audio"), data->getBool("video"));

      this->_peer->createOffer(constraints, data);
    }
  }

  void Echotest::onEvent(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context) {
    auto jsep = data->jsep();

    if(jsep->type() != "") {
      auto parsed = Jsep::create(jsep->type(), this->_delegate->onAnswer(jsep->sdp()));
      this->_peer->setRemoteDescription(parsed);
    }
  }

  void Echotest::onOffer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {
    auto offer = this->_delegate->onOffer(sdp);

    auto body = std::make_unique<JSON>();
    body->boolean("audio", context->getBool("audio"));
    body->boolean("video", context->getBool("video"));
    body->boolean("record", context->getBool("record"));

    std::shared_ptr<Jsep> jsep = Jsep::create("offer", offer);
    this->_peer->setLocalDescription(jsep);
    this->_signaling->jsep(body->serialize(), jsep, context);
  }

  void Echotest::onAnswer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {}

  std::shared_ptr<Plugin> EchotestFactory::create() {
    return std::make_shared<Echotest>();
  }

}  // namespace Janus
