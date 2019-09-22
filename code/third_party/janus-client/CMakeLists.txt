cmake_minimum_required(VERSION 3.6)

include(ExternalProject)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(EXTRA_TEST "VOID" CACHE STRING "values: COVERAGE, THREADS")

set(ROOT ${CMAKE_CURRENT_LIST_DIR})
set(GENERATED_DIR ${ROOT}/generated)
set(THIRD_PARTY_DIR ${ROOT}/third_party)
set(EXT_DIR ${ROOT}/cmake)

set(ENV_ARGS -DCMAKE_POSITION_INDEPENDENT_CODE=ON -DCMAKE_BUILD_TYPE=Release CACHE STRING "Env related external CMake arguments" FORCE)
if(ANDROID)
  add_definitions(-DANDROID -DGUID_ANDROID)
  set(ENV_ARGS ${ENV_ARGS} -DANDROID=ON -DANDROID_ABI=${ANDROID_ABI} -DANDROID_PLATFORM=${ANDROID_PLATFORM} -DANDROID_NDK=${ANDROID_NDK} -DCMAKE_CXX_FLAGS=${CMAKE_CXX_FLAGS} -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE} -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM})
endif()

# if(IOS)
  # set(ENV_ARGS ${ENV_ARGS} -DCMAKE_OSX_SYSROOT=iphoneos -DCMAKE_OSX_ARCHITECTURES=armv7 -DCMAKE_XCODE_EFFECTIVE_PLATFORMS="-iphoneos;-iphonesimulator" -DCMAKE_XCODE_ATTRIBUTE_CODE_SIGNING_REQUIRED="NO" -DCMAKE_XCODE_ATTRIBUTE_ENABLE_BITCODE="NO" -DCMAKE_CXX_COMPILER_WORKS=TRUE -DCMAKE_C_COMPILER_WORKS=TRUE -DCMAKE_MACOSX_BUNDLE=YES)
# endif()

ExternalProject_Add(jansson
  PREFIX ${CMAKE_BINARY_DIR}/third_party/jansson
  INSTALL_COMMAND mkdir -p ${CMAKE_BINARY_DIR}/third_party/jansson/lib && cp -fv ${CMAKE_BINARY_DIR}/third_party/jansson/src/jansson-build/lib/libjansson.a ${CMAKE_BINARY_DIR}/third_party/jansson/lib
  SOURCE_DIR ${THIRD_PARTY_DIR}/jansson
  BUILD_BYPRODUCTS ${CMAKE_BINARY_DIR}/third_party/jansson/lib/libjansson.a
  CMAKE_ARGS ${ENV_ARGS} -DJANSSON_WITHOUT_TESTS=ON -DJANSSON_EXAMPLES=OFF -DJANSSON_BUILD_DOCS=OFF)
add_library(libjansson STATIC IMPORTED)
set_property(TARGET libjansson PROPERTY IMPORTED_LOCATION ${CMAKE_BINARY_DIR}/third_party/jansson/lib/libjansson.a)

ExternalProject_Add(crossguid
  PREFIX ${CMAKE_BINARY_DIR}/third_party/crossguid
  INSTALL_COMMAND mkdir -p ${CMAKE_BINARY_DIR}/third_party/crossguid/lib && cp -fv ${CMAKE_BINARY_DIR}/third_party/crossguid/src/crossguid-build/libcrossguid.a ${CMAKE_BINARY_DIR}/third_party/crossguid/lib
  SOURCE_DIR ${THIRD_PARTY_DIR}/crossguid
  BUILD_BYPRODUCTS ${CMAKE_BINARY_DIR}/third_party/crossguid/lib/libcrossguid.a
  CMAKE_ARGS ${ENV_ARGS} -DCROSSGUID_TESTS=OFF)
add_library(libcrossguid INTERFACE)
target_link_libraries(libcrossguid INTERFACE ${CMAKE_BINARY_DIR}/third_party/crossguid/lib/libcrossguid.a)

