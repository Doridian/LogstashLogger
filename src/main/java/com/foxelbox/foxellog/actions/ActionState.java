package com.foxelbox.foxellog.actions;

public enum ActionState {
    IN_PLACE(0),
    ATTEMPTED_ROLLBACK(1),
    GONE(2),
    ATTEMPTED_REDO(3);

    private final int dbVal;

    ActionState(final int dbVal) {
        this.dbVal = dbVal;
    }

    public int getDbVal() {
        return dbVal;
    }

    public static ActionState getByDbVal(int dbVal) {
        switch (dbVal) {
            case 0:
                return IN_PLACE;
            case 1:
                return ATTEMPTED_ROLLBACK;
            case 2:
                return GONE;
            case 3:
                return ATTEMPTED_REDO;
        }
        return null;
    }
}
