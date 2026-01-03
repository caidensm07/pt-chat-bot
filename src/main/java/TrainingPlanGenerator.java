import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class TrainingPlanGenerator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== PT Training Plan Generator ===\n");

        System.out.print("Do you currently have an injury? (yes/no): ");
        String injuryResponse = scanner.nextLine().trim().toLowerCase();

        int injuryChoice = 0;
        if (injuryResponse.equals("yes")) {
            injuryChoice = askInjuryChoice(scanner);
            System.out.println("\n" + getRehabPlan(injuryChoice));
        }

        int painAtRest = askIntRange(scanner, "Pain at rest (0-10): ", 0, 10);
        int stiffness = askIntRange(scanner, "Morning stiffness (0-10): ", 0, 10);
        double sleepHours = askDouble(scanner, "Hours of sleep last night: ");

        boolean swelling = askYesNo(scanner, "Any swelling? (yes/no): ");
        boolean instability = askYesNo(scanner, "Any instability/giving way? (yes/no): ");
        boolean sharpPain = askYesNo(scanner, "Any sharp pain? (yes/no): ");
        boolean poppingWithPain = askYesNo(scanner, "Any popping WITH pain? (yes/no): ");

        int overallPain = askIntRange(scanner, "Overall pain during activity (0-10): ", 0, 10);

        int strength = askIntRange(scanner, "Muscle strength (1-10): ", 1, 10);
        int flexibility = askIntRange(scanner, "Flexibility (1-10): ", 1, 10);
        int fascia = askIntRange(scanner, "Mobility/fascia readiness (1-10): ", 1, 10);
        int cns = askIntRange(scanner, "CNS / energy (1-10): ", 1, 10);

        String zone = triageZone(
                painAtRest,
                stiffness,
                sleepHours,
                swelling,
                instability,
                sharpPain,
                poppingWithPain,
                overallPain
        );

        System.out.println("\n==============================");
        System.out.println("READINESS ZONE: " + zone);
        System.out.println("==============================");

        List<String> plan = generateTrainingPlan(zone, strength, flexibility, fascia, cns);

        for (String line : plan) {
            System.out.println("- " + line);
        }

        System.out.println("\nRules:");
        System.out.println("- Pain during activity ≤ 3/10");
        System.out.println("- No next-day symptom spike");
        System.out.println("- Stop if sharp pain or instability appears");

        scanner.close();
    }

    // ================================
    // TRIAGE LOGIC (TESTED)
    // ================================
    public static String triageZone(
            int painAtRest,
            int stiffness,
            double sleepHours,
            boolean swelling,
            boolean instability,
            boolean sharpPain,
            boolean poppingWithPain,
            int overallPain
    ) {
        if (swelling || instability || sharpPain || poppingWithPain || painAtRest >= 6) {
            return "RED";
        }
        if (overallPain >= 4 || stiffness >= 6 || sleepHours < 6.0) {
            return "YELLOW";
        }
        return "GREEN";
    }

    // ================================
    // TRAINING PLAN GENERATOR
    // ================================
    public static List<String> generateTrainingPlan(
            String zone,
            int strength,
            int flexibility,
            int fascia,
            int cns
    ) {
        List<String> plan = new ArrayList<>();

        if (zone.equals("RED")) {
            plan.add("No sprinting or intense lower-body work");
            plan.add("Light walking or cycling (10–20 min)");
            plan.add("Isometrics for pain reduction");
            plan.add("Focus on sleep and recovery");
            return plan;
        }

        if (zone.equals("YELLOW")) {
            plan.add("Technique drills only");
            plan.add("Light sled pulls or tempo runs");
            plan.add("Avoid max velocity and hard decels");
            plan.add("Moderate lifting (no max effort)");
            return plan;
        }

        // GREEN ZONE
        plan.add("Acceleration or max velocity session allowed");
        plan.add("Progress volume conservatively");
        plan.add("Lift heavy if CNS ≥ 7");
        plan.add("Include mobility and tissue prep");

        return plan;
    }

    // ================================
    // REHAB PLANS (TESTED)
    // ================================
    public static String getRehabPlan(int injuryChoice) {
        switch (injuryChoice) {
            case 1:
                return "=== Hamstring Rehab Plan ===\n"
                        + "• Isometrics and bridges\n"
                        + "• Progress to RDLs and tempo runs\n"
                        + "• Gradual return to sprinting";
            case 2:
                return "=== Knee Rehab Plan ===\n"
                        + "• Quad isometrics\n"
                        + "• Step-downs and split squats\n"
                        + "• Gradual return to running";
            case 3:
                return "=== Calf Rehab Plan ===\n"
                        + "• Isometric calf holds\n"
                        + "• Eccentric calf raises\n"
                        + "• Gradual plyometric loading";
            default:
                return "No rehab plan available.";
        }
    }

    // ================================
    // INPUT HELPERS
    // ================================
    private static boolean askYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes") || input.equals("y")) return true;
            if (input.equals("no") || input.equals("n")) return false;
            System.out.println("Please answer yes or no.");
        }
    }

    private static int askIntRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }

    private static double askDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static int askInjuryChoice(Scanner scanner) {
        System.out.println("\nSelect injury:");
        System.out.println("1. Hamstring");
        System.out.println("2. Knee");
        System.out.println("3. Calf");
        return askIntRange(scanner, "Enter number: ", 1, 3);
    }
}
