package org.jboss.as.platform.diagnostics;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author James Livingston (c) 2014 Red Hat Inc.
 */
public class DiagnosticsResourceDefinition extends SimpleResourceDefinition {
    static final DiagnosticsResourceDefinition INSTANCE = new DiagnosticsResourceDefinition();

    private DiagnosticsResourceDefinition() {
        super(DiagnosticsConstants.DIAGNOSTICS_PATH,
                DiagnosticsDescriptions.getResolver("diagnostics"));
    }

    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
        //resourceRegistration.registerSubModel(ClassLoadingResourceDefinition.INSTANCE);
    }

    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);

        resourceRegistration.registerOperationHandler(ThreadDumpHandler.DEFINITION, ThreadDumpHandler.INSTANCE);
    }
}
