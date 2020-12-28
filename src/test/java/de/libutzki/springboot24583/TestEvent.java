package de.libutzki.springboot24583;

class TestEvent {

	private final String payload;

	public TestEvent( final String payload ) {
		this.payload = payload;
	}

	@Override
	public int hashCode( ) {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( payload == null ) ? 0 : payload.hashCode( ) );
		return result;
	}

	@Override
	public boolean equals( final Object obj ) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass( ) != obj.getClass( ) ) {
			return false;
		}
		final TestEvent other = ( TestEvent ) obj;
		if ( payload == null ) {
			if ( other.payload != null ) {
				return false;
			}
		} else if ( !payload.equals( other.payload ) ) {
			return false;
		}
		return true;
	}

}