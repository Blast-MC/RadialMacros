package tech.blastmc.radial.config.migrators;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tech.blastmc.radial.config.DataMigrator;

public class Migrator1_1 extends DataMigrator {

    @Override
    public void apply(JsonObject json) {
        JsonArray groups = json.getAsJsonArray("groups");

        if (groups == null)
            return;

        for (JsonElement groupElement : groups) {
            if (!groupElement.isJsonObject())
                continue;

            JsonObject group = groupElement.getAsJsonObject();
            JsonArray options = group.getAsJsonArray("options");

            if (options == null)
                continue;

            for (JsonElement optionElement : options) {
                if (!optionElement.isJsonObject())
                    continue;

                JsonObject option = optionElement.getAsJsonObject();
                JsonArray oldCommands = option.getAsJsonArray("commands");

                if (oldCommands == null)
                    continue;

                JsonArray newCommands = new JsonArray();

                for (JsonElement commandElement : oldCommands) {
                    if (!commandElement.isJsonPrimitive()
                            || !commandElement.getAsJsonPrimitive().isString())
                        continue;

                    JsonObject command = new JsonObject();
                    command.addProperty("type", "COMMAND");
                    command.addProperty("value", commandElement.getAsString());

                    newCommands.add(command);
                }

                option.add("commands", newCommands);
            }
        }
    }

}
