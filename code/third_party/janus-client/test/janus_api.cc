#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>

#include "janus/janus_api.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Return;
using testing::Ref;
using testing::Eq;
using testing::Mock;
using testing::_;

namespace Janus {
  namespace Test {

    class JanusApiTest : public ::testing::Test {
     protected:
       void SetUp() override {
         this->conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();
         ON_CALL(*this->conf, url()).WillByDefault(Return("http://janus.endpoint"));

         this->peer = std::make_shared<NiceMock<Mocks::PeerMock>>();
         this->handle = std::make_shared<NiceMock<Mocks::PluginMock>>();
         this->platform = std::make_shared<NiceMock<Mocks::PlatformMock>>();
         ON_CALL(*this->platform, getPlugin("mock plugin")).WillByDefault(Return(this->handle));
         ON_CALL(*this->platform, createPeer(Eq(this->conf), Eq(this->handle))).WillByDefault(Return(this->peer));

         this->uut = std::make_shared<JanusApi>(platform);

         this->delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

         auto sessionMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
         this->http = std::make_shared<NiceMock<Mocks::HttpMock>>();
         ON_CALL(*this->http, post("/", IsType("create"))).WillByDefault(Return(Response(200, sessionMsg)));

         this->httpFactory = std::make_shared<NiceMock<Mocks::HttpFactoryMock>>();
         ON_CALL(*this->httpFactory, create("http://janus.endpoint")).WillByDefault(Return(this->http));

         this->transport = std::make_shared<NiceMock<Mocks::TransportMock>>();
         this->transportFactory = std::make_shared<NiceMock<Mocks::TransportFactoryMock>>();
         ON_CALL(*this->transportFactory, create(TransportType::HTTP, "http://janus.endpoint", "99999", Eq(uut))).WillByDefault(Return(this->transport));

         this->signaling = std::make_shared<NiceMock<Mocks::SignalingMock>>();

         this->signalingFactory = std::make_shared<NiceMock<Mocks::JanusSignalingFactoryMock>>();
         ON_CALL(*this->signalingFactory, create("99999", Eq(this->transport), _)).WillByDefault(Return(signaling));

         this->async = std::make_shared<NiceMock<Mocks::AsyncMock>>();
       }

       std::shared_ptr<NiceMock<Mocks::JanusConfMock>> conf;
       std::shared_ptr<JanusApi> uut;
       std::shared_ptr<NiceMock<Mocks::JanusDelegateMock>> delegate;

       std::shared_ptr<NiceMock<Mocks::HttpMock>> http;
       std::shared_ptr<NiceMock<Mocks::HttpFactoryMock>> httpFactory;

       std::shared_ptr<NiceMock<Mocks::TransportMock>> transport;
       std::shared_ptr<NiceMock<Mocks::TransportFactoryMock>> transportFactory;

       std::shared_ptr<NiceMock<Mocks::SignalingMock>> signaling;
       std::shared_ptr<NiceMock<Mocks::JanusSignalingFactoryMock>> signalingFactory;

       std::shared_ptr<NiceMock<Mocks::AsyncMock>> async;

       std::shared_ptr<NiceMock<Mocks::PeerMock>> peer;
       std::shared_ptr<NiceMock<Mocks::PluginMock>> handle;
       std::shared_ptr<NiceMock<Mocks::PlatformMock>> platform;
    };

    TEST_F(JanusApiTest, shouldReturnTheInfoObjectOnInfo) {
      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      EXPECT_EQ(this->uut->info().name, "Janus API");
      EXPECT_EQ(this->uut->info().version, 10);
    }

    TEST_F(JanusApiTest, shouldInitializeTheSession) {
      EXPECT_CALL(*this->http, post("/", IsType("create")));
      // EXPECT_CALL(*this->async, submit(Eq(this->uut)));
      EXPECT_CALL(*this->transportFactory, create(TransportType::HTTP, "http://janus.endpoint", "99999", Eq(uut)));

      EXPECT_EQ(this->uut->readyState(), ReadyState::OFF);
      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      EXPECT_EQ(this->uut->readyState(), ReadyState::READY);
    }

    TEST_F(JanusApiTest, shouldSendTheHeartbeat) {
      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      EXPECT_CALL(*this->transport, get());
      // EXPECT_CALL(*this->async, submit(Eq(this->uut)));

      this->uut->action();
    }

    TEST_F(JanusApiTest, shouldSetStatusToClosingOnClose) {
      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      this->uut->close();
      EXPECT_EQ(this->uut->readyState(), ReadyState::CLOSING);
    }

