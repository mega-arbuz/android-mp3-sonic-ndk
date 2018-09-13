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

import android.support.annotation.NonNull;

import org.vinuxproject.sonic.Sonic;

public class SonicAudioDevice extends AudioDevice {

    private static final int SONIC_SAMPLES_SIZE = 2048;

    private Sonic mSonic;

    private byte mModifiedSamples[];

    public SonicAudioDevice(int sampleRate, int numOfChannels) {
        super(sampleRate, numOfChannels);

        mSonic = new Sonic(sampleRate, numOfChannels);

        mModifiedSamples = new byte[SONIC_SAMPLES_SIZE];

        setSpeed(1.0f);
        setPitch(1.0f);
        setRate(1.0f);
    }

    public void setSpeed(float speed) {
        checkState();

        mSonic.setSpeed(speed);
    }

    public void setPitch(float pitch) {
        checkState();

        mSonic.setPitch(pitch);
    }

    public void setRate(float rate) {
        checkState();

        mSonic.setRate(rate);
    }

    public void setSampleRate(int sampleRate) {
        super.setSampleRate(sampleRate);
        mSonic.setSampleRate(sampleRate);
    }

    public void write(@NonNull byte[] audioData, int sizeInBytes) {
        // Before writing PCM data to audio track, pass it through Sonic buffer
        // Consider using different thread for reading from Sonic buffer
        if (audioData.length > 0)
        {
            mSonic.putBytes(audioData, audioData.length);
        }

        int available = mSonic.availableBytes();
        if (available > 0)
        {
            if (mModifiedSamples.length < available)
            {
                mModifiedSamples = new byte[available * 2];
            }
            mSonic.receiveBytes(mModifiedSamples, available);
            super.write(mModifiedSamples, available);
        }
    }

    @Override
    public void stop() {
        super.stop();

        checkState();

        mSonic.flush();
        mSonic = null;
        mModifiedSamples = null;
    }

    private void checkState() {
        if (mSonic == null) {
            throw new IllegalStateException();
        }
    }
}
