/*******************************************************************************
* Copyright (c) 2019, 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package io.openliberty.tools.intellij.lsp4mp4ij.psi.internal.reactivemessaging.properties;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.AbstractAnnotationTypeReferencePropertiesProvider;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.SearchContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;

import java.util.Locale;

import static io.openliberty.tools.intellij.lsp4mp4ij.psi.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static io.openliberty.tools.intellij.lsp4mp4ij.psi.core.utils.AnnotationUtils.isMatchAnnotation;
import static io.openliberty.tools.intellij.lsp4mp4ij.psi.core.utils.PsiTypeUtils.*;
import static io.openliberty.tools.intellij.lsp4mp4ij.psi.internal.reactivemessaging.MicroProfileReactiveMessagingConstants.*;

/**
 * Properties provider to collect MicroProfile properties from the MicroProfile
 * Reactive Messaging annotations.
 *
 * <ul>
 * <li>static incoming/outgoing properties for connector according the Java
 * annotation @Incoming/Outgoing (ex: mp.messaging.incoming.prices.connector).
 * </li>
 * <li>dynamic incoming/outgoing properties for a given connector. It uses the
 * dynamic syntax ${connector-name} (ex:
 * mp.messaging.incoming.${smallrye-kafka}.topic)</li>
 * <li>hints with all connector names (class annotated with @Connector)</li>
 * </ul>
 *
 * Here a JSON sample:
 *
 * <code>
 * {
 "properties": [
 {
 "type": "org.eclipse.microprofile.reactive.messaging.spi.Connector",
 "sourceMethod": "process(I)D",
 "required": true,
 "phase": 0,
 "name": "mp.messaging.incoming.prices.connector",
 "sourceType": "org.acme.kafka.PriceConverter",
 "source": true
 },
 {
 "type": "java.lang.String",
 "required": true,
 "phase": 0,
 "name": "mp.messaging.incoming.${smallrye-kafka}.topic",
 "description": "The consumed / populated Kafka topic. If not set, the channel name is used",
 "sourceType": "io.smallrye.reactive.messaging.kafka.KafkaConnector"
 }
 ],
 "hints": [
 {
 "values": [
 {
 "value": "smallrye-kafka",
 "sourceType": "io.smallrye.reactive.messaging.kafka.KafkaConnector"
 },
 {
 "value": "smallrye-amqp",
 "sourceType": "io.smallrye.reactive.messaging.amqp.AmqpConnector"
 }
 ],
 "name": "${mp.messaging.connector.binary}"
 }
 ]
 }
 * </code>
 *
 * @author Angelo ZERR
 *
 * @see <a href="https://github.com/eclipse/microprofile-reactive-messaging/blob/62c9ed5dffe01125941bb185f1433d6307b83c86/spec/src/main/asciidoc/architecture.asciidoc#configuration">https://github.com/eclipse/microprofile-reactive-messaging/blob/62c9ed5dffe01125941bb185f1433d6307b83c86/spec/src/main/asciidoc/architecture.asciidoc#configuration</a>
 *
 */
public class MicroProfileReactiveMessagingProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { CONNECTOR_ANNOTATION, INCOMING_ANNOTATION, OUTGOING_ANNOTATION,
	CHANNEL_ANNOTATION};

	private enum Direction {
		INCOMING, OUTGOING, INCOMING_AND_OUTGOING;
	}

	private enum MessageType {
		INCOMING, OUTGOING, CONNECTOR;
	}

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(PsiModifierListOwner psiElement, PsiAnnotation mprmAnnotation,
									 String annotationName, SearchContext context) {
		switch (annotationName) {
			case CONNECTOR_ANNOTATION:
				// @Connector(KafkaConnector.CONNECTOR_NAME)
				// @ConnectorAttribute(name = "bootstrap.servers", alias =
				// "kafka.bootstrap.servers", type = "string", defaultValue = "localhost:9092",
				// direction = Direction.INCOMING_AND_OUTGOING, description = "A comma-separated
				// list of host:port to use for establishing the initial connection to the Kafka
				// cluster.")
				// ...

				// public class KafkaConnector implements IncomingConnectorFactory,
				// OutgoingConnectorFactory {

				// public static final String CONNECTOR_NAME = "smallrye-kafka";

				processConnector(psiElement, mprmAnnotation, context);
				break;
			case INCOMING_ANNOTATION:
				// public class PriceConverter {
				// @Incoming("prices")
				// public double process(int priceInUsd) {
				processIncomingChannel(psiElement, mprmAnnotation, context);
				break;
			case CHANNEL_ANNOTATION: //Used for both incoming and outgoing channels
				// @Inject
				// @Channel("prices")
				// Emitter<double> pricesEmitter;
				if (isChannelField(psiElement)) {
					processIncomingChannel(psiElement, mprmAnnotation, context);
					processOutgoingChannel(psiElement, mprmAnnotation, context);
				}
				break;
			case OUTGOING_ANNOTATION:
				// public class PriceConverter {
				// @Outgoing("my-data-stream")
				// public double process(int priceInUsd) {
				processOutgoingChannel(psiElement, mprmAnnotation, context);
				break;
			default:
				break;
		}
	}

	private static boolean isChannelField(PsiElement element) {
		return element instanceof PsiField;
	}

	/**
	 * Generate static property for incoming connector (ex :
	 * mp.messaging.incoming.prices.connector).
	 *
	 * @param javaElement        the Java element.
	 * @param incomingAnnotation the incoming annotation.
	 * @param context            the search context.
	 */
	private void processIncomingChannel(PsiModifierListOwner javaElement, PsiAnnotation incomingAnnotation, SearchContext context) {
		processChannelConnector(javaElement, incomingAnnotation, MessageType.INCOMING, context);
	}

	/**
	 * Generate static property for outgoing connector (ex :
	 * mp.messaging.outgoing.generated-price.connector).
	 *
	 * @param javaElement        the Java element.
	 * @param outgoingAnnotation the outgoing annotation.
	 * @param context            the search context.
	 */
	private void processOutgoingChannel(PsiModifierListOwner javaElement, PsiAnnotation outgoingAnnotation, SearchContext context) {
		processChannelConnector(javaElement, outgoingAnnotation, MessageType.OUTGOING, context);
	}

	/**
	 * Generate static property for incoming/outgoing connector
	 *
	 * @param javaElement                  the Java element.
	 * @param incomingOrOutgoingAnnotation the incoming/outgoing annotation.
	 * @param messageType                  the message type to generate (incoming,
	 *                                     outgoing).
	 * @param context                      the search context.
	 */
	private void processChannelConnector(PsiModifierListOwner javaElement, PsiAnnotation incomingOrOutgoingAnnotation,
										 MessageType messageType, SearchContext context) {
		// Extract channel name from
		// - @Incoming("channel-name") or
		// - @Outgoing("channel-name") annotation
		String channelName = getAnnotationMemberValue(incomingOrOutgoingAnnotation, "value");
		if (StringUtils.isBlank(channelName)) {
			// channel name must not be blank, see
			// https://github.com/eclipse/microprofile-reactive-messaging/blob/62c9ed5dffe01125941bb185f1433d6307b83c86/api/src/main/java/org/eclipse/microprofile/reactive/messaging/Incoming.java#L95
			return;
		}
		String sourceType = getSourceType(javaElement);
		String sourceMethod = null;
		String sourceField = null;
		if (javaElement instanceof PsiMethod method) {
			sourceMethod = getSourceMethod(method);
		} else if (javaElement instanceof PsiField field) {
			sourceField = getSourceField(field);
		}
		boolean binary = isBinary(javaElement);
		String description = null;
		String type = "org.eclipse.microprofile.reactive.messaging.spi.Connector";
		addMpMessagingItem(channelName, false, "connector", messageType, sourceType, sourceField,
				sourceMethod, binary, type, description, null, context);
	}

	/**
	 * Generate dynamic property connector (attributes connector).
	 *
	 * @param javaElement         the Java element.
	 * @param connectorAnnotation the Connector annotation.
	 * @param context             the search context.
	 */
	private void processConnector(PsiModifierListOwner javaElement, PsiAnnotation connectorAnnotation,
								  SearchContext context) {

		// 1) Collect connector names into hints ${mp.messaging.connector.binary} and
		// ${mp.messaging.connector.source}
		String connectorName = getAnnotationMemberValue(connectorAnnotation, "value");
		String connectorHint = getHint("mp.messaging.connector.", javaElement);
		String description = null;
		String sourceType = getSourceType(javaElement);
		fillValueHint(connectorHint, connectorName, description, sourceType, context);

		// 2) Generate property per @ConnectorAttribute which provides attribute and
		// direction informations for the current connector
		// - mp.messaging.[attribute=incoming|outgoing].${connector-name}.[attribute]
		boolean binary = isBinary(javaElement);
		PsiAnnotation[] annotations = javaElement.getAnnotations();
		for (PsiAnnotation connectorAttributeAnnotation : annotations) {
			if (isMatchAnnotation(connectorAttributeAnnotation, CONNECTOR_ATTRIBUTE_ANNOTATION)) {
				processConnectorAttribute(connectorName, connectorAttributeAnnotation, sourceType, binary, context);
			} else if (isMatchAnnotation(connectorAttributeAnnotation, CONNECTOR_ATTRIBUTES_ANNOTATION)) {
				for (PsiNameValuePair pair : connectorAttributeAnnotation.getParameterList().getAttributes()) {
					if (pair.getValue() instanceof PsiArrayInitializerMemberValue connectorAttributeAnnotations) {
						for (Object annotation : connectorAttributeAnnotations.getInitializers()) {
							if (annotation instanceof PsiAnnotation psiAnnotation) {
								processConnectorAttribute(connectorName, psiAnnotation, sourceType, binary,
										context);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Generate the dynamic connector attribute property.
	 *
	 * @param connectorName       the connector name.
	 * @param connectorAnnotation the connector annotation.
	 * @param sourceType          the source type.
	 * @param binary              true if binary.
	 * @param context             the search context.
	 */
	private void processConnectorAttribute(String connectorName, PsiAnnotation connectorAnnotation, String sourceType,
										   boolean binary, SearchContext context) {
		String attributeName = getAnnotationMemberValue(connectorAnnotation, "name");
		String type = getType(getAnnotationMemberValue(connectorAnnotation, "type"));
		String description = getAnnotationMemberValue(connectorAnnotation, "description");
		String defaultValue = getAnnotationMemberValue(connectorAnnotation, "defaultValue");
		if (StringUtils.isEmpty(defaultValue)) {
			defaultValue = null;
		}
		Direction direction = getDirection(getAnnotationMemberValue(connectorAnnotation, "direction"));

		switch (direction) {
			case INCOMING:
				// Generate mp.messaging.incoming.${connector-name}.[attribute]
				// ex : mp.messaging.incoming.${smallrye-kafka}.topic
				addMpMessagingItem(connectorName, true, attributeName, MessageType.INCOMING, sourceType,
						null, null, binary, type, description, defaultValue, context);
				break;
			case OUTGOING:
				// Generate mp.messaging.outgoing.${connector-name}.[attribute]
				addMpMessagingItem(connectorName, true, attributeName, MessageType.OUTGOING, sourceType,
						null, null, binary, type, description, defaultValue, context);
				break;
			case INCOMING_AND_OUTGOING:
				// Generate mp.messaging.incoming.${connector-name}.[attribute]
				addMpMessagingItem(connectorName, true, attributeName, MessageType.INCOMING, sourceType,
						null, null, binary, type, description, defaultValue, context);
				// Generate mp.messaging.outgoing.${connector-name}.[attribute]
				addMpMessagingItem(connectorName, true, attributeName, MessageType.OUTGOING, sourceType,
						null, null, binary, type, description, defaultValue, context);
				break;
		}
		// Generate mp.messaging.connector.[connector-name].[attribute]
		addMpMessagingItem(connectorName, false, attributeName, MessageType.CONNECTOR, sourceType,
				null,null, binary, type, description, defaultValue, context);
	}

	private void addMpMessagingItem(String connectorOrChannelName, boolean dynamic, String attributeName,
									MessageType messageType, String sourceType, String sourceField, String sourceMethod,
									boolean binary, String type, String description, String defaultValue,
									SearchContext context) {
		String propertyName = getMPMessagingName(messageType, dynamic, connectorOrChannelName, attributeName);
		super.addItemMetadata(context.getCollector(), propertyName, type, description, sourceType, sourceField,
				sourceMethod, defaultValue, null, binary);
	}

	/**
	 * Returns the direction according the given enumeration value.
	 *
	 * @param connectorAttributeType
	 * @return the direction according the given enumeration value.
	 */
	private static Direction getDirection(String connectorAttributeType) {
		if (connectorAttributeType != null) {
			if (connectorAttributeType.endsWith("INCOMING_AND_OUTGOING")) {
				return Direction.INCOMING_AND_OUTGOING;
			}
			if (connectorAttributeType.endsWith("INCOMING")) {
				return Direction.INCOMING;
			}
			if (connectorAttributeType.endsWith("OUTGOING")) {
				return Direction.OUTGOING;
			}
		}
		return Direction.INCOMING_AND_OUTGOING;
	}

	/**
	 * Returns the Java type from the given connector attribute type (coming from
	 * the @ConnectorAttribute/type).
	 *
	 * @param connectorAttributeType
	 * @return the Java type from the given connector attribute type (coming from
	 *         the @ConnectorAttribute/type).
	 */
	private String getType(String connectorAttributeType) {
		if (StringUtils.isEmpty(connectorAttributeType)) {
			return null;
		}
		if (connectorAttributeType.equals("string")) {
			return "java.lang.String";
		}
		return connectorAttributeType;
	}

	private static String getMPMessagingName(MessageType messageType, boolean dynamic, String connectorOrChannelName,
											 String attributeName) {
		StringBuilder propertyName = new StringBuilder("mp.messaging");
		propertyName.append('.');
		// Use Locale.ROOT to avoid the "Turkish locale bug".
		// See: https://github.com/OpenLiberty/liberty-tools-intellij/issues/1092
		propertyName.append(messageType.name().toLowerCase(Locale.ROOT));
		propertyName.append('.');
		if (dynamic) {
			propertyName.append("${");
		}
		propertyName.append(connectorOrChannelName);
		if (dynamic) {
			propertyName.append("}");
		}
		propertyName.append('.');
		propertyName.append(attributeName);
		return propertyName.toString();
	}

	private static void fillValueHint(String hint, String value, String description, String sourceType,
									  SearchContext context) {
		if (hint == null || value == null) {
			return;
		}
		ItemHint itemHint = context.getCollector().getItemHint(hint);
		ValueHint valueHint = new ValueHint();
		valueHint.setValue(value);
		valueHint.setDescription(description);
		valueHint.setSourceType(sourceType);
		itemHint.getValues().add(valueHint);
	}

	private static String getHint(String baseKey, PsiModifierListOwner javaElement) {
		StringBuilder hint = new StringBuilder("${").append(baseKey);
		if (javaElement != null) {
			hint.append(isBinary(javaElement) ? "binary" : "source");
		}
		return hint.append("}").toString();
	}
}
