package org.adligo.aws_client.models;


/**
 * note this impl only allows 65,536
 * bytes in the payloadData 
 * I made a comment to oauth@ietf.org but not sure where it went
 * a copy of it is in the README.txt in the root of this project
 * 
 * @author scott
 *
 */
public class WebSocket6455FrameMutant implements I_WebSocket6455Frame {
	public static final String MASKING_KEY_MAY_NOT_BE_NULL = "masking key may not be null.";
	public static final String PAYLOAD_DATA_MUST_BE_LESS_THAN_65536_BYTES = "payload data must be less than 65536 bytes.";
	public static final String PAYLOAD_DATA_MAY_NOT_BE_NULL = "payload data may not be null.";
	public static final String OPCODE_DATA_MAY_NOT_BE_NULL = "Opcode data may not be null.";
	private boolean fin = true;
	private boolean rsv1 = false;
	private boolean rsv2 = false;
	private boolean rsv3 = false;
	private Opcode6455 opcode;
	private boolean mask = true;
	private MaskingKey maskingKey;
	/**
	 * note this field is contextual if your dealing with a frame from the server
	 * it may be masked depending on the mask boolean
	 * 
	 * if your dealing with a frame going to the server
	 * it will not be masked
	 * @see I_WebSocketFrame#toSendableBytes()
	 */
	private byte[] payloadData;
	
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#isFin()
	 */
	@Override
	public boolean isFin() {
		return fin;
	}
	public void setFin(boolean fin) {
		this.fin = fin;
	}
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#isRsv1()
	 */
	@Override
	public boolean isRsv1() {
		return rsv1;
	}
	public void setRsv1(boolean rsv1) {
		this.rsv1 = rsv1;
	}
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#isRsv2()
	 */
	@Override
	public boolean isRsv2() {
		return rsv2;
	}
	public void setRsv2(boolean rsv2) {
		this.rsv2 = rsv2;
	}
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#isRsv3()
	 */
	@Override
	public boolean isRsv3() {
		return rsv3;
	}
	public void setRsv3(boolean rsv3) {
		this.rsv3 = rsv3;
	}
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#getOpcode()
	 */
	@Override
	public Opcode6455 getOpcode() {
		return opcode;
	}
	public void setOpcode(Opcode6455 p) {
		if (p == null) {
			throw new IllegalArgumentException(OPCODE_DATA_MAY_NOT_BE_NULL);
		}
		this.opcode = p;
	}
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#isMask()
	 */
	@Override
	public boolean isMask() {
		return mask;
	}
	public void setMask(boolean mask) {
		this.mask = mask;
	}
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#getPayloadData()
	 */
	@Override
	public byte[] getPayloadData() {
		return payloadData;
	}
	public void setPayloadData(byte[] p) {
		if (p == null) {
			throw new IllegalArgumentException(PAYLOAD_DATA_MAY_NOT_BE_NULL);
		}
		if (p.length > 65536) {
			throw new IllegalArgumentException(PAYLOAD_DATA_MUST_BE_LESS_THAN_65536_BYTES);
		}
		this.payloadData = p;
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#getPayloadSize()
	 */
	@Override
	public int getPayloadSize() {
		return payloadData.length;
	}
	
	/* (non-Javadoc)
	 * @see org.adligo.aws_client.models.I_WebSocket6455Frame#getMaskingKey()
	 */
	@Override
	public MaskingKey getMaskingKey() {
		return maskingKey;
	}
	public void setMaskingKey(MaskingKey p) {
		if (p == null) {
			throw new IllegalArgumentException(MASKING_KEY_MAY_NOT_BE_NULL);
		}
		this.maskingKey = p;
	}
	
	@Override
	public byte[] getCleanPayloadData() {
		if (payloadData == null) {
			return new byte [] {};
		}
		byte [] toRet = new byte[payloadData.length];
		int whichMaskKey = 0;
		for (int i=0;i < payloadData.length;i++) {
			byte pay = payloadData[i];
			byte key = maskingKey.getByte(whichMaskKey);
            toRet[i] = (byte) (pay^key);
            whichMaskKey++;
            if (whichMaskKey >= 4) {
            	whichMaskKey = 0;
            }
		}
		return null;
	}
	
	
	@Override
	public byte[] toSendableBytes() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
