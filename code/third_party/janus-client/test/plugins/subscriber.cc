#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/plugins/subscriber.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Ref;
using testing::Return;

namespace Janus {
  namespace Test {

    class SubscriberTest : public testing::Test {
     protected:
       void SetUp() override {
         this->signaling = std::make_shared<NiceMock<Mocks::SignalingMock>>();
         this->peer = std::make_shared<NiceMock<Mocks::PeerMock>>();
         this->delegate = std::make_shared<NiceMock<Mocks::PluginDelegateMock>>();
       }

       std::shared_ptr<NiceMock<Mocks::SignalingMock>> signaling;
       std::shared_ptr<NiceMock<Mocks::PeerMock>> peer;
       std::shared_ptr<NiceMock<Mocks::PluginDelegateMock>> delegate;
    };

    TEST_F(SubscriberTest, shouldJoinAsSubscriber) {
      auto uut = std::make_shared<Subscriber>();
      uut->init(this->signaling, this->peer);

      auto data = ArgBundle::create();
      data->setLong("room", 99);
      data->setLong("feed", 88);

      EXPECT_CALL(*this->signaling, message("{\"request\": \"join\", \"ptype\": \"subscriber\", \"room\": 99, \"feed\": 88}", data));

      uut->dispatch("join", data);
    }

    TEST_F(SubscriberTest, shouldSetTheOfferAsRemoteDescription) {
      auto uut = std::make_shared<Subscriber>();
      uut->init(this->signaling, this->peer);
      uut->setDelegate(this->delegate);

      auto context = ArgBundle::create();

      EXPECT_CALL(*delegate, onOffer("the sdp")).WillOnce(Return("my sdp"));

      auto evtMsg = "{ \"plugindata\": { \"plugin\": \"janus.plugin.videoroom\", \"data\": { \"videoroom\": \"attached\" } }, \"jsep\": { \"type\": \"offer\", \"sdp\": \"the sdp\" } }";
      auto event = std::make_shared<Event>(evtMsg);

      EXPECT_CALL(*this->peer, setRemoteDescription(IsOfferWithBody("my sdp")));
      EXPECT_CALL(*this->peer, createAnswer(ConstraintsHaveValues(false, false, true, true), Ref(context)));

      uut->onEvent(event, context);
    }

    TEST_F(SubscriberTest, shouldSendAStartRequestOnAnswer) {
      auto uut = std::make_shared<Subscriber>();
      uut->init(this->signaling, this->peer);
      uut->setDelegate(this->delegate);

      EXPECT_CALL(*delegate, onAnswer("the sdp")).WillOnce(Return("my sdp"));

      auto context = ArgBundle::create();

      auto body = "{\"request\": \"start\"}";

      EXPECT_CALL(*this->signaling, jsep(body, IsAnswerWithBody("my sdp"), Ref(context)));
      EXPECT_CALL(*this->peer, setLocalDescription(IsAnswerWithBody("my sdp")));

      uut->onAnswer("the sdp", context);
    }

    TEST(SubscriberFactory, shouldCreateASubscriberPlugin) {
      auto factory = std::make_shared<SubscriberFactory>();

      EXPECT_EQ(factory->info().id, "janus.plugin.videoroom.subscriber");
      EXPECT_EQ(factory->info().name, "Videoroom subscriber pseudo-plugin");
      EXPECT_EQ(factory->info().version, 0);

      EXPECT_NE(factory->create(), nullptr);
    }

  }
}
