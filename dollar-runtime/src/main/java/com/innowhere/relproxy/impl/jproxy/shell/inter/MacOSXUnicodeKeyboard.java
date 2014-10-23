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

import static java.awt.Event.ALT_MASK;

/**
 * https://discussions.apple.com/thread/1899290
 * http://superuser.com/questions/13086/how-do-you-type-unicode-characters-using-hexadecimal-codes  (buscar OS X)
 * http://controlyourmac.com/2012/05/understanding-mac-keyboard.html
 *
 * @author jmarranz
 */
public class MacOSXUnicodeKeyboard extends Keyboard {
    protected Charset cs;

    public MacOSXUnicodeKeyboard(Charset cs) {
        this.cs = cs;
    }

    @Override
    public boolean type(char character) {
        if (super.type(character))
            return true;

        int bi = getUnicodeInt(cs, character);

        String unicodeDigits = Integer.toString(bi, 16); // En hexadecimal

        robot.keyPress(ALT_MASK);  // "Since the ALT_MASK modifier is the Option key in OS X" https://developer.apple.com/library/mac/documentation/java/conceptual/java14development/07-NativePlatformIntegration/NativePlatformIntegration.html     

        try {
            for (int i = 0; i < unicodeDigits.length(); i++) {
                type(unicodeDigits.charAt(i));
            }
        } finally {
            robot.keyRelease(ALT_MASK);
        }

        return true;
    }

}