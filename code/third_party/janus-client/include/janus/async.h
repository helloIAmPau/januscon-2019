#pragma once

#include <memory>
#include <queue>
#include <mutex>
#include <condition_variable>

namespace Janus {

  class Task {
   public:
     virtual void action() = 0;
  };

  class Async {
   public:
     virtual void submit(const std::shared_ptr<Task>& task) = 0;
     virtual void shutdown() = 0;
  };

  class AsyncImpl : public Async {
   public:
     AsyncImpl();
     ~AsyncImpl();
     void submit(const std::shared_ptr<Task>& task);
     void shutdown();
   private:
     std::queue<std::shared_ptr<Task>> _tasks;
     std::mutex mutex;
     std::condition_variable notEmpty;

     bool enabled = false;
     std::mutex enabledMutex;
     bool isEnabled();
     void setEnabled(bool value);

     pthread_t thread;
     static void* loop(void* context);
  };

}  // namespace Janus
