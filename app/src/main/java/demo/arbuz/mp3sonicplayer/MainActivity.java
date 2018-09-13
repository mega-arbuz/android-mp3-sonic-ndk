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

package demo.arbuz.mp3sonicplayer;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import demo.arbuz.mp3sonicplayer.audio.AudioPlayer;

public class MainActivity extends AppCompatActivity {

    private static final long TIMER_REFRESH_MS = 50;

    private AudioPlayer mAudioPlayer;

    private TextView mTxtTimer;

    private TextInputLayout mInputSpeed;
    private TextInputLayout mInputPitch;
    private TextInputLayout mInputRate;

    private Button mBtnPlay;
    private Button mBtnPause;
    private Button mBtnResume;
    private Button mBtnStop;
    private Button mBtnSetParams;

    private RadioGroup mRadioGrpPlayer;
    private RadioGroup mRadioGrpFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtTimer = findViewById(R.id.txtTimer);

        mInputSpeed = findViewById(R.id.inputSpeed);
        mInputPitch = findViewById(R.id.inputPitch);
        mInputRate = findViewById(R.id.inputRate);

        mBtnPlay = findViewById(R.id.btnPlay);
        mBtnPause = findViewById(R.id.btnPause);
        mBtnResume = findViewById(R.id.btnResume);
        mBtnStop = findViewById(R.id.btnStop);
        mBtnSetParams = findViewById(R.id.btnSetParams);

        mRadioGrpPlayer = findViewById(R.id.radioGrpPlayer);
        mRadioGrpFile = findViewById(R.id.radioGrpFile);

        mBtnPlay.setOnClickListener(v -> play());
        mBtnPause.setOnClickListener(v -> pause());
        mBtnResume.setOnClickListener(v -> resume());
        mBtnStop.setOnClickListener(v -> stop());
        mBtnSetParams.setOnClickListener(v -> setPlaybackParameters());

        findViewById(R.id.radioSimple).setOnClickListener(v -> updateUI());
        findViewById(R.id.radioSonic).setOnClickListener(v -> updateUI());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
    }

    private void play() {
        if (mAudioPlayer != null)
        {
            return;
        }

        mAudioPlayer = new AudioPlayer(getPlayerType());

        try
        {
            // Possible to play files from phone storage (need to add permission for reading storage)
            // mAudioPlayer.play("/sdcard/alice.mp3");
            mAudioPlayer.play(getResources().openRawResourceFd(getFileResource()), () -> runOnUiThread(() -> {
                // Do something when track playback ended
                mTxtTimer.setText(R.string.timer);
                mAudioPlayer = null;
                updateUI();
            }));

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        setPlaybackParameters();

        // Start thread that will update track time
        new Thread(() -> {
            while (mAudioPlayer != null && mAudioPlayer.isPlaying())
            {
                try
                {
                    String time = new SimpleDateFormat("mm:ss.SS", Locale.US).format(mAudioPlayer.getElapsedTimeInMillis());
                    runOnUiThread(() -> mTxtTimer.setText(time));
                } catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }

                try
                {
                    Thread.sleep(TIMER_REFRESH_MS);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

        updateUI();
    }

    private void pause() {
        if (mAudioPlayer != null)
        {
            mAudioPlayer.pause();
        }

        updateUI();
    }

    private void resume() {
        if (mAudioPlayer != null)
        {
            mAudioPlayer.resume();
        }

        updateUI();
    }

    private void stop() {
        if (mAudioPlayer != null)
        {
            mAudioPlayer.stop();
        }

        updateUI();
    }

    private void setPlaybackParameters() {
        if (mAudioPlayer != null)
        {
            try
            {
                float speed = Float.parseFloat(mInputSpeed.getEditText().getText().toString());
                float pitch = Float.parseFloat(mInputPitch.getEditText().getText().toString());
                float rate = Float.parseFloat(mInputRate.getEditText().getText().toString());

                mAudioPlayer.setSpeed(speed);
                mAudioPlayer.setPitch(pitch);
                mAudioPlayer.setRate(rate);
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
        }
    }

    private int getPlayerType() {
        int selected = mRadioGrpPlayer.getCheckedRadioButtonId();
        switch (selected)
        {
            case R.id.radioSimple:
                return AudioPlayer.PLAYER_TYPE_SIMPLE;
            case R.id.radioSonic:
                return AudioPlayer.PLAYER_TYPE_SONIC;
            default:
                return AudioPlayer.PLAYER_TYPE_SONIC;
        }
    }

    private int getFileResource() {
        int selected = mRadioGrpFile.getCheckedRadioButtonId();
        switch (selected)
        {
            case R.id.radioLong:
                return R.raw.alice_long;
            case R.id.radioShort:
                return R.raw.alice_short;
            default:
                return R.raw.alice_short;
        }
    }

    private void updateUI() {
        mBtnPlay.setEnabled(mAudioPlayer == null || !mAudioPlayer.isPlaying());
        mBtnStop.setEnabled(mAudioPlayer != null && mAudioPlayer.isPlaying());
        mBtnPause.setEnabled(mAudioPlayer != null && !mAudioPlayer.isPaused());
        mBtnResume.setEnabled(mAudioPlayer != null && mAudioPlayer.isPaused());
        mBtnSetParams.setEnabled(getPlayerType() == AudioPlayer.PLAYER_TYPE_SONIC);
    }
}
