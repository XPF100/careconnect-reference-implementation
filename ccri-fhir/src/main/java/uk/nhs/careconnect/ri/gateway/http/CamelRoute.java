package uk.nhs.careconnect.ri.gateway.http;


import ca.uhn.fhir.context.FhirContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.gatewaylib.camel.processor.BinaryResource;
import uk.nhs.careconnect.ri.gatewaylib.camel.processor.CompositionDocumentBundle;
import uk.nhs.careconnect.ri.gatewaylib.camel.interceptor.GatewayPostProcessor;
import uk.nhs.careconnect.ri.gatewaylib.camel.interceptor.GatewayPreProcessor;
import uk.nhs.careconnect.ri.gatewaylib.camel.processor.BundleMessage;

import java.io.InputStream;

@Component
public class CamelRoute extends RouteBuilder {

	@Autowired
	protected Environment env;

	@Value("${fhir.restserver.serverBase}")
	private String eprBase;

	@Value("${fhir.restserver.edmsBase}")
	private String edmsBase;

	@Value("${fhir.restserver.tieBase}")
	private String tieBase;

	@Value("${fhir.resource.serverBase}")
    private String hapiBase;
	
    @Override
    public void configure() 
    {

		GatewayPreProcessor camelProcessor = new GatewayPreProcessor();

		GatewayPostProcessor camelPostProcessor = new GatewayPostProcessor();

		FhirContext ctx = FhirContext.forDstu3();
		BundleMessage bundleMessage = new BundleMessage(ctx);
        CompositionDocumentBundle compositionDocumentBundle = new CompositionDocumentBundle(ctx, hapiBase);
        //DocumentReferenceDocumentBundle documentReferenceDocumentBundle = new DocumentReferenceDocumentBundle(ctx,hapiBase);
        BinaryResource binaryResource = new BinaryResource(ctx, hapiBase);

		// Validation Service

		from("direct:FHIRValidate")
				.routeId("FHIR Validation")
				.process(camelProcessor) // Add in correlation Id if not present
				.to("direct:TIEServer");


		// Complex processing

		from("direct:FHIRBundleCollection")
				.routeId("Bundle Collection Queue")
				.process(camelProcessor) // Add in correlation Id if not present
				.wireTap("seda:FHIRBundleCollection");

		// This bundle goes to the EDMS Server. See also Binary
		from("direct:FHIRBundleDocumentCreate")
				.routeId("Bundle Document")
				.process(camelProcessor) // Add in correlation Id if not present
				.enrich("direct:FHIRBundleMessage") // Send a copy to EPR for main CCRI load
				.enrich("direct:EDMSServer", compositionDocumentBundle);

		from("direct:FHIRBundleDocumentUpdate")
				.routeId("Bundle Document Update")
				.process(camelProcessor) // Add in correlation Id if not present
				.enrich("direct:EDMSServer", compositionDocumentBundle);


		from("seda:FHIRBundleCollection")
				.routeId("Bundle Processing")
				.to("direct:FHIRDocumentReferenceBundle") //, documentReferenceDocumentBundle)
				.process(bundleMessage);

		from("seda:FHIRBundleMessageQueue")
				.routeId("Bundle Message Processing Queue")
				.process(bundleMessage); // Goes direct to EPR FHIR Server

		from("direct:FHIRBundleMessage")
				.routeId("Bundle Message Processing")
				.process(bundleMessage); // Goes direct to EPR FHIR Server

		from("direct:FHIRDocumentReferenceBundle")
				.routeId("Bundle Process Binary")
				.process(binaryResource);

	// Integration Server (TIE)

		from("direct:FHIREncounterDocument")
				.routeId("TIE Encounter")
				.to("direct:TIEServer");

		from("direct:FHIRCarePlanDocument")
				.routeId("TIE CarePlan")
				.to("direct:TIEServer");

		from("direct:FHIRPatientOperation")
				.routeId("TIE PatientOperation")
				.to("direct:TIEServer");

		from("direct:TIEServer")
				.routeId("TIE FHIR Server")
				.process(camelProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
				.to(tieBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

		// EPR Server

		// Simple processing - low level resource operations
		from("direct:FHIRPatient")
			.routeId("Gateway Patient")
				.to("direct:HAPIServer");

		from("direct:FHIRPractitioner")
				.routeId("Gateway Practitioner")
				.to("direct:HAPIServer");

        from("direct:FHIRPractitionerRole")
                .routeId("Gateway PractitionerRole")
                .to("direct:HAPIServer");

        from("direct:FHIROrganisation")
                .routeId("Gateway Organisation")
                .to("direct:HAPIServer");

        from("direct:FHIRLocation")
                .routeId("Gateway Location")
                .to("direct:HAPIServer");

		from("direct:FHIRObservation")
			.routeId("Gateway Observation")
			.to("direct:HAPIServer");

		from("direct:FHIREncounter")
				.routeId("Gateway Encounter")
				.to("direct:HAPIServer");

		from("direct:FHIRCondition")
				.routeId("Gateway Condition")
				.to("direct:HAPIServer");

		from("direct:FHIRProcedure")
				.routeId("Gateway Procedure")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationRequest")
				.routeId("Gateway MedicationRequest")
				.to("direct:HAPIServer");

		from("direct:FHIRMedication")
				.routeId("Gateway Medication")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationStatement")
				.routeId("Gateway MedicationStatement")
				.to("direct:HAPIServer");

		from("direct:FHIRImmunization")
				.routeId("Gateway Immunization")
				.to("direct:HAPIServer");

		from("direct:FHIREpisodeOfCare")
				.routeId("Gateway EpisodeOfCare")
				.to("direct:HAPIServer");

		from("direct:FHIRAllergyIntolerance")
				.routeId("Gateway AllergyIntolerance")
				.to("direct:HAPIServer");

		from("direct:FHIRCapabilityStatement")
				.routeId("Gateway CapabilityStatement")
				.to("direct:HAPIServer");


		from("direct:FHIRComposition")
				.routeId("Gateway Composition")
				.to("direct:HAPIServer");

		from("direct:FHIRDiagnosticReport")
				.routeId("Gateway DiagnosticReport")
				.to("direct:HAPIServer");

		from("direct:FHIRCarePlan")
				.routeId("Gateway CarePlan")
				.to("direct:HAPIServer");

		from("direct:FHIRDocumentReference")
				.routeId("Gateway DocumentReference")
				.to("direct:HAPIServer");

		from("direct:FHIRBinary")
			.routeId("Gateway Binary")
			.to("direct:EDMSServer");

		from("direct:FHIRReferralRequest")
				.routeId("Gateway ReferralRequest")
				.to("direct:HAPIServer");

		from("direct:FHIRHealthcareService")
				.routeId("Gateway HealthcareService")
				.to("direct:HAPIServer");

		from("direct:FHIREndpoint")
				.routeId("Gateway Endpoint")
				.to("direct:HAPIServer");

		from("direct:FHIRQuestionnaire")
				.routeId("Gateway Questionnaire")
				.to("direct:HAPIServer");

		from("direct:FHIRQuestionnaireResponse")
				.routeId("Gateway QuestionnaireResponse")
				.to("direct:HAPIServer");

		from("direct:FHIRList")
				.routeId("Gateway List")
				.to("direct:HAPIServer");

		from("direct:FHIRRelatedPerson")
				.routeId("Gateway RelatedPerson")
				.to("direct:HAPIServer");

		from("direct:FHIRCareTeam")
				.routeId("Gateway CareTeam")
				.to("direct:HAPIServer");

		from("direct:FHIRMedicationDispense")
				.routeId("Gateway MedicationDispense")
				.to("direct:HAPIServer");

		from("direct:EDMSServer")
				.routeId("EDMS FHIR Server")
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
				.to(edmsBase)
				.process(camelPostProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

		from("direct:HAPIServer")
            .routeId("EPR FHIR Server")
				.process(camelProcessor)
				.to("log:uk.nhs.careconnect.FHIRGateway.start?level=INFO&showHeaders=true&showExchangeId=true")
                .to(eprBase)
				.process(camelPostProcessor)
                .to("log:uk.nhs.careconnect.FHIRGateway.complete?level=INFO&showHeaders=true&showExchangeId=true")
				.convertBodyTo(InputStream.class);

    }
}
