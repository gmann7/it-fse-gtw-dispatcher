/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.dispatcher.dto.response;

import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DateValidationDTO extends ResponseDTO { 

	@Size(min = 0, max = 10000)
	@Schema(description = "Dettaglio del warning")
	private String objectId;
		

	public DateValidationDTO(final LogTraceInfoDTO traceInfo, String inObjectId) {
		super(traceInfo);
		objectId = inObjectId;
	}
	
}
