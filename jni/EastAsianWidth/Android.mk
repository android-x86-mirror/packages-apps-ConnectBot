LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := liborg_connectbot_util_EastAsianWidth
LOCAL_SRC_FILES := org_connectbot_util_EastAsianWidth.c
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE) $(LOCAL_PATH)/unicode
LOCAL_SHARED_LIBRARIES := libicuuc liblog

include $(BUILD_SHARED_LIBRARY)
