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

import me.neilellis.dollar.relproxy.impl.jproxy.JProxyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author jmarranz
 */
public class CommandSave extends Command {
    public static final String NAME = "save";
    protected final String path;

    public CommandSave(JProxyShellProcessor parent, String url) {
        super(parent, NAME);
        this.path = url;
    }

    @Nullable public static CommandSave createCommandSave(JProxyShellProcessor parent, @NotNull String cmd) {
        String url = getParameter(NAME, cmd);
        if (url == null) {
            System.out.println("Command error: <path> parameter is required");
            return null;
        }

        return new CommandSave(parent, url);
    }

    @Override
    public boolean run() {
        try {
            List<String> codeBuffer = parent.getCodeBuffer();
            StringBuilder code = new StringBuilder();
            for (String line : codeBuffer) {
                code.append(line);
                code.append("\n");
            }
            byte[] content = code.toString().getBytes(parent.getEncoding()); // Como no conocemos encoding...
            JProxyUtil.saveFile(new File(path), content);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void runPostCommand() {
    }
}
