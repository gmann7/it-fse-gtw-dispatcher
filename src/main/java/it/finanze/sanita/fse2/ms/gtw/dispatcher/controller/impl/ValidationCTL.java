/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.controller.IValidationCTL;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTPayloadDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.ValidationCDAReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.ValidationResDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CdaUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.CfUtility;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation controller.
 */
@Slf4j
@RestController
public class ValidationCTL extends AbstractCTL implements IValidationCTL {

	@Autowired
	private IKafkaSRV kafkaSRV;

	@Autowired
	private LoggerHelper logger;

	@Autowired
	private IErrorHandlerSRV errorHandlerSRV;

	@Override
	public ResponseEntity<ValidationResDTO> validate(final ValidationCDAReqDTO requestBody, final MultipartFile file, final HttpServletRequest request) {
		final Date startDateOperation = new Date();
		LogTraceInfoDTO traceInfoDTO = getLogTraceInfo();

		String workflowInstanceId = Constants.App.MISSING_WORKFLOW_PLACEHOLDER;
		JWTPayloadDTO jwtPayloadToken = null;
		ValidationCDAReqDTO jsonObj = null;
		String warning = null;
		Document docT = null;
 
		String subjectFiscalCode = Constants.App.JWT_MISSING_SUBJECT;
		
		try {
			jwtPayloadToken = extractAndValidateJWT(request,EventTypeEnum.VALIDATION);

			subjectFiscalCode = CfUtility.extractFiscalCodeFromJwtSub(jwtPayloadToken.getSub());
			jsonObj = getAndValidateValidationReq(request.getParameter("requestBody"));
			final byte[] bytes = getAndValidateFile(file);
			final String cda = extractCDA(bytes, jsonObj.getMode());
			docT = Jsoup.parse(cda);
			workflowInstanceId = CdaUtility.getWorkflowInstanceId(docT);

			log.info("[START] {}() with arguments {}={}, {}={}","validate","traceId", traceInfoDTO.getTraceID(),"wif", workflowInstanceId);

			validateJWT(jwtPayloadToken, cda);
			
			warning = validate(cda, jsonObj.getActivity(), workflowInstanceId);
			String message = null;
			if (jsonObj.getActivity().equals(ActivityEnum.VERIFICA)) {
				message = "Attenzione - è stato chiamato l'endpoint di validazione con VERIFICA";
			}

			kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, EventStatusEnum.SUCCESS,message, jwtPayloadToken);

			String issuer = !StringUtility.isNullOrEmpty(jwtPayloadToken.getIss()) ? jwtPayloadToken.getIss()
							: Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
			 
			logger.info(Constants.App.LOG_TYPE_CONTROL,workflowInstanceId, "Validation CDA completed for workflow instance Id " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.OK, startDateOperation, issuer, CdaUtility.getDocumentType(docT), subjectFiscalCode,
					jwtPayloadToken);
			request.setAttribute("JWT_ISSUER", issuer);
		} catch (final ValidationException e) {
			errorHandlerSRV.validationExceptionHandler(startDateOperation, traceInfoDTO, workflowInstanceId, jwtPayloadToken, e, CdaUtility.getDocumentType(docT));
		}

		if (jsonObj != null && jsonObj.getMode() == null) {
			String schematronWarn = StringUtility.isNullOrEmpty(warning) ? "" : warning;
			warning = "[" + schematronWarn + "[WARNING_EXTRACT]" + Constants.Misc.WARN_EXTRACTION_SELECTION + "]";
		}

		warning = StringUtility.isNullOrEmpty(warning) ? null : warning;
		if (jsonObj != null && ActivityEnum.VALIDATION.equals(jsonObj.getActivity())) {
			return new ResponseEntity<>(new ValidationResDTO(traceInfoDTO, workflowInstanceId, warning),
					HttpStatus.CREATED);
		}

		log.info("[EXIT] {}() with arguments {}={}, {}={}","validate","traceId", traceInfoDTO.getTraceID(),"wif", workflowInstanceId);

		return new ResponseEntity<>(new ValidationResDTO(traceInfoDTO, workflowInstanceId, warning), HttpStatus.OK);
	}
	
}
