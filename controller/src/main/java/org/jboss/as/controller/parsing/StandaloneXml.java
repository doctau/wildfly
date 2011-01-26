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

package org.jboss.as.controller.parsing;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HASH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INTERFACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MAX_THREADS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PORT_OFFSET;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RUNTIME_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.START;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SYSTEM_PROPERTIES;
import static org.jboss.as.controller.parsing.ParseUtils.duplicateNamedElement;
import static org.jboss.as.controller.parsing.ParseUtils.hexStringToByteArray;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.nextElement;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.modules.ModuleLoader;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * A mapper between {@code standalone.xml} and a model.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class StandaloneXml extends CommonXml {

    public StandaloneXml(final ModuleLoader loader) {
        super(loader);
    }

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> operationList) throws XMLStreamException {
        final ModelNode address = new ModelNode().setEmptyList();
        if (Namespace.forUri(reader.getNamespaceURI()) != Namespace.DOMAIN_1_0 || Element.forName(reader.getLocalName()) != Element.SERVER) {
            throw unexpectedElement(reader);
        }
        readServerElement(reader, address, operationList);
    }

    private void readServerElement(final XMLExtendedStreamReader reader, final ModelNode address, final List<ModelNode> list) throws XMLStreamException {

        parseNamespaces(reader, address, list);

        // attributes
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i ++) {
            switch (Namespace.forUri(reader.getAttributeNamespace(i))) {
                case NONE: {
                    final String value = reader.getAttributeValue(i);
                    final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                    switch (attribute) {
                        case NAME: {
                            setServerName(address, list, value);
                            break;
                        }
                        default: throw unexpectedAttribute(reader, i);
                    }
                    break;
                }
                case XML_SCHEMA_INSTANCE: {
                    switch (Attribute.forName(reader.getAttributeLocalName(i))) {
                        case SCHEMA_LOCATION: {
                            parseSchemaLocations(reader, address, list, i);
                            break;
                        }
                        case NO_NAMESPACE_SCHEMA_LOCATION: {
                            // todo, jeez
                            break;
                        }
                        default: {
                            throw unexpectedAttribute(reader, i);
                        }
                    }
                    break;
                }
                default: throw unexpectedAttribute(reader, i);
            }
        }

        // elements - sequence

        Element element = nextElement(reader);
        if (element == Element.EXTENSIONS) {
            parseExtensions(reader, address, list);
            element = nextElement(reader);
        }
        if (element == Element.PATHS) {
            parsePaths(reader, address, list, true);
            element = nextElement(reader);
        }
        if (element == Element.MANAGEMENT) {
            parseManagementSocket(reader, address, list);
            element = nextElement(reader);
        }
        // Single profile
        if (element == Element.PROFILE) {
            parseServerProfile(reader, address, list);
            element = nextElement(reader);
        }
        // Interfaces
        final Set<String> interfaceNames = new HashSet<String>();
        if (element == Element.INTERFACES) {
            parseInterfaces(reader, interfaceNames, address, list, true);
            element = nextElement(reader);
        }
        // Single socket binding group
        if (element == Element.SOCKET_BINDING_GROUP) {
            parseSocketBindingGroup(reader, interfaceNames, address, list);
            element = nextElement(reader);
        }
        // System properties
        if (element == Element.SYSTEM_PROPERTIES) {
            parseSystemProperties(reader, address, list);
            element = nextElement(reader);
        }
        if (element == Element.DEPLOYMENTS) {
            parseServerDeployments(reader, list);
            element = nextElement(reader);
        }
        if (element != null) {
            throw unexpectedElement(reader);
        }

