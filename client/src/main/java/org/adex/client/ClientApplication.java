package org.adex.client;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.DecoratingProxy;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	ApplicationListener <ApplicationReadyEvent> ready(MessageClient messageClient) {
		return event ->  System.out.println( "Result :  " + messageClient.sendMessage("Student"));
	}

	@Bean
	@ImportRuntimeHints(MessageClientHints.class)
	@RegisterReflectionForBinding(Message.class)
	MessageClient messageClient(HttpServiceProxyFactory httpServiceProxyFactory) {
		return httpServiceProxyFactory.createClient(MessageClient.class);
	}

	@Bean
	HttpServiceProxyFactory httpServiceProxyFactory(WebClient.Builder builder) {
		WebClient.Builder webClient = builder.baseUrl("http://localhost:8080");
		return WebClientAdapter
				.createHttpServiceProxyFactory(webClient);
	}

	static class MessageClientHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints
					.proxies()
					.registerJdkProxy(
							MessageClient.class,
							SpringProxy.class,
							Advised.class,
							DecoratingProxy.class
					);
		}
	}

}

interface MessageClient  {

	@GetExchange("/message/{message}")
	Message sendMessage(@PathVariable String message);
}

record Message(String message) {}
