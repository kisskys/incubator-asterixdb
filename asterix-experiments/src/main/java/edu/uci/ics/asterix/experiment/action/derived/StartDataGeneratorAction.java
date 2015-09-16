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

import java.io.StringWriter;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.apache.commons.io.IOUtils;

import edu.uci.ics.asterix.experiment.action.base.AbstractAction;

public class StartDataGeneratorAction extends AbstractAction {

    @Override
    protected void doPerform() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        SSHClient sshClient = new SSHClient();
        sshClient.loadKnownHosts();
        sshClient.connect("asterix-1.ics.uci.edu");
        sshClient.authPublickey("zheilbro", "/Users/zheilbron/.ssh/id_rsa");
        Session session = sshClient.startSession();
        Command lsCmd = session.exec("ls");
        StringWriter sw = new StringWriter();
        IOUtils.copy(lsCmd.getInputStream(), sw);
        IOUtils.copy(lsCmd.getErrorStream(), sw);
        System.out.println(sw.toString());
        session.close();
        sw.close();
        sshClient.close();
    }
}
