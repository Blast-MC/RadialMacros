package tech.blastmc.radial.macros;

import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import tech.blastmc.radial.RadialMacros;
import tech.blastmc.radial.macros.RadialOption.RadialCommand;
import tech.blastmc.radial.macros.RadialOption.RadialCommand.CommandType;
import tech.blastmc.radial.util.ClientTickScheduler;

import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
public class CommandsRunner {

    private final List<RadialCommand> commands;

    public void run() {
        Iterator<RadialCommand> iterator = commands.iterator();
        next(iterator);
    }

    private void next(Iterator<RadialCommand> iterator) {
        if (!iterator.hasNext()) return;
        RadialCommand command = iterator.next();
        int wait = process(command);
        if (iterator.hasNext())
            if (wait == 0)
                next(iterator);
            else
                ClientTickScheduler.schedule(wait, () -> next(iterator));
    }

    private int process(RadialCommand command) {
        Minecraft mc = Minecraft.getInstance();
        var network = mc.getConnection();

        String value = command.getValue();
        value = VariableResolver.resolve(value);

        if (command.getType() == CommandType.COMMAND) {
            String raw = value.startsWith("/") ? value.substring(1) : value;
            network.sendCommand(raw);
        }
        if (command.getType() == CommandType.CHAT) {
            network.sendChat(value);
        }
        if (command.getType() == CommandType.SUGGEST) {
            mc.gui.setScreen(new ChatScreen(value, false));
        }
        if (command.getType() == CommandType.COPY) {
            mc.keyboardHandler.setClipboard(value);
        }

        int wait = 1;
        if (command.getType() == CommandType.WAIT) {
            try { wait = Integer.parseInt(value); }
            catch (NumberFormatException e) {
                RadialMacros.log("Error while parsing wait number: " + value);
            }
        }

        return wait;
    }

}
