package indie.tango.defuse.models;

public enum GameMod {

    EASY (180),
    NORMAL(120),
    HARD (60);

    private int time;

    GameMod(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }
}