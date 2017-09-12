/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar;

import dollar.api.DollarStatic;
import dollar.api.var;
import org.junit.jupiter.api.BeforeAll;

import java.util.Date;

class DollarPubSubTest {

    private static var profile;

    @BeforeAll
    public static void setUp() {
        profile = DollarStatic.$("name", "Neil")
                          .$("age", new Date().getYear() + 1900 - 1970)
                          .$("gender", "male")
                          .$("projects", DollarStatic.$jsonArray("snapito", "dollar"))
                          .$("location",
                             DollarStatic.$("city", "brighton")
                                     .$("postcode", "bn1 6jj")
                                     .$("number", 343)
                          );
    }


//    @Test
//    public void testBasic() throws InterruptedException {
//        final int[] received = {0};
//        Sub sub = $sub((message, s) -> {
//            System.out.println("Received");
//            received[0]++;
//            s.cancel();
//        }, "test.pub");
//        sub.await();
//        System.out.println("Subbed");
//        profile.$pub("test.pub");
//        sub.awaitFirst(60);
//        Thread.sleep(100);
//        assertEquals(1, received[0]);
//    }


}
