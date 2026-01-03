import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TrainingPlanGenerator {

    private static final String LOG_FILE = "pt_log.csv";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // In-memory state (last check-in)
    private static CheckIn lastCheckIn = null;

    // =========================
    // MAIN: chat command loop
    // =========================
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== PT Chat Bot (Java) ===");
        System.out.println("Type 'help' to see commands.\n");

        while (true) {
            System.out.print("you> ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            if (cmd.isEmpty()) continue;

            switch (cmd) {
                case "help":
                case "?":
                    printHelp();
                    break;

                case "checkin":
                    lastCheckIn = runCheckIn(scanner);
                    ensureLogHeaderExists(LOG_FILE);
                    appendCheckInToCsv(LOG_FILE, lastCheckIn);

                    String zone = triageZone(
                            lastCheckIn.painAtRest,
                            lastCheckIn.stiffness,
                            lastCheckIn.sleepHours,
                            lastCheckIn.swelling,
                            lastCheckIn.instability,
                            lastCheckIn.sharpPain,
                            lastCheckIn.poppingWithPain,
                            lastCheckIn.overallPain
                    );

                    System.out.println("\nPT Bot> Readiness Zone: " + zone);
                    System.out.println("PT Bot> Today's plan:");
                    for (String line : generateTrainingPlan(zone, lastCheckIn.strength, lastCheckIn.flexibility, lastCheckIn.fascia, lastCheckIn.cns)) {
                        System.out.println("- " + line);
                    }
                    System.out.println("\nPT Bot> Saved to " + LOG_FILE + "\n");
                    break;

                case "plan":
                    if (lastCheckIn == null) {
                        System.out.println("PT Bot> No check-in yet. Type: checkin\n");
                        break;
                    }
                    String z = triageZone(
                            lastCheckIn.painAtRest,
                            lastCheckIn.stiffness,
                            lastCheckIn.sleepHours,
                            lastCheckIn.swelling,
                            lastCheckIn.instability,
                            lastCheckIn.sharpPain,
                            lastCheckIn.poppingWithPain,
                            lastCheckIn.overallPain
                    );
                    System.out.println("PT Bot> Readiness Zone: " + z);
                    System.out.println("PT Bot> Today's plan:");
                    for (String line : generateTrainingPlan(z, lastCheckIn.strength, lastCheckIn.flexibility, lastCheckIn.fascia, lastCheckIn.cns)) {
                        System.out.println("- " + line);
                    }
                    System.out.println();
                    break;

                case "history":
                    ensureLogHeaderExists(LOG_FILE);
                    printLastNEntries(LOG_FILE, 5);
                    System.out.println();
                    break;

                case "rehab":
                    System.out.println("PT Bot> Type: rehab hamstring | rehab knee | rehab calf\n");
                    break;

                case "rehab hamstring":
                    System.out.println(getRehabPlan(1));
                    System.out.println();
                    break;

                case "rehab knee":
                    System.out.println(getRehabPlan(2));
                    System.out.println();
                    break;

                case "rehab calf":
                    System.out.println(getRehabPlan(3));
                    System.out.println();
                    break;

                case "quit":
                case "exit":
                    System.out.println("PT Bot> later.");
                    scanner.close();
                    return;

                default:
                    System.out.println("PT Bot> I didn't get that. Type 'help'.\n");
            }
        }
    }

    private static void printHelp() {
        System.out.println("""
Commands:
  help            Show commands
  checkin         Do a daily check-in (saves to pt_log.csv)
  plan            Show plan for your most recent check-in
  history         Show last 5 saved check-ins
  rehab hamstring Show hamstring rehab plan
  rehab knee      Show knee rehab plan
  rehab calf      Show calf rehab plan
  quit            Exit
""");
    }

    // =========================
    // CHECK-IN FLOW
    // =========================
    private static CheckIn runCheckIn(Scanner scanner) {
        System.out.println("\nPT Bot check-in (quick + honest).");

        int painAtRest = askIntRange(scanner, "Pain at rest (0-10): ", 0, 10);
        int stiffness = askIntRange(scanner, "Morning stiffness (0-10): ", 0, 10);
        double sleepHours = askDouble(scanner, "Hours of sleep last night (e.g., 7.5): ");

        boolean swelling = askYesNo(scanner, "Any swelling? (yes/no): ");
        boolean instability = askYesNo(scanner, "Any instability/giving way? (yes/no): ");
        boolean sharpPain = askYesNo(scanner, "Any sharp pain? (yes/no): ");
        boolean poppingWithPain = askYesNo(scanner, "Any popping WITH pain? (yes/no): ");

        int overallPain = askIntRange(scanner, "Overall pain during activity (0-10): ", 0, 10);

        int strength = askIntRange(scanner, "Strength (1-10): ", 1, 10);
        int flexibility = askIntRange(scanner, "Flexibility (1-10): ", 1, 10);
        int fascia = askIntRange(scanner, "Mobility/fascia readiness (1-10): ", 1, 10);
        int cns = askIntRange(scanner, "CNS/energy (1-10): ", 1, 10);

        String notes = askString(scanner, "Notes (optional): ");

        return new CheckIn(
                LocalDateTime.now().format(TS_FMT),
                painAtRest, stiffness, sleepHours,
                swelling, instability, sharpPain, poppingWithPain,
                overallPain,
                strength, flexibility, fascia, cns,
                notes
        );
    }

    // =========================
    // TRIAGE LOGIC (TESTED)
    // =========================
    public static String triageZone(
            int painAtRest, int stiffness, double sleepHours,
            boolean swelling, boolean instability, boolean sharpPain, boolean poppingWithPain,
            int overallPain
    ) {
        if (swelling || instability || sharpPain || poppingWithPain || painAtRest >= 6) return "RED";
        if (overallPain >= 4 || stiffness >= 6 || sleepHours < 6.0) return "YELLOW";
        return "GREEN";
    }

    // =========================
    // TRAINING PLAN
    // =========================
    public static List<String> generateTrainingPlan(String zone, int strength, int flexibility, int fascia, int cns) {
        List<String> plan = new ArrayList<>();

        if ("RED".equals(zone)) {
            plan.add("No sprinting or intense lower-body loading today.");
            plan.add("Easy walk/bike 10–20 minutes if comfortable.");
            plan.add("Isometrics (pain-calming): 3–5 sets of 30–45s.");
            plan.add("Prioritize sleep, hydration, and easy mobility.");
            return plan;
        }

        if ("YELLOW".equals(zone)) {
            plan.add("Keep it light: technique + low intensity only.");
            plan.add("Options: easy tempo, light sled drags, or bike 20–30 min.");
            plan.add("Avoid max sprinting and hard decels today.");
            plan.add("Strength work: moderate effort, clean reps.");
            return plan;
        }

        // GREEN
        plan.add("Train normally, but progress volume conservatively.");
        plan.add("Warm up thoroughly and ramp intensity gradually.");
        plan.add("If CNS ≥ 7, you can lift heavier (no grind reps).");
        plan.add("Keep pain ≤ 3/10 and no next-day spike.");
        return plan;
    }

    // =========================
    // REHAB PLANS (TESTED)
    // =========================
    public static String getRehabPlan(int injuryChoice) {
        switch (injuryChoice) {
            case 1:
                return "=== Hamstring Rehab Plan ===\n"
                        + "• Isometrics (bridge holds / curl holds)\n"
                        + "• Progress to RDLs + controlled tempo\n"
                        + "• Gradual return to sprinting with short reps\n";
            case 2:
                return "=== Knee Rehab Plan ===\n"
                        + "• Quad isometrics (wall sit / leg extension iso)\n"
                        + "• Step-downs + split squats (pain ≤ 3/10)\n"
                        + "• Gradual return to running (no hard decels early)\n";
            case 3:
                return "=== Calf Rehab Plan ===\n"
                        + "• Isometric calf holds\n"
                        + "• Eccentric calf raises\n"
                        + "• Gradual plyometric progression\n";
            default:
                return "Invalid injury choice.";
        }
    }

    // =========================
    // LOGGING (CSV)
    // =========================
    public static void ensureLogHeaderExists(String path) {
        File f = new File(path);
        if (f.exists() && f.length() > 0) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(f, false))) {
            pw.println("timestamp,painAtRest,stiffness,sleepHours,swelling,instability,sharpPain,poppingWithPain,overallPain,strength,flexibility,fascia,cns,notes");
        } catch (IOException e) {
            System.out.println("PT Bot> Could not write log header: " + e.getMessage());
        }
    }

    public static void appendCheckInToCsv(String path, CheckIn c) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            pw.println(c.toCsvRow());
        } catch (IOException e) {
            System.out.println("PT Bot> Could not append log: " + e.getMessage());
        }
    }

    private static void printLastNEntries(String path, int n) {
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("PT Bot> No history yet. Run: checkin");
            return;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.out.println("PT Bot> Could not read history: " + e.getMessage());
            return;
        }

        if (lines.size() <= 1) { // header only
            System.out.println("PT Bot> No saved check-ins yet. Run: checkin");
            return;
        }

        System.out.println("PT Bot> Last check-ins:");
        int start = Math.max(1, lines.size() - n); // skip header at 0
        for (int i = start; i < lines.size(); i++) {
            System.out.println("  " + lines.get(i));
        }
    }

    // =========================
    // INPUT HELPERS
    // =========================
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
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException ignored) {}
            System.out.println("Enter a number between " + min + " and " + max + ".");
        }
    }

    private static double askDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number (example: 7.5).");
            }
        }
    }

    private static String askString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // =========================
    // DATA CLASS
    // =========================
    private static class CheckIn {
        final String timestamp;
        final int painAtRest;
        final int stiffness;
        final double sleepHours;
        final boolean swelling;
        final boolean instability;
        final boolean sharpPain;
        final boolean poppingWithPain;
        final int overallPain;
        final int strength;
        final int flexibility;
        final int fascia;
        final int cns;
        final String notes;

        CheckIn(String timestamp,
                int painAtRest, int stiffness, double sleepHours,
                boolean swelling, boolean instability, boolean sharpPain, boolean poppingWithPain,
                int overallPain,
                int strength, int flexibility, int fascia, int cns,
                String notes
        ) {
            this.timestamp = timestamp;
            this.painAtRest = painAtRest;
            this.stiffness = stiffness;
            this.sleepHours = sleepHours;
            this.swelling = swelling;
            this.instability = instability;
            this.sharpPain = sharpPain;
            this.poppingWithPain = poppingWithPain;
            this.overallPain = overallPain;
            this.strength = strength;
            this.flexibility = flexibility;
            this.fascia = fascia;
            this.cns = cns;
            this.notes = notes == null ? "" : notes;
        }

        String toCsvRow() {
            // Escape quotes + wrap notes in quotes to keep CSV safe
            String safeNotes = notes.replace("\"", "\"\"");
            return timestamp + ","
                    + painAtRest + ","
                    + stiffness + ","
                    + sleepHours + ","
                    + swelling + ","
                    + instability + ","
                    + sharpPain + ","
                    + poppingWithPain + ","
                    + overallPain + ","
                    + strength + ","
                    + flexibility + ","
                    + fascia + ","
                    + cns + ","
                    + "\"" + safeNotes + "\"";
        }
    }
}
