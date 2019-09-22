#include <jni.h>
#include <djinni_support.hpp>
#include "crossguid/guid.hpp"

#include "defaults.h"

extern "C" {
  JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void*) {
    Janus::DEFAULT_ENV::setJvm(jvm);
    djinni::jniInit(jvm);

    return JNI_VERSION_1_6;
  }

  JNIEXPORT void JNICALL
  Java_com_github_helloiampau_janus_JanusFactory_init(JNIEnv* env, jobject) {
    xg::initJni(env);
  }
}
