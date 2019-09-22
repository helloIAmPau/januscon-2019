#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>

#include "janus/janus_signaling.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Return;

namespace Janus {
  namespace Test {

    TEST(JanusSignaling, shouldSendAPluginMessage) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();

      auto context = ArgBundle::create();

      auto msg = std::make_shared<JSON>("{\"test\": \"pippo\"}");

      EXPECT_CALL(*transport, request(IsPluginMessage(msg, "99999"), context));

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();

      auto uut = std::make_shared<JanusSignaling>("99999", transport, protocol);
      uut->message(msg->serialize(), context);
    }

    TEST(JanusSignaling, shouldSendAJsepMessage) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();

      auto context = ArgBundle::create();

      auto msg = std::make_shared<JSON>("{\"test\": \"pippo\"}");
      std::shared_ptr<Jsep> jsep = Jsep::create("offer", "the sdp");

      EXPECT_CALL(*transport, request(IsJsepMessage(msg, jsep, "99999"), context));

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();

      auto uut = std::make_shared<JanusSignaling>("99999", transport, protocol);
      uut->jsep(msg->serialize(), jsep, context);
    }

    TEST(JanusSignaling, shouldSendATrickleMessage) {
      Candidate candidate("track", 666, "the candidate", false);
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();

      EXPECT_CALL(*transport, request(IsTrickleMessage(candidate, "99999")));

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();

      auto uut = std::make_shared<JanusSignaling>("99999", transport, protocol);
      uut->trickle(candidate);
    }

    TEST(JanusSignaling, shouldSendADetachMessage) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      std::vector<std::pair<std::string, std::string>> context = { { "request", "detach" } };

      EXPECT_CALL(*transport, request(IsMessageWithHandleId("detach", "99999"), HasContextFields(context)));

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();

      auto uut = std::make_shared<JanusSignaling>("99999", transport, protocol);
      uut->detach();
    }

    TEST(JanusSignaling, shouldSendAnHangupMessage) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      std::vector<std::pair<std::string, std::string>> context = { { "request", "hangup" } };

      EXPECT_CALL(*transport, request(IsMessageWithHandleId("hangup", "99999"), HasContextFields(context)));

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();

      auto uut = std::make_shared<JanusSignaling>("99999", transport, protocol);
      uut->hangup();
    }

    TEST(JanusSignaling, shouldCallTheProtocolAttachMethod) {
      auto context = ArgBundle::create();

      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocol, attach("mock plugin", context));

      auto uut = std::make_shared<JanusSignaling>("999999", transport, protocol);
      uut->attach("mock plugin", context);
    }

    TEST(JanusSignalingFactory, shouldCreateAJanusSignalingObject) {
      auto factory = std::make_shared<JanusSignalingFactoryImpl>();
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();

      EXPECT_NE(factory->create("99999", transport, protocol), nullptr);
    }

  }  // namespace Test
}  // namespace Janus
