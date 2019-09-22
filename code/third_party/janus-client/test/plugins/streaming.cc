#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/plugins/streaming.h"
#include "janus/mocks.h"
#include "janus/arg_bundle_impl.h"

using testing::NiceMock;
using testing::Ref;
using testing::Return;

namespace Janus {
  namespace Test {

    class StreamingTest : public testing::Test {
     protected:
       void SetUp() override {
         this->signaling = std::make_shared<NiceMock<Mocks::SignalingMock>>();
         this->peer = std::make_shared<NiceMock<Mocks::PeerMock>>();
         this->delegate = std::make_shared<NiceMock<Mocks::PluginDelegateMock>>();

         this->uut = std::make_shared<Streaming>();
         this->uut->init(this->signaling, this->peer);
         this->uut->setDelegate(this->delegate);
       }

       std::shared_ptr<NiceMock<Mocks::SignalingMock>> signaling;
       std::shared_ptr<NiceMock<Mocks::PeerMock>> peer;
       std::shared_ptr<NiceMock<Mocks::PluginDelegateMock>> delegate;
       std::shared_ptr<Streaming> uut;
    };

    TEST_F(StreamingTest, shouldSendAListRequest) {
      auto context = ArgBundle::create();
      EXPECT_CALL(*this->signaling, message("{\"request\": \"list\"}", context));
      this->uut->dispatch("list", context);
    }

    TEST_F(StreamingTest, shouldGetAListOfStreaming) {
      auto context = ArgBundle::create();

      auto evtMsg = "{ \"plugindata\": { \"plugin\": \"janus.plugin.streaming\", \"data\": { \"streaming\": \"list\", \"list\": [] } } }";
      auto event = std::make_shared<Event>(evtMsg);

      EXPECT_CALL(*this->delegate, onEvent(std::dynamic_pointer_cast<JanusEvent>(event), context));
      this->uut->onEvent(event, context);
    }

    TEST_F(StreamingTest, shouldSendAWatchRequest) {
      auto context = ArgBundle::create();
      context->setInt("id", 9999);
      context->setBool("offer_video", true);
      context->setBool("offer_audio", true);

      EXPECT_CALL(*this->signaling, message("{\"request\": \"watch\", \"id\": 9999, \"offer_audio\": true, \"offer_video\": true, \"offer_data\": false}", context));

      this->uut->dispatch("watch", context);
    }

    TEST_F(StreamingTest, shouldStartTheWebrtcHandshakeOnPreparingEvent) {
      auto context = ArgBundle::create();

      EXPECT_CALL(*delegate, onOffer("the offer")).WillOnce(Return("m=audio\r\nm=video"));

      auto evtMsg = "{\"janus\":\"event\",\"sender\":1815153248,\"transaction\":\"sBJNyUhH6Vc6\",\"plugindata\":{\"plugin\":\"janus.plugin.streaming\",\"data\":{\"result\": {\"status\":\"preparing\"}}},\"jsep\":{\"type\":\"offer\",\"sdp\":\"the offer\"}}";
      auto evt = std::make_shared<Event>(evtMsg);

      EXPECT_CALL(*this->peer, setRemoteDescription(IsOfferWithBody("m=audio\r\nm=video")));
      EXPECT_CALL(*this->peer, createAnswer(ConstraintsHaveValues(false, false, true, true), context));

      uut->onEvent(evt, context);
    }

    TEST_F(StreamingTest, shouldSendAStartMessageOnAnswer) {
      auto context = ArgBundle::create();

      auto sdp = "the sdp";

      EXPECT_CALL(*delegate, onAnswer(sdp)).WillOnce(Return("my sdp"));

      auto body = "{\"request\": \"start\"}";
      EXPECT_CALL(*this->signaling, jsep(body, IsAnswerWithBody("my sdp"), context));
      EXPECT_CALL(*this->peer, setLocalDescription(IsAnswerWithBody("my sdp")));

      uut->onAnswer(sdp, context);
    }

    TEST(StreamingFactory, shouldCreateAStreamingHandle) {
      auto factory = std::make_shared<StreamingFactory>();

      EXPECT_NE(factory->create(), nullptr);
    }

  }
}
