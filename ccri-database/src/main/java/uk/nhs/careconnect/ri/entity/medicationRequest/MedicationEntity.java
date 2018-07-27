package uk.nhs.careconnect.ri.entity.medicationRequest;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Medication")
public class MedicationEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MEDICATION_ID")
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn (name = "MEDICATION_CONCEPT_ID",nullable = false,foreignKey= @ForeignKey(name="FK_MEDICATION_CODE"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private ConceptEntity medicationCode;

    public void setId(Long id) {
        this.id = id;
    }

    String batch;

    Date batchExpiry;

    public ConceptEntity getMedicationCode() {
        return medicationCode;
    }

    public void setMedicationCode(ConceptEntity medicationCode) {
        this.medicationCode = medicationCode;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public Date getBatchExpiry() {
        return batchExpiry;
    }

    public void setBatchExpiry(Date batchExpiry) {
        this.batchExpiry = batchExpiry;
    }
}