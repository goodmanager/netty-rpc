package com.felix.rpc.framework.register;

import com.felix.rpc.framework.common.dto.ConsulServiceInstanceDetail;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;

public class ConsulServiceRegister {

	private Consul client;

	public ConsulServiceRegister(Consul client) {
		this.client = client;
	}

	public void registerService(ConsulServiceInstanceDetail consulServiceInstanceDetail) throws NotRegisteredException {
		// register new service
		AgentClient agentClient = client.agentClient();
		Registration service = ImmutableRegistration.builder().id(consulServiceInstanceDetail.getId())
				.name(consulServiceInstanceDetail.getInterfaceName()).port(consulServiceInstanceDetail.getListenPort())
				.check(Registration.RegCheck.ttl(3L)) // registers with a TTL of 3 seconds
				.tags(consulServiceInstanceDetail.getTags()).build();

		agentClient.register(service);
		agentClient.pass(consulServiceInstanceDetail.getId());
	}

}
