package de.libutzki.springboot24583;

import static org.mockito.Mockito.mock;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TestConfiguration {
	@Bean
	ChildEventHandler childEventHandler( ) {
		return mock( ChildEventHandler.class );
	}

	static class ChildEventHandler {

		@EventHandler
		void on( final TestEvent testEvent ) {
		}

	}

}
