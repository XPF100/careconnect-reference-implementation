package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.*;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.daointerface.transforms.*;
import uk.nhs.careconnect.ri.entity.Terminology.CodeSystemEntity;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.allergy.AllergyIntoleranceEntity;
import uk.nhs.careconnect.ri.entity.carePlan.*;
import uk.nhs.careconnect.ri.entity.careTeam.CareTeamEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.episode.EpisodeOfCareEntity;
import uk.nhs.careconnect.ri.entity.immunisation.ImmunisationEntity;
import uk.nhs.careconnect.ri.entity.list.ListEntity;
import uk.nhs.careconnect.ri.entity.list.ListItem;
import uk.nhs.careconnect.ri.entity.medicationRequest.MedicationRequestEntity;
import uk.nhs.careconnect.ri.entity.observation.ObservationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureEntity;
import uk.nhs.careconnect.ri.entity.questionnaireResponse.QuestionnaireResponseEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.*;

import static uk.nhs.careconnect.ri.daointerface.daoutils.MAXROWS;

@Repository
@Transactional
public class CarePlanDao implements CarePlanRepository {

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
    EpisodeOfCareRepository episodeDao;

    @Autowired
    ConditionRepository conditionDao;

    @Autowired
    ListRepository listDao;

    @Autowired
    DocumentReferenceRepository documentDao;

    @Autowired
    QuestionnaireResponseRepository formDao;

    @Autowired
    ObservationRepository observationDao;

    @Autowired
    CareTeamRepository teamDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    @Autowired
    private PatientEntityToFHIRPatientTransformer patientEntityToFHIRPatientTransformer;

    @Autowired
    private ListEntityToFHIRListResourceTransformer listEntityToFHIRListResourceTransformer;

    @Autowired
    private QuestionnaireResponseEntityToFHIRQuestionnaireResponseTransformer questionnaireResponseEntityToFHIRQuestionnaireResponseTransformer;

    @Autowired
    private PractitionerEntityToFHIRPractitionerTransformer practitionerEntityToFHIRPractitionerTransformer;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organisationEntityToFHIROrganizationTransformer;

    @Autowired
    private ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    private static final Logger log = LoggerFactory.getLogger(CarePlanDao.class);

    @Override
    public void save(FhirContext ctx,CarePlanEntity carePlan) {

    }



    @Autowired
    CarePlanEntityToFHIRCarePlanTransformer carePlanEntityToFHIRCarePlanTransformer;

