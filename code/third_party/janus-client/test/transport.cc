#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>

#include "janus/transport.h"
#include "janus/mocks.h"

using testing::NiceMock;
using testing::Return;
using testing::Ref;

namespace Janus {
  namespace Test {

    class HttpTransportTest : public testing::Test {
     protected:
       void SetUp() override {
         this->context = ArgBundle::create();
         this->httpFactory = std::make_shared<NiceMock<Mocks::HttpFactoryMock>>();
         this->http = std::make_shared<NiceMock<Mocks::HttpMock>>();
         this->delegate = std::make_shared<NiceMock<Mocks::DelegateMock>>();

         ON_CALL(*this->httpFactory, create("http://janus.base.url/999999")).WillByDefault(Return(http));
       }

       std::shared_ptr<ArgBundle> context;
       std::shared_ptr<Mocks::HttpFactoryMock> httpFactory;
       std::shared_ptr<Mocks::HttpMock> http;
       std::shared_ptr<Mocks::DelegateMock> delegate;
    };

    TEST_F(HttpTransportTest, shouldGetEvents) {
      Response response(200, "{\"janus\": \"mock reply\", \"transaction\": \"a transaction\"}");
      EXPECT_CALL(*this->http, get("/")).WillOnce(Return(response));
      EXPECT_CALL(*this->delegate, onMessage(IsMessage("mock reply"), Any()));

      auto uut = std::make_shared<HttpTransport>("http://janus.base.url", "999999", this->httpFactory, this->delegate);
      uut->get();
    }

    TEST_F(HttpTransportTest, shouldSendRequestsWithContext) {
      Response response(200, "{\"janus\": \"mock reply\", \"transaction\": \"a transaction\"}");
      EXPECT_CALL(*this->delegate, onMessage(IsMessage("mock reply"), this->context));
      EXPECT_CALL(*http, post("/", IsType("mock request"))).WillOnce(Return(response));

      auto uuidMock = std::make_shared<NiceMock<Mocks::UuidMock>>();
      EXPECT_CALL(*uuidMock, create()).WillOnce(Return("a uuid"));
      auto request = std::make_shared<Message>("mock request", uuidMock);

      auto uut = std::make_shared<HttpTransport>("http://janus.base.url", "999999", httpFactory, delegate);
      uut->request(request, this->context);
    }

    TEST_F(HttpTransportTest, shouldSendRequests) {
      Response response(200, "{\"janus\": \"mock reply\", \"transaction\": \"a transaction\"}");
      EXPECT_CALL(*this->delegate, onMessage(IsMessage("mock reply"), Any()));
      EXPECT_CALL(*http, post("/", IsType("mock request"))).WillOnce(Return(response));

      auto uuidMock = std::make_shared<NiceMock<Mocks::UuidMock>>();
      EXPECT_CALL(*uuidMock, create()).WillOnce(Return("a uuid"));
      auto request = std::make_shared<Message>("mock request", uuidMock);

      auto uut = std::make_shared<HttpTransport>("http://janus.base.url", "999999", httpFactory, delegate);
      uut->request(request);
    }

    TEST_F(HttpTransportTest, shouldInterruptAllTheClientOnClose) {
      EXPECT_CALL(*http, interrupt()).Times(3);

      auto uut = std::make_shared<HttpTransport>("http://janus.base.url", "999999", httpFactory, delegate);
      uut->close();
    }

    TEST(TransportFactoryImpl, createsAnHttpTransport) {
      auto delegate = std::make_shared<NiceMock<Mocks::DelegateMock>>();

      auto factory = std::make_shared<TransportFactoryImpl>();
      EXPECT_EQ(factory->create(TransportType::HTTP, "http://janus.base.url", "999999", delegate)->type(), TransportType::HTTP);
    }

  }  // namespace Test
}  // namespace Janus
