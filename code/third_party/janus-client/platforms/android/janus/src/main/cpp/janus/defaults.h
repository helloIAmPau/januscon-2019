#pragma once

#include <jni.h>

namespace Janus {

  class DEFAULT_ENV {
   public:
     static void setJvm(JavaVM *jvm);

     static JNIEnv *get();

   private:
     static JavaVM *_jvm;
  };

}
