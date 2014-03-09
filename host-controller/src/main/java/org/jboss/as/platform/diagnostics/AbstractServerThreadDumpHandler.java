package org.jboss.as.platform.diagnostics;

import java.io.IOException;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.host.controller.ServerInventory;
import org.jboss.as.process.ProcessInfo;
import org.jboss.as.process.ProcessUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import com.redhat.gss.highcpu.gatherer.GathererFactory;
import com.redhat.gss.highcpu.gatherer.InformationGatherer;

public abstract class AbstractServerThreadDumpHandler {
    protected static final SimpleAttributeDefinition SERVER = new SimpleAttributeDefinitionBuilder(DiagnosticsConstants.SERVER, ModelType.STRING, false).build();

    private final ParametersValidator paramValidator = new ParametersValidator();
    private final ServerInventory serverInventory;

    public AbstractServerThreadDumpHandler(ServerInventory serverInventory) {
        this.serverInventory = serverInventory;
        paramValidator.registerValidator(SERVER.getName(), new StringLengthValidator(1));
    }

    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        paramValidator.validate(operation);

        String target = operation.require(SERVER.getName()).asString();

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