    TEST_F(JanusApiTest, shouldSkipTheGetCommandIfStatusIsNotReady) {
      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      this->uut->close();

      EXPECT_CALL(*this->transport, get()).Times(0);
      // EXPECT_CALL(*this->async, submit(Eq(this->uut))).Times(0);

      this->uut->action();

      EXPECT_EQ(this->uut->readyState(), ReadyState::OFF);
    }

    TEST_F(JanusApiTest, shouldGetThePluginList) {
      JanusPluginInfo plugin("test", "plugin", 0);
      std::vector<JanusPluginInfo> plugins = { plugin };

      EXPECT_CALL(*this->platform, getPlugins()).WillOnce(Return(plugins));
      EXPECT_EQ(this->uut->plugins().at(0).id, plugin.id);
    }

    auto errorMsg = "{ \"janus\": \"error\", \"error\": { \"code\": -1, \"reason\": \"test error\" } }";
    TEST_F(JanusApiTest, shouldRaiseErrorEventOnInitError) {
      EXPECT_CALL(*this->http, post("/", IsType("create"))).WillOnce(Return(Response(-1, errorMsg)));
      EXPECT_CALL(*this->delegate, onJanusError(HasError("test error")));
      EXPECT_CALL(*this->transportFactory, create(_, _, _, _)).Times(0);

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
    }

    TEST_F(JanusApiTest, shouldCloseTransportAndAsyncOnClose) {
      EXPECT_CALL(*this->transport, close());
      EXPECT_CALL(*this->async, shutdown());

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      this->uut->close();
      this->uut->close();
    }

    TEST_F(JanusApiTest, shouldAttachToAPlugin) {
      auto context = ArgBundle::create();

      EXPECT_CALL(*this->transport, request(IsAttachMessageWithId("mock"), context));

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      this->uut->attach("mock", context);
    }

    TEST_F(JanusApiTest, shouldDetachPluginsOnClose) {
      EXPECT_CALL(*this->handle, detach()).Times(1);

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
      auto msg = std::make_shared<Success>(successMsg);

      auto context = ArgBundle::create();
      context->setString("request", "attach");
      context->setString("plugin", "mock plugin");

      this->uut->onMessage(msg->serialize(), context);
      this->uut->close();
    }

    TEST_F(JanusApiTest, shouldSkipCloseTransportAndAsyncOnCloseWhenNotInitialized) {
      EXPECT_CALL(*this->transport, close()).Times(0);
      EXPECT_CALL(*this->async, shutdown()).Times(0);

      this->uut->close();
    }

    TEST_F(JanusApiTest, shouldThrowErrorEventOnErrorMessage) {
      EXPECT_CALL(*this->delegate, onJanusError(HasError("test error")));

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      auto msg = std::make_shared<Message>(errorMsg);
      this->uut->onMessage(msg->serialize(), ArgBundle::create());
    }

    TEST_F(JanusApiTest, shouldInitializeThePluginOnSuccessMessage) {
      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";

      auto msg = std::make_shared<Success>(successMsg);
      auto context = ArgBundle::create();
      context->setString("request", "attach");
      context->setString("plugin", "mock plugin");

      EXPECT_CALL(*this->delegate, onPluginEnabled("mock plugin", Eq(this->handle), Eq(context)));
      EXPECT_CALL(*this->handle, init(Eq(this->signaling), Eq(this->peer)));

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      this->uut->onMessage(msg->serialize(), context);
    }

    TEST_F(JanusApiTest, shouldRaiseHangupEventAndCleanHandleOnDetachSuccess) {
      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
      auto msg = std::make_shared<Success>(successMsg);
      auto attachContext = ArgBundle::create();
      attachContext->setString("request", "attach");
      attachContext->setString("plugin", "mock plugin");

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      this->uut->onMessage(msg->serialize(), attachContext);

      EXPECT_CALL(*this->handle, onHangup());
      EXPECT_CALL(*this->handle, onDetach());

      auto detachContext = ArgBundle::create();
      detachContext->setString("request", "detach");
      detachContext->setString("handle", "99999");

      auto detachMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"session_id\":99998}";
      auto detach = std::make_shared<Success>(detachMsg);
      this->uut->onMessage(detach->serialize(), detachContext);

      auto event = "{\"janus\":\"mock event\",\"sender\":99999,\"plugindata\":{\"data\":{\"test\":\"yolo\"}}}";
      auto evt = std::make_shared<Message>(event);

      EXPECT_CALL(*this->handle, onEvent(_, _)).Times(0);

      this->uut->onMessage(evt->serialize(), ArgBundle::create());
    }

