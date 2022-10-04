package it.finanze.sanita.fse2.ms.gtw.dispatcher.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.dispatcher.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.JWTTokenDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.ValidationDataDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.PublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.request.TSPublicationCreationReqDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ErrorInstanceEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.EventStatusEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.OperationLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.RestExecutionResultEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.enums.ResultLogEnum;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ConnectionRefusedException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions.ValidationPublicationErrorException;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.logging.LoggerHelper;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.ICdaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IErrorHandlerSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.service.IKafkaSRV;
import it.finanze.sanita.fse2.ms.gtw.dispatcher.utility.StringUtility;

@Service
public class ErrorHandlerSRV implements IErrorHandlerSRV {

    @Autowired
    private IKafkaSRV kafkaSRV;

    @Autowired
    private ICdaSRV cdaSRV;

    @Autowired
    private LoggerHelper logger;

    @Override
    public void connectionRefusedExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTTokenDTO jwtToken, 
        PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex,
        boolean isPublication, final String documentType) {
        if (jsonObj == null || !Boolean.TRUE.equals(jsonObj.isForcePublish())) {
            cdaSRV.consumeHash(validationInfo.getHash());
        }

        String errorMessage = ex.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_TIMEOUT.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        
        EventStatusEnum errorEventStatus = RestExecutionResultEnum.GENERIC_TIMEOUT.getEventStatusEnum();

        kafkaSRV.sendPublicationStatus(
                traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
                errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);

        final RestExecutionResultEnum errorType = RestExecutionResultEnum.get(capturedErrorType);

        String issuer = (jwtToken != null && jwtToken.getPayload() != null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : "ISSUER_UNDEFINED";
        if(isPublication) {
        	logger.error(errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType);
        } else {
        	logger.error(errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType);
        }
        throw new ValidationPublicationErrorException(errorType, StringUtility.sanitizeMessage(errorType.getTitle()), errorInstance);
    }

    @Override
    public void publicationValidationExceptionHandler(Date startDateOperation, ValidationDataDTO validationInfo, JWTTokenDTO jwtToken, 
        PublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e, boolean isPublication, final String documentType) {
        if (jsonObj == null || !Boolean.TRUE.equals(jsonObj.isForcePublish())) {
            cdaSRV.consumeHash(validationInfo.getHash());
        }

        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        EventStatusEnum errorEventStatus =  RestExecutionResultEnum.GENERIC_ERROR.getEventStatusEnum();
        
        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
            errorEventStatus = RestExecutionResultEnum.get(capturedErrorType).getEventStatusEnum();
        }

        if(isPublication) {
        	kafkaSRV.sendPublicationStatus(traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
        			errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        } else {
        	kafkaSRV.sendReplaceStatus(traceInfoDTO.getTraceID(), validationInfo.getWorkflowInstanceId(), errorEventStatus,
        			errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        }
        

        final RestExecutionResultEnum errorType = RestExecutionResultEnum.get(capturedErrorType);

        String issuer = (jwtToken != null && jwtToken.getPayload() != null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : "ISSUER_UNDEFINED";
        if(isPublication) {
        	logger.error(errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType);
        } else {
        	logger.error(errorMessage + " " + validationInfo.getWorkflowInstanceId(), OperationLogEnum.REPLACE_CDA2, ResultLogEnum.KO, startDateOperation, errorType.getErrorCategory(), issuer, documentType);
        }
        throw new ValidationPublicationErrorException(errorType,StringUtility.sanitizeMessage(errorMessage), errorInstance);
    }

    @Override
    public void tsFeedingValidationExceptionHandler(Date startDateOperation, String workflowInstanceId, JWTTokenDTO jwtToken, 
        TSPublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ValidationException e, final String documentType) {
        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        
        EventStatusEnum errorEventStatus =  RestExecutionResultEnum.GENERIC_ERROR.getEventStatusEnum();
        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
            errorEventStatus = RestExecutionResultEnum.get(capturedErrorType).getEventStatusEnum();
        }

        final RestExecutionResultEnum result = RestExecutionResultEnum.get(capturedErrorType);

        kafkaSRV.sendFeedingStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);

        String issuer = (jwtToken != null && jwtToken.getPayload()!= null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
        logger.error(errorMessage + " " + workflowInstanceId, OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, result.getErrorCategory(), issuer, documentType);
        throw new ValidationPublicationErrorException(result, StringUtility.sanitizeMessage(e.getError().getDetail()), errorInstance);
    }

    @Override
    public void tsFeedingConnectionRefusedExceptionHandler(Date startDateOperation, String workflowInstanceId, JWTTokenDTO jwtToken, 
        TSPublicationCreationReqDTO jsonObj, LogTraceInfoDTO traceInfoDTO, ConnectionRefusedException ex, final String documentType) {
        String errorMessage = ex.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_TIMEOUT.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();

        final RestExecutionResultEnum result = RestExecutionResultEnum.get(capturedErrorType);
        EventStatusEnum errorEventStatus = result.getEventStatusEnum();

        String issuer = (jwtToken != null && jwtToken.getPayload() != null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;

        kafkaSRV.sendFeedingStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jsonObj, jwtToken != null ? jwtToken.getPayload() : null);
        logger.error(errorMessage + " " + workflowInstanceId, OperationLogEnum.PUB_CDA2, ResultLogEnum.KO, startDateOperation, result.getErrorCategory(), issuer, documentType);
        throw new ValidationPublicationErrorException(result, StringUtility.sanitizeMessage(ex.getMessage()), errorInstance);
    }

    @Override
    public void validationExceptionHandler(Date startDateOperation, LogTraceInfoDTO traceInfoDTO, String workflowInstanceId, JWTTokenDTO jwtToken, 
        ValidationException e, final String documentType) {
        
        String errorMessage = e.getMessage();
        String capturedErrorType = RestExecutionResultEnum.GENERIC_ERROR.getType();
        String errorInstance = ErrorInstanceEnum.NO_INFO.getInstance();
        EventStatusEnum errorEventStatus =  RestExecutionResultEnum.GENERIC_ERROR.getEventStatusEnum();
        if (e.getError() != null) {
            errorMessage = e.getError().getDetail();
            capturedErrorType = e.getError().getType();
            errorInstance = e.getError().getInstance();
            errorEventStatus = RestExecutionResultEnum.get(capturedErrorType).getEventStatusEnum();
        }

        final RestExecutionResultEnum validationResult = RestExecutionResultEnum.get(capturedErrorType);
        kafkaSRV.sendValidationStatus(traceInfoDTO.getTraceID(), workflowInstanceId, errorEventStatus, errorMessage, jwtToken != null ? jwtToken.getPayload() : null);

        String issuer = (jwtToken !=null && jwtToken.getPayload()!=null && !StringUtility.isNullOrEmpty(jwtToken.getPayload().getIss())) ? jwtToken.getPayload().getIss() : Constants.App.JWT_MISSING_ISSUER_PLACEHOLDER;
        logger.error(e.getError().getDetail() + " " + workflowInstanceId, OperationLogEnum.VAL_CDA2, ResultLogEnum.KO, startDateOperation, validationResult.getErrorCategory(), issuer, documentType);
        throw new ValidationErrorException(validationResult, StringUtility.sanitizeMessage(e.getError().getDetail()), workflowInstanceId, errorInstance);
    }

}