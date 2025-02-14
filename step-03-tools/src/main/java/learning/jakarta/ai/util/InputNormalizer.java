package learning.jakarta.ai.util;

public class InputNormalizer {

    private InputNormalizer() {}

    public static double normalizeVersion(String input) {
        return Double.parseDouble(normalizeJakartaVersion(input));
    }

    public static String normalizeJakartaVersion(String input) {
        String lowerCaseInput = input.toLowerCase();
        if (lowerCaseInput.contains("11")) return "11";
        if (lowerCaseInput.contains("10")) return "10";
        if (lowerCaseInput.contains("9.1")) return "9.1";
        if (lowerCaseInput.contains("9")) return "9";
        if (lowerCaseInput.contains("8")) return "8";
        throw new IllegalArgumentException("Unsupported Jakarta EE version: " + input);
    }

    public static String normalizeProfile(String input) {
        String lowerCaseInput = input.toLowerCase().trim();
        if (lowerCaseInput.contains("full") || lowerCaseInput.contains("platform")) return "full";
        if (lowerCaseInput.contains("web")) return "web";
        if (lowerCaseInput.contains("core")) return "core";
        throw new IllegalArgumentException("Unsupported profile: " + input);
    }

    public static int normalizeJavaVersion(String input) {
        if (input.contains("21")) return 21;
        if (input.contains("17")) return 17;
        if (input.contains("11")) return 11;
        if (input.contains("8")) return 8;
        throw new IllegalArgumentException("Unsupported Java version: " + input);
    }

    public static boolean normalizeDocker(String input) {
        String lowerCaseInput = input.toLowerCase();
        if (lowerCaseInput.contains("yes") || lowerCaseInput.contains("true") || lowerCaseInput.contains("docker")) {
            return true;
        }
        if (lowerCaseInput.contains("no") || lowerCaseInput.contains("false")) {
            return false;
        }
        throw new IllegalArgumentException("Unsupported Docker flag: " + input);
    }

    public static String normalizeRuntime(String input) {
        String lowerCaseInput = input.toLowerCase();
        if (lowerCaseInput.contains("glassfish")) return "glassfish";
        if (lowerCaseInput.contains("open liberty") || lowerCaseInput.contains("liberty")) return "open-liberty";
        if (lowerCaseInput.contains("payara")) return "payara";
        if (lowerCaseInput.contains("tomee")) return "tomee";
        if (lowerCaseInput.contains("wildfly") || lowerCaseInput.contains("wild-fly")) return "wildfly";
        if (lowerCaseInput.contains("none")) return "none";
        throw new IllegalArgumentException("Unsupported runtime: " + input);
    }

    public static String normalizeArtifactId(String input) {
        return input.toLowerCase().replace(" ", "-");
    }
}