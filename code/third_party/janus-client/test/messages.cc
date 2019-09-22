#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>

#include "janus/messages.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Return;
using testing::_;
using testing::Eq;

namespace Janus {
  namespace Test {

    TEST(JanusMessage, initializesAnEmptyMessage) {
      auto uuidMock = std::make_shared<NiceMock<Mocks::UuidMock>>();
      EXPECT_CALL(*uuidMock, create()).WillOnce(Return("a uuid"));

      auto msg = std::make_unique<Message>("janus type", uuidMock);
      EXPECT_EQ(msg->janus(), "janus type");
      EXPECT_EQ(msg->transaction(), "a uuid");
    }

    TEST(JanusMessage, shouldParseAMessage) {
      auto serialized = "{\"janus\":\"mock message\", \"transaction\":\"mock transaction\"}";

      auto msg = std::make_unique<Message>(serialized);
      EXPECT_EQ(msg->janus(), "mock message");
      EXPECT_EQ(msg->transaction(), "mock transaction");
    }

    TEST(JanusMessage, shouldSerializeTheMessage) {
      auto uuidMock = std::make_shared<NiceMock<Mocks::UuidMock>>();
      EXPECT_CALL(*uuidMock, create()).WillOnce(Return("a uuid"));

      auto msg = std::make_unique<Message>("janus type", uuidMock);
      EXPECT_EQ(msg->serialize(), "{\"janus\": \"janus type\", \"transaction\": \"a uuid\"}");
    }


    auto errorMessage = "{\"janus\":\"error\",\"transaction\":\"a1b2c3d4\",\"error\":{\"code\":666,\"reason\":\"the error\"}}";
    TEST(JanusMessage, shouldParseAnErrorMessage) {
      auto msg = std::make_unique<Message>(errorMessage);

      EXPECT_EQ(msg->error().code, 666);
      EXPECT_EQ(msg->error().reason, "the error");
    }

    auto jsepEventMsg = "{\"janus\":\"event\",\"sender\":1815153248,\"transaction\":\"sBJNyUhH6Vc6\",\"plugindata\":{\"plugin\":\"janus.plugin.echotest\",\"data\":{\"echotest\":\"event\",\"result\":\"ok\"}},\"jsep\":{\"type\":\"offer\",\"sdp\":\"some sdp\"}}";
    TEST(Event, shouldParseEventData) {
      auto event = std::make_shared<Event>(jsepEventMsg);

      EXPECT_EQ(event->sender(), "1815153248");
      EXPECT_EQ(event->data(), "{\"echotest\": \"event\", \"result\": \"ok\"}");
      EXPECT_EQ(event->jsep()->type(), "offer");
      EXPECT_EQ(event->jsep()->sdp(), "some sdp");
    }

    TEST(PluginMessage, shouldSendDataToAnHandle) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      auto data = std::make_shared<JSON>("{\"test\": \"yolo\"}");
      auto id = "99999";
      auto context = ArgBundle::create();

      EXPECT_CALL(*transport, request(IsPluginMessage(data, id), context));

      PluginMessage::request(transport, data, id, context);
    }

    TEST(JsepMessage, shouldSendDataAndJsepToAnHandle) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      auto data = std::make_shared<JSON>("{\"test\": \"yolo\"}");
      std::shared_ptr<Jsep> jsep = Jsep::create("offer", "the sdp");
      auto id = "99999";
      auto context = ArgBundle::create();

      EXPECT_CALL(*transport, request(IsJsepMessage(data, jsep, id), context));

      JsepMessage::request(transport, data, jsep, id, context);
    }

    auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
    TEST(Success, shouldGetTheId) {
      auto msg = std::make_shared<Success>(successMsg);
      EXPECT_EQ(msg->id(), "99999");
    }

    TEST(Session, shouldCreateASession) {
      auto http = std::make_shared<NiceMock<Mocks::HttpMock>>();
      auto response = Response(200, successMsg);
      EXPECT_CALL(*http, post("/", IsType("create"))).WillOnce(Return(response));

      auto session = Session::request(http);
      EXPECT_EQ(session->id(), "99999");
    }

    TEST(Attach, shouldSendAnAttachMessage) {
      auto context = ArgBundle::create();
      context->setString("test", "yolo");

      std::vector<std::pair<std::string, std::string>> fields = { { "request", "attach" }, { "plugin", "mock id" }, { "test", "yolo" } };

      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      EXPECT_CALL(*transport, request(IsAttachMessageWithId("mock id"), HasContextFields(fields)));

      Attach::request(transport, "mock id", context);
    }

    TEST(Attach, shouldBindToADifferentPlugin) {
      auto context = ArgBundle::create();
      context->setString("test", "yolo");
      context->setString("bind", "another plugin");

      std::vector<std::pair<std::string, std::string>> fields = { { "request", "attach" }, { "plugin", "another plugin" }, { "test", "yolo" } };

      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      EXPECT_CALL(*transport, request(IsAttachMessageWithId("mock id"), HasContextFields(fields)));

      Attach::request(transport, "mock id", context);
    }


    TEST(Trickle, shouldSendATrickleMessage) {
      Candidate candidate("track", 666, "the candidate", false);
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();

      EXPECT_CALL(*transport, request(IsTrickleMessage(candidate, "99999")));

      Trickle::request(transport, candidate, "99999");
    }

    TEST(Trickle, shouldSetCompletedTrue) {
      Candidate candidate("", 666, "", true);
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();

      EXPECT_CALL(*transport, request(IsTrickleMessage(candidate, "99999")));

      Trickle::request(transport, candidate, "99999");
    }

    TEST(Detach, shouldSendDetachMessage) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      std::vector<std::pair<std::string, std::string>> context = { { "request", "detach" }, { "handle", "99999" } };

      EXPECT_CALL(*transport, request(IsMessageWithHandleId("detach", "99999"), HasContextFields(context)));

      Detach::request(transport, "99999");
    }

    TEST(Hangup, shouldSendDetachMessage) {
      auto transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
      std::vector<std::pair<std::string, std::string>> context = { { "request", "hangup" }, { "handle", "99999" } };

      EXPECT_CALL(*transport, request(IsMessageWithHandleId("hangup", "99999"), HasContextFields(context)));

      Hangup::request(transport, "99999");
    }

  }  // namespace Test
}  // namespace Janus
