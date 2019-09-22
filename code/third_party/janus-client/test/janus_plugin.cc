#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/janus_plugin.h"
#include "janus/mocks.h"
#include "janus/arg_bundle.hpp"
#include "janus/candidate.hpp"
#include "janus/janus_event.hpp"

using testing::NiceMock;
using testing::_;
using testing::Ref;

namespace Janus {
  namespace Test {

    class TestPlugin : public JanusPlugin {
     public:
       void dispatch(const std::string& name, const std::shared_ptr<ArgBundle>& data) {}
       void onEvent(const std::shared_ptr<JanusEvent>& data, const std::shared_ptr<ArgBundle>& context) {}
       void onOffer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {}
       void onAnswer(const std::string& sdp, const std::shared_ptr<ArgBundle>& context) {}
    };

    class PluginImplTest : public testing::Test {
     protected:
       void SetUp() override {
         signaling = std::make_shared<NiceMock<Mocks::SignalingMock>>();
         peer = std::make_shared<NiceMock<Mocks::PeerMock>>();
       }

       std::shared_ptr<NiceMock<Mocks::SignalingMock>> signaling;
       std::shared_ptr<NiceMock<Mocks::PeerMock>> peer;
    };

    TEST_F(PluginImplTest, shouldSendThDetachMessage) {
      EXPECT_CALL(*signaling, detach());

      auto plugin = std::make_shared<TestPlugin>();
      plugin->init(signaling, peer);
      plugin->detach();
    }

    TEST_F(PluginImplTest, shouldSendTheHangupMessage) {
      EXPECT_CALL(*signaling, hangup());

      auto plugin = std::make_shared<TestPlugin>();
      plugin->init(signaling, peer);
      plugin->hangup();
    }

    TEST_F(PluginImplTest, shouldRegisterTheDefaultJanusPlugins) {
      auto platform = std::make_shared<NiceMock<Mocks::PlatformMock>>();
      EXPECT_CALL(*platform, registerPluginFactory(IsThePluginFactory("janus.plugin.echotest")));
      EXPECT_CALL(*platform, registerPluginFactory(IsThePluginFactory("janus.plugin.streaming")));
      EXPECT_CALL(*platform, registerPluginFactory(IsThePluginFactory("janus.plugin.videoroom")));
      EXPECT_CALL(*platform, registerPluginFactory(IsThePluginFactory("janus.plugin.videoroom.subscriber")));

      JanusPlugin::registerJanusPlugins(platform);
    }

    TEST_F(PluginImplTest, shouldRedirectEventsToDelegate) {
      auto delegate = std::make_shared<NiceMock<Mocks::PluginDelegateMock>>();
      auto plugin = std::make_shared<TestPlugin>();

      plugin->init(signaling, peer);
      plugin->setDelegate(delegate);

      auto userMedia = std::make_shared<NiceMock<Mocks::UserMediaMock>>();
      // EXPECT_CALL(*peer, getMedia()).WillOnce(Return(userMedia));

      EXPECT_CALL(*delegate, onMediaChanged(Eq(userMedia)));

      plugin->onMediaChanged(userMedia);

      EXPECT_CALL(*delegate, onHangup());

      plugin->onHangup();

      EXPECT_CALL(*delegate, onDetach());

      plugin->onDetach();
    }

    TEST_F(PluginImplTest, shouldSendATrickleMessageOnIceCandidate) {
      Candidate candidate("", -99, "", true);
      EXPECT_CALL(*signaling, trickle(Ref(candidate)));

      auto delegate = std::make_shared<NiceMock<Mocks::PluginDelegateMock>>();
      auto plugin = std::make_shared<TestPlugin>();

      plugin->init(signaling, peer);
      plugin->setDelegate(delegate);

      plugin->onIceCandidate(candidate);
    }

    TEST_F(PluginImplTest, shouldClosePeerOnHangup) {
      EXPECT_CALL(*peer, close());

      auto delegate = std::make_shared<NiceMock<Mocks::PluginDelegateMock>>();
      auto plugin = std::make_shared<TestPlugin>();

      plugin->init(signaling, peer);
      plugin->setDelegate(delegate);

      plugin->onHangup();
    }

  }  // namespace Test
}  // namespace Janus

