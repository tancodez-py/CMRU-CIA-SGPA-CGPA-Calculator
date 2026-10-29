import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * CMRU B.Tech CIA Weighted Grading & SGPA/CGPA Calculator
 *
 * Compile: javac Main.java
 * Run:     java Main
 *
 * Default assessment split used in this prototype:
 * IAT/CIA = 20 marks, CCE = 30 marks, SEE = 50 marks.
 * Confirm the assessment split and grade boundaries with the current
 * official course regulations before using this for final academic results.
 */
public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("====================================================");
            System.out.println(" CMRU B.TECH CIA WEIGHTED GRADING & SGPA/CGPA CALCULATOR");
            System.out.println("====================================================");
            System.out.println("Assessment: IAT 20 + CCE 30 + SEE 50 = 100 marks");
            System.out.println("Confirm your course assessment scheme before relying on results.\n");

            System.out.print("Enter student name: ");
            String studentName = scanner.nextLine().trim();
            if (studentName.isEmpty()) {
                studentName = "Student";
            }

            int semester = readIntInRange(scanner, "Enter current semester number: ", 1, 12);
            int courseCount = readIntInRange(scanner, "Enter number of courses this semester: ", 1, 100);

            AcademicRecord record = new AcademicRecord(studentName);

            for (int i = 1; i <= courseCount; i++) {
                System.out.println("\n--- Course " + i + " ---");
                String courseName;
                while (true) {
                    System.out.print("Enter course name: ");
                    courseName = scanner.nextLine().trim();
                    if (!courseName.isEmpty()) {
                        break;
                    }
                    System.out.println("Course name cannot be empty.");
                }

                double iat = readDoubleInRange(scanner, "IAT/CIA marks (0-20): ", 0, 20);
                double cce = readDoubleInRange(scanner, "CCE marks (0-30): ", 0, 30);
                double see = readDoubleInRange(scanner, "SEE marks (0-50): ", 0, 50);
                int credits = readIntInRange(scanner, "Course credits (greater than 0): ", 1, 30);

                CourseComponent assessment = new CourseComponent(iat, cce, see, credits);
                record.addCourse(new CourseEnrollment(courseName, semester, assessment));
            }

            System.out.println("\n================ SEMESTER RESULTS ================");
            System.out.println("Student: " + record.getStudentName());
            System.out.println("Semester: " + semester);
            System.out.printf("%-24s %-10s %-8s %-12s %-8s%n",
                    "Course", "Total", "Grade", "Grade Point", "Credits");
            System.out.println("------------------------------------------------------------------");

            int currentCredits = 0;
            for (CourseEnrollment course : record.getCourses()) {
                currentCredits += course.getCredits();
                System.out.printf("%-24s %-10.2f %-8s %-12.1f %-8d%n",
                        course.getCourseName(),
                        course.getTotalMarks(),
                        GradeMapper.getGrade(course.getTotalMarks()),
                        GradeMapper.getGradePoint(course.getTotalMarks()),
                        course.getCredits());
            }

            double sgpa = GPACalculator.calculateSGPA(record.getCourses(), semester);
            System.out.printf("%nTotal semester credits: %d%n", currentCredits);
            System.out.printf("SGPA: %.2f%n", sgpa);

            int previousSemesterCount = readIntInRange(
                    scanner, "\nHow many previous semesters should be included in CGPA? ", 0, 11);

            List<Double> previousSGPAs = new ArrayList<>();
            List<Integer> previousCredits = new ArrayList<>();

            for (int i = 1; i <= previousSemesterCount; i++) {
                System.out.println("Previous semester " + i + ":");
                previousSGPAs.add(readDoubleInRange(scanner, "Official SGPA (0-10): ", 0, 10));
                previousCredits.add(readIntInRange(scanner, "Total credits for that semester: ", 1, 100));
            }

            double cgpa = GPACalculator.calculateCGPA(
                    previousSGPAs, previousCredits, sgpa, currentCredits);

            System.out.printf("Credit-weighted CGPA: %.2f%n", cgpa);
            System.out.println("\nGrade scale used by this prototype:");
            System.out.println("O: 95-100 (10 GP), A+: 85-<95 (9 GP), A: 75-<85 (8 GP)");
            System.out.println("B+: 65-<75 (7 GP), B: 55-<65 (6 GP), C: 45-<55 (5 GP)");
            System.out.println("P: 40-<45 (4 GP), below 40 (0 GP)");
            System.out.println("====================================================");
        }
    }

    private static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Enter a whole number between " + min + " and " + max + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }

    private static double readDoubleInRange(
            Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (Double.isFinite(value) && value >= min && value <= max) {
                    return value;
                }
                System.out.println("Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Enter a number.");
            }
        }
    }
}

