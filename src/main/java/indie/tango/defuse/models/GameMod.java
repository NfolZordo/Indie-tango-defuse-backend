package indie.tango.defuse.models;

public enum GameMod {

    EASY (180,2),
    NORMAL(120,1),
    HARD (60,0);

    private int time;
    private int countMistake;

    GameMod(int time, int countMistake) {
        this.time = time;
        this.countMistake = countMistake;
    }

    public int getTime() {
        return time;
    }

    public int getCountMistake() {
        return countMistake;
    }
}