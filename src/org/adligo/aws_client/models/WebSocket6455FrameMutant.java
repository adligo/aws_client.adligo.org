package org.adligo.aws_client.models;

import java.math.BigDecimal;

/**
 * note this impl only allows 65,536
 * bytes in the payloadData 
 * I made a comment to oauth@ietf.org but not sure where it went
 * a copy of it is in the README.txt in the root of this project
 * 
 * @author scott
 *
 */
public class WebSocket6455FrameMutant {
	private boolean fin = true;
	private boolean rsv1 = false;
	private boolean rsv2 = false;
	private boolean rsv3 = false;
	private Opcode6455 opcode;
	private boolean mask = false;
	private byte[] payloadData;
	
	public boolean isFin() {
		return fin;
	}
	public void setFin(boolean fin) {
		this.fin = fin;
	}
	public boolean isRsv1() {
		return rsv1;
	}
	public void setRsv1(boolean rsv1) {
		this.rsv1 = rsv1;
	}
	public boolean isRsv2() {
		return rsv2;
	}
	public void setRsv2(boolean rsv2) {
		this.rsv2 = rsv2;
	}
	public boolean isRsv3() {
		return rsv3;
	}
	public void setRsv3(boolean rsv3) {
		this.rsv3 = rsv3;
	}
	public Opcode6455 getOpcode() {
		return opcode;
	}
	public void setOpcode(Opcode6455 opcode) {
		this.opcode = opcode;
	}
	public boolean isMask() {
		return mask;
	}
	public void setMask(boolean mask) {
		this.mask = mask;
	}
	public byte[] getPayloadData() {
		return payloadData;
	}
	public void setPayloadData(byte[] payloadData) {
		this.payloadData = payloadData;
	}
	
}
