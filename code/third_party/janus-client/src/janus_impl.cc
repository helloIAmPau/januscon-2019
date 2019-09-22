#include "janus/janus_impl.h"

namespace Janus {

  std::shared_ptr<Janus> Janus::create(const std::shared_ptr<Platform>& platform) {
    return std::make_shared<JanusImpl>(platform);
  }

  JanusImpl::JanusImpl(const std::shared_ptr<Platform>& platform) {
    this->_platform = platform;
  }

  ReadyState JanusImpl::readyState() {
    if(this->_session == nullptr) {
      return ReadyState::OFF;
    }

    return this->_session->readyState();
  }

  ReadyState JanusImpl::init(const std::shared_ptr<JanusConf>& conf, const std::shared_ptr<JanusDelegate>& delegate) {
    if(this->readyState() != ReadyState::READY) {
      this->_session = this->_platform->getProtocol(conf, delegate);
    }

    return this->readyState();
  }

  ReadyState JanusImpl::close() {
    this->_session->close();
    this->_session = nullptr;

    return this->readyState();
  }

  std::vector<JanusPluginInfo> JanusImpl::plugins() {
    if(this->readyState() != ReadyState::READY) {
      return std::vector<JanusPluginInfo>();
    }

    return this->_session->plugins();
  }

  void JanusImpl::attach(const std::string& pluginId, const std::shared_ptr<ArgBundle>& context) {
    if(this->readyState() != ReadyState::READY) {
      return;
    }

    this->_session->attach(pluginId, context);
  }

}  // namespace Janus
