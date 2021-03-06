/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.atd.a1.sim;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import bgu.atd.a1.Action;
import bgu.atd.a1.ActorThreadPool;
import bgu.atd.a1.PrivateState;
import bgu.atd.a1.sim.actions.*;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;
import com.google.gson.*;
import util.Pair;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {


    public static ActorThreadPool actorThreadPool;
    public static Warehouse warehouse;
    private static Integer nthreads;
    private static ArrayList<Action<?>> phase1ActionsArray;
    private static ArrayList<Action<?>> phase2ActionsArray;
    private static ArrayList<Action<?>> phase3ActionsArray;
    private static Map<Action<?>, Pair<String, PrivateState>> phase1Actions;
    private static Map<Action<?>, Pair<String, PrivateState>> phase2Actions;
    private static Map<Action<?>, Pair<String, PrivateState>> phase3Actions;


    /**
     * Begin the simulation Should not be called before attachActorThreadPool()
     */
    public static void start() {
        try {
            CountDownLatch phase1Countdown = new CountDownLatch(phase1Actions.size());
            CountDownLatch phase2Countdown = new CountDownLatch(phase2Actions.size());
            CountDownLatch phase3Countdown = new CountDownLatch(phase3Actions.size());

            System.out.println("PHASE 1:");
            for (Action<?> action : phase1ActionsArray) {
                actorThreadPool.submit(action, phase1Actions.get(action).getKey(), phase1Actions.get(action).getValue());
                action.getResult().subscribe(() ->
                        action.getResult().subscribe(phase1Countdown::countDown));
            }
            phase1Countdown.await();
            System.out.println("PHASE 2:");
            for (Action<?> action : phase2ActionsArray) {
                actorThreadPool.submit(action, phase2Actions.get(action).getKey(), phase2Actions.get(action).getValue());
                action.getResult().subscribe(() ->
                        action.getResult().subscribe(phase2Countdown::countDown));
            }
            phase2Countdown.await();
            System.out.println("PHASE 3:");
            for (Action<?> action : phase3ActionsArray) {
                actorThreadPool.submit(action, phase3Actions.get(action).getKey(), phase3Actions.get(action).getValue());
                action.getResult().subscribe(() ->
                        action.getResult().subscribe(phase3Countdown::countDown));
            }
            phase3Countdown.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * attach an ActorThreadPool to the Simulator, this ActorThreadPool will be used to run the simulation
     *
     * @param myActorThreadPool - the ActorThreadPool which will be used by the simulator
     */
    public static void attachActorThreadPool(ActorThreadPool myActorThreadPool) {
        actorThreadPool = myActorThreadPool;
        actorThreadPool.warehouse = warehouse;
    }

    /**
     * shut down the simulation
     * returns list of private states
     */
    public static HashMap<String, PrivateState> end() {
        try {
            actorThreadPool.shutdown();
            System.out.println("SHUTTING DOWN COMPLETE");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new HashMap<String, PrivateState>(actorThreadPool.getActors());
    }


    public static void main(String[] args) {
        parse(args[0]);
        attachActorThreadPool(new ActorThreadPool(nthreads));
        actorThreadPool.start();
        start();
        Map<String, PrivateState> simResult;
        simResult = end();
        try {
            FileOutputStream fout = new FileOutputStream("result.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(simResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FINISHED");
    }

    private static void parse(String s) {
        File input = new File(s);
        try {
            FileReader fileReader = new FileReader(input);
            JsonElement fileElement = JsonParser.parseReader(fileReader);
            JsonObject fileObject = fileElement.getAsJsonObject();

            // Extracting nthreads field
            nthreads = fileObject.get("threads").getAsInt();

            // Extracting Computers
            JsonArray jsonArrayOfComputers = fileObject.get("Computers").getAsJsonArray();
            Map<Computer, AtomicBoolean> computerLockMap = new HashMap<>();
            for (JsonElement computerElement : jsonArrayOfComputers) {
                JsonObject computerJsonObject = computerElement.getAsJsonObject();

                // Extracting data
                String computerType = computerJsonObject.get("Type").getAsString();
                long successSig = computerJsonObject.get("Sig Success").getAsLong();
                long failSig = computerJsonObject.get("Sig Fail").getAsLong();

                Computer computer = new Computer(computerType, successSig, failSig);
                computerLockMap.put(computer, new AtomicBoolean(false));
            }
            warehouse = new Warehouse(computerLockMap);

            // Extracting Phase 1
            JsonArray jsonArrayOfPhase1 = fileObject.get("Phase 1").getAsJsonArray();
            phase1Actions = new HashMap<>();
            phase1ActionsArray = new ArrayList<>();
            for (JsonElement phase1ActionElement : jsonArrayOfPhase1) {
                JsonObject phase1ActionObject = phase1ActionElement.getAsJsonObject();

                // Find the Action Type
                extractActionFromJson(phase1ActionObject, phase1Actions, phase1ActionsArray);
            }

            // Extracting Phase 2
            JsonArray jsonArrayOfPhase2 = fileObject.get("Phase 2").getAsJsonArray();
            phase2Actions = new HashMap<>();
            phase2ActionsArray = new ArrayList<>();
            for (JsonElement phase2ActionElement : jsonArrayOfPhase2) {
                JsonObject phase2ActionObject = phase2ActionElement.getAsJsonObject();

                // Find the Action Type
                extractActionFromJson(phase2ActionObject, phase2Actions, phase2ActionsArray);
            }

            // Extracting Phase 3
            JsonArray jsonArrayOfPhase3 = fileObject.get("Phase 3").getAsJsonArray();
            phase3Actions = new HashMap<>();
            phase3ActionsArray = new ArrayList<>();
            for (JsonElement phase3ActionElement : jsonArrayOfPhase3) {
                JsonObject phase3ActionObject = phase3ActionElement.getAsJsonObject();

                // Find the Action Type
                extractActionFromJson(phase3ActionObject, phase3Actions, phase3ActionsArray);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void extractActionFromJson(JsonObject actionObject, Map<Action<?>, Pair<String, PrivateState>> phaseMap, ArrayList<Action<?>> phaseArray) {
        String actionName = actionObject.get("Action").getAsString();
        Action<?> action = null;
        String actorID = null;
        PrivateState privateState = null;
        switch (actionName) {
            case "Open Course":
                String department = actionObject.get("Department").getAsString();
                String course = actionObject.get("Course").getAsString();
                Integer space = actionObject.get("Space").getAsInt();
                JsonArray prerequisitesJsonArray = actionObject.get("Prerequisites").getAsJsonArray();
                List<String> prerequisites = new LinkedList<>();
                for (int i = 0; i < prerequisitesJsonArray.size(); i++) {
                    prerequisites.add(prerequisitesJsonArray.get(i).getAsString());
                }
                action = new OpenCourseAction(department, course, space, prerequisites);
                actorID = department;
                privateState = new DepartmentPrivateState();
                break;

            case "Add Student":
                department = actionObject.get("Department").getAsString();
                String student = actionObject.get("Student").getAsString();
                action = new AddStudentAction(department, student);
                actorID = department;
                privateState = new DepartmentPrivateState();
                break;

            case "Participate In Course":
                student = actionObject.get("Student").getAsString();
                course = actionObject.get("Course").getAsString();
                JsonArray gradesJsonArray = actionObject.get("Grade").getAsJsonArray();
                String[] grades = new String[gradesJsonArray.size()];
                for (int i = 0; i < gradesJsonArray.size(); i++) {
                    grades[i] = gradesJsonArray.get(i).getAsString();
                }
                action = new ParticipateInCourseAction(student, course, grades);
                actorID = course;
                privateState = new CoursePrivateState();
                break;

            case "Unregister":
                student = actionObject.get("Student").getAsString();
                course = actionObject.get("Course").getAsString();
                action = new UnregisterAction(student, course);
                actorID = course;
                privateState = new CoursePrivateState();
                break;

            case "Close Course":
                department = actionObject.get("Department").getAsString();
                course = actionObject.get("Course").getAsString();
                action = new CloseCourseAction(department, course);
                actorID = department;
                privateState = new DepartmentPrivateState();
                break;

            case "Add Spaces":
                course = actionObject.get("Course").getAsString();
                Integer number = actionObject.get("Number").getAsInt();
                action = new AddSpacesAction(course, number);
                actorID = course;
                privateState = new CoursePrivateState();
                break;

            case "Administrative Check":
                department = actionObject.get("Department").getAsString();
                JsonArray studentsJsonArray = actionObject.get("Students").getAsJsonArray();
                String[] students = new String[studentsJsonArray.size()];
                for (int i = 0; i < studentsJsonArray.size(); i++) {
                    students[i] = studentsJsonArray.get(i).getAsString();
                }
                String computerType = actionObject.get("Computer").getAsString();
                JsonArray conditionsJsonArray = actionObject.get("Conditions").getAsJsonArray();
                List<String> conditions = new LinkedList<>();
                for (int i = 0; i < conditionsJsonArray.size(); i++) {
                    conditions.add(conditionsJsonArray.get(i).getAsString());
                }
                action = new AdministrativeCheckAction(department, students, computerType, conditions);
                actorID = department;
                privateState = new DepartmentPrivateState();
                break;

            case "Register With Preferences":
                student = actionObject.get("Student").getAsString();
                JsonArray coursesJsonArray = actionObject.get("Preferences").getAsJsonArray();
                List<String> courses = new ArrayList<>();
                for (int i = 0; i < coursesJsonArray.size(); i++) {
                    courses.add(coursesJsonArray.get(i).getAsString());
                }
                JsonArray gradesJsonArray1 = actionObject.get("Grade").getAsJsonArray();
                String[] grades1 = new String[gradesJsonArray1.size()];
                for (int i = 0; i < gradesJsonArray1.size(); i++) {
                    grades1[i] = gradesJsonArray1.get(i).getAsString();
                }
                action = new RegisterWithPreferenceAction(student, courses, grades1);
                actorID = student;
                privateState = new StudentPrivateState();
                break;

        }
        phaseArray.add(action);
        phaseMap.put(action, new Pair<>(actorID, privateState));
    }
}
