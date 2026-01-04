import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TrainingPlanGenerator {

    private static final String LOG_FILE = "pt_log.csv";
    private static final String STATE_FILE = "rts_state.properties";
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // =========================
    // MAIN
    // =========================
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RTSState state = RTSState.load(STATE_FILE);

        System.out.println("=== Return-to-Sport Readiness Bot ===");
        System.out.println("Type 'assess' to run the Return-to-Sport Assessment.");
        System.out.println("Type 'quit' to exit.\n");

        while (true) {
            System.out.print("you> ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            if (cmd.equals("quit")) break;

            if (cmd.equals("assess") || cmd.equals("checkin")) {
                Assessment a = runAssessment(scanner, state);

                ensureLogHeaderExists(LOG_FILE);
                appendAssessmentToCsv(LOG_FILE, a);

                System.out.println("\nCurrent Phase: " + state.phase.label());
                System.out.println("Current Step: " + state.step + "/3");
                System.out.println("Decision: " + a.decision + "\n");

                System.out.println("Today's Plan:");
                for (String line : generatePlan(state.phase, state.step, a.decision)) {
                    System.out.println("- " + line);
                }

                applyDecision(state, a.decision);
                state.save(STATE_FILE);

                System.out.println("\nUpdated Phase: " + state.phase.label());
                System.out.println("Updated Step: " + state.step + "/3\n");
            } else {
                System.out.println("Commands: assess | quit\n");
            }
        }

        scanner.close();
    }

    // =========================
    // ASSESSMENT
    // =========================
    private static Assessment runAssessment(Scanner sc, RTSState state) {
        System.out.println("\n--- Return-to-Sport Readiness Assessment ---");

        int painRest = askInt(sc, "Pain at rest (0–10): ", 0, 10);
        int painActivity = askInt(sc, "Pain during activity (0–10): ", 0, 10);
        int stiffness = askInt(sc, "Morning stiffness (0–10): ", 0, 10);
        double sleep = askDouble(sc, "Hours of sleep last night: ");

        boolean swelling = askYesNo(sc, "Any swelling? ");
        boolean sharpPain = askYesNo(sc, "Any sharp pain? ");
        boolean instability = askYesNo(sc, "Any instability/giving way? ");

        LastResponse response = askLastResponse(sc);
        boolean functionPass = askYesNo(sc, "Can you do a controlled single-leg squat to a chair? ");
        int confidence = askInt(sc, "Confidence moving today (0–10): ", 0, 10);

System.out.println("Primary injury area (if applicable):");
System.out.println("0 - None / Not applicable");
System.out.println("1 - Hamstring");
System.out.println("2 - Knee");
System.out.println("3 - Ankle / Foot");
System.out.println("4 - Hip / Groin");
System.out.println("5 - Lower Back");
System.out.println("6 - Upper Body");

int injuryChoice = askInt(sc, "Select (0–6): ", 0, 6);
InjuryType injury = InjuryType.values()[injuryChoice];
System.out.println("Selected injury: " + injury);

        String zone = triageZone(
                painRest, stiffness, sleep,
                swelling, instability, sharpPain, false,
                painActivity
        );

        Decision decision = decide(zone, response, functionPass, confidence, painActivity);

        return new Assessment(
                LocalDateTime.now().format(TS_FMT),
                zone, decision, state.phase, state.step
        );
    }

    // =========================
    // DECISION LOGIC
    // =========================
    private static Decision decide(
            String zone,
            LastResponse response,
            boolean functionPass,
            int confidence,
            int painActivity
    ) {
        if (zone.equals("RED")) return Decision.REGRESS;
        if (response == LastResponse.WORSE) return Decision.REGRESS;
        if (!functionPass || painActivity >= 5 || confidence <= 3) return Decision.HOLD;
        if (zone.equals("GREEN") && response == LastResponse.BETTER && confidence >= 6)
            return Decision.PROGRESS;
        return Decision.HOLD;
    }

    private static void applyDecision(RTSState state, Decision d) {
        if (d == Decision.PROGRESS) {
            if (state.step < 3) state.step++;
            else if (state.phase != RTSPhase.PHASE_5) {
                state.phase = RTSPhase.values()[state.phase.ordinal() + 1];
                state.step = 1;
            }
        } else if (d == Decision.REGRESS) {
            if (state.step > 1) state.step--;
            else if (state.phase != RTSPhase.PHASE_1) {
                state.phase = RTSPhase.values()[state.phase.ordinal() - 1];
                state.step = 3;
            }
        }
    }

    // =========================
    // PLANS
    // =========================
    private static List<String> generatePlan(RTSPhase phase, int step, Decision d) {
        List<String> p = new ArrayList<>();
        p.add("Warm-up: 10 minutes easy + mobility.");

        switch (phase) {
            case PHASE_1 -> p.add("Low-impact cardio + isometrics.");
            case PHASE_2 -> p.add("Base strength + easy conditioning.");
            case PHASE_3 -> p.add("Running & movement drills (controlled).");
            case PHASE_4 -> p.add("High-intensity practice (controlled).");
            case PHASE_5 -> p.add("Full return to sport.");
        }

        p.add("Step " + step + " of phase.");
        p.add("Rule: pain ≤ 3/10 and no next-day worsening.");

        // Phase 1 follow-up guidance
if (phase == RTSPhase.PHASE_1) {
    p.add("Follow-up guidance: reassess symptoms before progressing beyond Phase 1.");
    p.add("If symptoms are not clearly improving, remain in Phase 1 and adjust volume.");
}
        
        return p;
    }

    // =========================
    // TRIAGE (FOR TESTS)
    // =========================
    public static String triageZone(
            int painAtRest, int stiffness, double sleepHours,
            boolean swelling, boolean instability, boolean sharpPain, boolean poppingWithPain,
            int overallPain
    ) {
        if (swelling || instability || sharpPain || painAtRest >= 6) return "RED";
        if (overallPain >= 4 || stiffness >= 6 || sleepHours < 6) return "YELLOW";
        return "GREEN";
    }

    // =========================
    // REHAB (FOR TESTS)
    // =========================
    public static String getRehabPlan(int injuryChoice) {
        if (injuryChoice == 1) return "Hamstring rehab progression.";
        if (injuryChoice == 2) return "Knee rehab progression.";
        return "General rehab.";
    }

    // =========================
    // INPUT HELPERS
    // =========================
    private static boolean askYesNo(Scanner s, String q) {
        System.out.print(q + " (y/n): ");
        return s.nextLine().trim().toLowerCase().startsWith("y");
    }

    private static int askInt(Scanner s, String q, int min, int max) {
        while (true) {
            System.out.print(q);
            try {
                int v = Integer.parseInt(s.nextLine());
                if (v >= min && v <= max) return v;
            } catch (Exception ignored) {}
        }
    }

    private static double askDouble(Scanner s, String q) {
        while (true) {
            System.out.print(q);
            try {
                return Double.parseDouble(s.nextLine());
            } catch (Exception ignored) {}
        }
    }

    private static LastResponse askLastResponse(Scanner s) {
    System.out.print("Last session response (if applicable — better / same / worse, default = same): ");
    String r = s.nextLine().trim().toLowerCase();

    if (r.startsWith("b")) return LastResponse.BETTER;
    if (r.startsWith("w")) return LastResponse.WORSE;


    return LastResponse.SAME;
    }

    // =========================
    // DATA STRUCTURES
    // =========================
    enum Decision { PROGRESS, HOLD, REGRESS }
    enum LastResponse { BETTER, SAME, WORSE }

    enum InjuryType {
    NONE,
    HAMSTRING,
    KNEE,
    ANKLE_FOOT,
    HIP_GROIN,
    LOWER_BACK,
    UPPER_BODY
}

    enum RTSPhase {
        PHASE_1("Phase 1 – Symptoms & ROM"),
        PHASE_2("Phase 2 – Base Strength"),
        PHASE_3("Phase 3 – Movement"),
        PHASE_4("Phase 4 – Practice"),
        PHASE_5("Phase 5 – Return");

        private final String label;
        RTSPhase(String l) { label = l; }
        String label() { return label; }

    }

    static class RTSState {
        RTSPhase phase;
        int step;

        RTSState(RTSPhase p, int s) { phase = p; step = s; }

        static RTSState load(String path) {
            Properties p = new Properties();
            try (FileInputStream f = new FileInputStream(path)) {
                p.load(f);
            } catch (Exception ignored) {}
            int ph = Integer.parseInt(p.getProperty("phase", "1"));
            int st = Integer.parseInt(p.getProperty("step", "1"));
            return new RTSState(RTSPhase.values()[ph - 1], st);
        }

        void save(String path) {
            Properties p = new Properties();
            p.setProperty("phase", String.valueOf(phase.ordinal() + 1));
            p.setProperty("step", String.valueOf(step));
            try (FileOutputStream f = new FileOutputStream(path)) {
                p.store(f, "RTS state");
            } catch (Exception ignored) {}
        }
    }

    static class Assessment {
        String timestamp;
        String zone;
        Decision decision;
        RTSPhase phase;
        int step;

        Assessment(String t, String z, Decision d, RTSPhase p, int s) {
            timestamp = t;
            zone = z;
            decision = d;
            phase = p;
            step = s;
        }
    }

    // =========================
    // CSV LOGGING
    // =========================
    private static void ensureLogHeaderExists(String path) {
        File f = new File(path);
        if (f.exists()) return;
        try (PrintWriter w = new PrintWriter(path)) {
            w.println("timestamp,zone,decision,phase,step");
        } catch (Exception ignored) {}
    }

    private static void appendAssessmentToCsv(String path, Assessment a) {
        try (PrintWriter w = new PrintWriter(new FileWriter(path, true))) {
            w.println(a.timestamp + "," + a.zone + "," + a.decision + "," +
                      a.phase.label() + "," + a.step);
        } catch (Exception ignored) {}
    }
}
