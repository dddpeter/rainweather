package com.dddpeter.app.rainweather.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.dddpeter.app.rainweather.R;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private int moveSoundId;
    private int mergeSoundId;
    private int gameOverSoundId;
    private int gameWinSoundId;
    private boolean soundEnabled = true;
    
    private SoundManager(Context context) {
        initSoundPool(context);
    }
    
    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void initSoundPool(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        }
        
        // 加载音效资源（使用系统默认音效）
        loadSounds();
    }
    
    private void loadSounds() {
        // 由于我们没有实际的音效文件，这里暂时禁用音效加载
        // 在实际项目中，你需要将音效文件放在 res/raw/ 目录下
        // 例如：moveSoundId = soundPool.load(context, R.raw.move_sound, 1);
        
        // 暂时设置为0，表示没有加载音效
        moveSoundId = 0;
        mergeSoundId = 0;
        gameOverSoundId = 0;
        gameWinSoundId = 0;
    }
    
    public void playMoveSound() {
        if (soundEnabled && moveSoundId != 0) {
            soundPool.play(moveSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
        }
    }
    
    public void playMergeSound() {
        if (soundEnabled && mergeSoundId != 0) {
            soundPool.play(mergeSoundId, 0.7f, 0.7f, 1, 0, 1.0f);
        }
    }
    
    public void playGameOverSound() {
        if (soundEnabled && gameOverSoundId != 0) {
            soundPool.play(gameOverSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }
    
    public void playGameWinSound() {
        if (soundEnabled && gameWinSoundId != 0) {
            soundPool.play(gameWinSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
