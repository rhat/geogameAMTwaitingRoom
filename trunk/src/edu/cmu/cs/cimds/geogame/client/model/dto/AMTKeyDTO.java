package edu.cmu.cs.cimds.geogame.client.model.dto;

import java.io.Serializable;

import edu.cmu.cs.cimds.geogame.client.model.db.AMTKey;

/**
 *
 * @author ajuarez
 */

public class AMTKeyDTO implements Serializable {

	private static final long serialVersionUID = 1232236544085056L;
	
	private String key;
	private Long expiration;
	
	public AMTKeyDTO() {}
	public AMTKeyDTO(AMTKey key) {
		this.key = key.getKey();
		this.expiration = key.getExpiration();
	}
	
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	
	public Long getExpiration() { return expiration; }
	public void setExpiration(Long expiration) { this.expiration = expiration; }

	
}