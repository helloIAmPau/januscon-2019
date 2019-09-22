#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/janus_impl.h"
#include "janus/mocks.h"
#include "janus/conf.h"

using testing::Return;
using testing::NiceMock;
using testing::Eq;
using testing::_;

namespace Janus {
  namespace Test {

    TEST(Janus, shouldCreateAnInstanceViaFactory) {
      auto platformMock = std::make_shared<Mocks::PlatformMock>();
      std::shared_ptr<Janus> uut = Janus::create(platformMock);

      EXPECT_NE(uut, nullptr);
    }

    TEST(Janus, startsTheProtocolOnBoot) {
      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();

      auto protocolMock = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocolMock, readyState()).WillOnce(Return(ReadyState::READY));
      auto platformMock = std::make_shared<Mocks::PlatformMock>();

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      EXPECT_CALL(*platformMock, getProtocol(Eq(conf), Eq(delegate))).WillOnce(Return(protocolMock));
      auto uut = Janus::create(platformMock);

      auto result = uut->init(conf, delegate);
      EXPECT_EQ(result, ReadyState::READY);
    }

    TEST(Janus, initializesTheSessionOnce) {
      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto protocolMock = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocolMock, readyState()).Times(3).WillRepeatedly(Return(ReadyState::READY));
      auto platformMock = std::make_shared<Mocks::PlatformMock>();
      EXPECT_CALL(*platformMock, getProtocol(Eq(conf), Eq(delegate))).WillOnce(Return(protocolMock));

      auto uut = Janus::create(platformMock);

      uut->init(conf, delegate);
      uut->init(conf, delegate);
    }

    TEST(Janus, shouldGetTheCurrentReadyState) {
      auto protocolMock = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocolMock, readyState()).Times(2).WillOnce(Return(ReadyState::OFF)).WillOnce(Return(ReadyState::READY));

      auto platformMock = std::make_shared<Mocks::PlatformMock>();
      EXPECT_CALL(*platformMock, getProtocol(_, _)).WillOnce(Return(protocolMock));

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();

      std::shared_ptr<Janus> uut = Janus::create(platformMock);
      EXPECT_EQ(uut->init(conf, delegate), ReadyState::OFF);
      EXPECT_EQ(uut->readyState(), ReadyState::READY);
    }

    TEST(Janus, hasReadyStateSetToOFFWhenSessionIsNotInitialized) {
      auto platformMock = std::make_shared<Mocks::PlatformMock>();

      std::shared_ptr<Janus> uut = Janus::create(platformMock);
      EXPECT_EQ(uut->readyState(), ReadyState::OFF);
    }

    TEST(Janus, closesTheLowerLevelProtocol) {
      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();

      auto protocolMock = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocolMock, readyState()).WillOnce(Return(ReadyState::READY));
      auto platformMock = std::make_shared<Mocks::PlatformMock>();

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      EXPECT_CALL(*platformMock, getProtocol(Eq(conf), Eq(delegate))).WillOnce(Return(protocolMock));
      auto uut = Janus::create(platformMock);

      uut->init(conf, delegate);
      auto result = uut->close();
      EXPECT_EQ(result, ReadyState::OFF);
    }

    TEST(Janus, getsPluginInformationFromSession) {
      JanusPluginInfo plugin("mockId", "", 999);
      std::vector<JanusPluginInfo> myList({ plugin });

      auto protocolMock = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocolMock, plugins()).WillOnce(Return(myList));

      auto platformMock = std::make_shared<Mocks::PlatformMock>();
      EXPECT_CALL(*platformMock, getProtocol(_, _)).WillOnce(Return(protocolMock));
      ON_CALL(*protocolMock, readyState()).WillByDefault(Return(ReadyState::READY));

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();

      std::shared_ptr<Janus> uut = Janus::create(platformMock);
      EXPECT_EQ(uut->plugins().size(), 0);
      uut->init(conf, delegate);
      EXPECT_EQ(uut->plugins().at(0).id, "mockId");
    }

    TEST(Janus, attachesToAPlugin) {
      auto context = ArgBundle::create();

      auto protocolMock = std::make_shared<NiceMock<Mocks::ProtocolMock>>();
      EXPECT_CALL(*protocolMock, attach("mockId", context));
      ON_CALL(*protocolMock, readyState()).WillByDefault(Return(ReadyState::READY));

      auto platformMock = std::make_shared<Mocks::PlatformMock>();
      EXPECT_CALL(*platformMock, getProtocol(_, _)).WillOnce(Return(protocolMock));

      auto delegate = std::make_shared<NiceMock<Mocks::JanusDelegateMock>>();

      auto conf = std::make_shared<NiceMock<Mocks::JanusConfMock>>();

      std::shared_ptr<Janus> uut = Janus::create(platformMock);
      uut->attach("mockId", context);
      uut->init(conf, delegate);
      uut->attach("mockId", context);
    }

  }  // namespace Test
}  // namespace Janus
