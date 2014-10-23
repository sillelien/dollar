/*
 *
 *  * See: https://github.com/jmarranz
 *  *
 *  * Copyright (c) 2014 Jose M. Arranz (additional work by Neil Ellis)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.innowhere.relproxy.impl.jproxy.shell.inter;

import com.innowhere.relproxy.impl.jproxy.JProxyUtil;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * @author jmarranz
 */
public class CommandLoad extends Command {
    public static final String NAME = "load";
    protected String url;

    public CommandLoad(JProxyShellProcessor parent, String url) {
        super(parent, NAME);
        this.url = url;
    }

    public static CommandLoad createCommandLoad(JProxyShellProcessor parent, String cmd) {
        String url = getParameter(NAME, cmd);
        if (url == null) {
            System.out.println("Command error: <url> parameter is required");
            return null;
        }

        return new CommandLoad(parent, url);
    }

    @Override
    public boolean run() {
        try {
            byte[] content;
            URI uri = new URI(url);
            if (uri.getScheme() == null) // Archivo
            {
                File file = new File(url);
                content = JProxyUtil.readFile(file);
            } else // URL (incluyendo file:///...)
            {
                URL urlObj = new URL(url);
                content = JProxyUtil.readURL(urlObj); // Como no conocemos encoding...
            }

            String code = new String(content, parent.getEncoding()); // Como no conocemos encoding...
            LinkedList<String> lines = new LinkedList<String>();
            Scanner scanner = new Scanner(code);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lines.add(line);
            }

            parent.setCodeBuffer(lines);
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
