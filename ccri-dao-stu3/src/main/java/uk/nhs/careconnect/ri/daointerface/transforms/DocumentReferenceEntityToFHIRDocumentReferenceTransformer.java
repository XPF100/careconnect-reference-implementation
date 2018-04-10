package uk.nhs.careconnect.ri.daointerface.transforms;

import org.apache.commons.collections4.Transformer;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.entity.documentReference.DocumentReferenceEntity;
import uk.nhs.careconnect.ri.entity.documentReference.DocumentReferenceIdentifier;

@Component
public class DocumentReferenceEntityToFHIRDocumentReferenceTransformer implements Transformer<DocumentReferenceEntity, DocumentReference> {


    @Override
    public DocumentReference transform(final DocumentReferenceEntity documentReferenceEntity) {
        final DocumentReference documentReference = new DocumentReference();

        Meta meta = new Meta();
                //.addProfile(CareConnectProfile.Condition_1);

        if (documentReferenceEntity.getUpdated() != null) {
            meta.setLastUpdated(documentReferenceEntity.getUpdated());
        }
        else {
            if (documentReferenceEntity.getCreated() != null) {
                meta.setLastUpdated(documentReferenceEntity.getCreated());
            }
        }
        documentReference.setMeta(meta);

        documentReference.setId(documentReferenceEntity.getId().toString());
        if (documentReferenceEntity.getCreated() != null) {
            documentReference.setCreated(documentReferenceEntity.getCreated());
        }
        if (documentReferenceEntity.getPatient() != null) {
            documentReference
                    .setSubject(new Reference("Patient/"+documentReferenceEntity.getPatient().getId())
                    .setDisplay(documentReferenceEntity.getPatient().getNames().get(0).getDisplayName()));
        }
        // KGM 10/4/2018 add practice setting and type
        if (documentReferenceEntity.getContextPracticeSetting() != null) {
            documentReference.getContext().getPracticeSetting().addCoding()
                    .setCode(documentReferenceEntity.getContextPracticeSetting().getCode())
                    .setDisplay(documentReferenceEntity.getContextPracticeSetting().getDisplay())
                    .setSystem(documentReferenceEntity.getContextPracticeSetting().getSystem());
        }

        if (documentReferenceEntity.getType() != null) {
            documentReference.getType().addCoding()
                    .setCode(documentReferenceEntity.getType().getCode())
                    .setDisplay(documentReferenceEntity.getType().getDisplay())
                    .setSystem(documentReferenceEntity.getType().getSystem());
        }

        for (DocumentReferenceIdentifier identifier : documentReferenceEntity.getIdentifiers()) {
            documentReference.addIdentifier()
                    .setSystem(identifier.getSystem().getUri())
                    .setValue(identifier.getValue());
        }

        return documentReference;

    }
}
