package uk.nhs.careconnect.ri.entity.Terminology;

import javax.persistence.*;


@Entity
@Table(name="ValueSetIncludeConcept")
public class ValueSetIncludeConcept {

	private static final int MAX_DESC_LENGTH = 400;

	public ValueSetIncludeConcept() {

	}


	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "VALUESET_INCLUDE_CONCEPT_ID")
	private Integer contentId;

	@ManyToOne
	@JoinColumn(name="CONCEPT_ID")
	private ConceptEntity concept;

	@ManyToOne
	@JoinColumn(name="VALUESET_INCLUDE_ID")
	private ValueSetInclude include;

	public ConceptEntity getConcept() {
		return concept;
	}

	public ValueSetInclude getInclude() {
		return include;
	}

	public Integer getId() {
		return contentId;
	}

	public void setConcept(ConceptEntity concept) {
		this.concept = concept;
	}

	public void setId(Integer contentId) {
		this.contentId = contentId;
	}

	public void setInclude(ValueSetInclude include) {
		this.include = include;
	}

}
