package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

public class AddStudentAction extends Action<String> {
    String department;
    String student;

    public AddStudentAction(String department, String student) {
        this.department = department;
        this.student = student;
        setActionName("Add Student");
    }

    public String getDepartment() {
        return department;
    }

    public String getStudent() {
        return student;
    }

    @Override
    protected void start() {
        DepartmentPrivateState privateState = (DepartmentPrivateState) pool.getPrivateState(actorID);
        sendMessage(new AddStudentMessage(), student, new StudentPrivateState()); // only in order to open an Actor for him
        privateState.getStudentList().add(student);
        privateState.addRecord(getActionName());
        complete("Student " + student + " was added to " + department + " department successfully");
        System.out.println("Student " + student + " was added to " + department + " department successfully");
    }
}

