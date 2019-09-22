#include "janus/janus_api.h"

namespace Janus {

  JanusApi::JanusApi(const std::shared_ptr<Platform>& platform) {
    this->_platform = platform;
  }

  void JanusApi::init(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<HttpFactory>& httpFactory, const std::shared_ptr<TransportFactory>& transportFactory, const std::shared_ptr<JanusSignalingFactory>& signalingFactory, const std::shared_ptr<Async>& async, const std::shared_ptr<JanusDelegate>& delegate) {
    this->readyState(ReadyState::INIT);

    this->_conf = conf;

    this->_delegate = delegate;
    this->_signalingFactory = signalingFactory;

    auto baseUrl = conf->url();

    auto http = httpFactory->create(baseUrl);
    auto session = Session::request(http);

    if(session->janus() == "error") {
      this->_delegate->onJanusError(session->error());

      return;
    }

    auto sessionId = session->id();

    this->_transport = transportFactory->create(TransportType::HTTP, baseUrl, sessionId, this->shared_from_this());

    this->readyState(ReadyState::READY);

    this->_async = async;
    this->_async->submit(this->shared_from_this());
  }

  Info JanusApi::info() {
    return Info("Janus API", 10);
  }

  ReadyState JanusApi::readyState() {
    std::lock_guard<std::mutex> lock(this->_readyStateMutex);

    return this->_readyState;
  }

  void JanusApi::readyState(const ReadyState& readyState) {
    std::lock_guard<std::mutex> lock(this->_readyStateMutex);

    this->_readyState = readyState;
  }

  void JanusApi::close() {
    if(this->readyState() != ReadyState::READY) {
      return;
    }

    this->readyState(ReadyState::CLOSING);

    this->_async->shutdown();

    {
      std::lock_guard<std::mutex> lock(this->_handlesMutex);
      auto iterator = this->_handles.begin();
      while(iterator != this->_handles.end()) {
        auto handle = iterator->second;

        handle->detach();
        iterator = this->_handles.erase(iterator);
      }
    }

    this->_transport->close();
  }

  std::vector<JanusPluginInfo> JanusApi::plugins() {
    return this->_platform->getPlugins();
  }

  void JanusApi::attach(const std::string& pluginId, const std::shared_ptr<ArgBundle>& context) {
    Attach::request(this->_transport, pluginId, context);
  }

  void JanusApi::onMessage(const std::string& body, const std::shared_ptr<ArgBundle>& context) {
    auto message = std::make_shared<Message>(body);

    if(message->janus() == "keepalive") {
      return;
    }

    if(message->janus() == "error") {
      this->_delegate->onJanusError(message->error());

      return;
    }

    // Attach reply
    if(message->janus() == "success" && context->getString("request") == "attach") {
      auto success = message->as<Success>();

      auto plugin = context->getString("plugin");
      auto handle = this->_platform->getPlugin(plugin);

      auto signaling = this->_signalingFactory->create(success->id(), this->_transport, this->shared_from_this());
      auto peer = this->_platform->createPeer(this->_conf, handle);

      handle->init(signaling, peer);

      {
        std::lock_guard<std::mutex> lock(this->_handlesMutex);
        this->_handles.insert({ success->id(), handle });
      }

      this->_delegate->onPluginEnabled(plugin, handle, context);

      return;
    }

    // Detach
    if(message->janus() == "success" && context->getString("request") == "detach") {
      auto success = message->as<Success>();
      auto handleId = context->getString("handle");
      auto handle = this->_handle(handleId);

      if(handle == nullptr) {
        return;
      }

      {
        std::lock_guard<std::mutex> lock(this->_handlesMutex);
        this->_handles.erase(handleId);
      }

      handle->onHangup();

      handle->onDetach();

      return;
    }

    // Event
    auto event = message->as<Event>();
    auto handle = this->_handle(event->sender());

    if(handle == nullptr) {
      return;
    }

    // Webrtc hangup event && success detach reply
    if(event->janus() == "hangup") {
      handle->onHangup();

      return;
    }

    handle->onEvent(event, context);
  }

  void JanusApi::action() {
    if(this->readyState() == ReadyState::READY) {
      this->_transport->get();
    }

    if(this->readyState() == ReadyState::READY) {
      this->_async->submit(this->shared_from_this());
    } else {
      this->readyState(ReadyState::OFF);
    }
  }

  std::shared_ptr<Plugin> JanusApi::_handle(const std::string& handleId) {
    std::shared_ptr<Plugin> handle = nullptr;

    {
      std::lock_guard<std::mutex> lock(this->_handlesMutex);
      auto iterator = this->_handles.find(handleId);
      if(iterator != this->_handles.end()) {
        handle = iterator->second;
      }
    }

    return handle;
  }

  std::shared_ptr<Protocol> JanusApiFactory::bootstrap(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<Platform>& platform, const std::shared_ptr<JanusDelegate>& delegate) {
    auto httpFactory = std::make_shared<HttpFactoryImpl>();
    auto transportFactory = std::make_shared<TransportFactoryImpl>();
    auto signalingFactory = std::make_shared<JanusSignalingFactoryImpl>();
    auto async = std::make_shared<AsyncImpl>();

    auto api = std::make_shared<JanusApi>(platform);
    api->init(conf, httpFactory, transportFactory, signalingFactory, async, delegate);

    return api;
  }

}  // namespace Janus

