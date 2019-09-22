ROOT_DIR=$(shell pwd)
GENERATED_DIR=$(ROOT_DIR)/generated/
SHELL=/bin/bash

all: deps gluecode

folder:
	mkdir -p third_party/

mbedtls:
	if [ ! -d third_party/mbedtls ]; then git clone https://github.com/ARMmbed/mbedtls third_party/mbedtls && cd third_party/mbedtls && git checkout mbedtls-2.15.1; fi

curl:
	if [ ! -d third_party/curl ]; then git clone https://github.com/curl/curl third_party/curl && cd third_party/curl && git checkout curl-7_63_0; fi

djinni:
	if [ ! -d third_party/djinni ]; then git clone https://github.com/helloiampau/djinni third_party/djinni && cd third_party/djinni && git checkout ac7d0d21449922143ccdeb1751b6a09a5a9ca790; fi

jansson:
	if [ ! -d third_party/jansson ]; then git clone https://github.com/akheron/jansson third_party/jansson && cd third_party/jansson && git checkout v2.11; fi

crossguid:
	if [ ! -d third_party/crossguid ]; then git clone https://github.com/graeme-hill/crossguid third_party/crossguid && cd third_party/crossguid && git checkout f43d980f6284256928dd637d23fd542df162f90c; fi

googletest:
	if [ ! -d third_party/googletest ]; then git clone https://github.com/google/googletest third_party/googletest && cd third_party/googletest && git checkout release-1.8.0; fi

deps: folder mbedtls curl djinni jansson crossguid googletest

gluecode:
	cd third_party/djinni && rm -rf src/target && src/run --java-out $(GENERATED_DIR)/java/com/github/helloiampau/janus/generated --java-package com.github.helloiampau.janus.generated --cpp-out $(GENERATED_DIR)/cpp/janus --cpp-namespace Janus --jni-out $(GENERATED_DIR)/jni --ident-jni-file native_foo_bar --ident-jni-class NativeFooBar --objc-out $(GENERATED_DIR)/objc --objc-type-prefix Janus --objcpp-out $(GENERATED_DIR)/objcpp --idl $(ROOT_DIR)/janus-client.djinni

clean_lib:
	rm -rfv build/libjanus.so build/CMakeFiles/janus.dir build/CMakeCache.txt

clean_tests: clean_lib
	rm -rfv build/janus_tests build/CMakeFiles/janus_tests.dir

memory_test: clean_tests
	cd build && cmake .. && make janus_tests && valgrind --error-exitcode=29 --leak-check=full --show-leak-kinds=all --show-reachable=yes ./janus_tests

thread_test: clean_tests
	cd build && cmake -DEXTRA_TEST="THREADS" .. && make janus_tests && ./janus_tests

coverage: clean_tests
	cd build && cmake -DEXTRA_TEST="COVERAGE" .. && make janus_tests && ./janus_tests && cd .. && bash <(curl -s https://codecov.io/bash)

debugger:
	gdbgui --host 0.0.0.0 build/janus_tests

.PHONY: all mbedtls curl djinni jansson crossguid googletest deps gluecode clean_lib clean_tests memory_test thread_test coverage debugger