ExternalProject_Add(mbedtls
  PREFIX ${CMAKE_BINARY_DIR}/third_party/mbedtls
  INSTALL_COMMAND mkdir -p ${CMAKE_BINARY_DIR}/third_party/mbedtls/lib && cp -fv ${CMAKE_BINARY_DIR}/third_party/mbedtls/src/mbedtls-build/library/libmbedcrypto.a ${CMAKE_BINARY_DIR}/third_party/mbedtls/src/mbedtls-build/library/libmbedx509.a ${CMAKE_BINARY_DIR}/third_party/mbedtls/src/mbedtls-build/library/libmbedtls.a ${CMAKE_BINARY_DIR}/third_party/mbedtls/lib
  SOURCE_DIR ${THIRD_PARTY_DIR}/mbedtls
  BUILD_BYPRODUCTS ${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedcrypto.a;${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedx509.a;${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedtls.a
  CMAKE_ARGS ${ENV_ARGS} -DENABLE_PROGRAMS=OFF -DENABLE_TESTING=OFF)
add_library(libmbedcrypto INTERFACE)
target_link_libraries(libmbedcrypto INTERFACE ${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedcrypto.a)
add_library(libmbedx509 INTERFACE)
target_link_libraries(libmbedx509 INTERFACE ${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedx509.a)
add_library(libmbedtls INTERFACE)
target_link_libraries(libmbedtls INTERFACE ${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedtls.a)

ExternalProject_Add(curl
  DEPENDS mbedtls
  PREFIX ${CMAKE_BINARY_DIR}/third_party/curl
  SOURCE_DIR ${THIRD_PARTY_DIR}/curl
  INSTALL_COMMAND mkdir -p ${CMAKE_BINARY_DIR}/third_party/curl/lib && cp -fv ${CMAKE_BINARY_DIR}/third_party/curl/src/curl-build/lib/libcurl.a ${CMAKE_BINARY_DIR}/third_party/curl/lib
  BUILD_BYPRODUCTS ${CMAKE_BINARY_DIR}/third_party/curl/lib/libcurl.a
  CMAKE_ARGS ${ENV_ARGS} -DCURL_ZLIB=OFF -DHAVE_POLL_FINE_EXITCODE=OFF -DCMAKE_USE_MBEDTLS=ON -DHTTP_ONLY=ON -DBUILD_SHARED_LIBS=OFF -DBUILD_TESTING=OFF -DUSE_MANUAL=OFF -DBUILD_CURL_EXE=OFF -DENABLE_THREADED_RESOLVER=OFF -DMBEDTLS_INCLUDE_DIRS=${THIRD_PARTY_DIR}/mbedtls/include -DMBEDCRYPTO_LIBRARY=${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedcrypto.a -DMBEDTLS_LIBRARY=${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedtls.a -DMBEDX509_LIBRARY=${CMAKE_BINARY_DIR}/third_party/mbedtls/lib/libmbedx509.a)
add_library(libcurl INTERFACE)
target_link_libraries(libcurl INTERFACE ${CMAKE_BINARY_DIR}/third_party/curl/lib/libcurl.a)

# Janus library

file(GLOB_RECURSE janus_srcs ${ROOT}/src/*.cc)
set(janus_includes
  ${GENERATED_DIR}/cpp
  ${ROOT}/include
  ${THIRD_PARTY_DIR}/crossguid/include
  ${THIRD_PARTY_DIR}/curl/include
  ${THIRD_PARTY_DIR}/jansson/android
  ${THIRD_PARTY_DIR}/jansson/src)

if(ANDROID)
  list(APPEND janus_srcs
    ${ROOT}/platforms/android/janus/src/main/cpp/janus/defaults.cc)

  list(APPEND janus_includes
    ${ROOT}/platforms/android/janus/src/main/cpp/
    ${THIRD_PARTY_DIR}/djinni/support-lib/jni/)
endif()

add_library(janus
  SHARED
  ${janus_srcs})

target_include_directories(janus
  SYSTEM
  PUBLIC
  ${janus_includes})

target_link_libraries(janus
  libjansson
  libcrossguid
  libcurl
  libmbedtls
  libmbedx509
  libmbedcrypto)

add_dependencies(janus
  jansson
  crossguid
  curl)

# END Janus Library

# Support Library

add_library(support
  SHARED 
  ${THIRD_PARTY_DIR}/djinni/support-lib/jni/djinni_main.cpp
  ${THIRD_PARTY_DIR}/djinni/support-lib/jni/djinni_support.cpp)

# End Support Library

# Android SDK Library

if(ANDROID)
  file(GLOB_RECURSE jni_srcs ${GENERATED_DIR}/jni/*.cpp)

  add_library(janus-android-sdk
    SHARED
    ${ROOT}/platforms/android/janus/src/main/cpp/janus/init.cc
    ${jni_srcs})

  target_include_directories(janus-android-sdk
    SYSTEM
    PUBLIC
    ${GENERATED_DIR}/cpp/janus/
    ${ROOT}/third_party/crossguid/include
    ${THIRD_PARTY_DIR}/djinni/support-lib/jni/)

  target_link_libraries(janus-android-sdk
    support
    janus)

  add_dependencies(janus-android-sdk
    support
    janus)
endif()

# End Android SDK library

# Test lib

set(CMAKE_BUILD_TYPE Debug)

ExternalProject_Add(gmock
  PREFIX ${CMAKE_BINARY_DIR}/third_party/gmock
  INSTALL_COMMAND mkdir -p ${CMAKE_BINARY_DIR}/third_party/gmock/lib && cp -fv ${CMAKE_BINARY_DIR}/third_party/gmock/src/gmock-build/libgmock.a ${CMAKE_BINARY_DIR}/third_party/gmock/lib
  SOURCE_DIR ${THIRD_PARTY_DIR}/googletest/googlemock
  CMAKE_ARGS ${ENV_ARGS})

file(GLOB_RECURSE test_srcs ${ROOT}/test/*.cc)
add_executable(janus_tests
  ${test_srcs})

target_include_directories(janus_tests
  SYSTEM
  PUBLIC
  ${THIRD_PARTY_DIR}/googletest/googletest/include
  ${THIRD_PARTY_DIR}/googletest/googlemock/include
  ${ROOT}/include
  ${ROOT}/test
  ${GENERATED_DIR}/cpp)

target_link_libraries(janus_tests
  janus
  /usr/lib/x86_64-linux-gnu/libuuid.a
  ${CMAKE_BINARY_DIR}/third_party/gmock/lib/libgmock.a
  pthread)

add_dependencies(janus_tests
  gmock
  janus)

# End Test lib

get_target_property(JANUS_COMPILE_FLAGS janus COMPILE_FLAGS)
if(JANUS_COMPILE_FLAGS STREQUAL "JANUS_COMPILE_FLAGS-NOTFOUND")
  SET(JANUS_COMPILE_FLAGS "")
endif()
get_target_property(JANUS_LINK_FLAGS janus LINK_FLAGS)
if(JANUS_LINK_FLAGS STREQUAL "JANUS_LINK_FLAGS-NOTFOUND")
  SET(JANUS_LINK_FLAGS "")
endif()
get_target_property(TESTS_COMPILE_FLAGS janus COMPILE_FLAGS)
if(TESTS_COMPILE_FLAGS STREQUAL "TESTS_COMPILE_FLAGS-NOTFOUND")
  SET(TESTS_COMPILE_FLAGS "")
endif()
get_target_property(TESTS_LINK_FLAGS janus LINK_FLAGS)
if(TESTS_LINK_FLAGS STREQUAL "TESTS_LINK_FLAGS-NOTFOUND")
  SET(TESTS_LINK_FLAGS "")
endif()

if(${EXTRA_TEST} MATCHES "COVERAGE")
  set_target_properties(janus
    PROPERTIES
    COMPILE_FLAGS "${JANUS_COMPILE_FLAGS} -fprofile-arcs -ftest-coverage"
    LINK_FLAGS "${JANUS_LINK_FLAGS} -lgcov --coverage")
  set_target_properties(janus_tests
    PROPERTIES
    COMPILE_FLAGS "${TESTS_COMPILE_FLAGS}  -fprofile-arcs -ftest-coverage"
    LINK_FLAGS "${TESTS_LINK_FLAGS} -lgcov --coverage")
elseif(${EXTRA_TEST} MATCHES "THREADS")
  set_target_properties(janus
    PROPERTIES
    COMPILE_FLAGS "${JANUS_COMPILE_FLAGS} -fsanitize=thread"
    LINK_FLAGS "${JANUS_LINK_FLAGS} -fsanitize=thread")
  set_target_properties(janus_tests
    PROPERTIES
    COMPILE_FLAGS "${TESTS_COMPILE_FLAGS} -fsanitize=thread"
    LINK_FLAGS "${TESTS_LINK_FLAGS} -fsanitize=thread")
endif()
