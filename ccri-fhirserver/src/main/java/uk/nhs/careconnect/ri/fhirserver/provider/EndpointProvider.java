package uk.nhs.careconnect.ri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Endpoint;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.EndpointRepository;
import uk.nhs.careconnect.ri.lib.OperationOutcomeFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class EndpointProvider implements ICCResourceProvider {


    @Autowired
    private EndpointRepository endpointDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Endpoint.class;
    }

    @Autowired
    FhirContext ctx;

    private static final Logger log = LoggerFactory.getLogger(PatientProvider.class);

    @Override
    public Long count() {
        return endpointDao.count();
    }

    @Update
    public MethodOutcome updateEndpoint(HttpServletRequest theRequest, @ResourceParam Endpoint endpoint, @IdParam IdType theId, @ConditionalUrlParam String theConditional, RequestDetails theRequestDetails) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);

    try {
        Endpoint newEndpoint = endpointDao.create(ctx, endpoint, theId, theConditional);
        method.setId(newEndpoint.getIdElement());
        method.setResource(newEndpoint);

    } catch (Exception ex) {

        if (ex instanceof OperationOutcomeException) {
            OperationOutcomeException outcomeException = (OperationOutcomeException) ex;
            method.setOperationOutcome(outcomeException.getOutcome());
            method.setCreated(false);
        } else {
            log.error(ex.getMessage());
            method.setCreated(false);
            method.setOperationOutcome(OperationOutcomeFactory.createOperationOutcome(ex.getMessage()));
        }
    }


        return method;
    }

    @Create
    public MethodOutcome createEndpoint(HttpServletRequest theRequest, @ResourceParam Endpoint endpoint) {


        MethodOutcome method = new MethodOutcome();
        method.setCreated(true);
        OperationOutcome opOutcome = new OperationOutcome();

        method.setOperationOutcome(opOutcome);
        try {
            Endpoint newEndpoint = endpointDao.create(ctx, endpoint,null,null);
            method.setId(newEndpoint.getIdElement());
            method.setResource(newEndpoint);
        } catch (Exception ex) {

            if (ex instanceof OperationOutcomeException) {
                OperationOutcomeException outcomeException = (OperationOutcomeException) ex;
                method.setOperationOutcome(outcomeException.getOutcome());
                method.setCreated(false);
            } else {
                log.error(ex.getClass().getCanonicalName());
                log.error(ex.getMessage());

                method.setCreated(false);
                method.setOperationOutcome(OperationOutcomeFactory.createOperationOutcome(ex.getMessage()));
            }
        }

        return method;
    }

    @Search
    public List<Endpoint> searchEndpoint(HttpServletRequest theRequest,
                                         @OptionalParam(name = Endpoint.SP_IDENTIFIER) TokenParam identifierCode,
                                         @OptionalParam(name = Endpoint.SP_RES_ID) TokenParam resid
    ) {
        return endpointDao.searchEndpoint(ctx, identifierCode, resid);
    }

    @Read()
    public Endpoint getEndpoint(@IdParam IdType endpointId) {

        Endpoint endpoint = endpointDao.read(ctx,endpointId);

        if ( endpoint == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No Endpoint/ " + endpointId.getIdPart()),
                    OperationOutcome.IssueSeverity.ERROR, OperationOutcome.IssueType.NOTFOUND);
        }

        return endpoint;
    }


}
