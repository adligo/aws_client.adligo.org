package org.adligo.aws_client;

import java.util.ArrayList;
import java.util.List;

public class DefaultPollingDaemon implements Runnable, I_PollingDaemon {
	private volatile List<I_PolledItem> items = new ArrayList<I_PolledItem>();
	private Thread daemonThread;
	
	protected void start() {
		Thread daemonThread = new Thread(this);
		daemonThread.start();
	}
	
	@Override
	public void run() {
		while (items.size() >= 1) {
			//prevent concurrent modification by making a copy
			List<I_PolledItem> new_items = new ArrayList<I_PolledItem>();
			new_items.addAll(items);
			
			for (I_PolledItem item : new_items) {
				item.poll();
			}
			try {
				Thread.yield();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	@Override
	public void addItem(I_PolledItem item) {
		items.add(item);
	}

	@Override
	public void removeItem(I_PolledItem item) {
		items.remove(item);
	}

}
