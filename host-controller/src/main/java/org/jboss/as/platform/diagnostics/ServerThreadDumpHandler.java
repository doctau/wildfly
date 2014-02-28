/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.platform.diagnostics;

import java.io.IOException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.as.host.controller.ServerInventory;
import org.jboss.as.process.ProcessInfo;
import org.jboss.as.process.ProcessUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import com.redhat.gss.highcpu.gatherer.GathererFactory;
import com.redhat.gss.highcpu.gatherer.InformationGatherer;

/**
 * @author James Livingston (c) 2014 Red Hat Inc.
 */
public class ServerThreadDumpHandler implements OperationStepHandler {
    private static final SimpleAttributeDefinition SERVER = new SimpleAttributeDefinitionBuilder("server", ModelType.STRING, false).build();

    public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder("server-thread-dump", DiagnosticsDescriptions.getResolver(DiagnosticsConstants.DIAGNOSTICS))
            .setParameters(SERVER)
            .setReplyType(ModelType.STRING)
            .setRuntimeOnly()
            .setReadOnly()
            .withFlag(OperationEntry.Flag.HOST_CONTROLLER_ONLY)
            .build();


    private final ParametersValidator paramValidator = new ParametersValidator();
    private final ServerInventory serverInventory;

    public ServerThreadDumpHandler(ServerInventory serverInventory) {
        this.serverInventory = serverInventory;
        paramValidator.registerValidator("server", new ModelTypeValidator(ModelType.STRING));

    }

    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        paramValidator.validate(operation);

        String target = operation.require("server").asString();

        String serverName = serverInventory.getServerProcessName(target);
        ProcessInfo pi = serverInventory.determineRunningProcesses().get(serverName);
        if (pi == null) {
            throw new OperationFailedException("Server '" + target + "' does not exist'");
        }

        int pid = ProcessUtils.getProcessId(serverName);
        if (pid == -1) {
            throw new OperationFailedException("Failed to retrieve process ID for server");
        }

        InformationGatherer gatherer = GathererFactory.attach(Integer.toString(pid));
        try {
            String dump = gatherer.takeTextDump();
            context.getResult().set(dump);
        } finally {
            try {
                gatherer.close();
            } catch (IOException e) {
                //
            }
        }

        context.stepCompleted();
    }
}
