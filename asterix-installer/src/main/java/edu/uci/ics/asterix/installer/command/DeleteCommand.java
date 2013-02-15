/*
 * Copyright 2009-2012 by The Regents of the University of California
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
package edu.uci.ics.asterix.installer.command;

import java.util.logging.Level;

import org.kohsuke.args4j.Option;

import edu.uci.ics.asterix.event.driver.EventDriver;
import edu.uci.ics.asterix.event.schema.pattern.Patterns;
import edu.uci.ics.asterix.installer.driver.ManagixUtil;
import edu.uci.ics.asterix.installer.error.OutputHandler;
import edu.uci.ics.asterix.installer.events.PatternCreator;
import edu.uci.ics.asterix.installer.model.AsterixInstance;
import edu.uci.ics.asterix.installer.model.AsterixInstance.State;
import edu.uci.ics.asterix.installer.service.ServiceProvider;

public class DeleteCommand extends AbstractCommand {

    @Override
    protected void execCommand() throws Exception {
        String asterixInstanceName = ((DeleteConfig) config).name;
        AsterixInstance instance = ManagixUtil.validateAsterixInstanceExists(asterixInstanceName, State.INACTIVE);
        PatternCreator pc = new PatternCreator();
        Patterns patterns = pc.createDeleteInstancePattern(instance);
        EventDriver.getClient(instance.getCluster(), false, OutputHandler.INSTANCE).submit(patterns);
        ServiceProvider.INSTANCE.getLookupService().removeAsterixInstance(asterixInstanceName);
        LOGGER.log(Level.INFO, " Asterix instance: " + asterixInstanceName + " deleted");
    }

    @Override
    protected CommandConfig getCommandConfig() {
        return new DeleteConfig();
    }

}

class DeleteConfig implements CommandConfig {

    @Option(name = "-h", required = false, usage = "Help")
    public boolean help = false;

    @Option(name = "-n", required = false, usage = "Name of Asterix Instance")
    public String name;

}