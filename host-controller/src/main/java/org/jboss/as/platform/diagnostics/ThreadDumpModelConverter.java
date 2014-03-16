package org.jboss.as.platform.diagnostics;

import static org.jboss.as.platform.diagnostics.ModelUtils.nullSafeSet;

import org.jboss.as.platform.mbean.PlatformMBeanConstants;
import org.jboss.dmr.ModelNode;

import com.redhat.gss.highcpu.data.SourceLocation;
import com.redhat.gss.highcpu.data.ThreadDump;
import com.redhat.gss.highcpu.data.ThreadFrame;
import com.redhat.gss.highcpu.data.ThreadInfo;
import com.redhat.gss.highcpu.data.ThreadInfo.ThreadState;
import com.redhat.gss.highcpu.data.ThreadSynchronizer;

public class ThreadDumpModelConverter {

    public static ModelNode dumpToModel(ThreadDump td) {
        final ModelNode mn = new ModelNode();

        mn.get("header").set(td.getHeader());
        mn.get("timestamp").set(td.getTimestamp());
        mn.get("jni-global-references").set(td.getJniGlobalReferences());

        ModelNode tns = mn.get("threads").setEmptyList();
        for (ThreadInfo ti: td.getThreads()) {
            tns.add(threadToModel(ti));
        }

        return mn;
    }

    private static ModelNode threadToModel(ThreadInfo ti) {
        final ModelNode mn = new ModelNode();

        mn.get(PlatformMBeanConstants.THREAD_STATE).set(convertState(ti.getState()).name());
        mn.get(DiagnosticsConstants.THREAD_STATE_DETAIL).set(ti.getState().name());
        mn.get(PlatformMBeanConstants.IN_NATIVE).set(ti.getState() == ThreadState.UNKNOWN);


        final ModelNode stack = mn.get(PlatformMBeanConstants.STACK_TRACE).setEmptyList();
        for (ThreadFrame tf: ti.getFrames()) {
            stack.add(frameToModel(tf));
        }

        if (!ti.getSynchronizers().isEmpty()) {
            final ModelNode synchronizers = mn.get(PlatformMBeanConstants.LOCKED_SYNCHRONIZERS); // and LOCKED_MONITORS?
            for (ThreadSynchronizer ts: ti.getSynchronizers()) {
                synchronizers.add(synchronizerToModel(ts));
            }
        }


        mn.get(PlatformMBeanConstants.THREAD_NAME).set(ti.getName());
        nullSafeSet(mn.get(PlatformMBeanConstants.THREAD_ID), ti.getJavaId());
        nullSafeSet(mn.get(DiagnosticsConstants.NATIVE_THREAD_ID), ti.getNativeId());
        nullSafeSet(mn.get(DiagnosticsConstants.THREAD_PRIORITY), ti.getPriority());
        nullSafeSet(mn.get(DiagnosticsConstants.THREAD_DAEMON), ti.getDaemon());

        ThreadSynchronizer ts = ti.getBlockingSynchronizer();
        if (ts != null) {
            ModelNode l = mn.get(PlatformMBeanConstants.LOCK_INFO);
            l.get(PlatformMBeanConstants.CLASS_NAME).set(ts.getKlass());
            l.get(PlatformMBeanConstants.IDENTITY_HASH_CODE).set(ts.getAddress());
            mn.get(PlatformMBeanConstants.LOCK_NAME).set(ts.getKlass() + '@' + ts.getAddress());
        }

        /*

        result.get(PlatformMBeanConstants.LOCK_OWNER_ID).set(threadInfo.getLockOwnerId());
        nullSafeSet(result.get(PlatformMBeanConstants.LOCK_OWNER_NAME), threadInfo.getLockOwnerName());
        result.get(PlatformMBeanConstants.SUSPENDED).set(threadInfo.isSuspended());
         */
        return mn;
    }

    private static ModelNode frameToModel(ThreadFrame tf) {
        final ModelNode mn = new ModelNode();

        mn.get(PlatformMBeanConstants.CLASS_NAME).set(tf.getKlass());
        mn.get(PlatformMBeanConstants.METHOD_NAME).set(tf.getMethod());


        mn.get(PlatformMBeanConstants.NATIVE_METHOD).set(tf.getLocation() == SourceLocation.NativeMethod);
        if (tf.getLocation() instanceof SourceLocation.KnownFile) {
            String file = ((SourceLocation.KnownFile)tf.getLocation()).getFile();
            mn.get(PlatformMBeanConstants.FILE_NAME).set(file);
        }
        if (tf.getLocation() instanceof SourceLocation.KnownLine) {
            long line = ((SourceLocation.KnownLine)tf.getLocation()).getLine();
            mn.get(PlatformMBeanConstants.LINE_NUMBER).set(line);
        }


        if (tf.getBlocking() != null)
            mn.get(DiagnosticsConstants.BLOCKING_SYNCHRONIZER).set(synchronizerToModel(tf.getBlocking()));
        if (tf.getBlocking() != null)
            mn.get(DiagnosticsConstants.WAITING_SYNCHRONIZER).set(synchronizerToModel(tf.getWaiting()));

        if (!tf.getTaken().isEmpty()) {
            ModelNode lsn = mn.get(PlatformMBeanConstants.LOCKED_SYNCHRONIZERS);
            for (ThreadSynchronizer ts: tf.getTaken()) {
                lsn.add(synchronizerToModel(ts));
            }
        }

        return mn;
    }

    private static ModelNode synchronizerToModel(ThreadSynchronizer ts) {
        ModelNode mn = new ModelNode();

        mn.get(PlatformMBeanConstants.CLASS_NAME).set(ts.getKlass());
        ModelUtils.nullSafeSet(mn.get(PlatformMBeanConstants.IDENTITY_HASH_CODE), ts.getAddress());
        ModelUtils.nullSafeSet(mn.get(DiagnosticsConstants.MONITOR_TD), ts.getMonitor());

        return mn;
    }

    private static Thread.State convertState(ThreadInfo.ThreadState state) {
        switch (state) {
        case RUNNABLE:
        case NATIVE:
        case UNKNOWN:
            return Thread.State.RUNNABLE;
        case BLOCKED_MONITOR:
            return Thread.State.BLOCKED;
        case WAITING_MONITOR:
        case WAITING_PARKING:
            return Thread.State.WAITING;
        case TIMED_WAIT_MONITOR:
        case TIMED_WAIT_PARKING:
        case TIMED_WAIT_SLEEPING:
            return Thread.State.TIMED_WAITING;
        default:
            throw new IllegalArgumentException();
        }
    }

}
