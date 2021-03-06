package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

public class RemoveCourseGradeMessage extends Action<Boolean> {
    String course;

    public RemoveCourseGradeMessage(String course) {
        this.course = course;
        setActionName("Remove Course Grade Message");
    }

    @Override
    protected void start() {
        StudentPrivateState studentPrivateState = (StudentPrivateState) pool.getPrivateState(actorID);
        if (studentPrivateState.getGrades().remove(course) != null) {
            System.out.println("student " + actorID + " removed "+ course + " course");
            complete(true);
            return;
        }
        complete(false);
    }
}
