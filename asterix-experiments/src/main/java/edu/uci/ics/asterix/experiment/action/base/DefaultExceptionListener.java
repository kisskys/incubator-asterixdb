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
package edu.uci.ics.asterix.experiment.action.base;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultExceptionListener implements IExceptionListener {

    private static final Logger LOGGER = Logger.getLogger(DefaultExceptionListener.class.getName());

    @Override
    public void caughtException(Throwable t) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.severe("Caught exception: " + t);
            LOGGER.severe("Stopping...");
            t.printStackTrace();
        }
        System.exit(1);
    }
}
