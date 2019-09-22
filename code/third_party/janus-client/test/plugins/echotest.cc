#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/plugins/echotest.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Ref;
using testing::Return;

namespace Janus {
  namespace Test {

    class EchotestTest : public testing::Test {
     protected:
       void SetUp() override {
         signaling = std::make_shared<NiceMock<Mocks::SignalingMock>>();
         peer = std::make_shared<NiceMock<Mocks::PeerMock>>();
         delegate = std::make_shared<NiceMock<Mocks::PluginDelegateMock>>();
       }

       std::shared_ptr<NiceMock<Mocks::SignalingMock>> signaling;
       std::shared_ptr<NiceMock<Mocks::PeerMock>> peer;
       std::shared_ptr<NiceMock<Mocks::PluginDelegateMock>> delegate;
    };

    TEST_F(EchotestTest, shouldInitializeTheWebrtcHandshakeOnConnect) {
      auto params = ArgBundle::create();
      params->setBool("audio", true);
      params->setBool("video", true);
      params->setBool("record", true);

      EXPECT_CALL(*peer, createOffer(ConstraintsHaveValues(true, true, true, true), Ref(params)));

      auto uut = std::make_shared<Echotest>();
      uut->init(signaling, peer);

      uut->dispatch("connect", params);
    }

    TEST_F(EchotestTest, shouldSetTheAnswerOnEvent) {
      EXPECT_CALL(*delegate, onAnswer("the sdp")).WillOnce(Return("my answer"));

      std::shared_ptr<Jsep> jsep = Jsep::create("answer", "the sdp");
      auto event = std::make_shared<NiceMock<Mocks::EventMock>>();
      EXPECT_CALL(*event, jsep()).WillOnce(Return(jsep));

      EXPECT_CALL(*peer, setRemoteDescription(IsAnswerWithBody("my answer")));

      auto uut = std::make_shared<Echotest>();
      uut->init(signaling, peer);
      uut->setDelegate(delegate);

      uut->onEvent(event, ArgBundle::create());
    }

    TEST_F(EchotestTest, shouldSendAJsepMessageOnOfferReady) {
      auto params = ArgBundle::create();
      params->setBool("audio", true);
      params->setBool("video", true);
      params->setBool("record", true);

      auto body = "{\"audio\": true, \"video\": true, \"record\": true}";
      EXPECT_CALL(*signaling, jsep(body, IsOfferWithBody("my offer"), Ref(params)));
      EXPECT_CALL(*peer, setLocalDescription(IsOfferWithBody("my offer")));

      auto uut = std::make_shared<Echotest>();
      uut->init(signaling, peer);
      uut->setDelegate(delegate);

      EXPECT_CALL(*delegate, onOffer("the offer")).WillOnce(Return("my offer"));

      uut->onOffer("the offer", params);
    }

    TEST(EchotestFactory, shouldCreateAnEchotestHandle) {
      auto factory = std::make_shared<EchotestFactory>();

      EXPECT_NE(factory->create(), nullptr);
    }
  }
}
