#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>
#include "janus/uuid.h"

using testing::MatchesRegex;

namespace Janus {
  namespace Test {

    TEST(Guid, shouldCreateAGuid) {
      auto uut = std::make_unique<UuidImpl>();
      EXPECT_THAT(uut->create(), MatchesRegex("[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}"));
    }

  }  // namespace Test
}  // namespace Janus
