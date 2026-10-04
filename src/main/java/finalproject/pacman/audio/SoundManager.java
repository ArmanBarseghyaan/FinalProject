package finalproject.pacman.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

/**
 * Загружает и воспроизводит только короткие игровые звуковые эффекты.
 * Музыкальные дорожки намеренно не используются.
 */
public final class SoundManager implements AutoCloseable {
    private static final String RESOURCE_DIRECTORY = "/finalproject/pacman/sounds/";

    public enum Sound {
        PELLET("pellet.wav"),
        ENERGIZER("energizer.wav"),
        GHOST_EATEN("ghost-eaten.wav"),
        DEATH("death.wav");

        private final String fileName;

        Sound(String fileName) {
            this.fileName = fileName;
        }
    }

    private final Map<Sound, Clip> clips = new EnumMap<>(Sound.class);
    private final Map<Sound, Integer> pausedPositions = new EnumMap<>(Sound.class);
    private boolean sessionActive;
    private boolean paused;
    private boolean closed;

    public SoundManager() {
        for (Sound sound : Sound.values()) {
            load(sound);
        }
    }

    private void load(Sound sound) {
        InputStream input = SoundManager.class.getResourceAsStream(
                RESOURCE_DIRECTORY + sound.fileName);
        try {
            if (input == null) {
                Path file = Paths.get("finalproject", "pacman", "sounds", sound.fileName);
                if (!Files.isRegularFile(file)) {
                    return;
                }
                input = Files.newInputStream(file);
            }

            try (InputStream soundInput = input;
                 AudioInputStream audio = AudioSystem.getAudioInputStream(soundInput)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                clips.put(sound, clip);
            }
        } catch (IOException | UnsupportedAudioFileException
                 | LineUnavailableException | IllegalArgumentException exception) {
            closeClip(sound);
        }
    }

    public void startGame() {
        if (closed) {
            return;
        }
        sessionActive = true;
        paused = false;
        pausedPositions.clear();
    }

    public void pause() {
        if (closed || paused || !sessionActive) {
            return;
        }
        paused = true;
        pausedPositions.clear();
        for (Map.Entry<Sound, Clip> entry : clips.entrySet()) {
            Clip clip = entry.getValue();
            if (clip.isRunning()) {
                pausedPositions.put(entry.getKey(), clip.getFramePosition());
                clip.stop();
            }
        }
    }

    public void resume() {
        if (closed || !paused || !sessionActive) {
            return;
        }
        paused = false;
        for (Map.Entry<Sound, Integer> entry : pausedPositions.entrySet()) {
            Clip clip = clips.get(entry.getKey());
            if (clip != null) {
                clip.setFramePosition(entry.getValue());
                clip.start();
            }
        }
        pausedPositions.clear();
    }

    public void playPellet() {
        playEffect(Sound.PELLET);
    }

    public void playEnergizer() {
        playEffect(Sound.ENERGIZER);
    }

    public void playGhostEaten() {
        playEffect(Sound.GHOST_EATEN);
    }

    public void playDeath() {
        playEffect(Sound.DEATH);
    }

    public void stopAll() {
        sessionActive = false;
        pausedPositions.clear();
        for (Clip clip : clips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
        }
    }

    private void playEffect(Sound sound) {
        Clip clip = clips.get(sound);
        if (clip == null || closed || paused || !sessionActive) {
            return;
        }
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private void closeClip(Sound sound) {
        Clip clip = clips.remove(sound);
        if (clip != null) {
            clip.close();
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        stopAll();
        for (Clip clip : clips.values()) {
            clip.close();
        }
        clips.clear();
    }
}
