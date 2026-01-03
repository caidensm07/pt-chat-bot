import java.util.*;

public class TrainingPlanGenerator {

    // --- Simple “readiness” rules ---
    // GREEN: pain <= 3 and no red flags
    // YELLOW: pain 4-5 OR low sleep OR stiffness high (but no red flags)
    // RED: swelling/instability/sharp pain/popping+pain OR rest pain >= 6

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the PT Training Plan Generator!");
        System.out.println("Answer a few questions and I’ll recommend today’s plan.\n");

        // Injury?
        System.out.print("Do you have any injuries right now? (yes/no): ");
        String injuryResponse = scanner.nextLine().trim().toLowerCase();

        int injuryChoice = 0;
        String injuryCause = "";
        int painLevel = 0;
        int recoveryProgress = 0;

        if (injuryResponse.equals("yes")) {
            injuryChoice = askInjuryChoice(scanner);
            System.out.println("\nYour Injury Rehab Plan:\n" + getRehabPlan(injuryChoice));

            System.out.print("How did the injury occur? (e.g., sprinting, overtraining, accident): ");
            injuryCause = scanner.nextLine().trim();

            painLevel = askIntRange(scanner, "Current pain level (1-10): ", 1, 10);
            recoveryProgress = askIntRange(scanner, "Recovery progress so far (1-10): ", 1, 10);
        } else {
            System.out.println("\nGreat — we’ll treat this like a normal training day.\n");
            // still ask pain level, because even “no injuries” can have soreness/pain
            painLevel = askIntRange(scanner, "Any pain today? (1-10, 1 = none/minimal): ", 1, 10);
        }

        // PT-style red flags + readiness inputs
        int painAtRest = askIntRange(scanner, "Pain at rest (0-10): ", 0, 10);
        int stiffness = askIntRange(scanner, "Morning stiffness/tightness (0-10): ", 0, 10);
        double sleepHours = askDouble(scanner, "Hours of sleep last night (e.g., 7.5): ");

        boolean swelling = askYesNo(scanner, "Any swelling today? (yes/no): ");
        boolean instability = askYesNo(scanner, "Any giving way/instability? (yes/no): ");
        boolean sharpPain = askYesNo(scanner, "Any sharp pain? (yes/no): ");
        boolean poppingWithPain = askYesNo(scanner, "Any popping WITH pain? (yes/no): ");

        // Training qualities (your original stuff)
        int muscleStrength = askIntRange(scanner, "Muscle strength (1-10): ", 1, 10);
        int flexibility = askIntRange(scanner, "Flexibility (1-10): ", 1, 10);
        int fascia = askIntRange(scanner, "Mobility/fascia readiness (1-10): ", 1, 10);
        int cns = askIntRange(scanner, "CNS / energy / recovery (1-10): ", 1, 10);

        // Decide zone
        String zone = triageZone(painAtRest, stiffness, sleepHours, swelling, instability, sharpPain, poppingWithPain, painLevel);

        System.out.println("\n==============================");
        System.out.println("PT BOT RESULT");
        System.out.println("==============================");
        System.out.println("Readiness Zone: " + zone);

        if (injuryResponse.equals("yes")) {
            System.out.println("Injury: " + injuryName(injuryChoice));
            System.out.println("Cause: " + injuryCause);
            System.out.println("Pain: " + painLevel + "/10, Recovery: " + recoveryProgress + "/10");
        }

        // Output plan
        System.out.println("\n--- Today’s Recommended Plan ---");
        List<String> plan = generateTrainingPlan(zone, muscleStrength, flexibility, fascia, cns, injuryChoice);
        for (String line : plan) {
            System.out.println("- " + line);
        }

        System.out.println("\nRules:");
        System.out.println("- Keep pain during activity ≤ 3/10.");
        System.out.println("- Symptoms should return to baseline within 24 hours.");
        System.out.println("- If swelling/instability/sharp pain keeps happening: get checked out.");

        scanner.close();
    }

    // ---------- Input Helpers ----------
    private static boolean askYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("yes") || ans.equals("y")) return true;
            if (ans.equals("no") || ans.equals("n")) return false;
            System.out.println("Please type yes or no.");
        }
    }

    private static int askIntRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(raw);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a number from " + min + " to " + max + ".");
        }
    }

    private static double askDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException ignored) {
                System.out.println("Enter a valid number (example: 7.5).");
            }
        }
    }

    private static int askInjuryChoice(Scanner scanner) {
        System.out.println("Select your injury area:");
        System.out.println("1. Hamstring");
        System.out.println("2. Knee");
        System.out.println("3. Calves");
        System.out.println("4. Lower Back");
        System.out.println("5. Foot");
        System.out.println("6. Shins");
        return askIntRange(scanner, "Enter 1-6: ", 1, 6);
    }

    // ---------- Logic ----------
    Public static String triageZone(
            int painAtRest, int stiffness, double sleepHours,
            boolean swelling, boolean instability, boolean sharpPain, boolean poppingWithPain,
            int overallPain
    ) {
        // RED flags
        if (swelling || instability || sharpPain || poppingWithPain || painAtRest >= 6) return "RED";

        // YELLOW flags
        if (overallPain >= 4 || stiffness >= 6 || sleepHours < 6.0) return "YELLOW";

        // GREEN
        return "GREEN";
    }

    private static List<String> generateTrainingPlan(
            String zone, int strength, int flexibility, int fascia, int cns, int injuryChoice
    ) {
        List<String> out = new Array
