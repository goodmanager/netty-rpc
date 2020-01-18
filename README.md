# netty-rpc-framework

netty-rpc-framework基于spring-boot、netty的rpc框架

***支持zookeeper和consul注册中心***

***支持protostuff对象序列化***

> probe-rpc-framework-common

基础类定义，包括注册中心、netty 服务器的配置信息，对象序列化和反序列化，rpc请求对象和返回对象定义

> probe-rpc-framework-service

rpc服务器端的实现，包括服务的netty 服务的启动、rpc服务扫描和服务注册

> probe-rpc-framework-client

rpc客户端的实现，包括netty的客户端启动、rpc服务发现和负载均衡