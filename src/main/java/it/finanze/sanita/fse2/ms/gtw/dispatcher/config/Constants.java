package it.finanze.sanita.fse2.ms.gtw.dispatcher.config;

/**
 * 
 * @author vincenzoingenito
 *
 * Constants application.
 */
public final class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.sanita.dispatcher";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.sanita.dispatcher.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.sanita.dispatcher.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.sanita.dispatcher.config";
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.sanita.dispatcher.config.mongo";
		
		/**
		 * Configuration mongo repository path.
		 */
		public static final String REPOSITORY_MONGO = "it.sanita.dispatcher.repository";
		 
		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Headers {
		
		/**
		 * JWT header field.
		 */
		public static final String JWT_HEADER = "Authorization";

		/**
		 * JWT header field of GovWay.
		 */
		public static final String JWT_GOVWAY_HEADER = "GovWay-ModI-Info";

		private Headers() {
			//This method is intentionally left blank.
		}

	}
 
	public static final class Profile {
		public static final String TEST = "test";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class App {
		
		public static final String JWT_TOKEN_TYPE = "JWT";

		public static final String BEARER_PREFIX = "Bearer ";

		private App() {
			//This method is intentionally left blank.
		}
	}
  
	public static final class OIDS {

        public static final String OID_MEF = "2.16.840.1.113883.2.9.4.3.2";
        
        private OIDS() {
            //This method is intentionally left blank.
        }
    }
	
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
