package uk.nhs.careconnect.ri.entity.healthcareService;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import uk.nhs.careconnect.ri.entity.BaseResource;

import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;


import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "HealthcareService")
public class HealthcareServiceEntity extends BaseResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_ID")
    private Long id;

    @Column(name="ACTIVE")
    private Boolean active;

    @Column(name="NAME")
    private String name;

    @ManyToOne
    @JoinColumn(name="ORGANISATION_ID",foreignKey= @ForeignKey(name="FK_SERVICE_SPECIALTY_SPECIALTY_ORGANISATION_ID"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private OrganisationEntity providedBy;

    @OneToMany(mappedBy="service", targetEntity = HealthcareServiceTelecom.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<HealthcareServiceTelecom> telecoms = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = HealthcareServiceIdentifier.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<HealthcareServiceIdentifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = HealthcareServiceSpecialty.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<HealthcareServiceSpecialty> specialties = new HashSet<>();

    @OneToMany(mappedBy="service", targetEntity = HealthcareServiceLocation.class)
    @LazyCollection(LazyCollectionOption.TRUE)
    Set<HealthcareServiceSpecialty> locations = new HashSet<>();

    public String getName() {
        return name;
    }

    public HealthcareServiceEntity setName(String name) {
        this.name = name;
        return this;
    }

    public Set<HealthcareServiceSpecialty> getLocations() {
        return locations;
    }

    public HealthcareServiceEntity setLocations(Set<HealthcareServiceSpecialty> locations) {
        this.locations = locations;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public HealthcareServiceEntity setActive(Boolean active) {
        this.active = active;
        return this;
    }

    public OrganisationEntity getProvidedBy() {
        return providedBy;
    }

    public HealthcareServiceEntity setProvidedBy(OrganisationEntity providedBy) {
        this.providedBy = providedBy;
        return this;
    }

    public Set<HealthcareServiceTelecom> getTelecoms() {
        return telecoms;
    }

    public HealthcareServiceEntity setTelecoms(Set<HealthcareServiceTelecom> telecoms) {
        this.telecoms = telecoms;
        return this;
    }

    public Set<HealthcareServiceSpecialty> getSpecialties() {
        return specialties;
    }

    public HealthcareServiceEntity setSpecialties(Set<HealthcareServiceSpecialty> specialties) {
        this.specialties = specialties;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Set<HealthcareServiceIdentifier> getIdentifiers() {
        return identifiers;
    }

    public HealthcareServiceEntity setIdentifiers(Set<HealthcareServiceIdentifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }
}
