package org.adligo.aws_client;

import org.adligo.i.adig.client.GRegistry;

public class AwsRegistry {
	static void setUp() {
		GRegistry.addCheckedInvoker(AwsClientInvokerNames.IO_FACTORY, new IO_Factory());
	}
}
