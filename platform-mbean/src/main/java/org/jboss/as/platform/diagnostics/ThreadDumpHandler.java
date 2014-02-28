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

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.as.platform.mbean.PlatformMBeanConstants;
import org.jboss.as.platform.mbean.ThreadMXBeanDumpAllThreadsHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author James Livingston (c) 2014 Red Hat Inc.
 */
public class ThreadDumpHandler implements OperationStepHandler {
    public static final ThreadDumpHandler INSTANCE = new ThreadDumpHandler();
    public static final OperationDefinition DEFINITION = new SimpleOperationDefinitionBuilder("thread-dump", DiagnosticsDescriptions.getResolver(DiagnosticsConstants.DIAGNOSTICS))
            .setReplyType(ModelType.STRING)
            .setRuntimeOnly()
            .setReadOnly()
            .withFlag(OperationEntry.Flag.HOST_CONTROLLER_ONLY)
            .build();

    private ThreadDumpHandler() {

    }

    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        System.out.println(context.getCallEnvironment().getProcessType());
        new RuntimeException().printStackTrace(System.out);

        switch (context.getCallEnvironment().getProcessType()) {
        case STANDALONE_SERVER:
        case HOST_CONTROLLER:
            // run on own JVM
            ModelNode subop = new ModelNode();
            subop.get(PlatformMBeanConstants.LOCKED_MONITORS).set(true);
            subop.get(PlatformMBeanConstants.LOCKED_SYNCHRONIZERS).set(true);
            ThreadMXBeanDumpAllThreadsHandler.INSTANCE.execute(context, subop);
            break;
        case DOMAIN_SERVER:
            // server in a domain
            context.getResult().set("THREAD DATA");
            context.stepCompleted();
            break;
        default:
            throw new OperationFailedException("Attempting to dump thread of unexpected server type");
        }
    }
}
