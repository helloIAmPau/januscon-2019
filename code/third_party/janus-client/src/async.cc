#include "janus/async.h"

namespace Janus {

  AsyncImpl::AsyncImpl() {
    this->setEnabled(true);

    pthread_create(&this->thread, nullptr, AsyncImpl::loop, reinterpret_cast<void*>(this));
  }

  AsyncImpl::~AsyncImpl() {
    this->shutdown();
    pthread_join(this->thread, nullptr);
  }

  void AsyncImpl::submit(const std::shared_ptr<Task>& task) {
    std::lock_guard<std::mutex> lock(this->mutex);
    this->_tasks.push(task);
    this->notEmpty.notify_one();
  }

  void AsyncImpl::shutdown() {
    this->setEnabled(false);
    this->notEmpty.notify_all();
  }

  void AsyncImpl::setEnabled(bool value) {
    std::lock_guard<std::mutex> lock(this->enabledMutex);
    this->enabled = value;
  }

  bool AsyncImpl::isEnabled() {
    std::lock_guard<std::mutex> lock(this->enabledMutex);
    return this->enabled;
  }

  void* AsyncImpl::loop(void* self) {
    AsyncImpl* context = reinterpret_cast<AsyncImpl*>(self);

    while(context->isEnabled() == true) {
      std::shared_ptr<Task> task;

      std::unique_lock<std::mutex> lock(context->mutex);
      context->notEmpty.wait(lock, [context]{ return context->_tasks.size() != 0 || context->isEnabled() == false; });

      if(context->isEnabled() == false) {
        return nullptr;
      }

      task = context->_tasks.front();
      context->_tasks.pop();

      lock.unlock();
      context->notEmpty.notify_one();

      task->action();
    }

    return nullptr;
  }

}  // namespace Janus
