package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.actions.messages.RegisterWithPreferenceMessage;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterWithPreferanceAction extends Action<Boolean> {
    String student;
    String[] courses;
    String[] grade;

    public RegisterWithPreferanceAction(String student, String[] courses, String[] grade) {
        this.student = student;
        this.courses = courses;
        this.grade = grade;
    }

    public String getStudent() {
        return student;
    }

    public String[] getCourses() {
        return courses;
    }

    @Override
    protected void start() {
        List<String> coursesList = new ArrayList(Arrays.asList(courses));
        if(coursesList.isEmpty()) {
            complete(false);
            return;
        }
        String course = coursesList.remove(0);
        StudentPrivateState privateState = (StudentPrivateState) pool.getPrivateState(actorID);
        List<String> studentPrereqs = new ArrayList<>(privateState.getGrades().keySet());
        List<Action<Boolean>> actions = new ArrayList<>();
        Action<Boolean> registerWithPreference = new RegisterWithPreferenceMessage(course, studentPrereqs);
        actions.add(registerWithPreference);
        then(actions, () -> {
            if(actions.get(0).getResult().get()) {
                complete(true);
                pool.getPrivateState(actorID).addRecord(getActionName());
            }
            else {
                RegisterWithPreferanceAction nextCourse = new RegisterWithPreferanceAction(student, Arrays.copyOfRange(courses, 1, courses.length), grade);
                sendMessage(nextCourse, course, new StudentPrivateState());
            }
        });
    }
}
