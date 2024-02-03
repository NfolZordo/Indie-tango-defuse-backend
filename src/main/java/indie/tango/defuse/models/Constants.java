package indie.tango.defuse.models;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static class Scenario {
        public List<String> steps;

        public Scenario(String... steps) {
            this.steps = Arrays.asList(steps);
        }
    }

    public static final String switchButton = "switch";
    public static final String greenButton = "greenButton";
    public static final String blueButton = "blueButton";
    public static final String redButton = "redButton";
    public static final String yellowButton = "yellowButton";
    public static final String greenWire = "greenWire";
    public static final String blueWire = "blueWire";
    public static final String redWire = "redWire";
    public static final String yellowWire = "yellowWire";

    public static List<Scenario> scenarios = Arrays.asList(
            new Scenario(redButton, blueButton, switchButton, greenWire),
            new Scenario(yellowWire, greenButton, redButton, greenWire),
            new Scenario(blueWire, yellowButton, greenWire, redWire),
            new Scenario(greenButton, switchButton, blueWire, yellowWire),
            new Scenario(switchButton, yellowWire, yellowButton, redButton),
            new Scenario(blueWire, greenButton, redButton, yellowWire),
            new Scenario(redWire, redButton, greenWire, switchButton),
            new Scenario(blueWire, redWire, greenButton, yellowWire),
            new Scenario(yellowButton, switchButton, blueButton, greenWire),
            new Scenario(blueWire, yellowButton, redWire, greenButton)
    );
}
