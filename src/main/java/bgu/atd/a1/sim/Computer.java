package bgu.atd.a1.sim;

import java.util.List;
import java.util.Map;

public class Computer {

	String computerType;
	long failSig;
	long successSig;
	
	public Computer(String computerType, long successSig, long failSig) {
		this.computerType = computerType;
		this.failSig = failSig;
		this.successSig = successSig;
	}
	
	/**
	 * this method checks if the courses' grades fulfill the conditions
	 * @param courses
	 * 							courses that should be pass
	 * @param coursesGrades
	 * 							courses' grade
	 * @return a signature if couersesGrades grades meet the conditions
	 */
	public long checkAndSign(List<String> courses, Map<String, Integer> coursesGrades){
		for (String course : courses) {
			if (!coursesGrades.containsKey(course) || coursesGrades.get(course) < 55){
				return failSig;
			}
		}
		return successSig;
	}
}
