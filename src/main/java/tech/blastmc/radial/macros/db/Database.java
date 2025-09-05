package tech.blastmc.radial.macros.db;

import org.lwjgl.glfw.GLFW;
import tech.blastmc.radial.config.Config;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;

import java.util.ArrayList;
import java.util.List;

public class Database {

    private final String version = "1.0";

    public List<RadialGroup> groups = new ArrayList<>() {{
        add(new RadialGroup("Example Group", GLFW.GLFW_KEY_Y, new ArrayList<>()));
    }};

    public static List<RadialGroup> getGroups() {
        return get().groups;
    }

    public static void setGroups(List<RadialGroup> groups) {
        if (groups == null)
            return;
        get().groups = groups;

        for (RadialGroup group : get().groups)
            for (RadialOption option : group.getOptions())
                option.setGroup(group);

    }

    public static List<RadialGroup> getGroupsForEdit() {
        return new ArrayList<>(get().getGroups());
    }

    public static void commit(List<RadialGroup> groups) {
        Database.get().groups = groups;
        Config.save();
    }

    private static Database INSTANCE;

    public static Database get() {
        if (INSTANCE == null)
            INSTANCE = new Database();
        return INSTANCE;
    }

}
