/*
 * Copyright (C) 2018 Oleg Shnaydman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.arbuz.mp3sonicplayer.audio;

import android.content.res.AssetFileDescriptor;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * Simple class for playing extracted audio from codec (PCM), using one thread {@link PlayerThread}
 * <p>
 * Class can use {@link AudioDevice} that simply writes PCM to Android AudioTrack or {@link SonicAudioDevice}
 * that passes the data through Sonic buffer before writing to AudioTrack and makes it possible to change speed, pitch and rate.
 * <p>
 * The purpose of this class is to be a simple example for audio player. This shouldn't be used as fully working player :)
 */
public class AudioPlayer {

    // Use simple AudioTrack
    public static final int PLAYER_TYPE_SIMPLE = 0;
    // Use AudioTrack with Sonic
    public static final int PLAYER_TYPE_SONIC  = 1;

    private int mPlayerType;

    private PlayerThread mPlayerThread;

    public interface OnAudioStoppedListener {
        void onAudioStopped();
    }

    public AudioPlayer(int type) {
        if (type != PLAYER_TYPE_SIMPLE &&
                type != PLAYER_TYPE_SONIC)
        {
            throw new IllegalArgumentException("Illegal type " + type);
        }

        mPlayerType = type;
    }

    /**
     * Play from Assets raw file
     */
    public void play(AssetFileDescriptor assetFileDescriptor, @Nullable OnAudioStoppedListener onAudioStoppedListener) throws IOException {
        if (mPlayerThread != null)
        {
            throw new IllegalStateException("Stop before playing again");
        }

        AudioDecoder audioDecoder = new AudioDecoder(assetFileDescriptor);
        AudioDevice audioDevice = createAudioDevice(mPlayerType, audioDecoder);

        startPlayerThread(audioDecoder, audioDevice, onAudioStoppedListener);
    }

    /**
     * Play from a file path in phone storage
     */
    public void play(String filePath, @Nullable OnAudioStoppedListener onAudioStoppedListener) throws IOException {
        if (mPlayerThread != null)
        {
            throw new IllegalStateException("Stop before playing again");
        }

        AudioDecoder audioDecoder = new AudioDecoder(filePath);
        AudioDevice audioDevice = createAudioDevice(mPlayerType, audioDecoder);

        startPlayerThread(audioDecoder, audioDevice, onAudioStoppedListener);
    }

    private AudioDevice createAudioDevice(int playerType, AudioDecoder audioDecoder) {
        switch (playerType)
        {
            case PLAYER_TYPE_SONIC:
                return new SonicAudioDevice(audioDecoder.getSampleRate(), audioDecoder.getNumOfChannels());
            case PLAYER_TYPE_SIMPLE:
                return new AudioDevice(audioDecoder.getSampleRate(), audioDecoder.getNumOfChannels());
        }

        return null;
    }

    private void startPlayerThread(AudioDecoder audioDecoder,
                                   AudioDevice audioDevice,
                                   @Nullable OnAudioStoppedListener onAudioStoppedListener) {
        mPlayerThread = new PlayerThread(audioDecoder, audioDevice, onAudioStoppedListener);
        mPlayerThread.start();
    }

    public boolean isSpeedChangeSupported() {
        return mPlayerType == PLAYER_TYPE_SONIC;
    }

    public boolean isPlaying() {
        return mPlayerThread != null && mPlayerThread.isPlaying();
    }

    public boolean isPaused() {
        return mPlayerThread != null && mPlayerThread.isPaused();
    }

    public void setSpeed(float speed) {
        checkState();

        if (mPlayerType == PLAYER_TYPE_SONIC)
        {
            mPlayerThread.setSpeed(speed);
        }
    }

    public void setPitch(float pitch) {
        checkState();

        if (mPlayerType == PLAYER_TYPE_SONIC)
        {
            mPlayerThread.setPitch(pitch);
        }
    }

    public void setRate(float rate) {
        checkState();

        if (mPlayerType == PLAYER_TYPE_SONIC)
        {
            mPlayerThread.setRate(rate);
        }
    }

    public void stop() {
        checkState();

        mPlayerThread.stopPlayback();
        mPlayerThread = null;
    }

    public void pause() {
        checkState();

        mPlayerThread.pausePlayback();
    }

    public void resume() {
        checkState();

        mPlayerThread.resumePlayback();
    }

    public long getElapsedTimeInMillis() {
        checkState();

        return mPlayerThread.getElapsedTimeInMillis();
    }

    private void checkState() {
        if (mPlayerThread == null)
        {
            throw new IllegalStateException();
        }
    }
}
