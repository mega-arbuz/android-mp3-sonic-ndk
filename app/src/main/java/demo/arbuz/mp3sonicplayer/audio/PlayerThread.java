package demo.arbuz.mp3sonicplayer.audio;

public class PlayerThread extends Thread {

    private AudioDecoder                       mAudioDecoder;
    private AudioDevice                        mAudioDevice;
    private AudioPlayer.OnAudioStoppedListener mOnAudioStoppedListener;

    public PlayerThread(AudioDecoder audioDecoder, AudioDevice audioDevice, AudioPlayer.OnAudioStoppedListener onAudioStoppedListener) {
        mAudioDevice = audioDevice;
        mAudioDecoder = audioDecoder;
        mOnAudioStoppedListener = onAudioStoppedListener;
    }

    @Override
    public void run() {
        mAudioDevice.play();

        mAudioDecoder.setBufferSize(mAudioDevice.getBufferMinSize());

        // Runs synchronous loop that decodes mp3 and sends events with decoded data
        // The data is immediately written to AudioTrack
        mAudioDecoder.start(new AudioDecoder.DecodedDataListener() {

            @Override
            public void onDataReady(byte[] data) {
                mAudioDevice.write(data, data.length);
            }

            @Override
            public void onSampleRateChanged(int sampleRate) {
                mAudioDevice.setSampleRate(sampleRate);
            }
        });

        mAudioDevice.stop();

        mAudioDecoder = null;
        mAudioDevice = null;

        if (mOnAudioStoppedListener != null)
        {
            mOnAudioStoppedListener.onAudioStopped();
        }
    }

    void setSpeed(float speed) {
        ((SonicAudioDevice) mAudioDevice).setSpeed(speed);
    }

    void setPitch(float pitch) {
        ((SonicAudioDevice) mAudioDevice).setPitch(pitch);
    }

    void setRate(float rate) {
        ((SonicAudioDevice) mAudioDevice).setRate(rate);
    }

    void stopPlayback() {
        mAudioDecoder.stop();
    }

    void pausePlayback() {
        mAudioDecoder.pause();
    }

    void resumePlayback() {
        mAudioDecoder.resume();
    }

    long getElapsedTimeInMillis() {
        return mAudioDecoder == null ? 0 : mAudioDecoder.getElapsedTimeInMillis();
    }

    boolean isPlaying() {
        return mAudioDecoder != null;
    }

    boolean isPaused() {
        return mAudioDecoder != null && mAudioDecoder.isPaused();
    }
}