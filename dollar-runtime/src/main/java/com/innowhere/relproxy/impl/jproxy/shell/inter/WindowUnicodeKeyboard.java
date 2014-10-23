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

import java.nio.charset.Charset;

import static java.awt.event.KeyEvent.*;

/**
 * http://stackoverflow.com/questions/1248510/convert-string-to-keyevents
 *
 * @author jmarranz
 */
public class WindowUnicodeKeyboard extends Keyboard {
    protected Charset cs;

    public WindowUnicodeKeyboard(Charset cs) {
        this.cs = cs;
    }

    @Override
    public boolean type(char character) {
        if (super.type(character))
            return true;

        // En Windows usar mintty porque usando la consola de MSYS por sí misma, que es realmente la de Windows, hay problemas con el set de caracteres, pues sería Cp1252 para Java pero Cp850 para la consola y salen mal por tanto los caracteres no ASCII

        int bi = getUnicodeInt(cs, character);

        String unicodeDigits = String.valueOf(bi);
        robot.keyPress(VK_ALT);
        try {
            for (int i = 0; i < unicodeDigits.length(); i++) {
                typeNumPad(Integer.parseInt(unicodeDigits.substring(i, i + 1)));
            }
        } finally {
            robot.keyRelease(VK_ALT);
        }


        /* Alternativa
        String unicodeDigits = String.valueOf(Character.codePointAt(new char[]{character},0));

        robot.keyPress(VK_ALT);
        for (int i = 0; i < unicodeDigits.length(); i++) {
            typeNumPad(Integer.parseInt(unicodeDigits.substring(i, i + 1)));
        }
        robot.keyRelease(VK_ALT);
        */

        return true;
    }

    private void typeNumPad(int digit) {
        switch (digit) {
            case 0:
                doType(VK_NUMPAD0);
                break;
            case 1:
                doType(VK_NUMPAD1);
                break;
            case 2:
                doType(VK_NUMPAD2);
                break;
            case 3:
                doType(VK_NUMPAD3);
                break;
            case 4:
                doType(VK_NUMPAD4);
                break;
            case 5:
                doType(VK_NUMPAD5);
                break;
            case 6:
                doType(VK_NUMPAD6);
                break;
            case 7:
                doType(VK_NUMPAD7);
                break;
            case 8:
                doType(VK_NUMPAD8);
                break;
            case 9:
                doType(VK_NUMPAD9);
                break;
            default:  // Para que se calle el FindBugs
        }
    }

}