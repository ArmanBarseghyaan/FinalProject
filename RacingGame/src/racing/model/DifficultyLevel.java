package racing.model;

public enum DifficultyLevel {
    EASY(1, 4, 3),
    MEDIUM(2, 6, 5),
    HARD(3, 9, 8);

    private final int level;
    private final int speedMultiplier;
    private final int spawnRate;

    DifficultyLevel(int level, int speedMultiplier, int spawnRate) {
        this.level = level;
        this.speedMultiplier = speedMultiplier;
        this.spawnRate = spawnRate;
    }

    public int getLevel() { return level; }
    public int getSpeedMultiplier() { return speedMultiplier; }
    public int getSpawnRate() { return spawnRate; }
}