class CourseComponent {
    private final double iatMarks;
    private final double cceMarks;
    private final double seeMarks;
    private final int credits;

    public CourseComponent(double iatMarks, double cceMarks, double seeMarks, int credits) {
        this.iatMarks = validate("IAT/CIA", iatMarks, 20);
        this.cceMarks = validate("CCE", cceMarks, 30);
        this.seeMarks = validate("SEE", seeMarks, 50);
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits must be greater than zero.");
        }
        this.credits = credits;
    }

    private static double validate(String component, double marks, double maximum) {
        if (!Double.isFinite(marks) || marks < 0 || marks > maximum) {
            throw new IllegalArgumentException(
                    component + " marks must be between 0 and " + maximum + ".");
        }
        return marks;
    }

    public double getTotalMarks() {
        return iatMarks + cceMarks + seeMarks;
    }

    public int getCredits() {
        return credits;
    }
}

class CourseEnrollment {
    private final String courseName;
    private final int semester;
    private final CourseComponent assessment;

    public CourseEnrollment(String courseName, int semester, CourseComponent assessment) {
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }
        if (semester <= 0) {
            throw new IllegalArgumentException("Semester must be greater than zero.");
        }
        if (assessment == null) {
            throw new IllegalArgumentException("Assessment details cannot be null.");
        }
        this.courseName = courseName.trim();
        this.semester = semester;
        this.assessment = assessment;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getSemester() {
        return semester;
    }

    public double getTotalMarks() {
        return assessment.getTotalMarks();
    }

    public int getCredits() {
        return assessment.getCredits();
    }
}

class AcademicRecord {
    private final String studentName;
    private final List<CourseEnrollment> courses = new ArrayList<>();

    public AcademicRecord(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void addCourse(CourseEnrollment course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        courses.add(course);
    }

    public List<CourseEnrollment> getCourses() {
        return new ArrayList<>(courses);
    }
}

class GradeMapper {
    private GradeMapper() {
    }

    public static String getGrade(double marks) {
        if (marks >= 95) return "O";
        if (marks >= 85) return "A+";
        if (marks >= 75) return "A";
        if (marks >= 65) return "B+";
        if (marks >= 55) return "B";
        if (marks >= 45) return "C";
        if (marks >= 40) return "P";
        return "F";
    }

    public static double getGradePoint(double marks) {
        if (marks >= 95) return 10.0;
        if (marks >= 85) return 9.0;
        if (marks >= 75) return 8.0;
        if (marks >= 65) return 7.0;
        if (marks >= 55) return 6.0;
        if (marks >= 45) return 5.0;
        if (marks >= 40) return 4.0;
        return 0.0;
    }
}

class GPACalculator {
    private GPACalculator() {
    }

    public static double calculateSGPA(List<CourseEnrollment> courses, int semester) {
        double totalWeightedPoints = 0.0;
        int totalCredits = 0;

        for (CourseEnrollment course : courses) {
            if (course.getSemester() == semester) {
                totalWeightedPoints += GradeMapper.getGradePoint(course.getTotalMarks())
                        * course.getCredits();
                totalCredits += course.getCredits();
            }
        }

        if (totalCredits == 0) {
            throw new IllegalArgumentException("No course credits found for this semester.");
        }
        return round2(totalWeightedPoints / totalCredits);
    }

    public static double calculateCGPA(
            List<Double> previousSGPAs,
            List<Integer> previousCredits,
            double currentSGPA,
            int currentCredits) {

        if (previousSGPAs.size() != previousCredits.size()) {
            throw new IllegalArgumentException("Each previous SGPA must have corresponding credits.");
        }

        double weightedPoints = currentSGPA * currentCredits;
        int totalCredits = currentCredits;

        for (int i = 0; i < previousSGPAs.size(); i++) {
            double sgpa = previousSGPAs.get(i);
            int credits = previousCredits.get(i);

            if (!Double.isFinite(sgpa) || sgpa < 0 || sgpa > 10 || credits <= 0) {
                throw new IllegalArgumentException("Invalid previous semester SGPA or credits.");
            }
            weightedPoints += sgpa * credits;
            totalCredits += credits;
        }

        if (totalCredits <= 0) {
            throw new IllegalArgumentException("Total credits must be greater than zero.");
        }
        return round2(weightedPoints / totalCredits);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
