#include <jni.h>
JNIEXPORT jstring JNICALL Java_com_maxx_ubuntutoolkit_MainActivity_test(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "ok");
}
