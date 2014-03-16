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

import java.io.IOException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.host.controller.ServerInventory;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import com.redhat.gss.highcpu.data.CpuUsage;
import com.redhat.gss.highcpu.data.Snapshot;
import com.redhat.gss.highcpu.data.ThreadDump;
import com.redhat.gss.highcpu.gatherer.GathererFactory;
import com.redhat.gss.highcpu.gatherer.InformationGatherer;

public class ServerHighCpuDumpHandler extends AbstractServerOperationHandler {
    protected static final SimpleAttributeDefinition COUNT = new SimpleAttributeDefinitionBuilder(DiagnosticsConstants.COUNT, ModelType.INT, false)
        .setDefaultValue(new ModelNode(6))
        .build();
    protected static final SimpleAttributeDefinition DELAY = new SimpleAttributeDefinitionBuilder(DiagnosticsConstants.DELAY, ModelType.LONG, false)
    .setDefaultValue(new ModelNode(20000L))
    .build();

    public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder(DiagnosticsConstants.SERVER_HIGH_CPU_DUMP, DiagnosticsDescriptions.getResolver(DiagnosticsConstants.DIAGNOSTICS))
        .setParameters(SERVER, COUNT, DELAY)
        .setReplyType(ModelType.OBJECT)
        .setRuntimeOnly()
        .setReadOnly()
        .build();

    public ServerHighCpuDumpHandler(ServerInventory serverInventory) {
        super(serverInventory);
    }

    protected void performOperation(OperationContext context, ModelNode operation, int pid) {
        int count = operation.require(COUNT.getName()).asInt();
        long delay = operation.require(DELAY.getName()).asLong();

        InformationGatherer gatherer = GathererFactory.attach(Integer.toString(pid));

        ModelNode result = new ModelNode();
        try {
            for (int i = 0; i < count; i++) {
                ThreadDump td = gatherer.takeParsedDump();
                CpuUsage usage = gatherer.takeParsedThreadCpuUsage();
                Snapshot shapshot = new Snapshot(System.currentTimeMillis(), usage, td);

                ModelNode mn = SnapshotModelConverter.dumpToModel(shapshot);
                result.get(i).set(mn);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    //
                }
            }
        } finally {
            try {
                gatherer.close();
            } catch (IOException e) {
                //
            }
        }

        context.getResult().set(result);
        context.stepCompleted();
    }

}
