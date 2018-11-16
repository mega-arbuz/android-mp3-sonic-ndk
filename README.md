# Android MP3 player with Sonic NDK
This is a simple demo app that decodes MP3 and plays it with Sonic. Sonic is a native library that allows to change pitch, speed up and slow down speech.
The project targets Android API 27 and compiles with CMake.

## About

### Alternatives and Motivation
If you need a good player that controls speech speed, use [ExoPlayer](https://developer.android.com/guide/topics/media/exoplayer), which works with Sonic and has good performance. I made this because other projects, with Sonic NDK, are old Eclipse projects that wouldn't work with new API. The structure and the code is as simple as possible, so this could be a good start and a reference if you want to build your own custom player, with Sonic and Android MediaCodec API.

### Sonic
Sonic is a cool library that changes speech rate while controlling pitch and makes audio books listenable on high speed.
The library is working with raw PCM data, so I added a decoder for MP3 (and other formats) files.

Files that were copied from Sonic repo have different license:
sonic. sonic.h sonicjni.c sonicjni.h Sonic.java

[Old Sonic NDK repo](https://github.com/waywardgeek/sonic-ndk)

[Sonic lib repo](https://github.com/waywardgeek/sonic)

### Decoder
You can find old examples of Sonic with mpg123 decoder, but it's a mess to compile them for Android Oreo. I'm using Android MediaCodec API for this project.

## Features
* Play MP3 (and other formats)
* Change rate, pitch and speed
* Choose between Sonic and regular Android AudioTrack (the difference between the classes can be used as a guide on how to integrate Sonic into existing project)
* Track time

![https://github.com/mega-arbuz/android-mp3-sonic-ndk/blob/master/media/screenshot_main.png](https://github.com/mega-arbuz/android-mp3-sonic-ndk/blob/master/media/screenshot_main.png "Screenshot - Main")
