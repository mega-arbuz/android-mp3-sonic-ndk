cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wno-unused-function -O3 -ansi -fPIC -pthread")

add_library( sonic

             SHARED

             src/main/cpp/sonicjni.c
             src/main/cpp/sonic.c )

find_library( log-lib

              log )

target_link_libraries( sonic

                       ${log-lib} )