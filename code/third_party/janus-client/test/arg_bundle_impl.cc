#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/arg_bundle_impl.h"

namespace Janus {
  namespace Test {

    TEST(ArgBundleImpl, shouldStoreAStringValue) {
      auto uut = ArgBundle::create();

      uut->setString("a string", "the string");
      EXPECT_EQ(uut->getString("a string"), "the string");
    }

    TEST(ArgBundleImpl, shouldReturnDefaultStringOnInvalidKeyword) {
      auto uut = ArgBundle::create();

      EXPECT_EQ(uut->getString("a string"), "");
    }

    TEST(ArgBundleImpl, shouldStoreAnIntValue) {
      auto uut = ArgBundle::create();

      uut->setInt("an int", 42);
      EXPECT_EQ(uut->getInt("an int"), 42);
    }

    TEST(ArgBundleImpl, shouldReturnDefaultIntOnInvalidKeyword) {
      auto uut = ArgBundle::create();

      EXPECT_EQ(uut->getInt("an int"), -999);
    }

    TEST(ArgBundleImpl, shouldStoreALongValue) {
      auto uut = ArgBundle::create();

      uut->setLong("an int", 42);
      EXPECT_EQ(uut->getLong("an int"), 42);
    }

    TEST(ArgBundleImpl, shouldReturnDefaultLongOnInvalidKeyword) {
      auto uut = ArgBundle::create();

      EXPECT_EQ(uut->getLong("an int"), -999);
    }

    TEST(ArgBundleImpl, shouldStoreABoolValue) {
      auto uut = ArgBundle::create();

      uut->setBool("is true", true);
      EXPECT_EQ(uut->getBool("is true"), true);
    }

    TEST(ArgBundleImpl, shouldReturnDefaultBoolOnInvalidKeyword) {
      auto uut = ArgBundle::create();

      EXPECT_EQ(uut->getBool("invalid"), false);
    }

  }  // namespace Test
}  // namespace Janus
