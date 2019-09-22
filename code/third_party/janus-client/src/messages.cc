#include "janus/messages.h"
#include "janus/transport.h"

namespace Janus {

  // Message
  Message::Message(const std::string& type, const std::shared_ptr<Uuid>& uuidFactory) {
    this->_content = std::make_shared<JSON>();

    this->_content->string("janus", type);
    this->_content->string("transaction", uuidFactory->create());
  }

  Message::Message(const std::string& serialized) {
    this->_content = std::make_shared<JSON>(serialized);
  }

  Message::Message(const std::shared_ptr<JSON>& content) {
    this->_content = content;
  }

  std::string Message::janus() {
    return this->_content->string("janus");
  }

  std::string Message::transaction() {
    return this->_content->string("transaction");
  }

  JanusError Message::error() {
    auto body = this->_content->object("error");

    return JanusError(body->integer("code"), body->string("reason"));
  }

  std::string Message::serialize() {
    return this->_content->serialize();
  }

  // Event
  std::string Event::sender() {
    return std::to_string(this->_content->integer("sender"));
  }

  std::string Event::data() {
    return this->_content->object("plugindata")->object("data")->serialize();
  }

  std::shared_ptr<Jsep> Event::jsep() {
    auto jsep = this->_content->object("jsep");

    return Jsep::create(jsep->string("type"), jsep->string("sdp"));
  }

  // Handle
  Handle::Handle(const std::string& type, const std::string& handleId) : Message(type, std::make_shared<UuidImpl>()) {
    this->_content->integer("handle_id", std::stoll(handleId));
  }

  std::string Handle::handleId() {
    return std::to_string(this->_content->integer("handle_id"));
  }

  // PluginMessage
  PluginMessage::PluginMessage(const std::shared_ptr<JSON>& data, const std::string& handleId) : Handle("message", handleId) {
    this->_content->object("body", data);
  }

  std::shared_ptr<JSON> PluginMessage::body() {
    return this->_content->object("body");
  }

  void PluginMessage::request(const std::shared_ptr<Transport>& transport, const std::shared_ptr<JSON>& data, const std::string& handleId, const std::shared_ptr<ArgBundle>& context) {
    auto msg = std::make_shared<PluginMessage>(data, handleId);

    transport->request(msg, context);
  }

  // JsepMessage
  JsepMessage::JsepMessage(const std::shared_ptr<JSON>& data, const std::shared_ptr<Jsep>& jsep, const std::string& handleId) : PluginMessage(data, handleId) {
    auto jsepJson = std::make_shared<JSON>();
    jsepJson->string("type", jsep->type());
    jsepJson->string("sdp", jsep->sdp());

    this->_content->object("jsep", jsepJson);
  }

  std::shared_ptr<Jsep> JsepMessage::jsep() {
    auto jsepJson = this->_content->object("jsep");

    return Jsep::create(jsepJson->string("type"), jsepJson->string("sdp"));
  }

  void JsepMessage::request(const std::shared_ptr<Transport>& transport, const std::shared_ptr<JSON>& data, const std::shared_ptr<Jsep>& jsep, const std::string& handleId, const std::shared_ptr<ArgBundle>& context) {
    auto msg = std::make_shared<JsepMessage>(data, jsep, handleId);

    transport->request(msg, context);
  }

  // Success
  std::string Success::id() {
    return std::to_string(this->_content->object("data")->integer("id"));
  }

  // Session
  std::shared_ptr<Session> Session::request(const std::shared_ptr<Http>& http) {
    auto uuid = std::make_shared<UuidImpl>();
    auto msg = std::make_shared<Message>("create", uuid);
    auto request = Request(msg->serialize(), Query(), Headers());

    auto response = http->post("/", request);

    return std::make_shared<Session>(response.body);
  }

  // Attach
  Attach::Attach(const std::string& pluginId) : Message("attach", std::make_shared<UuidImpl>()) {
    this->_content->string("plugin", pluginId);
  }

  Attach::Attach(const std::shared_ptr<JSON>& content) : Message(content) {}

  std::string Attach::plugin() {
    return this->_content->string("plugin");
  }

  void Attach::request(const std::shared_ptr<Transport>& transport, const std::string& id, const std::shared_ptr<ArgBundle>& context) {
    auto msg = std::make_shared<Attach>(id);

    context->setString("request", "attach");

    auto plugin = context->getString("bind") == "" ? id : context->getString("bind");
    context->setString("plugin", plugin);

    transport->request(msg, context);
  }

  // Trickle
  Trickle::Trickle(const Candidate& candidate, const std::string& handleId) : Handle("trickle", handleId) {
    auto info = std::make_shared<JSON>();
    this->_content->object("candidate", info);

    if(candidate.completed == true) {
      info->boolean("completed", true);

      return;
    }

    info->string("sdpMid", candidate.sdpMid);
    info->integer("sdpMLineIndex", candidate.sdpMLineIndex);
    info->string("candidate", candidate.candidate);
  }

  Candidate Trickle::candidate() {
    auto info = this->_content->object("candidate");

    return Candidate(info->string("sdpMid"), info->integer("sdpMLineIndex"), info->string("candidate"), this->completed());
  }

  bool Trickle::completed() {
    return this->_content->object("candidate")->boolean("completed");
  }

  void Trickle::request(const std::shared_ptr<Transport>& transport, const Candidate& candidate, const std::string& handleId) {
    auto msg = std::make_shared<Trickle>(candidate, handleId);

    transport->request(msg);
  }

  // Detach
  Detach::Detach(const std::string& handleId) : Handle::Handle("detach", handleId) {}

  void Detach::request(const std::shared_ptr<Transport> transport, const std::string& handleId) {
    auto msg = std::make_shared<Detach>(handleId);
    auto context = ArgBundle::create();
    context->setString("request", "detach");
    context->setString("handle", handleId);

    transport->request(msg, context);
  }

  // Hangup
  Hangup::Hangup(const std::string& handleId) : Handle::Handle("hangup", handleId) {}

  void Hangup::request(const std::shared_ptr<Transport> transport, const std::string& handleId) {
    auto msg = std::make_shared<Hangup>(handleId);
    auto context = ArgBundle::create();
    context->setString("request", "hangup");
    context->setString("handle", handleId);

    transport->request(msg, context);
  }


}  // namespace Janus
