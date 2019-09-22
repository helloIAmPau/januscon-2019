#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>
#include <string>
#include "janus/mocks.h"
#include "janus/platform_impl.h"
#include "janus/janus_api.h"
#include "janus/janus_impl.h"
#include "janus/conf.h"
#include "janus/plugin.hpp"
#include "janus/http.h"

using testing::NiceMock;
using testing::Return;
using testing::Eq;
using testing::_;

namespace Janus {
  namespace Test {

    TEST(Platform, shouldRegisterAProtocolFactory) {
      auto platform = Platform::create();

      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();
      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto protocol = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      auto factory = std::make_shared<NiceMock<Mocks::ProtocolFactoryMock>>();
      EXPECT_CALL(*factory, bootstrap(Eq(conf), _, Eq(delegate))).WillOnce(Return(protocol));

      platform->registerProtocolFactory(factory);
      auto created = platform->getProtocol(conf, delegate);

      EXPECT_EQ(created, protocol);
    }

    TEST(Platform, usesJanusApiAsDefault) {
      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();
      ON_CALL(*conf, url()).WillByDefault(Return("http://a.base.url"));

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto uut = Platform::create();
      auto api = uut->getProtocol(conf, delegate);
      EXPECT_EQ(api->info().name, "Janus API");
    }

    TEST(Platform, shouldRegistreThePeerFactory) {
      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();
      auto delegate = std::make_shared<NiceMock<Mocks::PluginMock>>();

      auto factory = std::make_shared<NiceMock<Mocks::PeerFactoryMock>>();
      EXPECT_CALL(*factory, create(Eq(conf), Eq(delegate)));

      auto platform = Platform::create();
      platform->registerPeerFactory(factory);
      platform->createPeer(conf, delegate);
    }

    TEST(Platform, shouldRegisterAPlugin) {
      class MockPluginFactory : public PluginFactory {
       public:
         JanusPluginInfo info() {
           return JanusPluginInfo("MockPlugin", "Mock Plugin", 0);
         }
         MOCK_METHOD0(create, std::shared_ptr<Plugin>());
      };

      auto mockPlugin = std::make_shared<NiceMock<Mocks::PluginMock>>();

      auto mockPluginFactory = std::make_shared<MockPluginFactory>();
      EXPECT_CALL(*mockPluginFactory, create()).WillOnce(Return(mockPlugin));
      auto platform = Platform::create();

      platform->registerPluginFactory(mockPluginFactory);
      EXPECT_EQ(platform->getPlugin("MockPlugin"), mockPlugin);
    }

    TEST(Platform, shouldListRegisteredPlugins) {
      class MockPluginFactory : public PluginFactory {
       public:
         JanusPluginInfo info() {
           return JanusPluginInfo("MockPlugin", "Mock Plugin", 0);
         }
         MOCK_METHOD0(create, std::shared_ptr<Plugin>());
      };

      class Mock2PluginFactory : public PluginFactory {
       public:
         JanusPluginInfo info() {
           return JanusPluginInfo("Mock2Plugin", "Mock 2 Plugin", 0);
         }
         MOCK_METHOD0(create, std::shared_ptr<Plugin>());
      };

      auto mockPluginFactory = std::make_shared<MockPluginFactory>();
      auto mock2PluginFactory = std::make_shared<Mock2PluginFactory>();

      auto platform = Platform::create();

      platform->registerPluginFactory(mockPluginFactory);
      platform->registerPluginFactory(mock2PluginFactory);

      EXPECT_EQ(platform->getPlugins().at(1).id, "MockPlugin");
      EXPECT_EQ(platform->getPlugins().at(0).id, "Mock2Plugin");
    }


    TEST(Platform, shouldRegisterTheDefautlPlugins) {
      auto uut = Platform::create();

      EXPECT_NE(uut->getPlugin("janus.plugin.echotest"), nullptr);
    }

    TEST(Platform, shouldReturnNullOnInvalidPlugin) {
      auto platform = Platform::create();
      EXPECT_EQ(platform->getPlugin("invalid"), nullptr);
    }

  }  // namespace Test
}  // namespace Janus
