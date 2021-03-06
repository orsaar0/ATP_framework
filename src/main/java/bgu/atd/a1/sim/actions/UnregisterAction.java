package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.List;

public class UnregisterAction extends Action<String> {
    String student;
    String course;

    public UnregisterAction(String student, String course) {
        this.student = student;
        this.course = course;
        setActionName("Unregister");
    }

    public String getStudent() {
        return student;
    }

    public String getCourse() {
        return course;
    }

    @Override
    protected void start() {
        CoursePrivateState coursePrivateState = (CoursePrivateState) pool.getPrivateState(actorID);
        if (coursePrivateState.getRegStudents().contains(student)) {
            List<Action<Boolean>> actions = new ArrayList<>();
            Action unregisterMessage = new UnregisterMessage(course);
            actions.add(unregisterMessage);
            then(actions, () -> {
                coursePrivateState.removeStudent(student);
                coursePrivateState.addRecord(getActionName());
                complete("Unregister student " + student + " from course " + course + " successfully.");
                System.out.println("Unregister student " + student + " from course " + course + " successfully.");
            });
            sendMessage(unregisterMessage, student, new StudentPrivateState());
        } else {
            complete("Unregister " + student + " is unnecessary because they are not register.");
            System.out.println("Unregister " + student + " from course " + course + " is unnecessary because they are not register.");
        }
    }
}
