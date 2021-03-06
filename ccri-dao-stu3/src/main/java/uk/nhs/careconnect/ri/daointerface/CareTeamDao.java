package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.CareTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.transforms.CareTeamEntityToFHIRCareTeamTransformer;
import uk.nhs.careconnect.ri.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.entity.careTeam.CareTeamIdentifier;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static uk.nhs.careconnect.ri.daointerface.daoutils.MAXROWS;

@Repository
@Transactional
public class CareTeamDao implements CareTeamRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    PractitionerRepository practitionerDao;

    @Autowired
    EncounterRepository encounterDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;
    
    @Autowired
    CareTeamEntityToFHIRCareTeamTransformer teamEntityToFHIRCareTeamTransformer;

    private static final Logger log = LoggerFactory.getLogger(CareTeamDao.class);

    @Override
    public void save(FhirContext ctx, CareTeamEntity team) throws OperationOutcomeException {

    }

    @Override
    public CareTeam read(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CareTeamEntity team = (CareTeamEntity) em.find(CareTeamEntity.class, Long.parseLong(theId.getIdPart()));
            return teamEntityToFHIRCareTeamTransformer.transform(team);
        }
        return null;
    }

    @Override
    public CareTeam create(FhirContext ctx, CareTeam team, IdType theId, String theConditional) throws OperationOutcomeException {
        log.debug("CareTeam.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        CareTeamEntity teamEntity = null;

        if (team.hasId()) teamEntity = readEntity(ctx, team.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/team")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<CareTeamEntity> results = searchEntity(ctx, null, new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/team"),null);
                    for (CareTeamEntity con : results) {
                        teamEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (teamEntity == null) teamEntity = new CareTeamEntity();


        PatientEntity patientEntity = null;
        if (team.hasSubject()) {
            log.trace(team.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(team.getSubject().getReference()));
            teamEntity.setPatient(patientEntity);
        }

        if (team.hasStatus()) {
            teamEntity.setStatus(team.getStatus());
        }

        if (team.hasPeriod()) {
            if (team.getPeriod().hasStart()) {
                teamEntity.setPeriodStartDateTime(team.getPeriod().getStart());
            }
            if (team.getPeriod().hasEnd()) {
                teamEntity.setPeriodEndDateTime(team.getPeriod().getEnd());
            }
        }
        if (team.hasContext()) {
            if (team.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(team.getContext().getReference()));
                teamEntity.setContextEncounter(encounterEntity);
            }
        }

        em.persist(teamEntity);



        for (Identifier identifier : team.getIdentifier()) {
            CareTeamIdentifier teamIdentifier = null;

            for (CareTeamIdentifier orgSearch : teamEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    teamIdentifier = orgSearch;
                    break;
                }
            }
            if (teamIdentifier == null)  teamIdentifier = new CareTeamIdentifier();

            teamIdentifier.setValue(identifier.getValue());
            teamIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            teamIdentifier.setCareTeam(teamEntity);
            em.persist(teamIdentifier);
        }




        return teamEntityToFHIRCareTeamTransformer.transform(teamEntity);
    }

    @Override
    public CareTeamEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CareTeamEntity teamIntolerance = (CareTeamEntity) em.find(CareTeamEntity.class, Long.parseLong(theId.getIdPart()));
            return teamIntolerance;
        }
        return null;
    }

    @Override
    public List<CareTeam> search(FhirContext ctx, ReferenceParam patient, TokenParam identifier, TokenParam id) {
        List<CareTeamEntity> qryResults = searchEntity(ctx,patient, identifier,id);
        List<CareTeam> results = new ArrayList<>();

        for (CareTeamEntity teamIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            CareTeam team = teamEntityToFHIRCareTeamTransformer.transform(teamIntoleranceEntity);
            results.add(team);
        }

        return results;
    }

    @Override
    public List<CareTeamEntity> searchEntity(FhirContext ctx, ReferenceParam patient, TokenParam identifier, TokenParam resid) {
        List<CareTeamEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<CareTeamEntity> criteria = builder.createQuery(CareTeamEntity.class);
        Root<CareTeamEntity> root = criteria.from(CareTeamEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<CareTeam> results = new ArrayList<CareTeam>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<CareTeamEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<CareTeamEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), -1);
                predList.add(p);
            }
        }
        if (resid != null) {
            Predicate p = builder.equal(root.get("id"),resid.getValue());
            predList.add(p);
        }
        if (identifier !=null)
        {
            Join<CareTeamEntity, CareTeamIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        
        ParameterExpression<Date> parameterLower = builder.parameter(Date.class);
        ParameterExpression<Date> parameterUpper = builder.parameter(Date.class);

        

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
        criteria.orderBy(builder.desc(root.get("periodStartDateTime")));

        TypedQuery<CareTeamEntity> typedQuery = em.createQuery(criteria).setMaxResults(MAXROWS);
        
        qryResults = typedQuery.getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(CareTeamEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }
}
