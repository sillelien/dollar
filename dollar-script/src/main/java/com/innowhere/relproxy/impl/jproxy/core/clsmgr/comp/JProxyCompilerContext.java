/*
 * Copyright (c) 2014 Neil Ellis
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

package com.innowhere.relproxy.impl.jproxy.core.clsmgr.comp;

import com.innowhere.relproxy.RelProxyException;
import com.innowhere.relproxy.jproxy.JProxyDiagnosticsListener;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.util.List;

/**
 * @author jmarranz
 */
public class JProxyCompilerContext {
    protected StandardJavaFileManager standardFileManager;
    protected DiagnosticCollector<JavaFileObject> diagnostics;
    protected JProxyDiagnosticsListener diagnosticsListener;

    public JProxyCompilerContext(StandardJavaFileManager standardFileManager, DiagnosticCollector<JavaFileObject> diagnostics, JProxyDiagnosticsListener diagnosticsListener) {
        this.standardFileManager = standardFileManager;
        this.diagnostics = diagnostics;
        this.diagnosticsListener = diagnosticsListener;
    }

    public StandardJavaFileManager getStandardFileManager() {
        return standardFileManager;
    }

    public DiagnosticCollector<JavaFileObject> getDiagnosticCollector() {
        return diagnostics;
    }

    public void close() {
        try {
            this.standardFileManager.close();
        } catch (IOException ex) {
            throw new RelProxyException(ex);
        }

        List<Diagnostic<? extends JavaFileObject>> diagList = diagnostics.getDiagnostics();
        if (!diagList.isEmpty()) {
            if (diagnosticsListener != null) {
                diagnosticsListener.onDiagnostics(diagnostics);
            } else {
                int i = 1;
                for (Diagnostic diagnostic : diagList) {
                    System.err.println("Diagnostic " + i);
                    System.err.println("  code: " + diagnostic.getCode());
                    System.err.println("  kind: " + diagnostic.getKind());
                    System.err.println("  line number: " + diagnostic.getLineNumber());
                    System.err.println("  column number: " + diagnostic.getColumnNumber());
                    System.err.println("  start position: " + diagnostic.getStartPosition());
                    System.err.println("  position: " + diagnostic.getPosition());
                    System.err.println("  end position: " + diagnostic.getEndPosition());
                    System.err.println("  source: " + diagnostic.getSource());
                    System.err.println("  message: " + diagnostic.getMessage(null));
                    i++;
                }
            }
        }
    }
}
