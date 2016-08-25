LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS += \
    -DENABLE_TPMS_TEST

LOCAL_MODULE := tpms

LOCAL_SRC_FILES := tpms.cpp

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)

LOCAL_MODULE := libtpms_jni

LOCAL_CFLAGS += \
    -DENABLE_TPMS_JNI

LOCAL_SRC_FILES := \
    tpms.cpp \
    com_apical_tpms_tpms.cpp

LOCAL_SHARED_LIBRARIES := \
    libcutils

LOCAL_MODULE_TAGS := optional

LOCAL_MULTILIB := both

include $(BUILD_SHARED_LIBRARY)

