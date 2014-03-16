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
import org.jboss.as.host.controller.ServerInventory;
import org.jboss.dmr.ModelNode;

import com.redhat.gss.highcpu.gatherer.GathererFactory;
import com.redhat.gss.highcpu.gatherer.InformationGatherer;

public abstract class AbstractServerThreadDumpHandler extends AbstractServerOperationHandler {
    public AbstractServerThreadDumpHandler(ServerInventory serverInventory) {
        super(serverInventory);
    }

    protected void performOperation(OperationContext context, ModelNode operation, int pid) {
        InformationGatherer gatherer = GathererFactory.attach(Integer.toString(pid));
        try {
            performDump(gatherer, context);
        } finally {
            try {
                gatherer.close();
            } catch (IOException e) {
                //
            }
        }

        context.stepCompleted();
    }

    protected abstract void performDump(InformationGatherer gatherer, OperationContext context);
}