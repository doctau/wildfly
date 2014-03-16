/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.host.controller.ServerInventory;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import com.redhat.gss.highcpu.data.ThreadDump;
import com.redhat.gss.highcpu.gatherer.InformationGatherer;

/**
 * @author James Livingston (c) 2014 Red Hat Inc.
 */
public class ServerParsedThreadDumpHandler extends AbstractServerThreadDumpHandler {
    public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder(DiagnosticsConstants.SERVER_PARSED_THREAD_DUMP, DiagnosticsDescriptions.getResolver(DiagnosticsConstants.DIAGNOSTICS))
            .setParameters(SERVER)
            .setReplyType(ModelType.STRING)
            .setRuntimeOnly()
            .setReadOnly()
            .build();

    public ServerParsedThreadDumpHandler(ServerInventory serverInventory) {
        super(serverInventory);

    }

    protected void performDump(InformationGatherer gatherer, OperationContext context) {
        ThreadDump dump = gatherer.takeParsedDump();
        ModelNode mn = ThreadDumpModelConverter.dumpToModel(dump);
        context.getResult().set(mn);
    }
}
