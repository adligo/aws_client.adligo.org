package org.adligo.aws_client;

public interface I_PollingDaemon {
	public void addItem(I_PolledItem item);
	public void removeItem(I_PolledItem item);
}
