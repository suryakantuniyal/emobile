package com.android.soundmanager;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import com.android.emobilepos.R;

import java.util.HashMap;


public class SoundManager {
    Context mContext;
    //    private SoundManager _instance;
    private SoundPool mSoundPool;
    private HashMap<Integer, Integer> mSoundPoolMap;
    private AudioManager mAudioManager;

    /**
     * Requests the instance of the Sound Manager and creates it if it does not
     * exist.
     *
     * @return Returns the single instance of the SoundManager
     */
    static synchronized public SoundManager getInstance() {
        return new SoundManager();
    }

    /**
     * Initialises the storage for the sounds
     *
     * @param theContext The Application context
     */
    public void initSounds(Context theContext) {
        mContext = theContext;
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSoundPoolMap = new HashMap<>();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Add a new Sound to the SoundPool
     *
     * @param Index   - The Sound Index for Retrieval
     * @param SoundID - The Android ID for the Sound asset.
     */
    public void addSound(int Index, int SoundID) {
        mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));
    }

    /**
     * Loads the various sound assets Currently hardcoded but could easily be
     * changed to be flexible.
     */
    public void loadSounds() {
        mSoundPoolMap.put(1, mSoundPool.load(mContext, R.raw.beep, 1));
        mSoundPoolMap.put(2, mSoundPool.load(mContext, R.raw.buzz, 1));
    }

    /**
     * Plays a Sound
     *
     * @param index - The Index of the Sound to be played
     * @param speed - The Speed to play not, not currently used but included for
     *              compatibility
     */
    public void playSound(int index, float speed) {
        float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (mSoundPool != null && mSoundPoolMap != null) {
            mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, speed);
        }
    }

    /**
     * Stop a Sound
     *
     * @param index - index of the sound to be stopped
     */
    public void stopSound(int index) {
        mSoundPool.stop(mSoundPoolMap.get(index));
    }

    public void cleanup() {
        if (mSoundPool != null)
            mSoundPool.release();
        mSoundPool = null;
        mSoundPoolMap.clear();
        mAudioManager.unloadSoundEffects();

    }

    public void playVibator(Context context, long timelong) {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vib.vibrate(timelong);
    }
}