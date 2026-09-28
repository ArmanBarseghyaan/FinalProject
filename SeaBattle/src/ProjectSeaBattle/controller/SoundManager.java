package com.seabattle.controller;

import com.seabattle.audio.SoundGenerator;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Управление аудиосистемой: эффекты выстрелов, всплесков, взрывов и выбираемая фоновая музыка.
 * Три трека: Travis Scott — FE!N, Eminem — Lose Yourself, Eminem — Superman.
 */
public class SoundManager {
    private static final SoundManager INSTANCE = new SoundManager();

    public enum Soundtrack {
        FEIN("Travis Scott — FE!N", "fein.wav"),
        LOSE_YOURSELF("Eminem — Lose Yourself", "lose_yourself.wav"),
        SUPERMAN("Eminem — Superman", "superman.wav"),
        OFF("Без музыки", null);

        public final String title;
        public final String fileName;

        Soundtrack(String title, String fileName) {
            this.title = title;
            this.fileName = fileName;
        }
    }

    private boolean soundEnabled = true;
    private Clip bgMusicClip;
    private Soundtrack currentSoundtrack = Soundtrack.OFF;
    private byte[] splashData;
    private byte[] explosionData;

    private SoundManager() {
        try {
            splashData = SoundGenerator.generateSplashSound();
            explosionData = SoundGenerator.generateExplosionSound();
        } catch (Exception e) {
            System.err.println("Аудио-инициализация: " + e.getMessage());
        }
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    /** Запустить выбранный саундтрек (вызывается после выбора в диалоге). */
    public void setSoundtrack(Soundtrack track) {
        stopBackgroundMusic();
        if (bgMusicClip != null) {
            bgMusicClip.close();
            bgMusicClip = null;
        }
        currentSoundtrack = track;
        if (track == Soundtrack.OFF || !soundEnabled) return;
        loadAndPlayTrack(track);
    }

    private void loadAndPlayTrack(Soundtrack track) {
        try {
            String base = System.getProperty("user.dir", ".");
            String[] paths = {
                track.fileName,
                base + "/" + track.fileName,
                base + "/src/" + track.fileName,
                base + "/out/production/SeaBattle/" + track.fileName,
                "C:/Users/USER/Desktop/aaa/SeaBattle/" + track.fileName,
                "C:/Users/USER/Desktop/aaa/SeaBattle/src/" + track.fileName,
                "C:/Users/USER/Desktop/aaa/SeaBattle/src/ProjectSeaBattle/" + track.fileName
            };

            boolean loaded = false;
            for (String path : paths) {
                File f = new File(path);
                if (f.exists()) {
                    try {
                        AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                        bgMusicClip = AudioSystem.getClip();
                        bgMusicClip.open(ais);
                        loaded = true;
                        break;
                    } catch (Exception ignored) {}
                }
            }

            if (!loaded) {
                // Процедурная генерация если WAV не найден
                byte[] data;
                switch (track) {
                    case FEIN: data = SoundGenerator.generateTravisScottFeinTrack(); break;
                    case LOSE_YOURSELF: data = SoundGenerator.generateEminemLoseYourselfTrack(); break;
                    case SUPERMAN: data = SoundGenerator.generateEminemSupermanTrack(); break;
                    default: return;
                }
                AudioFormat format = SoundGenerator.getStandardFormat();
                bgMusicClip = AudioSystem.getClip();
                bgMusicClip.open(format, data, 0, data.length);
            }

            bgMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Ошибка загрузки трека: " + e.getMessage());
        }
    }

    public Soundtrack getCurrentSoundtrack() { return currentSoundtrack; }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) stopBackgroundMusic();
        else if (currentSoundtrack != Soundtrack.OFF) loadAndPlayTrack(currentSoundtrack);
    }

    public void toggleSound() { setSoundEnabled(!soundEnabled); }

    public void playMissSound() {
        if (!soundEnabled || splashData == null) return;
        playSoundEffect(splashData);
    }

    public void playHitSound() {
        if (!soundEnabled || explosionData == null) return;
        playSoundEffect(explosionData);
    }

    public void playSunkSound() {
        if (!soundEnabled || explosionData == null) return;
        playSoundEffect(explosionData);
    }

    private void playSoundEffect(byte[] data) {
        new Thread(() -> {
            try {
                AudioFormat format = SoundGenerator.getStandardFormat();
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Exception ignored) {}
        }).start();
    }

    public void startBackgroundMusic() {
        if (!soundEnabled || bgMusicClip == null) return;
        try {
            if (!bgMusicClip.isRunning()) {
                bgMusicClip.setFramePosition(0);
                bgMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception ignored) {}
    }

    public void stopBackgroundMusic() {
        if (bgMusicClip != null && bgMusicClip.isRunning()) bgMusicClip.stop();
    }
}
