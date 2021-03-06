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
            <xsd:include schemaLocation="../xsd/opengis/xlink/1.0.0/xlinks.xsd"/>
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
            <xsd:include schemaLocation="../xsd/ses/sesAll.xsd"/>
            <xsd:include schemaLocation="../xsd/ses/sesCapabilities.xsd"/>
            <xsd:include schemaLocation="../xsd/ses/sesCommon.xsd"/>
            <xsd:include schemaLocation="../xsd/ses/sesDescribeSensor.xsd"/>
            <xsd:include schemaLocation="../xsd/ses/sesRenewRegistration.xsd"/>
            <xsd:include schemaLocation="../xsd/ses/sesMessageSchema.xsd"/>

            <xsd:element name="SesResourceProperties">
                <xsd:complexType>
                    <xsd:sequence>
                        <xsd:element ref="wsn-b:FixedTopicSet"/>
                        <xsd:element ref="wsn-t:TopicSet" minOccurs="0"/>
                        <xsd:element ref="wsn-b:TopicExpression" minOccurs="0" maxOccurs="unbounded"/>
                        <xsd:element ref="wsn-b:TopicExpressionDialect" minOccurs="0"
                            maxOccurs="unbounded"/>
                    </xsd:sequence>
                </xsd:complexType>
            </xsd:element>

            <xsd:complexType name="ServiceExceptionType">
                <xsd:complexContent>
                    <xsd:extension base="wsrf-bf:BaseFaultType">
                        <xsd:choice>
                            <xsd:element ref="ows:ExceptionReport"/>
                            <xsd:element ref="ows:Exception"/>
                        </xsd:choice>
                    </xsd:extension>
                </xsd:complexContent>
            </xsd:complexType>

            <xsd:element name="ServiceException" type="ses:ServiceExceptionType"/>

        </xsd:schema>
    </wsdl:types>

    <wsdl:message name="DestroyRequest">
        <wsdl:part name="DestroyRequest" element="wsrf-rl:Destroy"/>
    </wsdl:message>
    <wsdl:message name="DestroyResponse">
        <wsdl:part name="DestroyResponse" element="wsrf-rl:DestroyResponse"/>
    </wsdl:message>
    <wsdl:message name="ResourceNotDestroyedFault">
        <wsdl:part name="ResourceNotDestroyedFault" element="wsrf-rl:ResourceNotDestroyedFault"/>
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
    <wsdl:message name="SubscribeRequest">
        <wsdl:part name="SubscribeRequest" element="wsn-b:Subscribe"/>
    </wsdl:message>

    <wsdl:message name="SubscribeResponse">
        <wsdl:part name="SubscribeResponse" element="wsn-b:SubscribeResponse"/>
    </wsdl:message>

    <wsdl:message name="SubscribeCreationFailedFault">
        <wsdl:part name="SubscribeCreationFailedFault" element="wsn-b:SubscribeCreationFailedFault"
        />
    </wsdl:message>

    <wsdl:message name="TopicExpressionDialectUnknownFault">
        <wsdl:part name="TopicExpressionDialectUnknownFault"
            element="wsn-b:TopicExpressionDialectUnknownFault"/>
    </wsdl:message>

    <wsdl:message name="InvalidFilterFault">
        <wsdl:part name="InvalidFilterFault" element="wsn-b:InvalidFilterFault"/>
    </wsdl:message>

    <wsdl:message name="InvalidProducerPropertiesExpressionFault">
        <wsdl:part name="InvalidProducerPropertiesExpressionFault"
            element="wsn-b:InvalidProducerPropertiesExpressionFault"/>
    </wsdl:message>

    <wsdl:message name="InvalidMessageContentExpressionFault">
        <wsdl:part name="InvalidMessageContentExpressionFault"
            element="wsn-b:InvalidMessageContentExpressionFault"/>
    </wsdl:message>

    <wsdl:message name="UnrecognizedPolicyRequestFault">
        <wsdl:part name="UnrecognizedPolicyRequestFault"
            element="wsn-b:UnrecognizedPolicyRequestFault"/>
    </wsdl:message>

    <wsdl:message name="UnsupportedPolicyRequestFault">
        <wsdl:part name="UnsupportedPolicyRequestFault"
            element="wsn-b:UnsupportedPolicyRequestFault"/>
    </wsdl:message>

    <wsdl:message name="NotifyMessageNotSupportedFault">
        <wsdl:part name="NotifyMessageNotSupportedFault"
            element="wsn-b:NotifyMessageNotSupportedFault"/>
    </wsdl:message>

    <wsdl:message name="UnacceptableInitialTerminationTimeFault">
        <wsdl:part name="UnacceptableInitialTerminationTimeFault"
            element="wsn-b:UnacceptableInitialTerminationTimeFault"/>
    </wsdl:message>

    <wsdl:message name="GetCurrentMessageRequest">
        <wsdl:part name="GetCurrentMessageRequest" element="wsn-b:GetCurrentMessage"/>
    </wsdl:message>

    <wsdl:message name="GetCurrentMessageResponse">
        <wsdl:part name="GetCurrentMessageResponse" element="wsn-b:GetCurrentMessageResponse"/>
    </wsdl:message>

    <wsdl:message name="InvalidTopicExpressionFault">
        <wsdl:part name="InvalidTopicExpressionFault" element="wsn-b:InvalidTopicExpressionFault"/>
    </wsdl:message>

    <wsdl:message name="TopicNotSupportedFault">
        <wsdl:part name="TopicNotSupportedFault" element="wsn-b:TopicNotSupportedFault"/>
    </wsdl:message>

    <wsdl:message name="MultipleTopicsSpecifiedFault">
        <wsdl:part name="MultipleTopicsSpecifiedFault" element="wsn-b:MultipleTopicsSpecifiedFault"
        />
    </wsdl:message>

    <wsdl:message name="NoCurrentMessageOnTopicFault">
        <wsdl:part name="NoCurrentMessageOnTopicFault" element="wsn-b:NoCurrentMessageOnTopicFault"
        />
    </wsdl:message>

    <wsdl:message name="NotifyRequest">
        <wsdl:part name="NotifyRequest" element="wsn-b:Notify"/>
    </wsdl:message>
    <wsdl:message name="RegisterPublisherRequest">
        <wsdl:part name="RegisterPublisherRequest" element="wsn-br:RegisterPublisher"/>
    </wsdl:message>

    <wsdl:message name="RegisterPublisherResponse">
        <wsdl:part name="RegisterPublisherResponse" element="wsn-br:RegisterPublisherResponse"/>
    </wsdl:message>


    <!-- ============= MESSAGES ============= -->

    <!-- SES specific messages -->
    <wsdl:message name="GetCapabilitiesRequest">
        <wsdl:part name="GetCapabilitiesRequest" element="ses:GetCapabilities"/>
    </wsdl:message>
    <wsdl:message name="GetCapabilitiesResponse">
        <wsdl:part name="GetCapabilitiesResponse" element="ses:Capabilities"/>
    </wsdl:message>

    <wsdl:message name="DescribeSensorRequest">
        <wsdl:part name="DescribeSensorRequest" element="ses:DescribeSensor"/>
    </wsdl:message>
    <wsdl:message name="DescribeSensorResponse">
        <wsdl:part name="DescribeSensorResponse" element="sml:SensorML"/>
    </wsdl:message>

    <wsdl:message name="ServiceException">
        <wsdl:part name="ServiceException" type="ses:OWSServiceException"/>
    </wsdl:message>

    <!-- =================================================== -->
    <!-- ==================== SES PortType ==================== -->
    <!-- =================================================== -->
    <wsdl:portType name="SesPortType" wsrf-rp:ResourceProperties="ses:SesResourceProperties">

        <!-- ============= implements GetCapabilities ============= -->
        <wsdl:operation name="GetCapabilities">
            <wsdl:input wsa:Action="http://www.opengis.net/ses/GetCapabilitiesRequest"
                message="ses:GetCapabilitiesRequest"/>
            <wsdl:output wsa:Action="http://www.opengis.net/ses/GetCapabilitiesResponse"
                message="ses:GetCapabilitiesResponse"/>
            <wsdl:fault name="Exception" message="ses:ServiceException"/>
        </wsdl:operation>

        <!-- ============= implements DescribeSensor  ============= -->
        <wsdl:operation name="DescribeSensor">
            <wsdl:input wsa:Action="http://www.opengis.net/ses/DescribeSensorRequest"
                message="ses:DescribeSensorRequest"/>
            <wsdl:output wsa:Action="http://www.opengis.net/ses/DescribeSensorResponse"
                message="ses:DescribeSensorResponse"/>
            <wsdl:fault name="Exception" message="ses:ServiceException"/>
        </wsdl:operation>

        <!-- ============= extends NotificationConsumer ============= -->
        <wsdl:operation name="Notify">
            <wsdl:input wsa:Action="http://docs.oasis-open.org/wsn/bw-2/NotificationConsumer/Notify"
                message="ses:Notify"/>
        </wsdl:operation>

        <!-- ============= extends NotificationProducer ============= -->
        <wsdl:operation name="Subscribe">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsn/bw-2/NotificationProducer/SubscribeRequest"
                message="ses:SubscribeRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsn/bw-2/NotificationProducer/SubscribeResponse"
                message="ses:SubscribeResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="InvalidFilterFault" message="ses:InvalidFilterFault"/>
            <wsdl:fault name="TopicExpressionDialectUnknownFault"
                message="ses:TopicExpressionDialectUnknownFault"/>
            <wsdl:fault name="InvalidTopicExpressionFault" message="ses:InvalidTopicExpressionFault"/>
            <wsdl:fault name="TopicNotSupportedFault" message="ses:TopicNotSupportedFault"/>
            <wsdl:fault name="InvalidProducerPropertiesExpressionFault"
                message="ses:InvalidProducerPropertiesExpressionFault"/>
            <wsdl:fault name="InvalidMessageContentExpressionFault"
                message="ses:InvalidMessageContentExpressionFault"/>
            <wsdl:fault name="UnacceptableInitialTerminationTimeFault"
                message="ses:UnacceptableInitialTerminationTimeFault"/>
            <wsdl:fault name="UnrecognizedPolicyRequestFault"
                message="ses:UnrecognizedPolicyRequestFault"/>
            <wsdl:fault name="UnsupportedPolicyRequestFault"
                message="ses:UnsupportedPolicyRequestFault"/>
            <wsdl:fault name="NotifyMessageNotSupportedFault"
                message="ses:NotifyMessageNotSupportedFault"/>
            <wsdl:fault name="SubscribeCreationFailedFault"
                message="ses:SubscribeCreationFailedFault"/>
        </wsdl:operation>

        <wsdl:operation name="GetCurrentMessage">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsn/bw-2/NotificationProducer/GetCurrentMessageRequest"
                message="ses:GetCurrentMessageRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsn/bw-2/NotificationProducer/GetCurrentMessageResponse"
                message="ses:GetCurrentMessageResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="TopicExpressionDialectUnknownFault"
                message="ses:TopicExpressionDialectUnknownFault"/>
            <wsdl:fault name="InvalidTopicExpressionFault" message="ses:InvalidTopicExpressionFault"/>
            <wsdl:fault name="TopicNotSupportedFault" message="ses:TopicNotSupportedFault"/>
            <wsdl:fault name="NoCurrentMessageOnTopicFault"
                message="ses:NoCurrentMessageOnTopicFault"/>
            <wsdl:fault name="MultipleTopicsSpecifiedFault"
                message="ses:MultipleTopicsSpecifiedFault"/>
        </wsdl:operation>

        <!-- ========= extends RegisterPublisher ======= -->
        <!-- Will be used by SES to both register push-style sensors but also to register pull-style for SOS 
			(via extension mechanism - has to be specified) -->
        <wsdl:operation name="RegisterPublisher">
            <wsdl:input
                wsa:Action="http://docs.oasis-open.org/wsn/brw-2/RegisterPublisher/RegisterPublisherRequest"
                message="ses:RegisterPublisherRequest"/>
            <wsdl:output
                wsa:Action="http://docs.oasis-open.org/wsn/brw-2/RegisterPublisher/RegisterPublisherResponse"
                message="ses:RegisterPublisherResponse"/>
            <wsdl:fault name="ResourceUnknownFault" message="ses:ResourceUnknownFault"/>
            <wsdl:fault name="InvalidTopicExpressionFault" message="ses:InvalidTopicExpressionFault"/>
            <wsdl:fault name="TopicNotSupportedFault" message="ses:TopicNotSupportedFault"/>
            <wsdl:fault name="PublisherRegistrationRejectedFault"
                message="ses:PublisherRegistrationRejectedFault"/>
            <wsdl:fault name="PublisherRegistrationFailedFault"
                message="ses:PublisherRegistrationFailedFault"/>
            <wsdl:fault name="UnacceptableInitialTerminationTimeFault"
                message="ses:UnacceptableInitialTerminationTimeFault"/>
        </wsdl:operation>

        <!-- =========== implements WS-ResourceProperties operations =========== -->
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
    </wsdl:portType>

    <!-- ============================= ========================= -->
    <!-- ====================== BINDING ========================= -->
    <!-- ======================================================= -->
    <wsdl:binding name="SesBinding" type="ses:SesPortType">
        <wsdl-soap12:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>

        <wsdl:operation name="GetCapabilities">
            <wsdl-soap12:operation soapAction="GetCapabilities"/>
            <wsdl:input>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="Exception">
                <wsdl-soap12:fault use="encoded" name="Exception"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="DescribeSensor">
            <wsdl-soap12:operation soapAction="DescribeSensor"/>
            <wsdl:input>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="Exception">
                <wsdl-soap12:fault use="encoded" name="Exception"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="Notify">
            <wsdl-soap12:operation soapAction="Notify"/>
            <wsdl:input>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
        </wsdl:operation>

        <wsdl:operation name="Subscribe">
            <wsdl-soap12:operation soapAction="Subscribe"/>
            <wsdl:input>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidFilterFault">
                <wsdl-soap12:fault use="encoded" name="InvalidFilterFault"/>
            </wsdl:fault>
            <wsdl:fault name="TopicExpressionDialectUnknownFault">
                <wsdl-soap12:fault use="encoded" name="TopicExpressionDialectUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidTopicExpressionFault">
                <wsdl-soap12:fault use="encoded" name="InvalidTopicExpressionFault"/>
            </wsdl:fault>
            <wsdl:fault name="TopicNotSupportedFault">
                <wsdl-soap12:fault use="encoded" name="TopicNotSupportedFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidProducerPropertiesExpressionFault">
                <wsdl-soap12:fault use="encoded" name="InvalidProducerPropertiesExpressionFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidMessageContentExpressionFault">
                <wsdl-soap12:fault use="encoded" name="InvalidMessageContentExpressionFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnacceptableInitialTerminationTimeFault">
                <wsdl-soap12:fault use="encoded" name="UnacceptableInitialTerminationTimeFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnrecognizedPolicyRequestFault">
                <wsdl-soap12:fault use="encoded" name="UnrecognizedPolicyRequestFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnsupportedPolicyRequestFault">
                <wsdl-soap12:fault use="encoded" name="UnsupportedPolicyRequestFault"/>
            </wsdl:fault>
            <wsdl:fault name="NotifyMessageNotSupportedFault">
                <wsdl-soap12:fault use="encoded" name="NotifyMessageNotSupportedFault"/>
            </wsdl:fault>
            <wsdl:fault name="SubscribeCreationFailedFault">
                <wsdl-soap12:fault use="encoded" name="SubscribeCreationFailedFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="GetCurrentMessage">
            <wsdl-soap12:operation soapAction="GetCurrentMessage"/>
            <wsdl:input>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="TopicExpressionDialectUnknownFault">
                <wsdl-soap12:fault use="encoded" name="TopicExpressionDialectUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidTopicExpressionFault">
                <wsdl-soap12:fault use="encoded" name="InvalidTopicExpressionFault"/>
            </wsdl:fault>
            <wsdl:fault name="TopicNotSupportedFault">
                <wsdl-soap12:fault use="encoded" name="TopicNotSupportedFault"/>
            </wsdl:fault>
            <wsdl:fault name="NoCurrentMessageOnTopicFault">
                <wsdl-soap12:fault use="encoded" name="NoCurrentMessageOnTopicFault"/>
            </wsdl:fault>
            <wsdl:fault name="MultipleTopicsSpecifiedFault">
                <wsdl-soap12:fault use="encoded" name="MultipleTopicsSpecifiedFault"/>
            </wsdl:fault>
        </wsdl:operation>

        <wsdl:operation name="RegisterPublisher">
            <wsdl-soap12:operation soapAction="RegisterPublisher"/>
            <wsdl:input>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:input>
            <wsdl:output>
                <wsdl-soap12:body use="literal"
                    encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"/>
            </wsdl:output>
            <wsdl:fault name="ResourceUnknownFault">
                <wsdl-soap12:fault use="encoded" name="ResourceUnknownFault"/>
            </wsdl:fault>
            <wsdl:fault name="InvalidTopicExpressionFault">
                <wsdl-soap12:fault use="encoded" name="InvalidTopicExpressionFault"/>
            </wsdl:fault>
            <wsdl:fault name="TopicNotSupportedFault">
                <wsdl-soap12:fault use="encoded" name="TopicNotSupportedFault"/>
            </wsdl:fault>
            <wsdl:fault name="PublisherRegistrationRejectedFault">
                <wsdl-soap12:fault use="encoded" name="PublisherRegistrationRejectedFault"/>
            </wsdl:fault>
            <wsdl:fault name="PublisherRegistrationFailedFault">
                <wsdl-soap12:fault use="encoded" name="PublisherRegistrationFailedFault"/>
            </wsdl:fault>
            <wsdl:fault name="UnacceptableInitialTerminationTimeFault">
                <wsdl-soap12:fault use="encoded" name="UnacceptableInitialTerminationTimeFault"/>
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
    <wsdl:service name="SesService">
        <wsdl:port name="SesPort" binding="ses:SesBinding">
            <wsdl-soap12:address location="http://localhost:8080/SES/services/ses"/>
        </wsdl:port>
    </wsdl:service>
</wsdl:definitions>
