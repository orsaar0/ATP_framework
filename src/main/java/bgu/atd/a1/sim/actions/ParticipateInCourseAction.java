package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.List;

public class ParticipateInCourseAction extends Action<String> {

    String student;
    String course;
    String[] grades;

    public ParticipateInCourseAction(String student, String course, String[] grades) {
        this.student = student;
        this.course = course;
        this.grades = grades;
        setActionName("Participate In Course");
    }

    public String getStudent() {
        return student;
    }

    public String getCourse() {
        return course;
    }

    public String[] getGrades() {
        return grades;
    }

    @Override
    protected void start() {
        CoursePrivateState coursePrivateState = (CoursePrivateState) pool.getPrivateState(actorID);
        List<Action<Boolean>> actions = new ArrayList<>();
        Action<Boolean> participateMessage = new ParticipateMessage(student, course, grades, coursePrivateState.getPrerequisites());
        actions.add(participateMessage);
        then(actions, () -> {
            if (participateMessage.getResult().get() && coursePrivateState.getAvailableSpots() > 0) {
                coursePrivateState.addStudent(student);
                complete("Student " + student + " is participating course " + course + " successfully.");
                System.out.println("Student " + student + " is participating course " + course + " successfully, with " + coursePrivateState.getAvailableSpots() + " spots left");
                coursePrivateState.addRecord(getActionName());
            } else {
                complete("Student " + student + " doesnt meet prerequisites to participate at " + course + " course OR No room available for Student" + student + " to participate at " + course + " course.");
                System.out.println("Student " + student + " doesnt meet prerequisites to participate at " + course + " course.");
            }
        });
        sendMessage(participateMessage, student, new StudentPrivateState());
    }
}
