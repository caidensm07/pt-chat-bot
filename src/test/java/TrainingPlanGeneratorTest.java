import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrainingPlanGeneratorTest {

    @Test
    void redZoneIfSwelling() {
        String zone = TrainingPlanGenerator.triageZone(
                0, 0, 8.0,
                true, false, false, false,
                1
        );
        assertEquals("RED", zone);
    }

    @Test
    void redZoneIfHighRestPain() {
        String zone = TrainingPlanGenerator.triageZone(
                6, 2, 8.0,
                false, false, false, false,
                2
        );
        assertEquals("RED", zone);
    }

    @Test
    void yellowZoneIfLowSleep() {
        String zone = TrainingPlanGenerator.triageZone(
                0, 2, 5.5,
                false, false, false, false,
                2
        );
        assertEquals("YELLOW", zone);
    }

    @Test
    void greenZoneIfStable() {
        String zone = TrainingPlanGenerator.triageZone(
                0, 2, 8.0,
                false, false, false, false,
                2
        );
        assertEquals("GREEN", zone);
    }

    @Test
    void rehabPlanHamstringContainsHamstring() {
        String plan = TrainingPlanGenerator.getRehabPlan(1);
        assertTrue(plan.toLowerCase().contains("hamstring"));
    }
}
