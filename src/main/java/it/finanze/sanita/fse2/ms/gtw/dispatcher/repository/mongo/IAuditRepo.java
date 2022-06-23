package it.finanze.sanita.fse2.ms.gtw.dispatcher.repository.mongo;

import java.util.Map;

import org.springframework.stereotype.Repository;

/**
 * 
 *	@author vincenzoingenito
 *
 */
@Repository
public interface IAuditRepo {
 

	/**
	 * Salvataggio audit request and response.
	 */
	void save(Map<String, Object> auditMap);

}
