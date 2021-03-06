package uk.nhs.careconnect.ri.dao.Dstu2.Location;

import ca.uhn.fhir.rest.annotation.ConditionalUrlParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.dao.Stu3.CodeSystem.CodeSystemRepository;
import uk.nhs.careconnect.ri.dao.Stu3.CodeSystem.ConceptRepository;
import uk.nhs.careconnect.ri.dao.Dstu2.Organisation.OrganisationRepository;
import uk.nhs.careconnect.ri.entity.AddressEntity;
import uk.nhs.careconnect.ri.entity.location.LocationAddress;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.location.LocationIdentifier;
import uk.nhs.careconnect.ri.entity.location.LocationTelecom;
import uk.org.hl7.fhir.core.Dstu2.CareConnectSystem;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RILocationRepository implements LocationRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private LocationEntityToFHIRLocationTransformer locationEntityToFHIRLocationTransformer;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private ConceptRepository codeSvc;


    private static final Logger log = LoggerFactory.getLogger(RILocationRepository.class);

    public void save(LocationEntity location)
    {
        em.persist(location);
    }

    public Location read(IdType theId) {

        LocationEntity locationEntity = (LocationEntity) em.find(LocationEntity.class,Long.parseLong(theId.getIdPart()));

        return locationEntity == null
                ? null
                : locationEntityToFHIRLocationTransformer.transform(locationEntity);

    }

    @Override
    public Location create(Location location, @IdParam IdType theId, @ConditionalUrlParam String theConditional) {

        LocationEntity locationEntity = null;
        log.debug("Called Location Create Condition Url: "+theConditional);
        if (location.hasId()) {
            locationEntity =  (LocationEntity) em.find(LocationEntity.class,Long.parseLong(location.getId()));
        }
        if (theConditional != null) {
            try {

                //CareConnectSystem.ODSOrganisationCode
                if (theConditional.contains("fhir.nhs.uk/Id/ods-site-code")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<LocationEntity> results = searchLocationEntity(new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.ODSSiteCode),null);
                    for (LocationEntity org : results) {
                        locationEntity = org;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }
        if (locationEntity == null) {
            locationEntity = new LocationEntity();
        }
        locationEntity.setName(location.getName());
        // disabled for stu3  locationEntity.setStatus(location.getStatus());

        if (location.hasType() && location.getType().getCoding().size()>0) {
            locationEntity.setType(codeSvc.findCode(location.getType().getCoding().get(0).getSystem(),location.getType().getCoding().get(0).getCode()));
        }

      
        if (location.getManagingOrganization() != null) {
            log.debug("Location Org Ref="+location.getManagingOrganization().getReference());
            locationEntity.setManagingOrganisation(organisationRepository.readEntity(new IdType().setValue(location.getManagingOrganization().getReference())));
        }

        em.persist(locationEntity);

        for (Identifier ident : location.getIdentifier()) {
            log.debug("Location SDS = " + ident.getValue() + " System =" + ident.getSystem());
            LocationIdentifier locationIdentifier = null;

            for (LocationIdentifier orgSearch : locationEntity.getIdentifiers()) {
                if (ident.getSystem().equals(orgSearch.getSystemUri()) && ident.getValue().equals(orgSearch.getValue())) {
                    locationIdentifier = orgSearch;
                    break;
                }
            }
            if (locationIdentifier == null) {
                locationIdentifier = new LocationIdentifier();
                locationIdentifier.setLocation(locationEntity);

            }

            locationIdentifier.setValue(ident.getValue());
            locationIdentifier.setSystem(codeSystemSvc.findSystem(ident.getSystem()));
            log.debug("Location System Code: "+locationIdentifier.getSystemUri());

            em.persist(locationIdentifier);
        }

        for (ContactPoint telecom : location.getTelecom()) {
            LocationTelecom locationTelecom = null;

            for (LocationTelecom orgSearch : locationEntity.getTelecoms()) {
                if (telecom.getValue().equals(orgSearch.getValue())) {
                    locationTelecom = orgSearch;
                    break;
                }
            }
            if (locationTelecom == null) {
                locationTelecom = new LocationTelecom();
                locationTelecom.setLocation(locationEntity);
            }

            locationTelecom.setValue(telecom.getValue());
            locationTelecom.setSystemDstu2(telecom.getSystem());
            locationTelecom.setTelecomUseDstu2(telecom.getUse());

            em.persist(locationTelecom);
        }
        Address address =location.getAddress();

        LocationAddress locationAddress = null;


        if (locationEntity.getAddresses().size()>0) locationAddress = locationEntity.getAddresses().get(0);
        if (locationAddress == null) {
            locationAddress = new LocationAddress();

            locationAddress.setLocation(locationEntity);
        }
        AddressEntity addr = locationAddress.getAddress();
        if (addr == null) {
            addr = locationAddress.setAddress(new AddressEntity());
        }

        if (address.getLine().size()>0) addr.setAddress1(address.getLine().get(0).getValue());
        if (address.getLine().size()>1) addr.setAddress2(address.getLine().get(1).getValue());
        if (address.getLine().size()>2) addr.setAddress3(address.getLine().get(2).getValue());
        if (address.getCity() != null) addr.setCity(address.getCity());
        if (address.getDistrict() != null) addr.setCounty(address.getDistrict());
        if (address.getPostalCode() != null) addr.setPostcode(address.getPostalCode());
        if (address.getUse() != null) locationAddress.setAddressUseDstu2(address.getUse());
        em.persist(addr);
        em.persist(locationAddress);



       // log.info("Called PERSIST id="+locationEntity.getId().toString());
        location.setId(locationEntity.getId().toString());

        return location;
    }


       public List<Location> searchLocation (

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    )
    {
        List<LocationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<LocationEntity> criteria = builder.createQuery(LocationEntity.class);
        Root<LocationEntity> root = criteria.from(LocationEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Location> results = new ArrayList<Location>();

        if (identifier !=null)
        {
            Join<LocationEntity, LocationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%"+name.getValue()+"%"))
                    );

            predList.add(p);
        }


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).getResultList();

        for (LocationEntity locationEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Location location = locationEntityToFHIRLocationTransformer.transform(locationEntity);
            results.add(location);
        }

        return results;
    }
    public List<LocationEntity> searchLocationEntity (

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier,
            @OptionalParam(name = Location.SP_NAME) StringParam name
    )
    {
        List<LocationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<LocationEntity> criteria = builder.createQuery(LocationEntity.class);
        Root<LocationEntity> root = criteria.from(LocationEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Location> results = new ArrayList<Location>();

        if (identifier !=null)
        {
            Join<LocationEntity, LocationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal("%"+name.getValue()+"%"))
                    );

            predList.add(p);
        }


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).getResultList();


        return qryResults;
    }


}
