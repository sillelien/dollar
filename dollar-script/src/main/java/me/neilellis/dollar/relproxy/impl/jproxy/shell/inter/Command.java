/*
 * Copyright (c) 2014-2015 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar.relproxy.impl.jproxy.shell.inter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author jmarranz
 */
public abstract class Command {
    protected final JProxyShellProcessor parent;
    protected final String name;

    public Command(JProxyShellProcessor parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Nullable public static Command createCommand(JProxyShellProcessor parent, String cmd) {
        cmd = cmd.trim();
        if (cmd.equals("clear")) {
            return new CommandOther(parent, cmd);
        } else if (cmd.startsWith("delete")) {
            CommandDelete command = CommandDelete.createCommandDelete(parent, cmd);
            if (command != null) { return command; } else { return new CommandError(parent); }
        } else if (cmd.equals("display")) {
            return new CommandOther(parent, cmd);
        } else if (cmd.startsWith("edit")) {
            CommandEdit command = CommandEdit.createCommandEdit(parent, cmd);
            if (command != null) { return command; } else { return new CommandError(parent); }
        } else if (cmd.equals("exec")) {
            return new CommandOther(parent, cmd);
        } else if (cmd.equals("exit")) {
            return new CommandOther(parent, cmd);
        } else if (cmd.equals("help")) {
            return new CommandOther(parent, cmd);
        } else if (cmd.startsWith("insert")) {
            CommandInsert command = CommandInsert.createCommandInsert(parent, cmd);
            if (command != null) { return command; } else { return new CommandError(parent); }
        } else if (cmd.startsWith("load")) {
            CommandLoad command = CommandLoad.createCommandLoad(parent, cmd);
            if (command != null) { return command; } else { return new CommandError(parent); }
        } else if (cmd.equals("quit")) {
            return new CommandOther(parent, cmd);
        } else if (cmd.startsWith("save")) {
            CommandSave command = CommandSave.createCommandSave(parent, cmd);
            if (command != null) { return command; } else { return new CommandError(parent); }
        }

        return null; // No es un comando
    }

    @Nullable protected static String getParameter(String cmdName, @NotNull String cmd) {
        int pos = cmd.indexOf(cmdName + " ");
        if (pos != 0) { return null; }
        pos = cmd.indexOf(' ');
        String param = cmd.substring(pos + 1);
        param = param.trim();
        return param;
    }

    public abstract boolean run();

    public abstract void runPostCommand();
}
