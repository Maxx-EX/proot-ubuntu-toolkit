package com.maxx.ubuntutoolkit
object NativeLib {
    init { System.loadLibrary("native-lib") }
    external fun getArch(): String
}
