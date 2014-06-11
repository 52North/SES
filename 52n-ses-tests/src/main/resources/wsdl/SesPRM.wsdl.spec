<?xml version="1.0" encoding="UTF-8"?>
<wsdl:definitions targetNamespace="http://www.opengis.net/ses/0.0"
    xmlns:ses="http://www.opengis.net/ses/0.0" xmlns:wsa="http://www.w3.org/2005/08/addressing"
    xmlns="http://schemas.xmlsoap.org/wsdl/" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
    xmlns:wsdl-soap12="http://schemas.xmlsoap.org/wsdl/soap12/"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:wsrf-r="http://docs.oasis-open.org/wsrf/r-2"
    xmlns:wsrf-rl="http://docs.oasis-open.org/wsrf/rl-2"
    xmlns:wsrf-bf="http://docs.oasis-open.org/wsrf/bf-2"
    xmlns:wsrf-rp="http://docs.oasis-open.org/wsrf/rp-2"
    xmlns:wsn-b="http://docs.oasis-open.org/wsn/b-2"
    xmlns:wsn-br="http://docs.oasis-open.org/wsn/br-2"
    xmlns:wsn-t="http://docs.oasis-open.org/wsn/t-1"
    xmlns:sml="http://www.opengis.net/sensorML/1.0.1" xmlns:ows="http://www.opengis.net/ows/1.1"
    name="SES">

    <wsdl:types>
        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.w3.org/1999/xlink">
            <xsd:include schemaLocation="http://schemas.opengis.net/xlink/1.0.0/xlinks.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified" targetNamespace="urn:us:gov:ic:ism:v2">
            <xsd:include schemaLocation="../xsd/opengis/ic/2.0/IC-ISM-v2.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://www.w3.org/2001/SMIL20/Language">
            <xsd:include
                schemaLocation="../xsd/opengis/gml/3.1.1/smil/smil20-language.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.w3.org/2001/SMIL20/">
            <xsd:include schemaLocation="../xsd/opengis/gml/3.1.1/smil/smil20.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.opengis.net/ows/1.1">
            <xsd:include schemaLocation="../xsd/opengis/ows/1.1.0/owsAll.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.opengis.net/gml">
            <xsd:include schemaLocation="../xsd/opengis/gml/3.1.1/base/gml.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.opengis.net/ogc">
            <xsd:include schemaLocation="../xsd/filter/1.1.0_experimental/filter.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.opengis.net/eml/0.0">
            <xsd:include schemaLocation="../xsd/08-132/eml.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://www.opengis.net/swe/1.0.1">
            <xsd:include schemaLocation="../xsd/opengis/sweCommon/1.0.1/swe.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://www.opengis.net/sensorML/1.0.1">
            <xsd:include schemaLocation="../xsd/opengis/sensorML/1.0.1/sensorML.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://www.w3.org/2005/08/addressing">
            <xsd:include schemaLocation="../xsd/w3/2005/08/addressing/ws-addr.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://docs.oasis-open.org/wsrf/rl-2">
            <xsd:include schemaLocation="../xsd/oasis-open/wsrf/rl-2.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://docs.oasis-open.org/wsrf/rp-2">
            <xsd:include schemaLocation="../xsd/oasis-open/wsrf/rp-2.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://docs.oasis-open.org/wsrf/r-2">
            <xsd:include schemaLocation="../xsd/oasis-open/wsrf/r-2.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://docs.oasis-open.org/wsn/b-2">
            <xsd:include schemaLocation="../xsd/oasis-open/wsn/b-2.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://docs.oasis-open.org/wsn/t-1">
            <xsd:include schemaLocation="../xsd/oasis-open/wsn/t-1.xsd"/>
        </xsd:schema>
        <xsd:schema elementFormDefault="qualified"
            targetNamespace="http://docs.oasis-open.org/wsn/br-2">
            <xsd:include schemaLocation="../xsd/oasis-open/wsn/br-2.xsd"/>
        </xsd:schema>

        <xsd:schema elementFormDefault="qualified" targetNamespace="http://www.opengis.net/ses/0.0">

            <xsd:include schemaLocation="../xsd/ses/sesCommon.xsd"/>
            <xsd:include schemaLocation="../xsd/ses/sesRenewRegistration.xsd"/>

            <xsd:element name="SesPRMResourceProperties">
                <xsd:complexType>
                    <xsd:sequence>
                        <!-- The resource contains the properties defined for a PublisherRegistration resource-->
                        <xsd:element ref="wsrf-rl:CurrentTime"/>
                        <xsd:element ref="wsrf-rl:TerminationTime"/>

                        <!-- The resource contains the properties defined for a PublisherRegistration resource -->
                        <xsd:element ref="wsn-br:PublisherReference" minOccurs="0" maxOccurs="1"/>
                        <xsd:element ref="wsn-br:Topic" minOccurs="0" maxOccurs="unbounded"/>
                        <xsd:element ref="wsn-br:Demand" minOccurs="1" maxOccurs="1"/>
                        <xsd:element ref="wsn-br:CreationTime" minOccurs="0" maxOccurs="1"/>

                        <!-- The resource also provides the SensorML description of the registered publisher if it is a sensor. -->
                        <xsd:element ref="sml:SensorML" minOccurs="0" maxOccurs="1"/>

                    </xsd:sequence>
                </xsd:complexType>
            </xsd:element>
        </xsd:schema>
    </wsdl:types>

    <wsdl:message name="DestroyRegistrationRequest">
        <wsdl:part name="DestroyRegistrationRequest" element="wsn-br:DestroyRegistration"/>
    </wsdl:message>
    <wsdl:message name="DestroyRegistrationResponse">
        <wsdl:part name="DestroyRegistrationResponse" element="wsn-br:DestroyRegistrationResponse"/>
    </wsdl:message>
    <wsdl:message name="DestroyRequest">
        <wsdl:part name="DestroyRequest" element="wsrf-rl:Destroy"/>
    </wsdl:message>
    <wsdl:message name="DestroyResponse">
        <wsdl:part name="DestroyResponse" element="wsrf-rl:DestroyResponse"/>
    </wsdl:message>
    <wsdl:message name="ResourceNotDestroyedFault">
        <wsdl:part name="ResourceNotDestroyedFault" element="wsrf-rl:ResourceNotDestroyedFault"/>
    </wsdl:message>
    <wsdl:message name="WSNBRResourceNotDestroyedFault">
        <wsdl:part name="WSNBRResourceNotDestroyedFault" element="wsn-br:ResourceNotDestroyedFault"
        />
    </wsdl:message>
    <wsdl:message name="ResourceUnknownFault">
        <wsdl:part name="ResourceUnknownFault" element="wsrf-r:ResourceUnknownFault"/>
    </wsdl:message>
    <wsdl:message name="ResourceUnavailableFault">
        <wsdl:part name="ResourceUnavailableFault" element="wsrf-r:ResourceUnavailableFault"/>
    </wsdl:message>
    <wsdl:message name="SetTerminationTimeRequest">
        <wsdl:part name="SetTerminationTimeRequest" element="wsrf-rl:SetTerminationTime"/>
    </wsdl:message>
    <wsdl:message name="SetTerminationTimeResponse">
        <wsdl:part name="SetTerminationTimeResponse" element="wsrf-rl:SetTerminationTimeResponse"/>
    </wsdl:message>
    <wsdl:message name="UnableToSetTerminationTimeFault">
        <wsdl:part name="UnableToSetTerminationTimeFault"
            element="wsrf-rl:UnableToSetTerminationTimeFault"/>
    </wsdl:message>
    <wsdl:message name="TerminationTimeChangeRejectedFault">
        <wsdl:part name="TerminationTimeChangeRejectedFault"
            element="wsrf-rl:TerminationTimeChangeRejectedFault"/>
    </wsdl:message>
    <wsdl:message name="GetResourcePropertyDocumentRequest">
        <wsdl:part name="GetResourcePropertyDocumentRequest"
            element="wsrf-rp:GetResourcePropertyDocument"/>
    </wsdl:message>
    <wsdl:message name="GetResourcePropertyDocumentResponse">
        <wsdl:part name="GetResourcePropertyDocumentResponse"
            element="wsrf-rp:GetResourcePropertyDocumentResponse"/>
    </wsdl:message>
    <wsdl:message name="GetResourcePropertyRequest">
        <wsdl:part name="GetResourcePropertyRequest" element="wsrf-rp:GetResourceProperty"/>
    </wsdl:message>
    <wsdl:message name="GetResourcePropertyResponse">
        <wsdl:part name="GetResourcePropertyResponse" element="wsrf-rp:GetResourcePropertyResponse"
        />
    </wsdl:message>
    <wsdl:message name="InvalidResourcePropertyQNameFault">
        <wsdl:part name="InvalidResourcePropertyQNameFault"
            element="wsrf-rp:InvalidResourcePropertyQNameFault"/>
    </wsdl:message>
    <wsdl:message name="GetMultipleResourcePropertiesRequest">
        <wsdl:part name="GetMultipleResourcePropertiesRequest"
            element="wsrf-rp:GetMultipleResourceProperties"/>
    </wsdl:message>
    <wsdl:message name="GetMultipleResourcePropertiesResponse">
        <wsdl:part name="GetMultipleResourcePropertiesResponse"
            element="wsrf-rp:GetMultipleResourcePropertiesResponse"/>
    </wsdl:message>
    <wsdl:message name="QueryResourcePropertiesRequest">
        <wsdl:part name="QueryResourcePropertiesRequest" element="wsrf-rp:QueryResourceProperties"/>
    </wsdl:message>
    <wsdl:message name="QueryResourcePropertiesResponse">
        <wsdl:part name="QueryResourcePropertiesResponse"
            element="wsrf-rp:QueryResourcePropertiesResponse"/>
    </wsdl:message>
    <wsdl:message name="UnknownQueryExpressionDialectFault">
        <wsdl:part name="UnknownQueryExpressionDialectFault"
            element="wsrf-rp:UnknownQueryExpressionDialectFault"/>
    </wsdl:message>
    <wsdl:message name="InvalidQueryExpressionFault">
        <wsdl:part name="InvalidQueryExpressionFault" element="wsrf-rp:InvalidQueryExpressionFault"
        />
    </wsdl:message>
    <wsdl:message name="QueryEvaluationErrorFault">
        <wsdl:part name="QueryEvaluationErrorFault" element="wsrf-rp:QueryEvaluationErrorFault"/>
    </wsdl:message>
    <wsdl:message name="SetResourcePropertiesRequest">
        <wsdl:part name="SetResourcePropertiesRequest" element="wsrf-rp:SetResourceProperties"/>
    </wsdl:message>
    <wsdl:message name="SetResourcePropertiesResponse">
        <wsdl:part name="SetResourcePropertiesResponse"
            element="wsrf-rp:SetResourcePropertiesResponse"/>
    </wsdl:message>
    <wsdl:message name="InvalidModificationFault">
        <wsdl:part name="InvalidModificationFault" element="wsrf-rp:InvalidModificationFault"/>
    </wsdl:message>
    <wsdl:message name="UnableToModifyResourcePropertyFault">
        <wsdl:part name="UnableToModifyResourcePropertyFault"
            element="wsrf-rp:UnableToModifyResourcePropertyFault"/>
    </wsdl:message>
    <wsdl:message name="SetResourcePropertyRequestFailedFault">
        <wsdl:part name="SetResourcePropertyRequestFailedFault"
            element="wsrf-rp:SetResourcePropertyRequestFailedFault"/>
    </wsdl:message>

    <wsdl:message name="UnacceptableInitialTerminationTimeFault">
        <wsdl:part name="UnacceptableInitialTerminationTimeFault"
            element="wsn-b:UnacceptableInitialTerminationTimeFault"/>
    </wsdl:message>

    <wsdl:message name="RenewRegistrationRequest">
        <wsdl:part name="RenewRegistrationRequest" element="ses:RenewRegistration"/>
    </wsdl:message>
    <wsdl:message name="RenewRegistrationResponse">
        <wsdl:part name="RenewRegistrationResponse" element="ses:RenewRegistrationResponse"/>
    </wsdl:message>

    <!-- =================================================== -->
    <!-- ======== SES PublisherRegistrationManager PortType======== -->
    <!-- =================================================== -->
    <wsdl:portType name="SesPRMPortType" wsrf-rp:ResourceProperties="ses:SesPRMResourceProperties">

        <!-- =========== implements WS-PublisherRegistrationManager interface =========== -->
        <wsdl:operation name="DestroyRegistration">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsn/brw-2/PublisherRegistrationManager/DestroyRegistrationRequest"
                name="DestroyRegistrationRequest" message="ses:DestroyRegistrationRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsn/brw-2/PublisherRegistrationManager/DestroyRegistrationResponse"
                name="DestroyRegistrationResponse" message="ses:DestroyRegistrationResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="WSNBRResourceNotDestroyedFault"
                message="ses:WSNBRResourceNotDestroyedFault"/>
        </wsdl:operation>

        <!-- =========== implements the following WS-ResourceProperties operations =========== -->
        <wsdl:operation name="GetResourcePropertyDocument">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/GetResourcePropertyDocument/GetResourcePropertyDocumentRequest"
                name="GetResourcePropertyDocumentRequest"
                message="ses:GetResourcePropertyDocumentRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/GetResourcePropertyDocument/GetResourcePropertyDocumentResponse"
                name="GetResourcePropertyDocumentResponse"
                message="ses:GetResourcePropertyDocumentResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
        </wsdl:operation>

        <wsdl:operation name="GetResourceProperty">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/GetResourceProperty/GetResourcePropertyRequest"
                name="GetResourcePropertyRequest" message="ses:GetResourcePropertyRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/GetResourceProperty/GetResourcePropertyResponse"
                name="GetResourcePropertyResponse" message="ses:GetResourcePropertyResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
            <wsdl:fault name="InvalidResourcePropertyQNameFault"
                message="ses:InvalidResourcePropertyQNameFault"/>
        </wsdl:operation>

        <wsdl:operation name="GetMultipleResourceProperties">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/GetMultipleResourceProperties/GetMultipleResourcePropertiesRequest"
                name="GetMultipleResourcePropertiesRequest"
                message="ses:GetMultipleResourcePropertiesRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/GetMultipleResourceProperties/GetMultipleResourcePropertiesResponse"
                name="GetMultipleResourcePropertiesResponse"
                message="ses:GetMultipleResourcePropertiesResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
            <wsdl:fault name="InvalidResourcePropertyQNameFault"
                message="ses:InvalidResourcePropertyQNameFault"/>
        </wsdl:operation>

        <wsdl:operation name="QueryResourceProperties">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest"
                name="QueryResourcePropertiesRequest" message="ses:QueryResourcePropertiesRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesResponse"
                name="QueryResourcePropertiesResponse" message="ses:QueryResourcePropertiesResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
            <wsdl:fault name="UnknownQueryExpressionDialectFault"
                message="ses:UnknownQueryExpressionDialectFault"/>
            <wsdl:fault name="InvalidQueryExpressionFault" message="ses:InvalidQueryExpressionFault"/>
            <wsdl:fault name="QueryEvaluationErrorFault" message="ses:QueryEvaluationErrorFault"/>
        </wsdl:operation>

        <wsdl:operation name="SetResourceProperties">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/SetResourceProperties/SetResourcePropertiesRequest"
                name="SetResourcePropertiesRequest" message="ses:SetResourcePropertiesRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rpw-2/SetResourceProperties/SetResourcePropertiesResponse"
                name="SetResourcePropertiesResponse" message="ses:SetResourcePropertiesResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
            <wsdl:fault name="InvalidModificationFault" message="ses:InvalidModificationFault"/>
            <wsdl:fault name="UnableToModifyResourcePropertyFault"
                message="ses:UnableToModifyResourcePropertyFault"/>
            <wsdl:fault name="InvalidResourcePropertyQNameFault"
                message="ses:InvalidResourcePropertyQNameFault"/>
            <wsdl:fault name="SetResourcePropertyRequestFailedFault"
                message="ses:SetResourcePropertyRequestFailedFault"/>
        </wsdl:operation>

        <!-- implements the WS-ResourceLifetime operations -->
        <wsdl:operation name="Destroy">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rlw-2/ImmediateResourceTermination/DestroyRequest"
                name="DestroyRequest" message="ses:DestroyRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rlw-2/ImmediateResourceTermination/DestroyResponse"
                name="DestroyResponse" message="ses:DestroyResponse"/>
            <wsdl:fault name="ResourceNotDestroyedFault" message="ses:ResourceNotDestroyedFault"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
        </wsdl:operation>
        <wsdl:operation name="SetTerminationTime">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsrf/rlw-2/ScheduledResourceTermination/SetTerminationTimeRequest"
                name="SetTerminationTimeRequest" message="ses:SetTerminationTimeRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsrf/rlw-2/ScheduledResourceTermination/SetTerminationTimeResponse"
                name="SetTerminationTimeResponse" message="ses:SetTerminationTimeResponse"/>
            <wsdl:fault name="UnableToSetTerminationTimeFault"
                message="ses:UnableToSetTerminationTimeFault"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="ResourceUnavailableFault" message="ses:ResourceUnavailableFault"/>
            <wsdl:fault name="TerminationTimeChangeRejectedFault"
                message="ses:TerminationTimeChangeRejectedFault"/>
        </wsdl:operation>

        <!-- ========= implements the Renew operation ======= -->
        <wsdl:operation name="RenewRegistration">
            <wsdl:input
                wsa:Action="http://www.opengis.net/ses/SesPublisherRegistrationManager/RenewRegistrationRequest"
                name="RenewRegistrationRequest" message="ses:RenewRegistrationRequest"/>
            <wsdl:output
                wsa:Action="http://www.opengis.net/ses/SesPublisherRegistrationManager/RenewRegistrationResponse"
                name="RenewRegistrationResponse" message="ses:RenewRegistrationResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="UnacceptableTerminationTimeFault"
                message="ses:UnacceptableTerminationTimeFault"/>
        </wsdl:operation>

    </wsdl:portType>

    <!-- ============================= ========================= -->
    <!-- ====================== BINDING ========================= -->
    <!-- ======================================================= -->
    <wsdl:binding name="SesPRMBinding" type="ses:SesPRMPortType">
        <wsdl-soap12:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>

        <wsdl:operation name="RenewRegistration">
            <wsdl-soap12:operation soapAction="RenewRegistration"/>
            <wsdl:input name="RenewRegistrationRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="RenewRegistrationResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnacceptableTerminationTimeFault">
                <wsdl-soap12:fault use="encoded" name="UnacceptableTerminationTimeFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="DestroyRegistration">
            <wsdl-soap12:operation soapAction="DestroyRegistration"/>
            <wsdl:input name="DestroyRegistrationRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="DestroyRegistrationResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="WSNBRResourceNotDestroyedFault">
                <wsdl-soap12:fault use="encoded" name="ResourceNotDestroyedFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="Destroy">
            <wsdl-soap12:operation soapAction="Destroy"/>
            <wsdl:input name="DestroyRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="DestroyResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceNotDestroyedFault">
                <wsdl-soap12:fault use="encoded" name="ResourceNotDestroyedFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
        </wsdl:operation>
        <wsdl:operation name="SetTerminationTime">
            <wsdl-soap12:operation soapAction="SetTerminationTime"/>
            <wsdl:input name="SetTerminationTimeRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="SetTerminationTimeResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="UnableToSetTerminationTimeFault">
                <wsdl-soap12:fault use="encoded" name="UnableToSetTerminationTimeFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
            <wsdl:fault name="TerminationTimeChangeRejectedFault">
                <wsdl-soap12:fault use="encoded" name="TerminationTimeChangeRejectedFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="GetResourcePropertyDocument">
            <wsdl-soap12:operation soapAction="GetResourcePropertyDocument"/>
            <wsdl:input name="GetResourcePropertyDocumentRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="GetResourcePropertyDocumentResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="GetResourceProperty">
            <wsdl-soap12:operation soapAction="GetResourceProperty"/>
            <wsdl:input name="GetResourcePropertyRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="GetResourcePropertyResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidResourcePropertyQNameFault">
                <wsdl-soap12:fault use="encoded" name="InvalidResourcePropertyQNameFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="GetMultipleResourceProperties">
            <wsdl-soap12:operation soapAction="GetMultipleResourceProperties"/>
            <wsdl:input name="GetMultipleResourcePropertiesRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="GetMultipleResourcePropertiesResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidResourcePropertyQNameFault">
                <wsdl-soap12:fault use="encoded" name="InvalidResourcePropertyQNameFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="QueryResourceProperties">
            <wsdl-soap12:operation soapAction="QueryResourceProperties"/>
            <wsdl:input name="QueryResourcePropertiesRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="QueryResourcePropertiesResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnknownQueryExpressionDialectFault">
                <wsdl-soap12:fault use="encoded" name="UnknownQueryExpressionDialectFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidQueryExpressionFault">
                <wsdl-soap12:fault use="encoded" name="InvalidQueryExpressionFault"/>
            </wsdl:fault>
            <wsdl:fault name="QueryEvaluationErrorFault">
                <wsdl-soap12:fault use="encoded" name="QueryEvaluationErrorFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="SetResourceProperties">
            <wsdl-soap12:operation soapAction="http://oasis.org/SetResourceProperties"/>
            <wsdl:input name="SetResourcePropertiesRequest">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output name="SetResourcePropertiesResponse">
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="ResourceUnavailableFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnavailableFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidModificationFault">
                <wsdl-soap12:fault use="encoded" name="InvalidModificationFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnableToModifyResourcePropertyFault">
                <wsdl-soap12:fault use="encoded" name="UnableToModifyResourcePropertyFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidResourcePropertyQNameFault">
                <wsdl-soap12:fault use="encoded" name="InvalidResourcePropertyQNameFault"/>
            </wsdl:fault>
            <wsdl:fault name="SetResourcePropertyRequestFailedFault">
                <wsdl-soap12:fault use="encoded" name="SetResourcePropertyRequestFailedFault"/>
            </wsdl:fault>
        </wsdl:operation>

    </wsdl:binding>
    <wsdl:service name="SesPRMService">
        <wsdl:port name="SesPRMPort" binding="ses:SesPRMBinding">
            <wsdl-soap12:address
                location="http://localhost:8080/SES/services/SesPublisherRegistrationManager"/>
        </wsdl:port>
    </wsdl:service>
</wsdl:definitions>
