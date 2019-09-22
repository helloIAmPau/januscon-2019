#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>
#include "janus/http.h"
#include "janus/mocks.h"
#include "janus/janus_impl.h"
#include "janus/json.h"

using testing::NiceMock;
using testing::Return;

namespace Janus {
  namespace Test {

    TEST(http, performsAGetRequest) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://httpbin");
      auto response = http->get("/get");

      EXPECT_EQ(response.status, 200);
      auto body = std::make_shared<JSON>(response.body);
      EXPECT_EQ(body->string("url"), "http://httpbin/get");
      EXPECT_EQ(body->object("headers")->string("User-Agent"), "Janus Native HTTP Client");
    }

    TEST(http, performsAPostRequest) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://httpbin");
      auto response = http->post("/post");

      EXPECT_EQ(response.status, 200);
      auto body = std::make_shared<JSON>(response.body);
      EXPECT_EQ(body->string("url"), "http://httpbin/post");
      EXPECT_EQ(body->object("headers")->string("User-Agent"), "Janus Native HTTP Client");
    }

    TEST(http, shouldSendData) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://httpbin");
      Query query({ { "argument", "to_send" } });
      auto data = "{ \"a\": \"payload\" }";
      auto response = http->post("/post", Request(data, query));

      EXPECT_EQ(response.status, 200);
      auto body = std::make_shared<JSON>(response.body);
      EXPECT_EQ(body->string("data"), data);
      EXPECT_EQ(body->object("headers")->string("Content-Type"), "application/json");
      EXPECT_EQ(body->object("args")->string("argument"), "to_send");
    }

    TEST(http, shouldSendPostData) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://httpbin");
      auto data = "{ \"a\": \"payload\" }";
      auto response = http->post("/post", Request(data));

      EXPECT_EQ(response.status, 200);
      auto body = std::make_shared<JSON>(response.body);
      EXPECT_EQ(body->string("data"), data);
      EXPECT_EQ(body->object("headers")->string("Content-Type"), "application/json");
    }

    TEST(http, shouldSendQueryData) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://httpbin");
      Query query({ { "argument", "to_send" } });
      auto response = http->get("/get", Request(query));

      EXPECT_EQ(response.status, 200);
      auto body = std::make_shared<JSON>(response.body);
      EXPECT_EQ(body->object("args")->string("argument"), "to_send");
    }

    TEST(http, shouldForwardHttpCodes) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://httpbin");
      auto response = http->get("/post");

      EXPECT_EQ(response.status, 405);
    }

    TEST(http, shouldForwardACurlError) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("http://fake");
      auto response = http->get("/post");

      auto error = "{\"janus\": \"error\", \"transaction\": \"__internal__\", \"error\": {\"code\": 6, \"reason\": \"http error 6\"}}";
      EXPECT_EQ(response.status, 6);
      EXPECT_EQ(response.body, error);
    }

    TEST(http, shouldWorkWithHTTPS) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("https://https");
      auto response = http->get("/get");

      EXPECT_EQ(response.status, 200);
      auto body = std::make_shared<JSON>(response.body);
      EXPECT_EQ(body->string("url"), "https://https/get");
    }

    TEST(http, shouldInterruptARequest) {
      class TestTask : public Task {
       public:
         TestTask() {
           auto httpFactory = std::make_shared<HttpFactoryImpl>();

           this->_http = httpFactory->create("http://httpbin");
         }
         std::mutex mutex;
         std::shared_ptr<Http> _http;
         std::shared_ptr<Http> http() {
           std::lock_guard<std::mutex> lock(this->mutex);

           return this->_http;
         }
         void action() {
           auto response = this->http()->get("/delay/10");

           EXPECT_EQ(response.status, 42);
         }
      };

      // running this test in async mode
      auto async = std::make_unique<AsyncImpl>();
      auto testTask = std::make_shared<TestTask>();
      async->submit(testTask);

      usleep(10000);

      testTask->http()->interrupt();
    }

    TEST(httpFactory, shouldCreateAnHttpClient) {
      auto httpFactory = std::make_shared<HttpFactoryImpl>();

      auto http = httpFactory->create("an url");
      EXPECT_NE(http, nullptr);
    }

  }  // namespace Test
}  // namespace Janus
