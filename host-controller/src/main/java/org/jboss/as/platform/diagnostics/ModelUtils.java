package org.jboss.as.platform.diagnostics;

import org.jboss.dmr.ModelNode;

public class ModelUtils {
    public static void nullSafeSet(ModelNode modelNode, Long v) {
        if (v != null) {
            modelNode.set(v);
        }
    }

    public static void nullSafeSet(ModelNode modelNode, Integer v) {
        if (v != null) {
            modelNode.set(v);
        }
    }

    public static void nullSafeSet(ModelNode modelNode, Boolean v) {
        if (v != null) {
            modelNode.set(v);
        }
    }

    public static void nullSafeSet(ModelNode modelNode, String v) {
        if (v != null) {
            modelNode.set(v);
        }
    }
}
