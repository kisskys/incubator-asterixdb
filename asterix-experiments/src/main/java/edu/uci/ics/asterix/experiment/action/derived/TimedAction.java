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

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.asterix.experiment.action.base.AbstractAction;
import edu.uci.ics.asterix.experiment.action.base.IAction;

public class TimedAction extends AbstractAction {

    private final Logger LOGGER = Logger.getLogger(TimedAction.class.getName());

    private final IAction action;

    public TimedAction(IAction action) {
        this.action = action;
    }

    @Override
    protected void doPerform() throws Exception {
        long start = System.currentTimeMillis();
        action.perform();
        long end = System.currentTimeMillis();
        if (LOGGER.isLoggable(Level.INFO)) {
            System.out.println("Elapsed time = " + (end - start) + " for action " + action);
        }
    }
}
