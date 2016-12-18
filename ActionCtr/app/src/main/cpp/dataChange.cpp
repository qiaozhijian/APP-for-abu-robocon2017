#include <jni.h>
extern "C"
{
JNIEXPORT jbyteArray JNICALL
Java_com_action_app_actionctr_ndktool_JniShareUtils_floatToByte(JNIEnv *env,
                                                                               jobject instance,
                                                                               jfloat data) {
    jbyteArray array;
    jbyte *temp;
    temp = (jbyte *) &data;
    array = env->NewByteArray(4);
    env->SetByteArrayRegion(array, 0, 4, temp);
    return array;
}

}
