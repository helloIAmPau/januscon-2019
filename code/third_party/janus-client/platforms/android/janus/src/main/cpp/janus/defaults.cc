#include "janus/defaults.h"

namespace Janus {
  JavaVM* DEFAULT_ENV::_jvm;

  void DEFAULT_ENV::setJvm(JavaVM* jvm) {
    DEFAULT_ENV::_jvm = jvm;
  }

  JNIEnv* DEFAULT_ENV::get() {
    JNIEnv* threadEnv;
    DEFAULT_ENV::_jvm->AttachCurrentThread(&threadEnv, nullptr);

    return threadEnv;
  }
}