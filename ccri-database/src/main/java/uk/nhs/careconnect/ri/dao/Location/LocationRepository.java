package uk.nhs.careconnect.ri.dao.Location;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;

import java.util.List;

public interface LocationRepository  {
    void save(LocationEntity location);

    Location read(IdType theId);

    List<Location> searchLocation(

            @OptionalParam(name = Location.SP_IDENTIFIER) TokenParam identifier
    );
}