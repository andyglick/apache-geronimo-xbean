/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.shell.deploy;

import java.io.PrintWriter;

import org.apache.felix.gogo.commands.Command;
import org.apache.geronimo.cli.deployer.BaseCommandArgs;
import org.apache.geronimo.deployment.cli.AbstractCommand;
import org.apache.geronimo.deployment.cli.CommandLogin;
import org.apache.geronimo.deployment.cli.ConsoleReader;
import org.apache.geronimo.deployment.cli.ServerConnection;
import org.apache.geronimo.deployment.cli.StreamConsoleReader;
/**
 * @version $Rev$ $Date$
 */
@Command(scope = "deploy", name = "login", description = "Saves the username and password for this connection")
public class LoginCommand extends ConnectCommand {

    @Override
    protected Object doExecute() throws Exception {
        ServerConnection connection = connect();

        AbstractCommand command = new CommandLogin();

        ConsoleReader consoleReader = new StreamConsoleReader(session.getKeyboard(),new PrintWriter(session.getConsole(),true));

        BaseCommandArgs args = new BaseCommandArgs(new String[0]);

        command.execute(consoleReader, connection, args);
        return null;
    }
}