    @Override
    public Long count() {

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(CarePlanEntity.class)));
        //cq.where(/*your stuff*/);
        return em.createQuery(cq).getSingleResult();
    }



    @Override
    public CarePlan read(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CarePlanEntity carePlanIntolerance = (CarePlanEntity) em.find(CarePlanEntity.class, Long.parseLong(theId.getIdPart()));

            return carePlanIntolerance == null
                    ? null
                    : carePlanEntityToFHIRCarePlanTransformer.transform(carePlanIntolerance);
        } else {
            return null;
        }
    }

    @Override
    public CarePlanEntity readEntity(FhirContext ctx,IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            CarePlanEntity carePlanIntolerance = (CarePlanEntity) em.find(CarePlanEntity.class, Long.parseLong(theId.getIdPart()));
            return carePlanIntolerance;
        }
        return null;
    }

    @Override
    public CarePlan create(FhirContext ctx, CarePlan carePlan, IdType theId, String theConditional) throws OperationOutcomeException {

        log.debug("CarePlan.save");
        //  log.info(ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(encounter));
        CarePlanEntity carePlanEntity = null;

        if (carePlan.hasId()) carePlanEntity = readEntity(ctx, carePlan.getIdElement());

        if (theConditional != null) {
            try {


                if (theConditional.contains("fhir.leedsth.nhs.uk/Id/carePlan")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<CarePlanEntity> results = searchEntity(ctx, null, null,null,new TokenParam().setValue(spiltStr[1]).setSystem("https://fhir.leedsth.nhs.uk/Id/carePlan"),null, null);
                    for (CarePlanEntity con : results) {
                        carePlanEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (carePlanEntity == null) carePlanEntity = new CarePlanEntity();
        log.debug("CarePlan.Mainsave");

        PatientEntity patientEntity = null;
        if (carePlan.hasSubject()) {
            log.trace(carePlan.getSubject().getReference());
            patientEntity = patientDao.readEntity(ctx, new IdType(carePlan.getSubject().getReference()));
            carePlanEntity.setPatient(patientEntity);
        }

        if (carePlan.hasStatus()) {
            carePlanEntity.setStatus(carePlan.getStatus());
        }
        if (carePlan.hasIntent()) {
            carePlanEntity.setIntent(carePlan.getIntent());
        }
        if (carePlan.hasPeriod()) {
            if (carePlan.getPeriod().hasStart()) {
                carePlanEntity.setPeriodStartDateTime(carePlan.getPeriod().getStart());
            }
            if (carePlan.getPeriod().hasEnd()) {
                carePlanEntity.setPeriodEndDateTime(carePlan.getPeriod().getEnd());
            }
        }
        if (carePlan.hasContext()) {
            if (carePlan.getContext().getReference().contains("Encounter")) {
                EncounterEntity encounterEntity = encounterDao.readEntity(ctx, new IdType(carePlan.getContext().getReference()));
                carePlanEntity.setContextEncounter(encounterEntity);
            } else if (carePlan.getContext().getReference().contains("EpisodeOfCare")) {
                EpisodeOfCareEntity episodeEntity = episodeDao.readEntity(ctx, new IdType(carePlan.getContext().getReference()));
                carePlanEntity.setContextEpisodeOfCare(episodeEntity);
            }
        }

        em.persist(carePlanEntity);

        log.debug("CarePlan.saveAddresses");
        for (Reference reference : carePlan.getAddresses()) {
            log.info("Address Reference = "+reference.getReference());
            ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(reference.getReference()));
            CarePlanCondition carePlanCondition = null;
            for (CarePlanCondition conditionSearch : carePlanEntity.getAddresses()) {
                if (conditionSearch.getCondition().getCode().getCode().equals(conditionEntity.getCode())) {
                    carePlanCondition = conditionSearch;
                    break;
                }
            }
            if (conditionEntity != null && carePlanCondition == null) {
                carePlanCondition = new CarePlanCondition();
                carePlanCondition.setCondition(conditionEntity);
                carePlanCondition.setCarePlan(carePlanEntity);
                em.persist(carePlanCondition);
            }
        }
        log.debug("CarePlan.saveCategory");
        for (CodeableConcept conceptCategory : carePlan.getCategory()) {
            ConceptEntity concept = conceptDao.findAddCode(conceptCategory.getCodingFirstRep());

            CarePlanCategory carePlanCategory = null;
            for (CarePlanCategory categorySearch : carePlanEntity.getCategories()) {
                if (concept.getCode().equals(categorySearch.getCategory().getCode())) {
                    carePlanCategory = categorySearch;
                    break;
                }
            }
            if (carePlanCategory == null) {
                carePlanCategory = new CarePlanCategory();
                carePlanCategory.setCategory(concept);
                carePlanCategory.setCarePlan(carePlanEntity);
                em.persist(carePlanCategory);
            }
        }

        log.debug("CarePlan.saveIdentifier");
        for (Identifier identifier : carePlan.getIdentifier()) {
            CarePlanIdentifier carePlanIdentifier = null;

            for (CarePlanIdentifier orgSearch : carePlanEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    carePlanIdentifier = orgSearch;
                    break;
                }
            }
            if (carePlanIdentifier == null)  carePlanIdentifier = new CarePlanIdentifier();

            carePlanIdentifier.setValue(identifier.getValue());
            carePlanIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            carePlanIdentifier.setCarePlan(carePlanEntity);
            em.persist(carePlanIdentifier);
        }

        log.debug("CarePlan.saveTeams");
        for (CarePlanTeam team : carePlanEntity.getTeams()) {
            em.remove(team);
        }
        for (Reference reference : carePlan.getCareTeam()) {
            CarePlanTeam carePlanTeam = new CarePlanTeam();
            carePlanTeam.setCarePlan(carePlanEntity);
            if (reference.getReference().contains("CareTeam")) {
                CareTeamEntity careTeamEntity = teamDao.readEntity(ctx, new IdType(reference.getReference()));
                if (careTeamEntity != null) {
                    carePlanTeam.setTeam(careTeamEntity);
                    em.persist(carePlanTeam);
                }
            }

        }

        log.debug("CarePlan.saveActivity");
        for (CarePlan.CarePlanActivityComponent component : carePlan.getActivity()) {
            CarePlanActivity activity= null;
            CarePlanActivityDetail detail = null;
            if (component.hasDetail()) {
                for (CarePlanActivity searchActivity : carePlanEntity.getActivities()) {
                    for (CarePlanActivityDetail searchDetail : searchActivity.getDetails()) {
                        if (searchDetail.getCode().getCode().equals(component.getDetail().getCode())) {
                            activity= searchActivity;
                            detail= searchDetail;
                            break;
                        }

                    }
                }
            }
            if (activity == null ) {
                activity =  new CarePlanActivity();
                activity.setCarePlan(carePlanEntity);
                em.persist(activity);
            }
            if (component.hasDetail()) {
                if (detail == null) {
                    detail = new CarePlanActivityDetail();
                    detail.setCarePlanActivity(activity);
                }
                if (component.getDetail().hasStatus()) {
                    detail.setStatus(component.getDetail().getStatus());
                }
                if (component.getDetail().hasCode()) {
                    log.debug("CarePlan Detail "+component.getDetail().getCode().getCodingFirstRep().getCode());
                    ConceptEntity concept = conceptDao.findAddCode(component.getDetail().getCode().getCodingFirstRep());
                    if (concept != null) {
                        detail.setCode(concept);
                        em.persist(detail);
                    }
                }

            }
        }
        log.debug("CarePlan.saveAuthor");
        for (CarePlanAuthor author : carePlanEntity.getAuthors()) {
            em.remove(author);
        }
        for (Reference reference : carePlan.getAuthor()) {
            CarePlanAuthor author = new CarePlanAuthor();
            author.setCarePlan(carePlanEntity);
            if (reference.getReference().contains("Practitioner")) {
                PractitionerEntity practitionerEntity = practitionerDao.readEntity(ctx, new IdType(reference.getReference()));
                author.setPractitioner(practitionerEntity);
            }
            if (reference.getReference().contains("Organization")) {
                OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(reference.getReference()));
                author.setOrganisation(organisationEntity);
            }

            em.persist(author);
        }

        for (Reference reference : carePlan.getSupportingInfo()) {
              CarePlanSupportingInformation carePlanSupportingInformation = new CarePlanSupportingInformation();
              carePlanSupportingInformation.setCarePlan(carePlanEntity);
              buildItem(ctx,reference,carePlanSupportingInformation);
        }

        return carePlanEntityToFHIRCarePlanTransformer.transform(carePlanEntity);
    }


    private void buildItem(FhirContext ctx,Reference item, CarePlanSupportingInformation itemEntity ) {

        if (item.getReference().contains("Condition")) {

            ConditionEntity conditionEntity = conditionDao.readEntity(ctx, new IdType(item.getReference()));
            itemEntity.setCondition(conditionEntity);

        } else if (item.getReference().contains("Observation")) {

            ObservationEntity observationEntity = observationDao.readEntity(ctx, new IdType(item.getReference()));
            itemEntity.setObservation(observationEntity);
        } else if (item.getReference().contains("QuestionnaireResponse")) {

            QuestionnaireResponseEntity
                    questionnaireEntity = formDao.readEntity(ctx, new IdType(item.getReference()));
            itemEntity.setForm(questionnaireEntity);

        } else if (item.getReference().contains("List")) {

            ListEntity listEntity = listDao.readEntity(ctx, new IdType(item.getReference()));
            itemEntity.setListResource(listEntity);

        } else if (item.getReference().contains("DocumentReference")) {

            DocumentReferenceEntity documentReferenceEntity = documentDao.readEntity(ctx, new IdType(item.getReference()));
            itemEntity.setDocumentReference(documentReferenceEntity);

        }
        em.persist(itemEntity);

    }

    @Override
    public List<Resource> search(FhirContext ctx,
                                 @OptionalParam(name = CarePlan.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CarePlan.SP_DATE) DateRangeParam date
            , @OptionalParam(name = CarePlan.SP_CATEGORY) TokenOrListParam categories
            , @OptionalParam(name = CarePlan.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CarePlan.SP_RES_ID) TokenParam resid
            , @IncludeParam(allow= {
            "CarePlan:subject"
            ,"CarePlan:supportingInformation"
            , "*"}) Set<Include> includes) {
        List<CarePlanEntity> qryResults = searchEntity(ctx,patient, date,categories, identifier,resid, includes);
        List<Resource> results = new ArrayList<>();

        for (CarePlanEntity carePlanIntoleranceEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            CarePlan carePlanIntolerance = carePlanEntityToFHIRCarePlanTransformer.transform(carePlanIntoleranceEntity);
            results.add(carePlanIntolerance);
        }



        if (includes!=null) {
            log.info("Reverse includes");
            for (CarePlanEntity carePlanEntity : qryResults) {
                if (includes !=null) {
                    for (Include include : includes) {
                        switch(include.getValue()) {
                            case "CarePlan:subject":
                                PatientEntity patientEntity = carePlanEntity.getPatient();
                                if (patientEntity !=null) results.add(patientEntityToFHIRPatientTransformer.transform(patientEntity));
                                break;
                            case "CarePlan:supportingInformation":
                                for (CarePlanSupportingInformation carePlanSupportingInformation : carePlanEntity.getSupportingInformation()) {
                                    Resource resource = getResource(carePlanSupportingInformation,results);
                                    if (resource != null)
                                        results.add(resource);
                                }
                                break;
                            case "*":
                                PatientEntity patientEntity2 = carePlanEntity.getPatient();
                                if (patientEntity2 !=null) results.add(patientEntityToFHIRPatientTransformer.transform(patientEntity2));

                                for (CarePlanSupportingInformation carePlanSupportingInformation : carePlanEntity.getSupportingInformation()) {
                                    Resource resource = getResource(carePlanSupportingInformation,results);
                                    if (resource != null)
                                        results.add(resource);
                                }
                                for (CarePlanAuthor carePlanAuthor : carePlanEntity.getAuthors()) {
                                    if (carePlanAuthor.getPractitioner() != null)
                                        results.add(practitionerEntityToFHIRPractitionerTransformer.transform(carePlanAuthor.getPractitioner()));
                                    if (carePlanAuthor.getOrganisation() != null)
                                        results.add(organisationEntityToFHIROrganizationTransformer.transform(carePlanAuthor.getOrganisation()));
                                }
                                break;
                        }
                    }
                }

            }
        }

        return results;
    }

    private Resource getResource(CarePlanSupportingInformation carePlanSupportingInformation, List<Resource> results) {
        if (carePlanSupportingInformation.getListResource() != null) {
            ListEntity list = carePlanSupportingInformation.getListResource();
            if (list != null) {
                for (ListItem items : list.getItems()) {
                    if (items.getCondition() != null) {
                        results.add(conditionEntityToFHIRConditionTransformer.transform(items.getCondition()));
                    }
                }
            }
            return listEntityToFHIRListResourceTransformer.transform(carePlanSupportingInformation.getListResource());
        }
        if (carePlanSupportingInformation.getForm() != null) {
            return questionnaireResponseEntityToFHIRQuestionnaireResponseTransformer.transform(carePlanSupportingInformation.getForm());
        }
        return null;
    }

    @Override
    public List<CarePlanEntity> searchEntity(FhirContext ctx,
                                             @OptionalParam(name = CarePlan.SP_PATIENT) ReferenceParam patient
            , @OptionalParam(name = CarePlan.SP_DATE) DateRangeParam date
            , @OptionalParam(name = CarePlan.SP_CATEGORY) TokenOrListParam categories
            , @OptionalParam(name = CarePlan.SP_IDENTIFIER) TokenParam identifier
            , @OptionalParam(name = CarePlan.SP_RES_ID) TokenParam resid
            , @IncludeParam(allow= {
            "CarePlan:subject"
            ,"CarePlan:supportingInformation"
            , "*"}) Set<Include> includes
                                             ) {


        List<CarePlanEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<CarePlanEntity> criteria = builder.createQuery(CarePlanEntity.class);
        Root<CarePlanEntity> root = criteria.from(CarePlanEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<CarePlan> results = new ArrayList<CarePlan>();

        if (patient != null) {
            // KGM 4/1/2018 only search on patient id
            if (daoutils.isNumeric(patient.getIdPart())) {
                Join<CarePlanEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

                Predicate p = builder.equal(join.get("id"), patient.getIdPart());
                predList.add(p);
            } else {
                Join<CarePlanEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

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
            Join<CarePlanEntity, CarePlanIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }

        if (categories!=null) {
            List<Predicate> predOrList = new LinkedList<Predicate>();
            Join<CarePlanEntity, CarePlanCategory> joinCategory = root.join("categories", JoinType.LEFT);
            Join<CarePlanCategory, ConceptEntity> joinConcept = joinCategory.join("category", JoinType.LEFT);
            Join<ConceptEntity, CodeSystemEntity> joinCodeSystem = joinConcept.join("codeSystemEntity", JoinType.LEFT);

            for (TokenParam code : categories.getValuesAsQueryTokens()) {
                log.trace("Search on CarePlan.category code = " + code.getValue());

                Predicate p = null;
                if (code.getSystem() != null) {
                    p = builder.and(builder.equal(joinCodeSystem.get("codeSystemUri"), code.getSystem()),builder.equal(joinConcept.get("code"), code.getValue()));
                } else {
                    p = builder.equal(joinConcept.get("code"), code.getValue());
                }
                predOrList.add(p);

            }
            if (predOrList.size()>0) {
                Predicate p = builder.or(predOrList.toArray(new Predicate[0]));
                predList.add(p);
            }
        }

        ParameterExpression<Date> parameterLower = builder.parameter(Date.class);
        ParameterExpression<Date> parameterUpper = builder.parameter(Date.class);

        if (date !=null)
        {


            if (date.getLowerBoundAsInstant() != null) log.debug("getLowerBoundAsInstant()="+date.getLowerBoundAsInstant().toString());
            if (date.getUpperBoundAsInstant() != null) log.debug("getUpperBoundAsInstant()="+date.getUpperBoundAsInstant().toString());


            if (date.getLowerBound() != null) {

                DateParam dateParam = date.getLowerBound();
                log.debug("Lower Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {

                    case GREATERTHAN:
                    case GREATERTHAN_OR_EQUALS: {
                        Predicate p = builder.greaterThanOrEqualTo(root.<Date>get("periodStartDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case APPROXIMATE:
                    case EQUAL: {

                        Predicate plow = builder.greaterThanOrEqualTo(root.<Date>get("periodStartDateTime"), parameterLower);
                        predList.add(plow);
                        break;
                    }
                    case NOT_EQUAL: {
                        Predicate p = builder.notEqual(root.<Date>get("periodStartDateTime"), parameterLower);
                        predList.add(p);
                        break;
                    }
                    case STARTS_AFTER: {
                        Predicate p = builder.greaterThan(root.<Date>get("periodStartDateTime"), parameterLower);
                        predList.add(p);
                        break;

                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }

            if (date.getUpperBound() != null) {

                DateParam dateParam = date.getUpperBound();

                log.debug("Upper Param - " + dateParam.getValue() + " Prefix - " + dateParam.getPrefix());

                switch (dateParam.getPrefix()) {
                    case APPROXIMATE:
                    case EQUAL: {
                        Predicate pupper = builder.lessThan(root.<Date>get("periodStartDateTime"), parameterUpper);
                        predList.add(pupper);
                        break;
                    }

                    case LESSTHAN_OR_EQUALS: {
                        Predicate p = builder.lessThanOrEqualTo(root.<Date>get("periodStartDateTime"), parameterUpper);
                        predList.add(p);
                        break;
                    }
                    case ENDS_BEFORE:
                    case LESSTHAN: {
                        Predicate p = builder.lessThan(root.<Date>get("periodStartDateTime"), parameterUpper);
                        predList.add(p);

                        break;
                    }
                    default:
                        log.trace("DEFAULT DATE(0) Prefix = " + date.getValuesAsQueryTokens().get(0).getPrefix());
                }
            }

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
        criteria.orderBy(builder.desc(root.get("periodStartDateTime")));

        TypedQuery<CarePlanEntity> typedQuery = em.createQuery(criteria).setMaxResults(MAXROWS);

        if (date != null) {
            if (date.getLowerBound() != null)
                typedQuery.setParameter(parameterLower, date.getLowerBoundAsInstant(), TemporalType.TIMESTAMP);
            if (date.getUpperBound() != null)
                typedQuery.setParameter(parameterUpper, date.getUpperBoundAsInstant(), TemporalType.TIMESTAMP);
        }
        qryResults = typedQuery.getResultList();

        return qryResults;
    }
}
