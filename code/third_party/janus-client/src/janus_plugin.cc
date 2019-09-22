#include "janus/janus_plugin.h"
#include "janus/plugins/echotest.h"
#include "janus/plugins/streaming.h"
#include "janus/plugins/videoroom.h"
#include "janus/plugins/subscriber.h"

namespace Janus {

  void JanusPlugin::init(const std::shared_ptr<Signaling>& signaling, const std::shared_ptr<Peer>& peer) {
    this->_signaling = signaling;
    this->_peer = peer;
  }

  void JanusPlugin::detach() {
    this->_signaling->detach();
  }

  void JanusPlugin::hangup() {
    this->_signaling->hangup();
  }

  void JanusPlugin::registerJanusPlugins(std::shared_ptr<Platform> platform) {
    auto echotest = std::make_shared<EchotestFactory>();
    platform->registerPluginFactory(echotest);

    auto streaming = std::make_shared<StreamingFactory>();
    platform->registerPluginFactory(streaming);

    auto videoroom = std::make_shared<VideoroomFactory>();
    platform->registerPluginFactory(videoroom);

    auto subscriber = std::make_shared<SubscriberFactory>();
    platform->registerPluginFactory(subscriber);
  }

  void JanusPlugin::setDelegate(const std::shared_ptr<PluginDelegate>& delegate) {
    this->_delegate = delegate;
  }

  void JanusPlugin::onMediaChanged(const std::shared_ptr<Media>& media) {
    this->_delegate->onMediaChanged(media);
  }

  void JanusPlugin::onHangup() {
    this->_peer->close();

    this->_delegate->onHangup();
  }

  void JanusPlugin::onDetach() {
    this->_delegate->onDetach();
  }

  void JanusPlugin::onIceCandidate(const Candidate& candidate) {
    this->_signaling->trickle(candidate);
  }

}  // namespace Janus
