#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <string>
#include <memory>
#include "janus/json.h"

namespace Janus {
  namespace Test {

    TEST(JSON, parsesAJsonString) {
      auto testString = "{\"pippo\": \"test\"}";
      auto json = std::make_unique<JSON>(testString);

      auto result = json->serialize();
      EXPECT_EQ(result, testString);
    }

    TEST(JSON, initializeAnEmptyJSON) {
      auto json = std::make_unique<JSON>();
      EXPECT_EQ(json->serialize(), "{}");
    }

    TEST(JSON, wrapsAJSON_TObject) {
      auto root = json_object();
      auto value = json_integer(42);
      json_object_set(root, "value", value);
      json_decref(value);
      auto json = std::make_unique<Janus::JSON>(root);
      json_decref(root);

      EXPECT_EQ(json->integer("value"), 42);
    }

    TEST(JSON, getsAFieldAsString) {
      auto testString = "{\"pippo\": \"test\"}";
      auto json = std::make_unique<Janus::JSON>(testString);

      EXPECT_EQ(json->string("pippo"), "test");
    }

    TEST(JSON, getsAFieldAsInteger) {
      auto testString = "{\"pippo\": 123}";
      auto json = std::make_unique<Janus::JSON>(testString);

      EXPECT_EQ(json->integer("pippo"), 123);
    }

    TEST(JSON, getsAFieldAsBoolean) {
      auto testString = "{\"pippo\": true}";
      auto json = std::make_unique<Janus::JSON>(testString);

      EXPECT_EQ(json->boolean("pippo"), true);
    }

    TEST(JSON, getsANestedJSON) {
      auto testString = "{\"pippo\": {\"inner\":true}}";
      auto json = std::make_unique<Janus::JSON>(testString);

      EXPECT_EQ(json->object("pippo")->boolean("inner"), true);
    }

    TEST(JSON, addsAnIntegerField) {
      auto json = std::make_unique<Janus::JSON>();

      json->integer("testInteger", 42);

      EXPECT_EQ(json->integer("testInteger"), 42);
    }

    TEST(JSON, addsAStringField) {
      auto json = std::make_unique<Janus::JSON>();

      auto testString = "a test string";
      json->string("testString", testString);

      EXPECT_EQ(json->string("testString"), testString);
    }

    TEST(JSON, addsABooleanField) {
      auto json = std::make_unique<Janus::JSON>();

      json->boolean("testBool", true);

      EXPECT_EQ(json->boolean("testBool"), true);
    }

    TEST(JSON, shouldReturnEmptyObjectIfObjectDoesNotExist) {
      auto json = std::make_unique<Janus::JSON>();
      EXPECT_EQ(json->object("notExists")->serialize(), "{}");
    }

    TEST(JSON, shouldAlwaysReturnSomething) {
      auto json = std::make_unique<JSON>();

      EXPECT_EQ(json->string("empty"), "");
      EXPECT_EQ(json->integer("empty"), -999);
      EXPECT_EQ(json->boolean("empty"), false);
    }

    TEST(JSON, shouldSetAnObjectField) {
      auto json = std::make_unique<JSON>();
      auto content = std::make_shared<JSON>();

      content->boolean("test", true);
      json->object("content", content);

      EXPECT_EQ(json->object("content")->boolean("test"), true);
    }

    TEST(JSON, shouldMapObjectValues) {
      auto testString = "{\"pippo\": {\"inner\":true}, \"pluto\": {\"inner\":false}}";
      auto json = std::make_unique<JSON>(testString);

      json->map([](std::string key, std::shared_ptr<JSON> value) {
          if(key == "pippo") {
            EXPECT_EQ(value->boolean("inner"), true);
          } else if(key == "pluto") {
            EXPECT_EQ(value->boolean("inner"), false);
          } else {
            EXPECT_EQ(true, false);
          }
      });
    }

    TEST(JSON, shouldCheckForAKey) {
      auto json = std::make_unique<JSON>("{\"valid\": \"a valid key\"}");

      EXPECT_EQ(json->hasKey("valid"), true);
      EXPECT_EQ(json->hasKey("invalid"), false);
    }

  }  // namespace Test
}  // namespace Janus
