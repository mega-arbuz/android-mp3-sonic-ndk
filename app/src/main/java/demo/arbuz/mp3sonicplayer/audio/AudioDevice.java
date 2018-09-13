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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;

public class AudioDevice {

    private static final int BUFFER_MIN_MAGNITUDE = 4;

    private AudioTrack mAudioTrack;

    private int mBufferMinSize;

    // AudioTrack constructor is deprecated but the Builder is available only from SDK-23
    @SuppressWarnings("deprecation")
    public AudioDevice(int sampleRate, int numOfChannels) {
        if (numOfChannels < 0 || numOfChannels > 2)
        {
            throw new IllegalArgumentException("Invalid num of channels (1,2): " + numOfChannels);
        }

        int format = numOfChannels == 2 ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
        mBufferMinSize = BUFFER_MIN_MAGNITUDE * AudioTrack.getMinBufferSize(sampleRate, format, AudioFormat.ENCODING_PCM_16BIT);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                format,
                AudioFormat.ENCODING_PCM_16BIT,
                mBufferMinSize,
                AudioTrack.MODE_STREAM);
    }

    public int getBufferMinSize() {
        return mBufferMinSize;
    }

    public void play() {
        checkState();

        mAudioTrack.play();
    }

    public void write(@NonNull byte[] audioData, int sizeInBytes) {
        checkState();

        mAudioTrack.write(audioData, 0, sizeInBytes);
    }

    public void stop() {
        checkState();

        // Pause will stop playback immediately
        mAudioTrack.pause();
        // Discard data
        mAudioTrack.flush();
        // Stop and release
        mAudioTrack.stop();
        mAudioTrack.release();

        mAudioTrack = null;
    }

    public void setSampleRate(int sampleRate) {
        mAudioTrack.setPlaybackRate(sampleRate);
    }

    private void checkState() {
        if (mAudioTrack == null)
        {
            throw new IllegalStateException();
        }
    }
}
