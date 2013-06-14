package edu.cmu.cs.cimds.geogame.client.model.db;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;

/**
 *
 * @author rmk216@ist.psu.edu
 */
@Entity
@Table(name="amtkey")
public class AMTKey extends PersistentEntity {
	
	private static final long serialVersionUID = 88765656755864150L;
	
	private String key;
	private Long expiration;
	
	@Column(name="key", nullable=false)
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	
	@Column(name="expiration", nullable=true)
	public Long getExpiration() { return expiration; }
	public void setExpiration(Long expiration) { this.expiration = expiration; }

		
	@Override
	public boolean equals(Object o) {
		if(o.getClass()==ItemType.class) {
			return this.getKey().equals(o) && this.getExpiration().equals(o);
		} else {
			return super.equals(o);
		}
	}
	
	public static String newKey(){
		return java.util.UUID.randomUUID().toString();
	}
	
	public static boolean expired(Long timestamp){
		return System.currentTimeMillis() > timestamp;
	}
	
}