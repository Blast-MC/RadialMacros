package tech.blastmc.radial.config;

import com.google.gson.JsonObject;
import tech.blastmc.radial.RadialMacros;
import tech.blastmc.radial.config.migrators.Migrator1_1;
import tech.blastmc.radial.macros.db.Database;

import java.util.HashMap;
import java.util.Map;

public abstract class DataMigrator {

    private static final Map<String, DataMigrator> MIGRATORS = new HashMap<>();

    public static void update(JsonObject json, String version) {
        String targetVersion = Database.get().getVersion();

        RadialMacros.log("Running data migrator from version " + version + " to " + targetVersion);

        Version current = Version.parse(version);
        Version target = Version.parse(targetVersion);

        if (current.compareTo(target) > 0)
            throw new IllegalArgumentException("Data version " + current + " is newer than supported version " + target);

        while (current.compareTo(target) < 0) {
            Version next = current.nextMinor();
            String nextVersion = next.toString();

            DataMigrator migrator = MIGRATORS.get(nextVersion);

            if (migrator != null) {
                RadialMacros.log("Applying data migrator for version " + nextVersion);
                migrator.apply(json);
            } else {
                RadialMacros.log("No data migrator exists for version " + nextVersion);
            }

            current = next;
            Database.get().setVersion(current.toString());
        }
    }

    static {
        MIGRATORS.put("1.1", new Migrator1_1());
    }

    private record Version(int major, int minor) implements Comparable<Version> {

        private static Version parse(String value) {
            String[] parts = value.split("\\.");

            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid version: " + value);

            try {
                return new Version(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1])
                );
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid version: " + value, exception);
            }
        }

        private Version nextMinor() {
            return new Version(major, minor + 1);
        }

        @Override
        public int compareTo(Version other) {
            int majorComparison = Integer.compare(major, other.major);

            if (majorComparison != 0)
                return majorComparison;

            return Integer.compare(minor, other.minor);
        }

        @Override
        public String toString() {
            return major + "." + minor;
        }
    }

    public abstract void apply(JsonObject json);

}
