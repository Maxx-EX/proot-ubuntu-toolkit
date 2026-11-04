cmake_minimum_required(VERSION 3.22.1)
project("ubuntutoolkit" C)
add_library(native-lib SHARED native-lib.c)
target_link_libraries(native-lib log)
