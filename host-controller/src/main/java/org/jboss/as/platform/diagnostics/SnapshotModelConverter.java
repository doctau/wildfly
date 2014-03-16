package org.jboss.as.platform.diagnostics;

import org.jboss.dmr.ModelNode;

import com.redhat.gss.highcpu.data.CpuUsage;
import com.redhat.gss.highcpu.data.Snapshot;

public class SnapshotModelConverter {

    public static ModelNode dumpToModel(Snapshot ss) {
        final ModelNode mn = new ModelNode();

        mn.get("timestamp").set(ss.getTime());
        mn.get("thread-dump").set(ThreadDumpModelConverter.dumpToModel(ss.getTd()));
        mn.get("cpu-usage").set(cpuToModel(ss.getUsage()));

        return mn;
    }

    private static ModelNode cpuToModel(CpuUsage usage) {
        final ModelNode mn = new ModelNode();

        mn.set(usage.getData());

        return mn;
    }
}
