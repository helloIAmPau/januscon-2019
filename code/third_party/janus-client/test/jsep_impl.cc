#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include "janus/jsep_impl.h"
#include "janus/mocks.h"

using testing::NiceMock;

namespace Janus {
  namespace Test {

    TEST(JsepFactory, shouldCreateAJsepObject) {
      auto uut = Jsep::create("test", "test sdp");

      EXPECT_EQ(uut->type(), "test");
      EXPECT_EQ(uut->sdp(), "test sdp");
    }

    TEST(JsepImpl, shouldCheckIfTHeSdpContainsAMedium) {
      auto uut = Jsep::create("test", "stuff\r\nstuff\r\nm=test codecs\r\nstuff");

      EXPECT_EQ(uut->hasMedia("test"), true);
      EXPECT_EQ(uut->hasMedia("invalid"), false);
    }

  }  // namespace Test
}  // namespace Janus
