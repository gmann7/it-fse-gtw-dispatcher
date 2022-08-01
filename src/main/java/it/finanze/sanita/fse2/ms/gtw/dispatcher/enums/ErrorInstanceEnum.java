package it.finanze.sanita.fse2.ms.gtw.dispatcher.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorInstanceEnum {

	NO_INFO("", "No specific information for this error, refeer to type for any info"),
	CDA_EXTRACTION("/cda-extraction", "Error while extracting CDA from PDF document"),
	DIFFERENT_HASH("/jwt-hash-match", "Hash of document different from hash in JWT"),
	MISSING_MANDATORY_ELEMENT("/request-missing-field", "Missing required field in request body"),
	INVALID_DATE_FORMAT("/request-invalid-date-format", "Field of type date not correctly inputed"),
	SEMANTIC_WARNING("/schematron-malformed/warning", "Schematron malformed with non-blocking problem"),
	SEMANTIC_ERROR("/schematron-malformed/error", "Schematron malformed with blocking error"),
	DOCUMENT_TYPE_MISMATCH("/jwt-document-type", "Mismatch on document type from JWT to CDA"),
	PERSON_ID_MISMATCH("/jwt-person-id", "Mismatch on person-id from JWT to CDA"),
	MISSING_JWT("/jwt", "JWT token completely missing"),
	MISSING_JWT_FIELD("/jwt-mandatory-field-missing", "Mandatory field in JWT is missing"),
	JWT_MALFORMED_FIELD("/jwt-mandatory-field-malformed", "Malformed JWT field"),
	FHIR_RESOURCE_ERROR("/fhir-resource", "Error creating fhir resource"),
	NON_PDF_FILE("/multipart-file", "File type must be a PDF document"),
	EMPTY_FILE("/empty-multipart-file", "File type must not be empty");

	private String instance;
	private String description;

	public static ErrorInstanceEnum get(String inInstance) {
		ErrorInstanceEnum out = null;
		for (ErrorInstanceEnum v: ErrorInstanceEnum.values()) {
			if (v.getInstance().equalsIgnoreCase(inInstance)) {
				out = v;
				break;
			}
		}
		return out;
	}

	public static RestExecutionResultEnum fromRawResult(RawValidationEnum rawResult) {

		RestExecutionResultEnum result;

		switch (rawResult) {
			case VOCABULARY_ERROR:
				result = RestExecutionResultEnum.VOCABULARY_ERROR;
				break;
			case SEMANTIC_ERROR:
				result = RestExecutionResultEnum.SEMANTIC_ERROR;
				break;
			case SYNTAX_ERROR:
				result = RestExecutionResultEnum.SYNTAX_ERROR;
				break;
			case OK:
				result = RestExecutionResultEnum.OK;
				break;
			default:
				result = RestExecutionResultEnum.GENERIC_ERROR;
				break;
		}

		return result;
	}

}