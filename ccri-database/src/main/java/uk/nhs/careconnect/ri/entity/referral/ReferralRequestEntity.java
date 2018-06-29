package uk.nhs.careconnect.ri.entity.referral;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.entity.procedure.ProcedureIdentifier;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ReferralRequest")
public class ReferralRequestEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="REFERRAL_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_PATIENT_REFERRAL"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PatientEntity patient;

    @ManyToOne
    @JoinColumn(name="SPECIALTY_CONCEPT_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_SPECIALTY_CONCEPT"))
    private ConceptEntity specialty;

    @ManyToOne
    @JoinColumn(name="REQUESTOR_ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_ORGANISATION"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private OrganisationEntity requesterOrganisation;

    @ManyToOne
    @JoinColumn(name="REQUESTOR_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_REFERRAL_REQUEST_PRACTITIONER"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private PractitionerEntity requesterPractitioner;

    @OneToMany(mappedBy="referral", targetEntity=ReferralRequestIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<ReferralRequestIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="referral", targetEntity=ReferralRequestRecipient.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<ReferralRequestRecipient> recipients = new HashSet<>();

    public Set<ReferralRequestIdentifier> getIdentifiers() {
        return identifiers;
    }

    public ReferralRequestEntity setIdentifiers(Set<ReferralRequestIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public Set<ReferralRequestRecipient> getRecipients() {
        return recipients;
    }

    public ReferralRequestEntity setRecipients(Set<ReferralRequestRecipient> recipients) {
        this.recipients = recipients;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public ReferralRequestEntity setPatient(PatientEntity patient) {
        this.patient = patient;
        return this;
    }

    public ConceptEntity getSpecialty() {
        return specialty;
    }

    public ReferralRequestEntity setSpecialty(ConceptEntity specialty) {
        this.specialty = specialty;
        return this;
    }

    public OrganisationEntity getRequesterOrganisation() {
        return requesterOrganisation;
    }

    public ReferralRequestEntity setRequesterOrganisation(OrganisationEntity requesterOrganisation) {
        this.requesterOrganisation = requesterOrganisation;
        return this;
    }

    public PractitionerEntity getRequesterPractitioner() {
        return requesterPractitioner;
    }

    public ReferralRequestEntity setRequesterPractitioner(PractitionerEntity requesterPractitioner) {
        this.requesterPractitioner = requesterPractitioner;
        return this;
    }
}
