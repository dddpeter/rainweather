package com.dddpeter.app.rainweather.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import com.dddpeter.app.rainweather.R;

public class SoundManager {
    private static SoundManager instance;
    private SoundPool soundPool;
    private Context context;
    
    // 音效ID
    private int moveSoundId;
    private int mergeSoundId;
    private int newTileSoundId;
    private int gameOverSoundId;
    private int gameWinSoundId;
    
    // 音效控制
    private boolean soundEnabled = true;
    
    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initSoundPool();
    }
    
    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }
    
    private void initSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        
        // 加载音效资源
        loadSounds();
    }
    
    
    private void loadSounds() {
        try {
            // 加载raw文件夹中的音效文件
            moveSoundId = soundPool.load(context, R.raw.move, 1);
            mergeSoundId = soundPool.load(context, R.raw.merge, 1);
            newTileSoundId = soundPool.load(context, R.raw.new_tile, 1);
            gameOverSoundId = soundPool.load(context, R.raw.game_over, 1);
            gameWinSoundId = soundPool.load(context, R.raw.win, 1);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果加载失败，设置为0
            moveSoundId = 0;
            mergeSoundId = 0;
            newTileSoundId = 0;
            gameOverSoundId = 0;
            gameWinSoundId = 0;
        }
    }
    
    
    public void playMoveSound() {
        if (soundEnabled && !isDeviceMuted() && moveSoundId != 0) {
            soundPool.play(moveSoundId, 0.5f, 0.5f, 1, 0, 1.0f);
        }
    }
    
    public void playMergeSound() {
        if (soundEnabled && !isDeviceMuted() && mergeSoundId != 0) {
            soundPool.play(mergeSoundId, 0.7f, 0.7f, 1, 0, 1.0f);
        }
    }
    
    public void playGameOverSound() {
        if (soundEnabled && !isDeviceMuted() && gameOverSoundId != 0) {
            soundPool.play(gameOverSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }
    
    public void playGameWinSound() {
        if (soundEnabled && !isDeviceMuted() && gameWinSoundId != 0) {
            soundPool.play(gameWinSoundId, 0.8f, 0.8f, 1, 0, 1.0f);
        }
    }
    
    public void playNewTileSound() {
        if (soundEnabled && !isDeviceMuted() && newTileSoundId != 0) {
            soundPool.play(newTileSoundId, 0.6f, 0.6f, 1, 0, 1.0f);
        }
    }
    
    // 音效控制
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * 检查设备是否处于静音模式（公开方法）
     */
    public boolean isDeviceMuted() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int ringerMode = audioManager.getRingerMode();
            return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
        }
        return false;
    }
    
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
