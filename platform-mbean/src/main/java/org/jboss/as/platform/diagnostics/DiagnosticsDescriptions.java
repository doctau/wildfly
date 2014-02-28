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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_LENGTH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NILLABLE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.platform.mbean.PlatformMBeanConstants.GET_LOGGER_LEVEL;
import static org.jboss.as.platform.mbean.PlatformMBeanConstants.GET_PARENT_LOGGER_NAME;
import static org.jboss.as.platform.mbean.PlatformMBeanConstants.LEVEL_NAME;
import static org.jboss.as.platform.mbean.PlatformMBeanConstants.LOGGER_NAME;
import static org.jboss.as.platform.mbean.PlatformMBeanConstants.LOGGING;
import static org.jboss.as.platform.mbean.PlatformMBeanConstants.SET_LOGGER_LEVEL;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Static methods for creating domain management API descriptions for platform mbean resources.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class DiagnosticsDescriptions {

    static final String RESOURCE_NAME = DiagnosticsDescriptions.class.getPackage().getName() + ".LocalDescriptions";

    public static StandardResourceDescriptionResolver getResolver(final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder("");
        for (String kp : keyPrefix) {
            if (prefix.length() > 0) {
                prefix.append('.');
            }
            prefix.append(kp);
        }
        return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, DiagnosticsDescriptions.class.getClassLoader(), true, false);
    }

    private DiagnosticsDescriptions() {
    }


    public static ModelNode getGetLoggerLevelDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(GET_LOGGER_LEVEL);
        node.get(DESCRIPTION).set(bundle.getString("logging.get-logger-level"));

        addLoggerNameParam(node.get(REQUEST_PROPERTIES), bundle);

        node.get(REPLY_PROPERTIES);
        node.get(REPLY_PROPERTIES, TYPE).set(ModelType.STRING);
        node.get(REPLY_PROPERTIES, NILLABLE).set(true);

        return node;
    }

    public static ModelNode getSetLoggerLevelDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(SET_LOGGER_LEVEL);
        node.get(DESCRIPTION).set(bundle.getString("logging.set-logger-level"));

        final ModelNode reqProps = node.get(REQUEST_PROPERTIES);
        addLoggerNameParam(reqProps, bundle);
        final ModelNode level = reqProps.get(LEVEL_NAME);
        level.get(DESCRIPTION).set(bundle.getString(LOGGING + "." + LEVEL_NAME));
        level.get(TYPE).set(ModelType.STRING);
        level.get(REQUIRED).set(false);

        node.get(REPLY_PROPERTIES).setEmptyObject();

        return node;
    }

    public static ModelNode getGetParentLoggerNameDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(GET_PARENT_LOGGER_NAME);
        node.get(DESCRIPTION).set(bundle.getString("logging.get-parent-logger-name"));

        addLoggerNameParam(node.get(REQUEST_PROPERTIES), bundle);

        node.get(REPLY_PROPERTIES);
        node.get(REPLY_PROPERTIES, TYPE).set(ModelType.STRING);
        node.get(REPLY_PROPERTIES, NILLABLE).set(true);

        return node;
    }

    private static void addLoggerNameParam(final ModelNode requestProperties, final ResourceBundle bundle) {
        final ModelNode param = requestProperties.get(LOGGER_NAME);
        param.get(DESCRIPTION).set(bundle.getString(LOGGING + "." + LOGGER_NAME));
        param.get(TYPE).set(ModelType.STRING);
        param.get(MIN_LENGTH).set(0);
        param.get(REQUIRED).set(true);
    }

    public static ModelNode getDescriptionOnlyOperation(final Locale locale, final String name, final String descriptionKeyBase) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(name);
        node.get(DESCRIPTION).set(bundle.getString(descriptionKeyBase + "." + name));

        node.get(REQUEST_PROPERTIES).setEmptyObject();
        node.get(REPLY_PROPERTIES).setEmptyObject();

        return node;
    }

    private static ResourceBundle getResourceBundle(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return ResourceBundle.getBundle(RESOURCE_NAME, locale);
    }
}
