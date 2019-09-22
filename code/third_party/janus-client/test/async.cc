#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <memory>
#include "janus/async.h"

namespace Janus {
  namespace Test {

    class MyTask : public Task {
     public:
       void action() {
         std::lock_guard<std::mutex> lock(this->mutex);
         this->_value += 1000;
       }

       int value() {
         std::lock_guard<std::mutex> lock(this->mutex);
         return this->_value;
       }
     private:
       int _value = 0;
       std::mutex mutex;
    };

    TEST(Async, shouldHandleTasks) {
      auto t = std::make_shared<MyTask>();
      auto uut = std::make_shared<AsyncImpl>();

      uut->submit(t);
      usleep(10000);
      EXPECT_EQ(t->value(), 1000);

      uut->submit(t);
      usleep(10000);
      EXPECT_EQ(t->value(), 2000);

      uut->shutdown();
    }

  }  // namespace Test
}  // namespace Janus
