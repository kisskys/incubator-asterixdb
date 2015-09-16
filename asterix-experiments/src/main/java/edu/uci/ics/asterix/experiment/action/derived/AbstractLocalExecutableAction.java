/*
 * Copyright 2009-2013 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.uci.ics.asterix.experiment.action.derived;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractLocalExecutableAction extends AbstractExecutableAction {

    private final ProcessBuilder pb;

    private Process p;

    protected AbstractLocalExecutableAction() {
        pb = new ProcessBuilder();
    }

    protected InputStream getErrorStream() {
        return p == null ? null : p.getErrorStream();
    }

    protected InputStream getInputStream() {
        return p == null ? null : p.getInputStream();
    }

    @Override
    protected boolean doExecute(String command, Map<String, String> env) throws Exception {
        List<String> cmd = Arrays.asList(command.split(" "));
        pb.command(cmd);
        pb.environment().putAll(env);
        p = pb.start();
        int exitVal = p.waitFor();
        return exitVal == 0;
    }
}
