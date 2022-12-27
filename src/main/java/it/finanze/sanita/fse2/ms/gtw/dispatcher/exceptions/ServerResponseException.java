/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic exception to handle server-side errors.
 */
@Getter
@Setter
@AllArgsConstructor
public class ServerResponseException extends RuntimeException {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -8171450249714278521L;
	
	private String microservice;
	
	private String target;
	
	private String message;
	
	private HttpStatus status;
	
	private int statusCode;
	
	private String detail;
}
