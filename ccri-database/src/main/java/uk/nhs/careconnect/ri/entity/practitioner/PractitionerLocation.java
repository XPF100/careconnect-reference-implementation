package uk.nhs.careconnect.ri.entity.practitioner;


import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.nhs.careconnect.ri.entity.BaseResource;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

import javax.persistence.*;

@Entity
@Table(name = "PractitionerLocation")
public class PractitionerLocation extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="PRACTITIONER_LOCATION_ID")
    private Long myId;

    @ManyToOne
    @JoinColumn(name = "PRACTITIONER_ROLE_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_LOCATION_PRACTITIONER_ROLE_ID"))
    private PractitionerRole practitionerRole;

    @ManyToOne
    @JoinColumn(name="LOCATION_ID",foreignKey= @ForeignKey(name="FK_PRACTITIONER_LOCATION_LOCATION_ID"))
    @LazyCollection(LazyCollectionOption.TRUE)
    private LocationEntity location;

    public Long getId()
    {
        return this.myId;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public PractitionerRole setLocation(LocationEntity location) {
        this.location = location;
        return this.practitionerRole;
    }

    public PractitionerRole getPractitionerRole() {
        return practitionerRole;
    }

    public PractitionerLocation setPractitionerRole(PractitionerRole practitionerRole) {
        this.practitionerRole = practitionerRole;
        return this;
    }


}
