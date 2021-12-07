package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;

public class AddStudentMessage extends Action<Boolean> {

    @Override
    protected void start() {
        complete(true);
    }
}
