#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/plugins/videoroom.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Ref;
using testing::Return;

namespace Janus {
  namespace Test {

    class VideoroomTest : public testing::Test {
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

    TEST_F(VideoroomTest, shoudRequestTheRoomList) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);

      auto data = ArgBundle::create();
      data->setString("random", "field");

      std::vector<std::pair<std::string, std::string>> fields = { { "request", "list" }, { "random", "field" } };

      EXPECT_CALL(*this->signaling, message("{\"request\": \"list\"}", HasContextFields(fields)));

      uut->dispatch("list", data);
    }

    TEST_F(VideoroomTest, shouldJoinAsPublisher) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);

      auto data = ArgBundle::create();
      data->setInt("room", 99);
      data->setString("display", "my name");

      EXPECT_CALL(*this->signaling, message("{\"request\": \"join\", \"ptype\": \"publisher\", \"room\": 99, \"display\": \"my name\"}", data));

      uut->dispatch("join", data);
    }

    TEST_F(VideoroomTest, shouldDelegateTheListEvent) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);
      uut->setDelegate(this->delegate);

      auto context = ArgBundle::create();
      context->setString("request", "list");

      auto evtMsg = "{ \"plugindata\": { \"plugin\": \"janus.plugin.videoroom\", \"data\": { \"videoroom\": \"success\", \"rooms\": [] } } }";
      auto event = std::make_shared<Event>(evtMsg);

      EXPECT_CALL(*this->delegate, onEvent(std::dynamic_pointer_cast<JanusEvent>(event), context));
      uut->onEvent(event, context);
    }

    TEST_F(VideoroomTest, shouldCreateAnOfferOnPublish) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);

      auto context = ArgBundle::create();
      context->setBool("audio", true);
      context->setBool("video", true);

      EXPECT_CALL(*this->peer, createOffer(ConstraintsHaveValues(true, true, false, false), Ref(context)));

      uut->dispatch("publish", context);
    }

    TEST_F(VideoroomTest, shouldSendAPublishRequestOnOffer) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);
      uut->setDelegate(this->delegate);

      EXPECT_CALL(*delegate, onOffer("the sdp")).WillOnce(Return("my sdp"));

      auto context = ArgBundle::create();
      context->setBool("audio", true);
      context->setBool("video", true);

      auto body = "{\"request\": \"publish\", \"audio\": true, \"video\": true, \"data\": false}";

      EXPECT_CALL(*this->signaling, jsep(body, IsOfferWithBody("my sdp"), Ref(context)));
      EXPECT_CALL(*this->peer, setLocalDescription(IsOfferWithBody("my sdp")));

      uut->onOffer("the sdp", context);
    }

    TEST_F(VideoroomTest, shouldSetTheAnswerAsRemoteDescriptionForPublisher) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);
      uut->setDelegate(this->delegate);

      EXPECT_CALL(*delegate, onAnswer("the sdp")).WillOnce(Return("my sdp"));

      auto context = ArgBundle::create();

      auto evtMsg = "{ \"plugindata\": { \"plugin\": \"janus.plugin.videoroom\", \"data\": { \"videoroom\": \"event\", \"configured\": \"ok\" } }, \"jsep\": { \"type\": \"answer\", \"sdp\": \"the sdp\" } }";
      auto event = std::make_shared<Event>(evtMsg);

      EXPECT_CALL(*this->peer, setRemoteDescription(IsAnswerWithBody("my sdp")));

      uut->onEvent(event, context);
    }

    TEST_F(VideoroomTest, shouldAttachASubscriberHandle) {
      auto uut = std::make_shared<Videoroom>();
      uut->init(this->signaling, this->peer);

      auto context = ArgBundle::create();

      std::vector<std::pair<std::string, std::string>> fields = { { "bind", "janus.plugin.videoroom.subscriber" } };
      EXPECT_CALL(*this->signaling, attach("janus.plugin.videoroom", HasContextFields(fields)));

      uut->dispatch("subscribe", context);
    }

    TEST(VideoroomFactory, shouldCreateAVideoroomHandle) {
      auto factory = std::make_shared<VideoroomFactory>();

      EXPECT_NE(factory->create(), nullptr);
    }

  }  // namespace Test
}  // namespace Janus

