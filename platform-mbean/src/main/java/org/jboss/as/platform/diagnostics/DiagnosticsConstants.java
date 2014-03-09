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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;

import org.jboss.as.controller.PathElement;

/**
 * Constants used in this module.
 *
 * @author James Livingston (c) 2014 Red Hat Inc.
 */
public class DiagnosticsConstants {
    public static final String DIAGNOSTICS = "diagnostics";


    public static final PathElement DIAGNOSTICS_PATH = PathElement.pathElement(CORE_SERVICE, DIAGNOSTICS);


    public static final String SERVER_TEXT_THREAD_DUMP = "server-text-thread-dump";
    public static final String SERVER_PARSED_THREAD_DUMP = "server-object-thread-dump";


    public static final String SERVER = "server";


    public static final String THREAD_STATE_DETAIL = "thread-state-detail";


    public static final String NATIVE_THREAD_ID = "thread-native-id";
    public static final String THREAD_PRIORITY = "thread-priority";
    public static final String THREAD_DAEMON = "thread-daemon";
    public static final String BLOCKING_SYNCHRONIZER = "blocked-synchronizer";
    public static final String WAITING_SYNCHRONIZER = "waiting-synchronizer";
    public static final String MONITOR_TD = "monitor=id";

    private DiagnosticsConstants() {
        // prevent instantiation
    }
}
