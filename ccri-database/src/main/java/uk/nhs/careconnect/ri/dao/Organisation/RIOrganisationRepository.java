package uk.nhs.careconnect.ri.dao.Organisation;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationIdentifier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class RIOrganisationRepository implements OrganisationRepository {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    private OrganisationEntityToFHIROrganizationTransformer organizationEntityToFHIROrganizationTransformer;

    public void save(OrganisationEntity organization)
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.persist(organization);
    }

    public Organization read(IdType theId) {
        EntityManager em = entityManagerFactory.createEntityManager();

        OrganisationEntity organizationEntity = (OrganisationEntity) em.find(OrganisationEntity.class,Long.parseLong(theId.getIdPart()));

        return organizationEntity == null
                ? null
                : organizationEntityToFHIROrganizationTransformer.transform(organizationEntity);

    }
    public List<Organization> searchOrganization (
            @OptionalParam(name = Organization.SP_IDENTIFIER) TokenParam identifier
    )
    {
        EntityManager em = entityManagerFactory.createEntityManager();
        List<OrganisationEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<OrganisationEntity> criteria = builder.createQuery(OrganisationEntity.class);
        Root<OrganisationEntity> root = criteria.from(OrganisationEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Organization> results = new ArrayList<Organization>();

        if (identifier !=null)
        {
            Join<OrganisationEntity, OrganisationIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

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

        for (OrganisationEntity organizationEntity : qryResults)
        {
           // log.trace("HAPI Custom = "+doc.getId());
            Organization organization = organizationEntityToFHIROrganizationTransformer.transform(organizationEntity);
            results.add(organization);
        }

        return results;
    }


}