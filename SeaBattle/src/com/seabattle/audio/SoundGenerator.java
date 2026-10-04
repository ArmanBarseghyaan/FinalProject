package com.seabattle.audio;

import javax.sound.sampled.AudioFormat;
import java.util.Random;

/**
 * Синтезатор PCM-звуков: реалистичный всплеск воды, взрыв и три саундтрека.
 */
public class SoundGenerator {
    public static final float SAMPLE_RATE = 44100.0f;
    private static final Random RANDOM = new Random();

    public static AudioFormat getStandardFormat() {
        return new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
    }

    // ─────────────────────────────────────────────────
    //  РЕАЛИСТИЧНЫЙ ВСПЛЕСК ВОДЫ (промах)
    // ─────────────────────────────────────────────────
    public static byte[] generateSplashSound() {
        int durationMs = 550;
        int numSamples = (int) (SAMPLE_RATE * durationMs / 1000);
        byte[] pcm = new byte[numSamples * 2];
        Random rnd = new Random(7);

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = (double) i / numSamples;

            // 1. Удар: короткий низкочастотный «шлепок»
            double impact = 0;
            if (t < 0.035) {
                double impFreq = 280.0 - 180.0 * (t / 0.035);
                impact = Math.sin(2.0 * Math.PI * impFreq * t) * Math.exp(-60 * t) * 0.9;
            }

            // 2. Белый шум пузырей (кавитация)
            double noise = (rnd.nextDouble() * 2.0 - 1.0);
            double bubbleEnv = Math.exp(-9.0 * t) * 0.5;

            // 3. Резонанс жидкости — нисходящий тон
            double resonFreq = 500.0 - 350.0 * progress;
            double resonance = Math.sin(2.0 * Math.PI * resonFreq * t) * Math.exp(-5.0 * t) * 0.35;

            // 4. Вторичные капли
            double drops = 0;
            double[] dropTimes = {0.08, 0.14, 0.22, 0.31};
            for (double dt : dropTimes) {
                if (t >= dt && t < dt + 0.04) {
                    double lt = t - dt;
                    drops += Math.sin(2.0 * Math.PI * 900.0 * lt) * Math.exp(-80.0 * lt) * 0.3;
                }
            }

            double mix = impact + noise * bubbleEnv + resonance + drops;
            // Фейд-аут
            if (i > numSamples - 2000) mix *= (double)(numSamples - i) / 2000.0;
            mix = Math.max(-0.95, Math.min(0.95, mix));

            short s = (short)(mix * 28000);
            pcm[2*i]   = (byte)(s & 0xFF);
            pcm[2*i+1] = (byte)((s >> 8) & 0xFF);
        }
        return pcm;
    }

    // ─────────────────────────────────────────────────
    //  ВЗРЫВ (попадание)
    // ─────────────────────────────────────────────────
    public static byte[] generateExplosionSound() {
        int durationMs = 650;
        int numSamples = (int) (SAMPLE_RATE * durationMs / 1000);
        byte[] pcm = new byte[numSamples * 2];
        Random rnd = new Random(13);

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double noise = (rnd.nextDouble() * 2.0 - 1.0);
            double boom  = Math.sin(2.0 * Math.PI * 80.0 * t);
            double mix   = noise * 0.7 + boom * 0.3;
            double env   = Math.exp(-4.5 * t);
            short  s     = (short)(mix * 22000 * env);
            pcm[2*i]   = (byte)(s & 0xFF);
            pcm[2*i+1] = (byte)((s >> 8) & 0xFF);
        }
        return pcm;
    }

    // ─────────────────────────────────────────────────
    //  TRAVIS SCOTT — FE!N
    // ─────────────────────────────────────────────────
    public static byte[] generateTravisScottFeinTrack() {
        double bpm = 148.0;
        double beat = 60.0 / bpm;
        int totalBeats = 64;
        int numSamples = (int)(SAMPLE_RATE * totalBeats * beat);
        byte[] pcm = new byte[numSamples * 2];

        double[][] notes = {
            {0,1.4,311.13,311.13,0.8},{1.5,2.4,466.16,466.16,0.9},{2.5,3.2,415.30,415.30,0.85},{3.25,3.9,369.99,369.99,0.8},
            {4,5.4,349.23,349.23,0.8},{5.5,6.4,311.13,311.13,0.85},{6.5,7.3,311.13,311.13,0.85},{7.4,7.95,466.16,415.30,0.9},
            {8,9.4,311.13,311.13,0.85},{9.5,10.4,466.16,466.16,0.9},{10.5,11.2,415.30,415.30,0.85},{11.25,11.9,493.88,493.88,0.95},
            {12,12.9,466.16,466.16,0.85},{13,13.9,415.30,415.30,0.8},{14,14.9,369.99,369.99,0.8},{15,15.9,349.23,349.23,0.8},
            {16,17.4,311.13,311.13,0.9},{17.5,18.4,466.16,466.16,0.95},{18.5,19.2,415.30,415.30,0.9},{19.25,19.9,369.99,369.99,0.85},
            {20,21.4,349.23,349.23,0.85},{21.5,22.4,311.13,311.13,0.9},{22.5,23.3,311.13,311.13,0.9},{23.4,23.95,466.16,415.30,0.95},
            {24,25.4,311.13,311.13,0.9},{25.5,26.4,466.16,466.16,0.95},{26.5,27.2,415.30,415.30,0.9},{27.25,27.9,493.88,493.88,1.0},
            {28,28.9,466.16,466.16,0.9},{29,29.9,415.30,415.30,0.85},{30,30.9,369.99,369.99,0.85},{31,31.9,349.23,311.13,0.85},
            {32,33.4,311.13,311.13,0.9},{33.5,34.4,466.16,466.16,0.95},{34.5,35.2,415.30,415.30,0.9},{35.25,35.9,369.99,369.99,0.85},
            {36,37.4,349.23,349.23,0.85},{37.5,38.4,311.13,311.13,0.9},{38.5,39.3,311.13,311.13,0.9},{39.4,39.95,466.16,415.30,0.95},
            {40,41.4,311.13,311.13,0.9},{41.5,42.4,466.16,466.16,0.95},{42.5,43.2,415.30,415.30,0.9},{43.25,43.9,493.88,493.88,1.0},
            {44,44.9,466.16,466.16,0.9},{45,45.9,415.30,415.30,0.85},{46,46.9,369.99,369.99,0.85},{47,47.9,349.23,349.23,0.85},
            {48,49.4,311.13,311.13,0.9},{49.5,50.4,466.16,466.16,0.95},{50.5,51.2,415.30,415.30,0.9},{51.25,51.9,369.99,369.99,0.85},
            {52,53.4,349.23,349.23,0.85},{53.5,54.4,311.13,311.13,0.9},{54.5,55.3,311.13,311.13,0.9},{55.4,55.95,466.16,415.30,0.95},
            {56,57.4,311.13,311.13,0.9},{57.5,58.4,466.16,466.16,0.95},{58.5,59.2,415.30,415.30,0.9},{59.25,59.9,493.88,493.88,1.0},
            {60,60.9,466.16,466.16,0.9},{61,61.9,415.30,415.30,0.85},{62,62.9,369.99,369.99,0.85},{63,63.9,349.23,311.13,0.85}
        };

        double[][] bass = {
            {0,3.5,38.89},{4,3.5,38.89},{8,3.5,30.87},{12,3.5,29.14},
            {16,3.5,38.89},{20,3.5,38.89},{24,3.5,30.87},{28,3.5,34.65},
            {32,3.5,38.89},{36,3.5,38.89},{40,3.5,30.87},{44,3.5,29.14},
            {48,3.5,38.89},{52,3.5,38.89},{56,3.5,30.87},{60,3.5,34.65}
        };

        double p1=0,p2=0,bp=0;
        Random rnd = new Random(42);

        for (int i = 0; i < numSamples; i++) {
            double t = (double)i / SAMPLE_RATE;
            double cb = t / beat;

            double lead = 0;
            for (double[] n : notes) {
                if (cb >= n[0] && cb < n[1]) {
                    double prog = (cb - n[0]) / (n[1] - n[0]);
                    double freq = n[2] + (n[3]-n[2]) * prog;
                    p1 += freq / SAMPLE_RATE; p2 += freq*1.002 / SAMPLE_RATE;
                    double s1 = 2*(p1 - Math.floor(p1+0.5));
                    double s2 = 2*(p2 - Math.floor(p2+0.5));
                    double pulse = Math.sin(2*Math.PI*p1) > 0.1 ? 0.6 : -0.6;
                    double nt = (cb - n[0]) * beat;
                    double env = Math.min(1.0, nt/0.012) * Math.max(0.65, Math.exp(-0.7*nt));
                    lead = Math.tanh((s1*0.4+s2*0.35+pulse*0.25)*2.0)*env*n[4]*0.45;
                    break;
                }
            }

            double bassS = 0;
            for (double[] b : bass) {
                if (cb >= b[0] && cb < b[0]+b[1]) {
                    double ht = (cb-b[0])*beat;
                    double bf = b[2] + (ht<0.045 ? 75*(1-ht/0.045) : 0);
                    bp += bf/SAMPLE_RATE;
                    double bw = Math.sin(2*Math.PI*bp);
                    double bsat = Math.sin(2*Math.PI*bp*2)*0.25;
                    bassS = Math.tanh((bw+bsat)*2.4)*Math.exp(-0.70*ht)*0.50;
                    break;
                }
            }

            double drum = 0;
            double barPos = cb % 4.0;
            double hatT = (cb % 0.25) * beat;
            if (hatT < 0.035) drum += (rnd.nextDouble()*2-1)*Math.exp(-95*hatT)*0.18;
            double snDist = Math.abs(barPos - 2.0);
            if (snDist*beat < 0.16) {
                double st = snDist*beat;
                drum += ((rnd.nextDouble()*2-1)*0.75 + Math.sin(2*Math.PI*230*st)*0.25) * Math.exp(-25*st) * 0.40;
            }

            double mix = lead*0.45 + bassS*0.42 + drum*0.26;
            if (i < 800) mix *= i/800.0;
            else if (i > numSamples-800) mix *= (double)(numSamples-i)/800.0;
            mix = Math.max(-0.95, Math.min(0.95, mix));
            short s = (short)(mix * 32767);
            pcm[2*i]   = (byte)(s & 0xFF);
            pcm[2*i+1] = (byte)((s>>8)&0xFF);
        }
        return pcm;
    }

    // ─────────────────────────────────────────────────
    //  EMINEM — LOSE YOURSELF (86 BPM, Dm, пианино+рэп)
    // ─────────────────────────────────────────────────
    public static byte[] generateEminemLoseYourselfTrack() {
        double bpm = 86.0;
        double beat = 60.0 / bpm;
        int totalBeats = 64;
        int numSamples = (int)(SAMPLE_RATE * totalBeats * beat);
        byte[] pcm = new byte[numSamples * 2];

        // Культовый пианинный рифф Dm: D-C-A#-A-G-F
        double[] pianoFreqs = {293.66, 261.63, 233.08, 220.00, 196.00, 174.61, 196.00, 220.00};

        Random rnd = new Random(11);
        double pianoPhase = 0;

        for (int i = 0; i < numSamples; i++) {
            double t = (double)i / SAMPLE_RATE;
            double cb = t / beat;

            // Пианино: 8-нотный цикличный паттерн
            int noteIdx = (int)(cb * 2) % pianoFreqs.length;
            double noteStart = Math.floor(cb * 2) / 2.0;
            double noteTime = t - noteStart * beat;

            double freq = pianoFreqs[noteIdx];
            pianoPhase += freq / SAMPLE_RATE;

            double harmonic1 = Math.sin(2*Math.PI*pianoPhase);
            double harmonic2 = Math.sin(2*Math.PI*pianoPhase*2) * 0.45;
            double harmonic3 = Math.sin(2*Math.PI*pianoPhase*3) * 0.20;
            double pianoEnv = Math.exp(-3.5 * noteTime) * Math.min(1.0, noteTime / 0.008);
            double pianoS = (harmonic1 + harmonic2 + harmonic3) * pianoEnv * 0.5;

            // Бас-гитара
            double bassFreq = freq / 2.0;
            double bassPhase_local = t * bassFreq;
            double bass = Math.sin(2*Math.PI*bassPhase_local) * Math.exp(-1.5*(t % beat)) * 0.4;

            // Хип-хоп ударные
            double drum = 0;
            double barPos = cb % 4.0;
            // Кик на 1 и 3
            for (double k : new double[]{0.0, 2.0}) {
                double kd = Math.abs(barPos - k) * beat;
                if (kd < 0.15) drum += Math.sin(2*Math.PI*55*(kd)) * Math.exp(-18*kd) * 0.6;
            }
            // Снэйр на 2 и 4
            for (double s : new double[]{1.0, 3.0}) {
                double sd = Math.abs(barPos - s) * beat;
                if (sd < 0.12) drum += (rnd.nextDouble()*2-1) * Math.exp(-22*sd) * 0.45;
            }
            // Хэты
            double hatT = (cb % 0.5) * beat;
            if (hatT < 0.03) drum += (rnd.nextDouble()*2-1) * Math.exp(-120*hatT) * 0.15;

            double mix = pianoS*0.50 + bass*0.30 + drum*0.28;
            if (i < 1000) mix *= i/1000.0;
            else if (i > numSamples-1000) mix *= (double)(numSamples-i)/1000.0;
            mix = Math.max(-0.95, Math.min(0.95, mix));
            short samp = (short)(mix * 32767);
            pcm[2*i]   = (byte)(samp & 0xFF);
            pcm[2*i+1] = (byte)((samp>>8)&0xFF);
        }
        return pcm;
    }

    // ─────────────────────────────────────────────────
    //  EMINEM — SUPERMAN (130 BPM, Gm, гитара)
    // ─────────────────────────────────────────────────
    public static byte[] generateEminemSupermanTrack() {
        double bpm = 130.0;
        double beat = 60.0 / bpm;
        int totalBeats = 64;
        int numSamples = (int)(SAMPLE_RATE * totalBeats * beat);
        byte[] pcm = new byte[numSamples * 2];

        // Мелодия в Gm
        double[] guitarNotes = {392.00, 349.23, 329.63, 293.66, 261.63, 293.66, 329.63, 349.23};
        Random rnd = new Random(99);
        double guitarPhase = 0;

        for (int i = 0; i < numSamples; i++) {
            double t = (double)i / SAMPLE_RATE;
            double cb = t / beat;

            int noteIdx = (int)(cb) % guitarNotes.length;
            double noteStart = Math.floor(cb);
            double noteTime = t - noteStart * beat;

            double freq = guitarNotes[noteIdx];
            guitarPhase += freq / SAMPLE_RATE;

            // Гитарный тембр: пила + нечётные гармоники
            double saw = 2*(guitarPhase - Math.floor(guitarPhase+0.5));
            double h3  = Math.sin(2*Math.PI*guitarPhase*3) * 0.20;
            double h5  = Math.sin(2*Math.PI*guitarPhase*5) * 0.10;
            double gEnv = Math.exp(-2.8*noteTime) * Math.min(1.0, noteTime/0.006);
            // Слегка дисторшн
            double guitar = Math.tanh((saw+h3+h5)*1.8) * gEnv * 0.45;

            // Бас-ритм
            double bassFreq = freq / 2.0;
            double bPhase = t * bassFreq;
            double bass = Math.sin(2*Math.PI*bPhase) * Math.exp(-2.0*(t % beat)) * 0.35;

            // Ударные
            double drum = 0;
            double barPos = cb % 4.0;
            for (double k : new double[]{0.0, 2.0}) {
                double kd = Math.abs(barPos-k) * beat;
                if (kd < 0.14) drum += Math.sin(2*Math.PI*60*kd) * Math.exp(-20*kd) * 0.55;
            }
            for (double s : new double[]{1.0, 3.0}) {
                double sd = Math.abs(barPos-s) * beat;
                if (sd < 0.11) drum += (rnd.nextDouble()*2-1) * Math.exp(-28*sd) * 0.42;
            }
            double hatT = (cb % 0.25) * beat;
            if (hatT < 0.025) drum += (rnd.nextDouble()*2-1) * Math.exp(-140*hatT) * 0.13;

            double mix = guitar*0.48 + bass*0.30 + drum*0.28;
            if (i < 1000) mix *= i/1000.0;
            else if (i > numSamples-1000) mix *= (double)(numSamples-i)/1000.0;
            mix = Math.max(-0.95, Math.min(0.95, mix));
            short samp = (short)(mix * 32767);
            pcm[2*i]   = (byte)(samp & 0xFF);
            pcm[2*i+1] = (byte)((samp>>8)&0xFF);
        }
        return pcm;
    }

    /** Псевдоним для обратной совместимости. */
    public static byte[] generateBackgroundMusicTrack() {
        return generateTravisScottFeinTrack();
    }
}
