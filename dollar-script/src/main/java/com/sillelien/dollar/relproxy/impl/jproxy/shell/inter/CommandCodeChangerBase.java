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

package com.sillelien.dollar.relproxy.impl.jproxy.shell.inter;

import org.jetbrains.annotations.NotNull;

/**
 * @author jmarranz
 */
public abstract class CommandCodeChangerBase extends Command {
    public static final int ERROR_LAST_REQUIRED = -1;
    public static final int ERROR_NO_LAST_LINE = -2;
    public static final int ERROR_NOT_A_NUMBER = -3;
    public static final int ERROR_VALUE_NOT_0_OR_NEGATIVE = -4;
    public static final int ERROR_LINE_1_NOT_VALID = -5;
    public static final int ERROR_OUT_OF_RANGE = -6;


    protected final int line;

    public CommandCodeChangerBase(JProxyShellProcessor parent, String name, int line) {
        super(parent, name);
        this.line = line;
    }

    public static int getLineFromParam(@NotNull JProxyShellProcessor parent, String name, @NotNull String cmd) {
        String param = getParameter(name, cmd);
        if (param == null) {
            return ERROR_LAST_REQUIRED;
        }

        int line;
        if (param.equals("last")) {
            int lastLine = parent.getLastLine();
            if (lastLine == -1) {
                return ERROR_NO_LAST_LINE;
            }
            line = lastLine;
        } else {
            try {
                line = Integer.parseInt(param);
            } catch (NumberFormatException ex) {
                return ERROR_NOT_A_NUMBER;
            }
            // Ojo es el valor dado por el usuario (empezando en 1 y con línea vacía)
            if (line <= 0) {
                return ERROR_VALUE_NOT_0_OR_NEGATIVE;
            } else if (line == 1) {
                return ERROR_LINE_1_NOT_VALID;
            }
            line -= JProxyShellProcessor.LINE_OFFSET;

            if (line >= parent.getCodeBuffer().size()) {
                return ERROR_OUT_OF_RANGE;
            }
        }
        return line;
    }


    @Override
    public boolean run() {
        return true;
    }

}
