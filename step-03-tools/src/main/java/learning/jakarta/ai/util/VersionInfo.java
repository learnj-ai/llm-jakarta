package learning.jakarta.ai.util;


public class VersionInfo {

    public static final String VERSION_PROPERTY = "version";
    public static final String COMPILE_DEFAULT_ARCHETYPE_VERSION = "2.2.3";
    public static final String ARCHETYPE_VERSION_ENV_VAR = System.getenv("ARCHETYPE_VERSION");
    public static final String ARCHETYPE_VERSION = (ARCHETYPE_VERSION_ENV_VAR != null)
            ? System.getenv("ARCHETYPE_VERSION")
            : COMPILE_DEFAULT_ARCHETYPE_VERSION;
}
