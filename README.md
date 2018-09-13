# Android MP3 player with Sonic NDK
This is a simple demo app that decodes MP3 and plays it with Sonic. Sonic is a native library that allows changing speech rate, speed and pitch.
The project targets Android API 28 and compiles with Cmake.

## Motivation
There are some Android projects with Sonic, but they are mostly old Eclipse projects that won't compile in new versions of Android Studio. I've decided to make this sample app, that will be used as a reference for anyone who want to build his own player with Sonic NDK implementation.

## Sonic
Sonic is a cool library that changes speech rate while controlling pitch and makes audio books listenable on high speed.
The library is working with raw PCM data, so I added a decoder for MP3 (and other formats) files.

## Decoder
You can find old examples of Sonic with mpg123 decoder, but it's a mess to compile them for Android Oreo. I'm using Android MediaCodec API for this project.

## Using this project
The project is licensed under Apache 2.0, you can use this project for any purpose. Keep in mind that the intention is to make the code as simple as is. This is not optimized and has a bad structure, so you don't want to use it as is :)
