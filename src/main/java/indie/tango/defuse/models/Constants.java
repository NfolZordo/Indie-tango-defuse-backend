package indie.tango.defuse.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<String> imgId = Arrays.asList(
            "1nx-MsLVgF5ywZZYqF96FrLi83b3y5jpV",
            "1d_V3nnL2_lK-FDK44JHhgALqyGflNnKx",
            "1fne3_taCvjl-5408UvFIJpaaFNlhn-Lc",
            "1z0H8Tvphieo_U19A7eHKk4nWnm4nu8aT",
            "1wAj2PtgD2UBzuynZMsIa9z5iq9Xhg_fR",
            "1WVS6wqlYP42rUskNFH7lqBQUWtDszo5W",
            "1xi5BtSmE4vryf67uZN4v5oiJnpmEo9Li",
            "1c_GG6j6q8ZxI4iQKcO6sJR1sRAYX1p_n",
            "19IS_bNdw-WErSQGVrZnkl6nwD__ZyX71",
            "1SVacGw_lxFuwP5Q1D-bl2n3FJLJHmO2F"
    );
}
