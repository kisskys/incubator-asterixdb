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
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import edu.uci.ics.asterix.common.exceptions.AsterixException;
import edu.uci.ics.asterix.experiment.action.base.AbstractAction;

public abstract class AbstractExecutableAction extends AbstractAction {

    private static final Logger LOGGER = Logger.getLogger(AbstractExecutableAction.class.getName());

    protected Map<String, String> getEnvironment() {
        return Collections.<String, String> emptyMap();
    }

    protected abstract String getCommand();

    protected abstract boolean doExecute(String command, Map<String, String> env) throws Exception;

    protected abstract InputStream getErrorStream();

    protected abstract InputStream getInputStream();

    @Override
    protected void doPerform() throws Exception {
        StringWriter sw = new StringWriter();
        String cmd = getCommand();
        if (!doExecute(cmd, getEnvironment())) {
            IOUtils.copy(getErrorStream(), sw);
            throw new AsterixException("Error executing command: " + cmd + ".\n Error = " + sw.toString());
        } else {
            IOUtils.copy(getInputStream(), sw);
            IOUtils.copy(getErrorStream(), sw);
        }
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(sw.toString());
        }
    }
}
