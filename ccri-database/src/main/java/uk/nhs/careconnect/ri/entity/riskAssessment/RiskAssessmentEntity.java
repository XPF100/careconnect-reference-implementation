package uk.nhs.careconnect.ri.entity.riskAssessment;

import org.hl7.fhir.dstu3.model.RiskAssessment;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.encounter.EncounterEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "RiskAssessment",
        indexes = {

        })
public class RiskAssessmentEntity extends BaseResource {

    private static final int MAX_DESC_LENGTH = 4096;

    public enum RiskAssessmentType  { component, valueQuantity }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="RISK_ID")
    private Long id;

    @OneToMany(mappedBy="risk", targetEntity=RiskAssessmentIdentifier.class)
    private Set<RiskAssessmentIdentifier> identifiers = new HashSet<>();

    @Enumerated(EnumType.ORDINAL)
    @Column(name="status")
    private RiskAssessment.RiskAssessmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CODE_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_RISK_CODE_CONCEPT_ID"))
    private ConceptEntity riskCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="METHOD_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_RISK_METHOD_CONCEPT_ID"))
    private ConceptEntity method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PATIENT_ID",foreignKey= @ForeignKey(name="FK_RISK_PATIENT_ID"))
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ENCOUNTER_ID",foreignKey= @ForeignKey(name="FK_RISK_ENCOUNTER_ID"))
    private EncounterEntity contextEncounter;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OCCURRENCE_START_DATETIME")
    private Date occurrenceStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OCCURRENCE_END_DATETIME")
    private Date occurrenceEndDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="START_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_RISK_START_CONCEPT_ID"))
    private ConceptEntity startConcept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "CONDITION_ID",foreignKey= @ForeignKey(name="FK_RISK_CONDITION_ID"))
    private ConditionEntity condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PERFORMER_PRACTITIONER_ID",foreignKey= @ForeignKey(name="FK_RISK_PERFORMER_PRACTITIONERT_ID"))
    private PractitionerEntity performedPractitioner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="REASON_CONCEPT_ID",nullable = true,foreignKey= @ForeignKey(name="FK_RISK_REASON_CONCEPT_ID"))
    private ConceptEntity reasonConcept;

    public Long getId() {
        return id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<RiskAssessmentIdentifier> getIdentifiers() {
        if (identifiers == null) { identifiers = new HashSet<RiskAssessmentIdentifier>(); }
        return identifiers;
    }

    public RiskAssessment.RiskAssessmentStatus getStatus() {
        return status;
    }

    public RiskAssessmentEntity setIdentifiers(Set<RiskAssessmentIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public RiskAssessmentEntity setStatus(RiskAssessment.RiskAssessmentStatus status) {
        this.status = status;
        return this;
    }

    public static int getMaxDescLength() {
        return MAX_DESC_LENGTH;
    }

    public ConceptEntity getStartConcept() {
        return startConcept;
    }

    public void setStartConcept(ConceptEntity startConcept) {
        this.startConcept = startConcept;
    }

    public ConceptEntity getRiskCode() {
        return riskCode;
    }

    public void setRiskCode(ConceptEntity riskCode) {
        this.riskCode = riskCode;
    }

    public ConceptEntity getMethod() {
        return method;
    }

    public void setMethod(ConceptEntity method) {
        this.method = method;
    }

    public EncounterEntity getContextEncounter() {
        return contextEncounter;
    }

    public void setContextEncounter(EncounterEntity contextEncounter) {
        this.contextEncounter = contextEncounter;
    }



    public ConditionEntity getCondition() {
        return condition;
    }

    public void setCondition(ConditionEntity condition) {
        this.condition = condition;
    }

    public PractitionerEntity getPerformedPractitioner() {
        return performedPractitioner;
    }

    public void setPerformedPractitioner(PractitionerEntity performedPractitioner) {
        this.performedPractitioner = performedPractitioner;
    }

    public ConceptEntity getReasonConcept() {
        return reasonConcept;
    }

    public void setReasonConcept(ConceptEntity reasonConcept) {
        this.reasonConcept = reasonConcept;
    }

    public Date getOccurrenceStartDateTime() {
        return occurrenceStartDateTime;
    }

    public void setOccurrenceStartDateTime(Date occurrenceStartDateTime) {
        this.occurrenceStartDateTime = occurrenceStartDateTime;
    }

    public Date getOccurrenceEndDateTime() {
        return occurrenceEndDateTime;
    }

    public void setOccurrenceEndDateTime(Date occurrenceEndDateTime) {
        this.occurrenceEndDateTime = occurrenceEndDateTime;
    }
}