    TEST_F(JanusApiTest, shouldSkipHangupEventOnDetachSuccessIfHandleDoesNotExist) {
      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"session_id\":99999}";
      auto msg = std::make_shared<Success>(successMsg);
      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);
      auto detachContext = ArgBundle::create();
      detachContext->setString("request", "detach");
      detachContext->setString("handle", "yolo");

      this->uut->onMessage(msg->serialize(), detachContext);
    }

    TEST_F(JanusApiTest, shouldPassEventToHandle) {
      auto event = "{\"janus\":\"mock event\",\"sender\":99999,\"plugindata\":{\"data\":{\"test\":\"yolo\"}}}";
      auto msg = std::make_shared<Message>(event);

      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
      auto success = std::make_shared<Success>(successMsg);

      auto context = ArgBundle::create();
      context->setString("plugin", "mock plugin");
      context->setString("request", "attach");

      EXPECT_CALL(*this->handle, onEvent(EventHasJsonField("test", "yolo"), Eq(context)));

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      this->uut->onMessage(success->serialize(), context);
      this->uut->onMessage(msg->serialize(), context);
    }

    TEST_F(JanusApiTest, shouldSkipEventIfHandleDoesNotExist) {
      auto event = "{\"janus\":\"mock event\",\"sender\":99999,\"plugindata\":{\"data\":{\"test\":\"yolo\"}}}";
      auto msg = std::make_shared<Message>(event);

      auto context = ArgBundle::create();

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      this->uut->onMessage(msg->serialize(), context);
    }

    // TEST_F(JanusApiTest, shouldRaiseOnMediaEventOnWebrtcupMessage) {
      // EXPECT_CALL(*this->handle, onMedia());

      // auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
      // auto success = std::make_shared<Message>(successMsg);

      // auto wrtc = "{\"janus\":\"webrtcup\",\"sender\":99999}";
      // auto msg = std::make_shared<Message>(wrtc);

      // auto context = ArgBundle::create();
      // context->setString("plugin", "mock plugin");
      // context->setString("request", "attach");

      // this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      // this->uut->onMessage(success->serialize(), context);
      // this->uut->onMessage(msg->serialize(), context);
    // }

    TEST_F(JanusApiTest, shouldRaiseOnHangupEventOnHangupMessage) {
      EXPECT_CALL(*this->handle, onHangup());

      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
      auto success = std::make_shared<Message>(successMsg);

      auto wrtc = "{\"janus\":\"hangup\",\"sender\":99999}";
      auto msg = std::make_shared<Message>(wrtc);

      auto context = ArgBundle::create();
      context->setString("plugin", "mock plugin");
      context->setString("request", "attach");

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      this->uut->onMessage(success->serialize(), context);
      this->uut->onMessage(msg->serialize(), context);
    }

    TEST_F(JanusApiTest, shouldSkipKeepAliveMessages) {
      EXPECT_CALL(*this->handle, onEvent(_, _)).Times(0);

      auto successMsg = "{\"janus\":\"success\",\"transaction\":\"a transaction\",\"data\":{\"id\":99999}}";
      auto success = std::make_shared<Message>(successMsg);

      auto eventMsg = "{\"janus\": \"keepalive\", \"transaction\": \"244035bf-2cec-49c5-b3e9-b031268e7ddc\", \"sender\": 99999}";
      auto msg = std::make_shared<Event>(eventMsg);

      auto context = ArgBundle::create();
      context->setString("plugin", "mock plugin");
      context->setString("request", "attach");

      this->uut->init(conf, this->httpFactory, this->transportFactory, this->signalingFactory, this->async, this->delegate);

      this->uut->onMessage(success->serialize(), context);
      this->uut->onMessage(msg->serialize(), context);
    }

    TEST(JanusApiFactory, shouldCreateAnApiObject) {
      auto uut = std::make_shared<JanusApiFactory>();

      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();
      EXPECT_CALL(*conf, url()).WillOnce(Return("http://janus.endpoint"));

      auto platform = std::make_shared<NiceMock<Mocks::PlatformMock>>();

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto api = uut->bootstrap(conf, platform, delegate);
      EXPECT_NE(api, nullptr);
    }

  }  // namespace Test
}  // namespace Janus
