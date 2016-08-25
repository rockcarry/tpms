LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := TpmsTest

LOCAL_JNI_SHARED_LIBRARIES := libtpms_jni
LOCAL_REQUIRED_MODULES := libtpms_jni

LOCAL_MULTILIB := both

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))

