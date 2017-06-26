/*
 * Copyright 2016-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.svalbard.encode;

import static java.util.stream.Collectors.joining;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.AbstractEncodingDocument;
import net.opengis.swe.x20.AbstractEncodingType;
import net.opengis.swe.x20.BooleanDocument;
import net.opengis.swe.x20.BooleanPropertyType;
import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryDocument;
import net.opengis.swe.x20.CategoryPropertyType;
import net.opengis.swe.x20.CategoryRangeDocument;
import net.opengis.swe.x20.CategoryRangePropertyType;
import net.opengis.swe.x20.CategoryRangeType;
import net.opengis.swe.x20.CategoryType;
import net.opengis.swe.x20.CountDocument;
import net.opengis.swe.x20.CountPropertyType;
import net.opengis.swe.x20.CountRangeDocument;
import net.opengis.swe.x20.CountRangePropertyType;
import net.opengis.swe.x20.CountRangeType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayDocument;
import net.opengis.swe.x20.DataArrayPropertyType;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.DataArrayType.Encoding;
import net.opengis.swe.x20.DataChoiceDocument;
import net.opengis.swe.x20.DataChoicePropertyType;
import net.opengis.swe.x20.DataChoiceType;
import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordPropertyType;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.MatrixDocument;
import net.opengis.swe.x20.MatrixPropertyType;
import net.opengis.swe.x20.MatrixType;
import net.opengis.swe.x20.QuantityDocument;
import net.opengis.swe.x20.QuantityPropertyType;
import net.opengis.swe.x20.QuantityRangeDocument;
import net.opengis.swe.x20.QuantityRangePropertyType;
import net.opengis.swe.x20.QuantityRangeType;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextDocument;
import net.opengis.swe.x20.TextEncodingDocument;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TextPropertyType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeDocument;
import net.opengis.swe.x20.TimePropertyType;
import net.opengis.swe.x20.TimeRangeDocument;
import net.opengis.swe.x20.TimeRangePropertyType;
import net.opengis.swe.x20.TimeRangeType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.UnitReference;
import net.opengis.swe.x20.VectorDocument;
import net.opengis.swe.x20.VectorPropertyType;
import net.opengis.swe.x20.VectorType;
import net.opengis.swe.x20.VectorType.Coordinate;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.janmayen.NcName;
import org.n52.shetland.ogc.OGCConstants;
import org.n52.shetland.ogc.sensorML.elements.SmlPosition;
import org.n52.shetland.ogc.sensorML.v20.SmlDataInterface;
import org.n52.shetland.ogc.sensorML.v20.SmlFeatureOfInterest;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweConstants;
import org.n52.shetland.ogc.swe.SweCoordinate;
import org.n52.shetland.ogc.swe.SweDataArray;
import org.n52.shetland.ogc.swe.SweDataComponentVisitor;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweEnvelope;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.SweSimpleDataRecord;
import org.n52.shetland.ogc.swe.SweVector;
import org.n52.shetland.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.shetland.ogc.swe.encoding.SweTextEncoding;
import org.n52.shetland.ogc.swe.simpleType.SweBoolean;
import org.n52.shetland.ogc.swe.simpleType.SweCategory;
import org.n52.shetland.ogc.swe.simpleType.SweCategoryRange;
import org.n52.shetland.ogc.swe.simpleType.SweCount;
import org.n52.shetland.ogc.swe.simpleType.SweCountRange;
import org.n52.shetland.ogc.swe.simpleType.SweObservableProperty;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swe.simpleType.SweQuantityRange;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.shetland.ogc.swe.simpleType.SweTime;
import org.n52.shetland.ogc.swe.simpleType.SweTimeRange;
import org.n52.shetland.ogc.swes.SwesConstants;
import org.n52.shetland.w3c.SchemaLocation;
import org.n52.svalbard.ConformanceClass;
import org.n52.svalbard.ConformanceClasses;
import org.n52.svalbard.SosHelperValues;
import org.n52.svalbard.XmlBeansEncodingFlags;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.encode.exception.NotYetSupportedEncodingException;
import org.n52.svalbard.encode.exception.UnsupportedEncoderInputException;
import org.n52.svalbard.util.CodingHelper;
import org.n52.svalbard.util.XmlHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SweCommonEncoderv20 extends AbstractXmlEncoder<XmlObject, Object> implements ConformanceClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonEncoderv20.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(
            SweConstants.NS_SWE_20,
            SweCoordinate.class, SweAbstractEncoding.class, SweAbstractDataComponent.class);

    private static final Set<String> CONFORMANCE_CLASSES = new HashSet<>(Arrays.asList(
            ConformanceClasses.SWE_V2_CORE,
            ConformanceClasses.SWE_V2_UML_SIMPLE_COMPONENTS,
            ConformanceClasses.SWE_V2_UML_RECORD_COMPONENTS,
            ConformanceClasses.SWE_V2_UML_BLOCK_ENCODINGS,
            ConformanceClasses.SWE_V2_UML_SIMPLE_ENCODINGS,
            ConformanceClasses.SWE_V2_XSD_SIMPLE_COMPONENTS,
            ConformanceClasses.SWE_V2_XSD_RECORD_COMPONENTS,
            ConformanceClasses.SWE_V2_XSD_BLOCK_COMPONENTS,
            ConformanceClasses.SWE_V2_XSD_SIMPLE_ENCODINGS,
            ConformanceClasses.SWE_V2_GENERAL_ENCODING_RULES,
            ConformanceClasses.SWE_V2_TEXT_ENCODING_RULES));
    private static final String DEFAULT_ELEMENT_TYPE_NAME = "Components";

    public SweCommonEncoderv20() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                     ENCODER_KEYS.stream().map(EncoderKey::toString).collect(joining(", ")));
    }

    @Override
    public Set<EncoderKey> getKeys() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Set<String> getConformanceClasses(String service, String version) {
        if (SosConstants.SOS.equals(service) && Sos2Constants.SERVICEVERSION.equals(version)) {
            return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
        }
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(final Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SweConstants.NS_SWE_20, SweConstants.NS_SWE_PREFIX);
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return Collections.singleton(SwesConstants.SWES_20_SCHEMA_LOCATION);
    }

    @Override
    public XmlObject encode(Object sosSweType, EncodingContext additionalValues) throws EncodingException {
        return XmlHelper.validateDocument(encode1(sosSweType, additionalValues), EncodingException::new);
    }

    private XmlObject encode1(Object sosSweType, EncodingContext additionalValues) throws EncodingException {
        if (sosSweType instanceof SweCoordinate) {
            return createCoordinate((SweCoordinate) sosSweType);
        } else if (sosSweType instanceof SweAbstractEncoding) {
            AbstractEncodingType encoding = createAbstractEncoding((SweAbstractEncoding) sosSweType);
            if (additionalValues.has(XmlBeansEncodingFlags.DOCUMENT)) {
                if (encoding instanceof TextEncodingType) {
                    TextEncodingDocument doc = TextEncodingDocument.Factory.newInstance(getXmlOptions());
                    doc.setTextEncoding((TextEncodingType) encoding);
                    return doc;
                } else {
                    AbstractEncodingDocument doc = AbstractEncodingDocument.Factory.newInstance(getXmlOptions());
                    doc.setAbstractEncoding(encoding);
                    return doc;
                }
            } else {
                return encoding;
            }
        } else if (sosSweType instanceof SweAbstractDataComponent) {
            return createAbstractDataComponent((SweAbstractDataComponent) sosSweType, additionalValues);
        } else {
            throw new UnsupportedEncoderInputException(this, sosSweType);
        }
    }

    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
    private XmlObject createAbstractDataComponent(SweAbstractDataComponent component,
                                                  EncodingContext additionalValues) throws EncodingException {
        if (component == null) {
            throw new UnsupportedEncoderInputException(this, component);
        }

        AbstractDataComponentType xmlComponent = component
                .accept(new SweDataComponentVisitor<AbstractDataComponentType, EncodingException>() {
                    @Override
                    public AbstractDataComponentType visit(SweField component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SweDataRecord component) throws EncodingException {
                        List<SweField> sosFields = component.getFields();
                        DataRecordType xbDataRecord = DataRecordType.Factory.newInstance(getXmlOptions());
                        if (sosFields == null) {
                            LOGGER.error("sosDataRecord contained no fields");
                        } else {
                            List<Field> xbFields = new ArrayList<>(sosFields.size());
                            for (SweField sosSweField : sosFields) {
                                if (sosSweField == null) {
                                    LOGGER.error("sosSweField is null is sosDataRecord");
                                } else {
                                    xbFields.add(createField(sosSweField));
                                }
                            }
                            xbDataRecord.setFieldArray(xbFields.toArray(new Field[xbFields.size()]));
                        }
                        return xbDataRecord;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweDataArray component) throws EncodingException {
                        if (component == null) {
                            return null;
                        }
                        if (component.isSetXml()) {
                            try {
                                XmlObject parse = XmlObject.Factory.parse(component.getXml());
                                if (parse instanceof DataArrayType) {
                                    return (DataArrayType) parse;
                                } else if (parse instanceof DataArrayDocument) {
                                    return ((DataArrayDocument) parse).getDataArray1();
                                }
                            } catch (XmlException e) {
                                LOGGER.warn("Error while parsing XML representation of DataArray^", e);
                            }
                        }
                        DataArrayType xmlDataArray = DataArrayType.Factory.newInstance(getXmlOptions());
                        if (component.isSetElementCount()) {
                            CountType xbCount = CountType.Factory.newInstance(getXmlOptions());
                            if (component.getElementCount().isSetValue()) {
                                xbCount
                                        .setValue(new BigInteger(Integer
                                                .toString(component.getElementCount().getValue())));
                            }
                            xmlDataArray.addNewElementCount().setCount(xbCount);
                        } else {
                            xmlDataArray.addNewElementCount().addNewCount();
                        }
                        if (component.isSetElementTyp()) {
                            DataArrayType.ElementType elementType = xmlDataArray.addNewElementType();
                            if (component.getElementType().isSetDefinition()) {
                                elementType.setName(component.getElementType().getDefinition());
                            } else {
                                elementType.setName(SweCommonEncoderv20.DEFAULT_ELEMENT_TYPE_NAME);
                            }
                            List<SweField> sosFields = ((SweDataRecord) component.getElementType()).getFields();
                            DataRecordType xbDataRecord = DataRecordType.Factory.newInstance(getXmlOptions());
                            if (sosFields == null) {
                                LOGGER.error("sosDataRecord contained no fields");
                            } else {
                                List<Field> xbFields = new ArrayList<>(sosFields.size());
                                for (SweField sosSweField : sosFields) {
                                    if (sosSweField == null) {
                                        LOGGER.error("sosSweField is null is sosDataRecord");
                                    } else {
                                        xbFields.add(createField(sosSweField));
                                    }
                                }
                                xbDataRecord.setFieldArray(xbFields.toArray(new Field[xbFields.size()]));
                            }
                            elementType.addNewAbstractDataComponent()
                                    .set(xbDataRecord);
                            elementType.getAbstractDataComponent()
                                    .substitute(SweConstants.QN_DATA_RECORD_SWE_200, DataRecordType.type);
                        }
                        if (component.isSetEncoding()) {
                            Encoding xbEncoding = xmlDataArray.addNewEncoding();
                            xbEncoding.setAbstractEncoding(createAbstractEncoding(component.getEncoding()));
                            xbEncoding.getAbstractEncoding()
                                    .substitute(SweConstants.QN_TEXT_ENCODING_SWE_200, TextEncodingType.type);
                        }
                        if (component.isSetValues()) {
                            xmlDataArray.addNewValues()
                                    .set(createValues(component.getValues(), component.getEncoding()));
                        }
                        return xmlDataArray;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweCount component) throws EncodingException {
                        CountType xbCount = CountType.Factory.newInstance(getXmlOptions());
                        if (component.isSetValue()) {
                            xbCount.setValue(new BigInteger(Integer.toString(component.getValue())));
                        }
                        return xbCount;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweCountRange component) throws EncodingException {
                        CountRangeType xml = CountRangeType.Factory.newInstance(getXmlOptions());
                        if (component.isSetValue()) {
                            xml.setValue(component.getValue().getRangeAsList());
                        }
                        return xml;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweBoolean component) throws EncodingException {
                        BooleanType xbBoolean = BooleanType.Factory.newInstance(getXmlOptions());
                        if (component.isSetValue()) {
                            xbBoolean.setValue(component.getValue());
                        }
                        return xbBoolean;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweCategory component) throws EncodingException {
                        CategoryType xbCategory = CategoryType.Factory.newInstance(getXmlOptions());
                        if (component.getCodeSpace() != null) {
                            xbCategory.addNewCodeSpace().setHref(component.getCodeSpace());
                        }
                        if (component.isSetValue()) {
                            xbCategory.setValue(component.getValue());
                        }
                        return xbCategory;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweCategoryRange component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SweObservableProperty component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SweQuantity component) throws EncodingException {
                        QuantityType xbQuantity = QuantityType.Factory.newInstance(getXmlOptions());
                        if (component.isSetAxisID()) {
                            xbQuantity.setAxisID(component.getAxisID());
                        }
                        if (component.isSetValue()) {
                            xbQuantity.setValue(component.getValue());
                        }
                        if (component.isSetUom()) {
                            xbQuantity.setUom(createUnitReference(component.getUom()));
                        } else {
                            xbQuantity.setUom(createUnknownUnitReference());
                        }
                        if (component.getQuality() != null) {
                            // TODO implement
                            LOGGER.warn("Quality encoding is not supported for {}", xbQuantity.schemaType());
                        }
                        return xbQuantity;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweQuantityRange component) throws EncodingException {
                        QuantityRangeType xbQuantityRange = QuantityRangeType.Factory.newInstance(getXmlOptions());
                        if (component.isSetAxisID()) {
                            xbQuantityRange.setAxisID(component.getAxisID());
                        }
                        if (component.isSetValue()) {
                            xbQuantityRange.setValue(component.getValue().getRangeAsList());
                        }
                        if (component.isSetUom()) {
                            xbQuantityRange.setUom(createUnitReference(component.getUom()));
                        } else {
                            xbQuantityRange.setUom(createUnknownUnitReference());
                        }
                        if (component.isSetQuality()) {
                            // TODO implement
                            LOGGER.warn("Quality encoding is not supported for {}", xbQuantityRange.schemaType());
                        }
                        return xbQuantityRange;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweText component) throws EncodingException {
                        TextType xbText = TextType.Factory.newInstance(getXmlOptions());
                        if (component.isSetValue()) {
                            xbText.setValue(component.getValue());
                        }
                        return xbText;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweTime component) throws EncodingException {
                        TimeType xbTime = TimeType.Factory.newInstance(getXmlOptions());
                        if (component.isSetValue()) {
                            xbTime.setValue(component.getValue());
                        }
                        if (component.isSetUom()) {
                            xbTime.setUom(createUnitReference(component.getUom()));
                        }
                        if (component.getQuality() != null) {
                            // TODO implement
                            LOGGER.warn("Quality encoding is not supported for {}", xbTime.schemaType());
                        }
                        return xbTime;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweTimeRange component) throws EncodingException {
                        TimeRangeType xbTimeRange = TimeRangeType.Factory.newInstance(getXmlOptions());
                        if (component.isSetUom()) {
                            xbTimeRange.addNewUom().setHref(component.getUom());
                        }
                        if (component.isSetValue()) {
                            xbTimeRange.setValue(component.getValue().getRangeAsStringList());
                        }
                        if (component.isSetQuality()) {
                            // TODO implement
                            LOGGER.warn("Quality encoding is not supported for {}", xbTimeRange.schemaType());
                        }
                        return xbTimeRange;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweEnvelope component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SweVector component) throws EncodingException {
                        VectorType xbVector = VectorType.Factory.newInstance(getXmlOptions());
                        if (component.isSetReferenceFrame()) {
                            xbVector.setReferenceFrame(component.getReferenceFrame());
                        }
                        if (component.isSetLocalFrame()) {
                            xbVector.setLocalFrame(component.getLocalFrame());
                        }
                        if (component.isSetCoordinates()) {
                            for (SweCoordinate<?> coordinate : component.getCoordinates()) {
                                xbVector.addNewCoordinate().set(createCoordinate(coordinate));
                            }
                        }
                        return xbVector;
                    }

                    @Override
                    public AbstractDataComponentType visit(SweSimpleDataRecord component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SmlPosition component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SmlDataInterface component) throws EncodingException {
                        return unsupported(component);
                    }

                    @Override
                    public AbstractDataComponentType visit(SmlFeatureOfInterest component) throws EncodingException {
                        return unsupported(component);
                    }

                    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
                    private AbstractDataComponentType unsupported(SweAbstractDataComponent component)
                            throws EncodingException {

                        String xml = component.getXml();

                        if (xml != null && !xml.isEmpty()) {
                            try {
                                return (AbstractDataComponentType) XmlObject.Factory.parse(xml, getXmlOptions());
                            } catch (XmlException ex) {
                                String message = String.format("Error while decoding %s:\n%s",
                                                               SweAbstractDataComponent.class.getName(), xml);
                                throw new EncodingException(ex, message);
                            }
                        }
                        throw new NotYetSupportedEncodingException(SweAbstractDataComponent.class.getName(), component);
                    }
                });

        if (component.isSetDefinition()) {
            xmlComponent.setDefinition(component.getDefinition());
        }
        if (component.isSetDescription()) {
            xmlComponent.setDescription(component.getDescription());
        }
        if (component.isSetIdentifier()) {
            xmlComponent.setIdentifier(component.getIdentifier());
        }
        if (component.isSetLabel()) {
            xmlComponent.setLabel(component.getLabel());
        }

        if (additionalValues.has(XmlBeansEncodingFlags.PROPERTY_TYPE) ||
            // TODO remove this
            additionalValues.has(SosHelperValues.FOR_OBSERVATION)) {
            return asPropertyType(xmlComponent);
        } else if (additionalValues.has(XmlBeansEncodingFlags.DOCUMENT)) {
            return asDocument(xmlComponent);
        } else {
            return xmlComponent;
        }
    }

    private XmlObject asPropertyType(AbstractDataComponentType type) throws NotYetSupportedEncodingException {
        if (type instanceof BooleanType) {
            BooleanPropertyType propertyType = BooleanPropertyType.Factory.newInstance();
            propertyType.setBoolean((BooleanType) type);
            return propertyType;
        } else if (type instanceof CountType) {
            CountPropertyType propertyType = CountPropertyType.Factory.newInstance();
            propertyType.setCount((CountType) type);
            return propertyType;
        } else if (type instanceof CountRangeType) {
            CountRangePropertyType propertyType = CountRangePropertyType.Factory.newInstance();
            propertyType.setCountRange((CountRangeType) type);
            return propertyType;
        } else if (type instanceof QuantityType) {
            QuantityPropertyType propertyType = QuantityPropertyType.Factory.newInstance();
            propertyType.setQuantity((QuantityType) type);
            return propertyType;
        } else if (type instanceof QuantityRangeType) {
            QuantityRangePropertyType propertyType = QuantityRangePropertyType.Factory.newInstance();
            propertyType.setQuantityRange((QuantityRangeType) type);
            return propertyType;
        } else if (type instanceof TimeType) {
            TimePropertyType propertyType = TimePropertyType.Factory.newInstance();
            propertyType.setTime((TimeType) type);
            return propertyType;
        } else if (type instanceof TimeRangeType) {
            TimeRangePropertyType propertyType = TimeRangePropertyType.Factory.newInstance();
            propertyType.setTimeRange((TimeRangeType) type);
            return propertyType;
        } else if (type instanceof CategoryType) {
            CategoryPropertyType propertyType = CategoryPropertyType.Factory.newInstance();
            propertyType.setCategory((CategoryType) type);
            return propertyType;
        } else if (type instanceof CategoryRangeType) {
            CategoryRangePropertyType propertyType = CategoryRangePropertyType.Factory.newInstance();
            propertyType.setCategoryRange((CategoryRangeType) type);
            return propertyType;
        } else if (type instanceof MatrixType) {
            MatrixPropertyType propertyType = MatrixPropertyType.Factory.newInstance();
            propertyType.setMatrix((MatrixType) type);
            return propertyType;
        } else if (type instanceof DataArrayType) {
            DataArrayPropertyType propertyType = DataArrayPropertyType.Factory.newInstance();
            propertyType.setDataArray1((DataArrayType) type);
            return propertyType;
        } else if (type instanceof DataChoiceType) {
            DataChoicePropertyType propertyType = DataChoicePropertyType.Factory.newInstance();
            propertyType.setDataChoice((DataChoiceType) type);
            return propertyType;
        } else if (type instanceof DataRecordType) {
            DataRecordPropertyType propertyType = DataRecordPropertyType.Factory.newInstance();
            propertyType.setDataRecord((DataRecordType) type);
            return propertyType;
        } else if (type instanceof TextType) {
            TextPropertyType propertyType = TextPropertyType.Factory.newInstance();
            propertyType.setText((TextType) type);
            return propertyType;
        } else if (type instanceof VectorType) {
            VectorPropertyType propertyType = VectorPropertyType.Factory.newInstance();
            propertyType.setVector((VectorType) type);
            return propertyType;
        } else {
            throw new NotYetSupportedEncodingException(type.getClass().getName(), type);
        }
    }

    private XmlObject asDocument(AbstractDataComponentType type) throws NotYetSupportedEncodingException {
        if (type instanceof BooleanType) {
            BooleanDocument document = BooleanDocument.Factory.newInstance();
            document.setBoolean((BooleanType) type);
            return document;
        } else if (type instanceof CountType) {
            CountDocument document = CountDocument.Factory.newInstance();
            document.setCount((CountType) type);
            return document;
        } else if (type instanceof CountRangeType) {
            CountRangeDocument document = CountRangeDocument.Factory.newInstance();
            document.setCountRange((CountRangeType) type);
            return document;
        } else if (type instanceof QuantityType) {
            QuantityDocument document = QuantityDocument.Factory.newInstance();
            document.setQuantity((QuantityType) type);
            return document;
        } else if (type instanceof QuantityRangeType) {
            QuantityRangeDocument document = QuantityRangeDocument.Factory.newInstance();
            document.setQuantityRange((QuantityRangeType) type);
            return document;
        } else if (type instanceof TimeType) {
            TimeDocument document = TimeDocument.Factory.newInstance();
            document.setTime((TimeType) type);
            return document;
        } else if (type instanceof TimeRangeType) {
            TimeRangeDocument document = TimeRangeDocument.Factory.newInstance();
            document.setTimeRange((TimeRangeType) type);
            return document;
        } else if (type instanceof CategoryType) {
            CategoryDocument document = CategoryDocument.Factory.newInstance();
            document.setCategory((CategoryType) type);
            return document;
        } else if (type instanceof CategoryRangeType) {
            CategoryRangeDocument document = CategoryRangeDocument.Factory.newInstance();
            document.setCategoryRange((CategoryRangeType) type);
            return document;
        } else if (type instanceof MatrixType) {
            MatrixDocument document = MatrixDocument.Factory.newInstance();
            document.setMatrix((MatrixType) type);
            return document;
        } else if (type instanceof DataArrayType) {
            DataArrayDocument document = DataArrayDocument.Factory.newInstance();
            document.setDataArray1((DataArrayType) type);
            return document;
        } else if (type instanceof DataChoiceType) {
            DataChoiceDocument document = DataChoiceDocument.Factory.newInstance();
            document.setDataChoice((DataChoiceType) type);
            return document;
        } else if (type instanceof DataRecordType) {
            DataRecordDocument document = DataRecordDocument.Factory.newInstance();
            document.setDataRecord((DataRecordType) type);
            return document;
        } else if (type instanceof TextType) {
            TextDocument document = TextDocument.Factory.newInstance();
            document.setText((TextType) type);
            return document;
        } else if (type instanceof VectorType) {
            VectorDocument document = VectorDocument.Factory.newInstance();
            document.setVector((VectorType) type);
            return document;
        } else {
            throw new NotYetSupportedEncodingException(type.getClass().getName(), type);
        }
    }

    private XmlString createValues(List<List<String>> values, SweAbstractEncoding encoding) {
        // TODO How to deal with the decimal separator - is it an issue here?
        SweTextEncoding textEncoding = (SweTextEncoding) encoding;

        String valueString = values.stream().map(block -> String.join(textEncoding.getTokenSeparator(), block))
                .collect(joining(textEncoding.getBlockSeparator()));
        // create XB result object
        XmlString xmlString = XmlString.Factory.newInstance(getXmlOptions());
        xmlString.setStringValue(valueString);
        return xmlString;
    }

    private DataRecordType.Field createField(SweField field) throws EncodingException {
        SweAbstractDataComponent element = field.getElement();
        DataRecordType.Field xmlField = DataRecordType.Field.Factory.newInstance(getXmlOptions());

        if (field.isSetName()) {
            xmlField.setName(NcName.makeValid(field.getName().getValue()));
        }

        XmlObject encodeObjectToXml = createAbstractDataComponent(element, EncodingContext.empty());
        XmlObject substituteElement = XmlHelper
                .substituteElement(xmlField.addNewAbstractDataComponent(), encodeObjectToXml);
        substituteElement.set(encodeObjectToXml);
        return xmlField;
    }

    private Coordinate createCoordinate(SweCoordinate<?> coordinate) throws EncodingException {
        Coordinate xbCoordinate = Coordinate.Factory.newInstance(getXmlOptions());
        xbCoordinate.setName(coordinate.getName());
        xbCoordinate.setQuantity((QuantityType) createAbstractDataComponent((SweQuantity) coordinate.getValue(), null));
        return xbCoordinate;
    }

    private AbstractEncodingType createAbstractEncoding(SweAbstractEncoding encoding)
            throws EncodingException {
        if (encoding instanceof SweTextEncoding) {
            return createTextEncoding((SweTextEncoding) encoding);
        }

        try {
            String xml = encoding.getXml();
            if (xml != null && !xml.isEmpty()) {
                XmlObject xmlObject = XmlObject.Factory.parse(xml, getXmlOptions());
                if (xmlObject instanceof AbstractEncodingType) {
                    return (AbstractEncodingType) xmlObject;
                }
            }
            throw new EncodingException("AbstractEncoding can not be encoded!");
        } catch (XmlException e) {
            throw new EncodingException("Error while encoding AbstractEncoding!", e);
        }
    }

    private TextEncodingType createTextEncoding(SweTextEncoding encoding) {
        TextEncodingType xml = TextEncodingType.Factory.newInstance(getXmlOptions());
        if (encoding.getBlockSeparator() != null) {
            xml.setBlockSeparator(encoding.getBlockSeparator());
        }
        if (encoding.isSetCollapseWhiteSpaces()) {
            xml.setCollapseWhiteSpaces(encoding.isCollapseWhiteSpaces());
        }
        if (encoding.getDecimalSeparator() != null) {
            xml.setDecimalSeparator(encoding.getDecimalSeparator());
        }
        if (encoding.getTokenSeparator() != null) {
            xml.setTokenSeparator(encoding.getTokenSeparator());
        }
        return xml;
    }

    private UnitReference createUnitReference(String uom) {
        UnitReference unitReference = UnitReference.Factory.newInstance(getXmlOptions());
        if (uom.startsWith("urn:") || uom.startsWith("http://")) {
            unitReference.setHref(uom);
        } else {
            unitReference.setCode(uom);
        }
        return unitReference;
    }

    private UnitReference createUnknownUnitReference() {
        UnitReference unitReference = UnitReference.Factory.newInstance(getXmlOptions());
        unitReference.setHref(OGCConstants.UNKNOWN);
        return unitReference;
    }

}
