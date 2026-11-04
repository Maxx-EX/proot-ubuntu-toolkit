#include <jni.h>
JNIEXPORT jstring JNICALL
Java_com_maxx_ubuntutoolkit_NativeLib_getArch(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "arm64-v8a");
}
