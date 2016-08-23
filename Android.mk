LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := tpms

LOCAL_SRC_FILES := tpms.c

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)

