LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libcom_google_ase_Exec
LOCAL_SRC_FILES := com_google_ase_Exec.cpp
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)
LOCAL_SHARED_LIBRARIES := liblog

include $(BUILD_SHARED_LIBRARY)
