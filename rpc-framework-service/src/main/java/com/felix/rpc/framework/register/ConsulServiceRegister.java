package com.felix.rpc.framework.register;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.felix.rpc.framework.common.dto.ConsulServiceInstanceDetail;

public class ConsulServiceRegister {

	private ConsulClient client;

	public ConsulServiceRegister(ConsulClient client) {
		this.client = client;
	}

	public void registerService(ConsulServiceInstanceDetail consulServiceInstanceDetail) {
		// register new service
		NewService newService = new NewService();
		newService.setId(consulServiceInstanceDetail.getId());
		newService.setName(consulServiceInstanceDetail.getInterfaceName());
		newService.setTags(consulServiceInstanceDetail.getTags());
		newService.setAddress(consulServiceInstanceDetail.getHostName());
		newService.setPort(consulServiceInstanceDetail.getListenPort());

		NewService.Check serviceCheck = new NewService.Check();
		// serviceCheck.setHttp(
		// "http://" + registerCenterConfig.getHostName() + ":" +
		// registerCenterConfig.getPort() + "/health");
		serviceCheck.setInterval("10s");

		newService.setCheck(serviceCheck);
		client.agentServiceRegister(newService);
	}

}