//        for (;;) {
//            switch (reader.nextTag()) {
//                case START_ELEMENT: {
//                    readHeadComment(reader, address, list);
//                    if (Namespace.forUri(reader.getNamespaceURI()) != Namespace.DOMAIN_1_0) {
//                        throw unexpectedElement(reader);
//                    }
//                    switch (Element.forName(reader.getLocalName())) {
//                        default: throw unexpectedElement(reader);
//                    }
//                }
//                case END_ELEMENT: {
//                    readTailComment(reader, address, list);
//                    return;
//                }
//                default: throw new IllegalStateException();
//            }
//        }
    }

    private void parseSocketBindingGroup(final XMLExtendedStreamReader reader, final Set<String> interfaces, final ModelNode address, final List<ModelNode> updates) throws XMLStreamException {
        final Set<String> socketBindings = new HashSet<String>();

        // Handle attributes
        String name = null;
        String defaultInterface = null;
        String portOffset = null;

        final EnumSet<Attribute> required = EnumSet.of(Attribute.NAME, Attribute.DEFAULT_INTERFACE);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i ++) {
            final String value = reader.getAttributeValue(i);
            if (reader.getAttributeNamespace(i) != null) {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME: {
                    name = value;
                    required.remove(attribute);
                    break;
                }
                case DEFAULT_INTERFACE: {
                    defaultInterface = value;
                    required.remove(attribute);
                    break;
                }
                case PORT_OFFSET: {
                    portOffset = value;
                    try {
                        int offset = Integer.parseInt(value);
                        if (offset < 0) {
                            throw new XMLStreamException(portOffset + " is not a valid " +
                                    attribute.getLocalName() + " -- must be greater than zero",
                                    reader.getLocation());
                        }
                    } catch (final NumberFormatException e) {
                        if (!Util.isExpression(value)) {
                            throw new XMLStreamException(portOffset + " is not a valid " +
                                    attribute.getLocalName(), reader.getLocation(), e);
                        }
                    }
                    break;
                }
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }

        if (! required.isEmpty()) {
            throw missingRequired(reader, required);
        }

        ModelNode groupAddress = address.clone().add(SOCKET_BINDING_GROUP, name);
        ModelNode op = Util.getEmptyOperation(ADD, groupAddress);
        op.get(DEFAULT_INTERFACE).set(defaultInterface);
        op.get(PORT_OFFSET).set(portOffset == null ? "0" : portOffset);

        updates.add(op);

        // Handle elements
        while (reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case DOMAIN_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case SOCKET_BINDING: {
                            // FIXME JBAS-8825
                            final String bindingName = parseSocketBinding(reader, interfaces, groupAddress, defaultInterface, updates);
                            if (socketBindings.contains(bindingName)) {
                                throw new XMLStreamException("socket-binding " + bindingName + " already declared", reader.getLocation());
                            }
                            socketBindings.add(bindingName);
                            break;
                        }
                        default:
                            throw unexpectedElement(reader);
                    }
                    break;
                }
                default:
                    throw unexpectedElement(reader);
            }
        }
    }

    private void parseServerDeployments(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        requireNoAttributes(reader);

        final Set<String> names = new HashSet<String>();

        while (reader.nextTag() != END_ELEMENT) {
            // Handle attributes
            String uniqueName = null;
            String runtimeName = null;
            byte[] hash = null;
            String startInput = null;
            final int count = reader.getAttributeCount();
            for (int i = 0; i < count; i ++) {
                final String value = reader.getAttributeValue(i);
                if (reader.getAttributeNamespace(i) != null) {
                    throw unexpectedAttribute(reader, i);
                } else {
                    final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                    switch (attribute) {
                        case NAME: {
                            if (!names.add(value)) {
                                throw duplicateNamedElement(reader, value);
                            }
                            uniqueName = value;
                            break;
                        }
                        case RUNTIME_NAME: {
                            runtimeName = value;
                            break;
                        }
                        case SHA1: {
                            try {
                                hash = hexStringToByteArray(value);
                            }
                            catch (final Exception e) {
                               throw new XMLStreamException("Value " + value +
                                       " for attribute " + attribute.getLocalName() +
                                       " does not represent a properly hex-encoded SHA1 hash",
                                       reader.getLocation(), e);
                            }
                            break;
                        }
                        case ALLOWED: {
                            if (!Boolean.parseBoolean(value)) {
                                throw new XMLStreamException("Attribute '" + attribute.getLocalName() + "' is not allowed", reader.getLocation());
                            }
                            break;
                        }
                        case START: {
                            startInput = value;
                            break;
                        }
                        default:
                            throw unexpectedAttribute(reader, i);
                    }
                }
            }
            if (uniqueName == null) {
                throw missingRequired(reader, Collections.singleton(Attribute.NAME));
            }
            if (runtimeName == null) {
                throw missingRequired(reader, Collections.singleton(Attribute.RUNTIME_NAME));
            }
            if (hash == null) {
                throw missingRequired(reader, Collections.singleton(Attribute.SHA1));
            }
            final boolean toStart = startInput == null ? true : Boolean.parseBoolean(startInput);

            // Handle elements
            requireNoContent(reader);

            final ModelNode deploymentAdd = new ModelNode();
            // TODO decide whether deployments are an attribute of list type or a child
//            deploymentAdd.get(OP_ADDR).add(DEPLOYMENT, uniqueName);
//            deploymentAdd.get(OP).set(ADD);
            deploymentAdd.get(OP_ADDR).setEmptyList();
            deploymentAdd.get(OP).set("add-deployment");
            deploymentAdd.get("unique-name").set(uniqueName);
            deploymentAdd.get("runtime-name").set(runtimeName);
            deploymentAdd.get("sha1").set(hash);
            deploymentAdd.get("start").set(toStart);
            list.add(deploymentAdd);
        }
    }

    private void parseServerProfile(final XMLExtendedStreamReader reader, final ModelNode address, final List<ModelNode> list) throws XMLStreamException {
        // Attributes
        // FIXME The other parser actually allows a name - we just ignore it for now
        // requireNoAttributes(reader);

        // Content
        final Set<String> configuredSubsystemTypes = new HashSet<String>();
        while (reader.nextTag() != END_ELEMENT) {
            if (Namespace.forUri(reader.getNamespaceURI()) != Namespace.UNKNOWN) {
                throw unexpectedElement(reader);
            }
            if (Element.forName(reader.getLocalName()) != Element.SUBSYSTEM) {
                throw unexpectedElement(reader);
            }
            if (!configuredSubsystemTypes.add(reader.getNamespaceURI())) {
                throw new XMLStreamException("Duplicate subsystem declaration", reader.getLocation());
            }
            // parse subsystem
            final List<ModelNode> subsystems = new ArrayList<ModelNode>();
            reader.handleAny(subsystems);
            // Process subsystems
            for(final ModelNode update : subsystems) {
                // TODO remove logging
                if(! update.has(OP_ADDR)) {
                    Logger.getLogger("missing address").error(update);
                }
                // Process relative subsystem path address
                final ModelNode subsystemAddress = address.clone();
                for(final Property path : update.get(OP_ADDR).asPropertyList()) {
                    subsystemAddress.add(path.getName(), path.getValue().asString());
                }
                update.get(OP_ADDR).set(subsystemAddress);
                list.add(update);
            }
        }
    }

    private void setServerName(final ModelNode address, final List<ModelNode> operationList, final String value) {
        if (value.length() > 0) {
            final ModelNode update = Util.getWriteAttributeOperation(address, NAME, value);
            operationList.add(update);
        }
    }

    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final ModelNode modelNode) throws XMLStreamException {

        writer.writeStartDocument();
        writer.writeStartElement(Element.SERVER.getLocalName());
        writeNamespaces(writer, modelNode);
        writeSchemaLocation(writer, modelNode);
        writeExtensions(writer, modelNode.get(EXTENSION));
        if (hasDefinedChild(modelNode, NAME)) {
            writeAttribute(writer, Attribute.NAME, modelNode.get(NAME).asString());
        }
        if (hasDefinedChild(modelNode, EXTENSION)) {
            writeExtensions(writer, modelNode.get(EXTENSION));
        }
        if(hasDefinedChild(modelNode, PATH)) {
            writePaths(writer, modelNode.get(PATH));
        }
        if (hasDefinedChild(modelNode, MANAGEMENT)) {
            writeServerManagement(writer, modelNode.get(MANAGEMENT));
        }
        writeServerProfile(writer, modelNode);
        if (hasDefinedChild(modelNode, INTERFACE)) {
            writeInterfaces(writer, modelNode.get(INTERFACE));
        }
        if (hasDefinedChild(modelNode, SOCKET_BINDING_GROUP)) {
            writeSocketBindingGroup(writer, modelNode.get(SOCKET_BINDING_GROUP), true);
        }
        if (hasDefinedChild(modelNode, SYSTEM_PROPERTIES)) {
            writeProperties(writer, modelNode.get(SYSTEM_PROPERTIES), Element.SYSTEM_PROPERTIES);
        }
        if (hasDefinedChild(modelNode, DEPLOYMENT)) {
            writeServerDeployments(writer, modelNode.get(DEPLOYMENT));
        }

        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void writeServerManagement(final XMLExtendedStreamWriter writer, final ModelNode serverManagement)
            throws XMLStreamException {
        String iface = serverManagement.get(INTERFACE).asString();
        String port = serverManagement.get(PORT).asString();

        writer.writeStartElement(Element.MANAGEMENT.getLocalName());
        writeAttribute(writer, Attribute.INTERFACE, iface);
        writeAttribute(writer, Attribute.PORT, port);
        if (hasDefinedChild(serverManagement, MAX_THREADS)) {
            writeAttribute(writer, Attribute.MAX_THREADS, serverManagement.get(MAX_THREADS).asString());
        }
        writer.writeEndElement();
    }

    private void writeServerDeployments(final XMLExtendedStreamWriter writer, final ModelNode modelNode)
            throws XMLStreamException {
        writer.writeStartElement(Element.DEPLOYMENTS.getLocalName());
        for (ModelNode deployment : modelNode.asList()) {
            String uniqueName = deployment.get(NAME).asString();
            String runtimeName = deployment.get(RUNTIME_NAME).asString();
            String sha1 = deployment.get(HASH).asString();
            boolean start = deployment.get(START).asBoolean();
            writer.writeStartElement(Element.DEPLOYMENT.getLocalName());
            writeAttribute(writer, Attribute.NAME, uniqueName);
            writeAttribute(writer, Attribute.RUNTIME_NAME, runtimeName);
            writeAttribute(writer, Attribute.SHA1, sha1);
            if (!start) {
                writeAttribute(writer, Attribute.START, "false");
            }
            writer.writeEndElement();

        }
        writer.writeEndElement();
    }

    private void writeServerProfile(final XMLExtendedStreamWriter writer, final ModelNode modelNode) throws XMLStreamException {
        writer.writeStartElement(Element.PROFILE.getLocalName());
        writer.writeAttribute(Attribute.PROFILE.getLocalName(), modelNode.get(PROFILE_NAME).asString());
        // 1) get the namespace URI from the model TODO -- extensions need to pass subsystem name into extension parsing context
        // 2) use extensionSubsystemWriters.get(namespaceURI) to get the XMLElementWriter
        writer.writeEndElement();
    }

}
