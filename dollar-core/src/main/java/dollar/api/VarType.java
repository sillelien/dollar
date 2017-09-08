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

package dollar.api;

public class VarType {
    private final boolean declaration;
    private final boolean fixed;
    private final boolean isVolatile;
    private final boolean numeric;
    private final boolean pure;
    private final boolean readonly;
    private boolean parameter;

    /**
     * @param readonly    true if the variable cannot be changed
     * @param isVolatile  true if the variable can be accessed by multiple threads
     * @param fixed       true if the variable is a fixed/static value
     * @param pure        true if the variable contains a pure value (i.e. a pure function/expession)
     * @param numeric
     * @param declaration true if this is the first use (declaration)
     */
    public VarType(boolean readonly, boolean isVolatile, boolean fixed, boolean pure, boolean numeric, boolean declaration) {
        this.readonly = readonly;
        this.isVolatile = isVolatile;
        this.fixed = fixed;
        this.pure = pure;
        this.numeric = numeric;
        this.declaration = declaration;
    }

    /**
     * @param readonly    true if the variable cannot be changed
     * @param isVolatile  true if the variable can be accessed by multiple threads
     * @param declaration true if this is the first use (declaration)
     */
    public VarType(boolean readonly, boolean isVolatile, boolean declaration, boolean pure) {

        this.readonly = readonly;
        this.isVolatile = isVolatile;
        this.declaration = declaration;
        fixed = false;
        this.pure = pure;
        numeric = false;
    }

    public boolean isDeclaration() {
        return declaration;
    }


    public boolean isFixed() {
        return fixed;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean isParameter() {
        return parameter;
    }

    public void setParameter(boolean parameter) {
        this.parameter = parameter;
    }

    public boolean isPure() {
        return pure;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public boolean isVolatile() {
        return isVolatile;
    }
}
