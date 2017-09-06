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

package dollar.internal.runtime.script;

public final class Constants {
    public static final double YEAR_IN_DAYS = 365.25;
    public static final double MONTH_IN_DAYS = YEAR_IN_DAYS / 12.0;
    public static final double WEEK_IN_DAYS = 7.0;
    public static final double DAY_IN_HOURS = 24.0;
    public static final double DAY_IN_MINUTES = DAY_IN_HOURS * 60.0;
    public static final double DAY_IN_SECONDS = DAY_IN_MINUTES * 60.0;
    public static final double DAY_IN_MILLIS = DAY_IN_SECONDS * 1000.0;
    public static final long MAX_MULTIPLY = 100000;
}
