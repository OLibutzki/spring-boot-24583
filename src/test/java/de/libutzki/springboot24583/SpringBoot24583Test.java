package de.libutzki.springboot24583;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.LinkedHashMap;
import java.util.Map;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.ParentContextApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import de.libutzki.springboot24583.TestConfiguration.ChildEventHandler;
import de.libutzki.springboot24583.child1.Child1Config;
import de.libutzki.springboot24583.child2.Child2Config;
import de.libutzki.springboot24583.parent.ParentConfig;

@Testcontainers
final class SpringBoot24583Test {

	private static final int AXON_HTTP_PORT = 8024;
	private static final int AXON_GRPC_PORT = 8124;

	@Container
	private final GenericContainer<?> axonServer = new GenericContainer<>( "axoniq/axonserver:4.4.6" )
			.withExposedPorts( AXON_HTTP_PORT, AXON_GRPC_PORT )
			.waitingFor( Wait.forHttp( "/actuator/info" ).forPort( AXON_HTTP_PORT ) );

	protected ConfigurableApplicationContext child1Context;
	protected ConfigurableApplicationContext child2Context;
	private ConfigurableApplicationContext parentContext;

	@BeforeEach
	void init( ) {
		final String[] args = new String[0];

		final StandardEnvironment environment = new StandardEnvironment( );
		final Map<String, Object> properties = new LinkedHashMap<>( );
		properties.put( "axon.axonserver.servers", String.format( "%s:%s", axonServer.getContainerIpAddress( ), axonServer.getMappedPort( AXON_GRPC_PORT ) ) );
		environment.getPropertySources( ).addFirst( new MapPropertySource( "Test Properties", properties ) );

		final SpringApplication parentApplication = new SpringApplication( ParentConfig.class );
		parentApplication.setEnvironment( environment );
		parentContext = parentApplication.run( args );
		final ParentContextApplicationContextInitializer parentContextApplicationContextInitializer = new ParentContextApplicationContextInitializer( parentContext );

		final SpringApplication springApplication1 = new SpringApplication( TestConfiguration.class, Child1Config.class );
		springApplication1.addInitializers( parentContextApplicationContextInitializer );
		child1Context = springApplication1.run( args );

		final SpringApplication springApplication2 = new SpringApplication( Child2Config.class );
		springApplication2.addInitializers( parentContextApplicationContextInitializer );
		child2Context = springApplication2.run( args );
	}

	@AfterEach
	void cleanup( ) {
		child1Context.close( );
		child2Context.close( );
		parentContext.close( );
	}

	@Test
	void test( ) {
		final ChildEventHandler child1EventHandler = child1Context.getBean( ChildEventHandler.class );
		final EventGateway child2EventGateway = child2Context.getBean( EventGateway.class );
		final TestEvent testEvent = new TestEvent( "Test-Payload" );
		child2EventGateway.publish( testEvent );
		verify( child1EventHandler, timeout( 3000 ) ).on( testEvent );
	}

}
