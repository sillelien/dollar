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

import com.sillelien.dollar.SecondUnit;
import com.sillelien.dollar.api.Unit;
import com.sillelien.dollar.api.var;

import java.util.Date;

class HelloUnit extends Unit {

    static {
        mainClass(HelloUnit.class);
    }

    {
        var a = $("A");
        var profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        profile.$pipe(SecondUnit.class).out();
        profile.out();
        System.out.println(args);
        System.out.println(a);
        System.out.println(in);
    }


}
