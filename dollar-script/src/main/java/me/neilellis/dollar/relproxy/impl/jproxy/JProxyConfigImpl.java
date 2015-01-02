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

package me.neilellis.dollar.relproxy.impl.jproxy;

import me.neilellis.dollar.relproxy.RelProxyException;
import me.neilellis.dollar.relproxy.RelProxyOnReloadListener;
import me.neilellis.dollar.relproxy.impl.GenericProxyConfigBaseImpl;
import me.neilellis.dollar.relproxy.jproxy.JProxyConfig;
import me.neilellis.dollar.relproxy.jproxy.JProxyDiagnosticsListener;
import org.jetbrains.annotations.NotNull;

/**
 * @author jmarranz
 */
public class JProxyConfigImpl extends GenericProxyConfigBaseImpl implements JProxyConfig {
    protected String inputPath;
    protected String classFolder;
    protected long scanPeriod = -1;
    protected Iterable<String> compilationOptions;
    protected JProxyDiagnosticsListener diagnosticsListener;
    protected boolean test = false;

    public String getClassFolder() {
        return classFolder;
    }

    public Iterable<String> getCompilationOptions() {
        return compilationOptions;
    }

    public String getInputPath() {
        return inputPath;
    }

    public JProxyDiagnosticsListener getJProxyDiagnosticsListener() {
        return diagnosticsListener;
    }

    public long getScanPeriod() {
        return scanPeriod;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    @NotNull @Override
    public JProxyConfig setClassFolder(String classFolder) {
        this.classFolder = classFolder;
        return this;
    }

    @NotNull @Override
    public JProxyConfig setCompilationOptions(Iterable<String> compilationOptions) {
        this.compilationOptions = compilationOptions;
        return this;
    }

    @NotNull @Override
    public JProxyConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @NotNull @Override
    public JProxyConfig setInputPath(String inputPath) {
        this.inputPath = inputPath;
        return this;
    }

    @NotNull @Override
    public JProxyConfig setJProxyDiagnosticsListener(JProxyDiagnosticsListener diagnosticsListener) {
        this.diagnosticsListener = diagnosticsListener;
        return this;
    }

    @NotNull @Override
    public JProxyConfig setRelProxyOnReloadListener(RelProxyOnReloadListener relListener) {
        this.relListener = relListener;
        return this;
    }

    @NotNull @Override
    public JProxyConfig setScanPeriod(long scanPeriod) {
        if (scanPeriod == 0) { throw new RelProxyException("scanPeriod cannot be zero"); }
        this.scanPeriod = scanPeriod;
        return this;
    }

}
