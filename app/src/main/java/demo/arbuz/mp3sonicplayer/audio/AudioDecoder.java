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
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioDecoder {

    private static final String AUDIO_MIME = "audio";

    private final static int  BUFFER_DEFAULT_SIZE = 4096;
    private final static long TIMEOUT_US          = 1000;

    private MediaExtractor mExtractor;
    private MediaCodec     mCodec;

    private int mBufferSize;

    private boolean mIsPlaying = false;
    private boolean mIsPaused  = false;

    private int mSampleRate;
    private int mNumOfChannels;

    public interface DecodedDataListener {

        void onDataReady(byte[] data);

        void onSampleRateChanged(int sampleRate);
    }

    public AudioDecoder(String filePath) throws IOException {
        mBufferSize = BUFFER_DEFAULT_SIZE;

        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(filePath);

        initExtractor();
    }

    public AudioDecoder(AssetFileDescriptor assetFileDescriptor) throws IOException {
        mBufferSize = BUFFER_DEFAULT_SIZE;

        mExtractor = new MediaExtractor();

        mExtractor.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(),
                assetFileDescriptor.getLength());

        initExtractor();
    }

    private void initExtractor() throws IOException {
        int audioTrackIndex = getAudioTrackIndex(mExtractor);

        MediaFormat format = mExtractor.getTrackFormat(audioTrackIndex);
        String mime = format.getString(MediaFormat.KEY_MIME);

        mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        mNumOfChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

        mCodec = MediaCodec.createDecoderByType(mime);

        // No surface, no crypto, used for decoding
        mCodec.configure(format, null, null, 0);

        mExtractor.selectTrack(audioTrackIndex);
    }

    public void setBufferSize(int bufferSize) {
        if (mIsPlaying)
        {
            throw new IllegalStateException("Can't change buffer size when playing");
        }
        mBufferSize = bufferSize;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getNumOfChannels() {
        return mNumOfChannels;
    }

    public long getElapsedTimeInMillis() {
        return mExtractor.getSampleTime() / 1000;
    }

    public void start(DecodedDataListener decodedDataListener) {
        mIsPlaying = true;

        mCodec.start();

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        ByteBuffer outputBuffer = null;

        int activeBufferIndex = 0;
        int availableOutBytes = 0;

        // Stores samples that will be written to audio track
        final byte[] dataBuffer = new byte[mBufferSize];
        int dataBufferFreeBytes = dataBuffer.length;

        boolean EOS = false;

        while (mIsPlaying)
        {

            // Pause decoding
            if (mIsPaused)
            {
                synchronized (this)
                {
                    try
                    {
                        wait();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            if (!EOS)
            {
                // Dequeue an input buffer
                int inIndex = mCodec.dequeueInputBuffer(TIMEOUT_US);
                if (inIndex >= 0)
                {
                    // Get buffer with stream data
                    ByteBuffer buffer = mCodec.getInputBuffer(inIndex);
                    if (buffer != null)
                    {
                        int sampleSize = mExtractor.readSampleData(buffer, 0);

                        // Pass the stream data to the codec
                        if (sampleSize < 0)
                        {
                            // No samples available, tell the codec that end of stream reached
                            mCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            EOS = true;
                        }
                        else
                        {
                            mCodec.queueInputBuffer(inIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);
                            mExtractor.advance();
                        }
                    }
                }
            }

            // Dequeue next output buffer when no bytes available for write
            if (availableOutBytes == 0)
            {
                activeBufferIndex = mCodec.dequeueOutputBuffer(info, TIMEOUT_US);

                if (activeBufferIndex >= 0)
                {
                    outputBuffer = mCodec.getOutputBuffer(activeBufferIndex);
                    availableOutBytes = info.size;

                    if (info.offset != 0)
                    {
                        throw new IllegalStateException("offset is not 0");
                    }
                }

                if (activeBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                {
                    mSampleRate = mCodec.getOutputFormat().getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    decodedDataListener.onSampleRateChanged(mSampleRate);
                }
            }

            // Get bytes from output buffer and write to data buffer
            if (outputBuffer != null && availableOutBytes > 0)
            {
                // Get all available bytes or enough to fill the buffer
                int length = Math.min(availableOutBytes, dataBufferFreeBytes);
                int offset = dataBuffer.length - dataBufferFreeBytes;
                outputBuffer.get(dataBuffer, offset, length);

                availableOutBytes -= length;
                dataBufferFreeBytes -= length;
            }

            // Write buffer to track when full
            if (dataBufferFreeBytes == 0)
            {
                decodedDataListener.onDataReady(dataBuffer);
                dataBufferFreeBytes = dataBuffer.length;
            }

            // Return buffer to codec when done reading all data
            if (outputBuffer != null && availableOutBytes == 0)
            {
                outputBuffer.clear();
                if (activeBufferIndex >= 0)
                {
                    mCodec.releaseOutputBuffer(activeBufferIndex, false);
                }
            }

            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
            {
                stop();
            }
        }

        mCodec.stop();
        mCodec.release();
        mExtractor.release();
    }

    public void stop() {
        mIsPlaying = false;
    }


    public void pause() {
        mIsPaused = true;
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public void resume() {
        mIsPaused = false;
        synchronized (this)
        {
            notify();
        }
    }

    private int getAudioTrackIndex(MediaExtractor extractor) {
        int audioTrackIndex = -1;
        for (int i = 0; i < extractor.getTrackCount(); i++)
        {
            // Select first track that has audio
            if (extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME).contains(AUDIO_MIME))
            {
                audioTrackIndex = i;
                break;
            }
        }
        return audioTrackIndex;
    }
}
