package es.gob.afirma.standalone;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Logger;

import es.gob.afirma.standalone.crypto.CypherDataManager;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/** Utilidades para el manejo y establecimiento del <i>Proxy</i> de red para las
 * conexiones de la aplicaci&oacute;n.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class ProxyUtil {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/** Indica si las variables del Proxy que pudiesen encontrarse en las variables de entorno de Java
	 * provienen de un establecimiento externo (en cuyo caso no se alteran nunca) o si han sido establecidas a
	 * trav&eacute;s del GUI de AutoFirma, en cuyo caso deben limpiarse al desmarcar la casilla "Usar proxy",
	 * para efectivamente dejar de usar un proxy de red. */
	private static boolean clearOnUncheck = false;

	private ProxyUtil() {
		// No instanciable
	}

	private static boolean setDefaultHttpProxy() {
		return setDefaultProxy("http://www.google.com", "http.proxyHost", "http.proxyPort"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static boolean setDefaultHttpsProxy() {
		return setDefaultProxy("https://www.google.com", "https.proxyHost", "https.proxyPort"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static boolean setDefaultProxy(final String urlToCkech, final String hostProperty, final String portProperty) {
		System.setProperty("java.net.useSystemProxies", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		final List<Proxy> l;
		final URI uri;
		try {
			uri = new URI(urlToCkech);
		    l = ProxySelector.getDefault().select(uri);
		}
		catch (final URISyntaxException e) {
			LOGGER.warning(
				"No se ha podido comprobar el proxy por defecto: " + e //$NON-NLS-1$
			);
		    return false;
		}
		if (l != null) {
		    for (final Proxy proxy : l) {
		    	LOGGER.info(
	    			"Las conexiones para protocolo '" + uri.getScheme() + "' son por defecto de tipo: " + proxy.type() //$NON-NLS-1$ //$NON-NLS-2$
    			);
		        final InetSocketAddress addr = (InetSocketAddress) proxy.address();

		        if (addr == null) {
		            continue;
		        }

				System.setProperty(hostProperty, addr.getHostName());
				System.setProperty(portProperty, Integer.toString(addr.getPort()));

		        LOGGER.info(
					"Se usara el Proxy configurado en sistema para las conexiones de red HTTP: " + addr.getHostName() + ":" + addr.getPort() //$NON-NLS-1$ //$NON-NLS-2$
				);

				return true;
		    }
		}
		return false;
	}

	/** Indica si hay un <i>Proxy</i> HTTP establecido en base a variables de entorno
	 * del sistema operativo.
	 * @return <code>true</code> si hay un <i>Proxy</i> establecido en base a
	 * variables de entorno del sistema operativo, <code>false</code> en caso
	 * contrario. */
	public static boolean isSystemProxySet() {
    	final String systemProxyHost = System.getenv("http.proxyHost"); //$NON-NLS-1$
    	final String systemProxyPort = System.getenv("http.proxyPort"); //$NON-NLS-1$
    	return systemProxyHost != null &&
		   systemProxyPort != null &&
    		   !systemProxyPort.isEmpty() &&
    		   		systemProxyPort.matches("\\d+"); //$NON-NLS-1$
	}

	/** Configura el <i>Proxy</i> de AutoFirma en base a las variables de entorno de sistema operativo.
	 * @return <code>true</code> si hab&iacute;a alguna configuraci&oacute;n en variables de entorno y esta se
	 *         estableci&oacute;, <code>false</code> en caso contrario. */
	private static boolean configureSystemProxy() {

		final String systemHttpProxyHost      = System.getenv("http.proxyHost");      //$NON-NLS-1$
    	final String systemHttpProxyPort      = System.getenv("http.proxyPort");      //$NON-NLS-1$
    	final String systemHttpNonProxyHosts  = System.getenv("http.nonProxyHosts");  //$NON-NLS-1$
    	final String systemHttpsProxyHost     = System.getenv("https.proxyHost");     //$NON-NLS-1$
    	final String systemHttpsProxyPort     = System.getenv("https.proxyPort");     //$NON-NLS-1$
    	final String systemHttpsNonProxyHosts = System.getenv("https.nonProxyHosts"); //$NON-NLS-1$
    	final String systemFtpProxyHost       = System.getenv("ftp.proxyHost");       //$NON-NLS-1$
    	final String systemFtpProxyPort       = System.getenv("ftp.proxyPort");       //$NON-NLS-1$
    	final String systemFtpNonProxyHosts   = System.getenv("ftp.nonProxyHosts");   //$NON-NLS-1$
    	final String systemSocksProxyHost     = System.getenv("socks.proxyHost");     //$NON-NLS-1$
    	final String systemSocksProxyPort     = System.getenv("socks.proxyPort");     //$NON-NLS-1$
    	final String systemSocksNonProxyHosts = System.getenv("socks.nonProxyHosts"); //$NON-NLS-1$
    	final String systemProxyUsername      = System.getenv("java.proxyUser");      //$NON-NLS-1$
    	final String systemProxyPassword      = System.getenv("java.proxyPassword");  //$NON-NLS-1$

    	boolean modified = false;

    	if (systemHttpProxyHost != null &&
    			systemHttpProxyPort != null &&
					!systemHttpProxyHost.trim().isEmpty() &&
						!systemHttpProxyPort.trim().isEmpty()) {
	    	System.setProperty("http.proxyHost", systemHttpProxyHost); //$NON-NLS-1$
			System.setProperty("http.proxyPort", systemHttpProxyPort); //$NON-NLS-1$
			if (systemHttpNonProxyHosts != null) {
				System.setProperty("http.nonProxyHosts", systemHttpNonProxyHosts); //$NON-NLS-1$
			}
			LOGGER.info(
				"Se usara el Proxy configurado en sistema para las conexiones de red HTTP: " + systemHttpProxyHost + ":" + systemHttpProxyPort //$NON-NLS-1$ //$NON-NLS-2$
			);
			modified = true;
    	}

		if (systemHttpsProxyHost != null &&
				systemHttpsProxyPort != null &&
					!systemHttpsProxyHost.trim().isEmpty() &&
						!systemHttpsProxyPort.trim().isEmpty()) {
			System.setProperty("https.proxyHost", systemHttpsProxyHost); //$NON-NLS-1$
			System.setProperty("https.proxyPort", systemHttpsProxyPort); //$NON-NLS-1$
			if (systemHttpsNonProxyHosts != null) {
				System.setProperty("https.nonProxyHosts", systemHttpsNonProxyHosts); //$NON-NLS-1$
			}
			LOGGER.info(
				"Se usara el Proxy configurado en sistema para las conexiones de red HTTPS: " + systemHttpsProxyHost + ":" + systemHttpsProxyPort //$NON-NLS-1$ //$NON-NLS-2$
			);
			modified = true;
		}

		if (systemFtpProxyHost != null &&
				systemFtpProxyPort != null &&
					!systemFtpProxyHost.trim().isEmpty() &&
						!systemFtpProxyPort.trim().isEmpty()) {
			System.setProperty("ftp.proxyHost", systemFtpProxyHost); //$NON-NLS-1$
			System.setProperty("ftp.proxyPort", systemFtpProxyPort); //$NON-NLS-1$
			if (systemFtpNonProxyHosts != null) {
				System.setProperty("ftp.nonProxyHosts", systemFtpNonProxyHosts); //$NON-NLS-1$
			}
			LOGGER.info(
				"Se usara el Proxy configurado en sistema para las conexiones de red FTP: " + systemFtpProxyHost + ":" + systemFtpProxyPort //$NON-NLS-1$ //$NON-NLS-2$
			);
			modified = true;
		}

		if (systemSocksProxyHost != null &&
				systemSocksProxyPort != null &&
					!systemSocksProxyHost.trim().isEmpty() &&
						!systemSocksProxyPort.trim().isEmpty()) {
			System.setProperty("socks.proxyHost", systemSocksProxyHost); //$NON-NLS-1$
			System.setProperty("socks.proxyPort", systemSocksProxyPort); //$NON-NLS-1$
			if (systemSocksNonProxyHosts != null) {
				System.setProperty("socks.nonProxyHosts", systemSocksNonProxyHosts); //$NON-NLS-1$
			}
			LOGGER.info(
				"Se usara el Proxy configurado en sistema para las conexiones de red SOCKS: " + systemSocksProxyHost + ":" + systemSocksProxyPort //$NON-NLS-1$ //$NON-NLS-2$
			);
			modified = true;
		}

		// Solo ponemos el autenticador si se ha establecido alguna configuracion de Proxy
		if (modified && systemProxyUsername != null && !systemProxyUsername.isEmpty()) {
			setProxyAuthenticator(systemProxyUsername, systemProxyPassword);
			LOGGER.info(
				"Se usaran las credeanciales de usuario configuradas en sistema para las conexiones de red con Proxy" //$NON-NLS-1$
			);
		}

		return modified;
	}

	private static void setProxyAuthenticator(final String proxyUsername, final String proxyPassword) {
		if (proxyUsername != null && !proxyUsername.trim().isEmpty() && proxyPassword != null) {
			Authenticator.setDefault(
				new Authenticator() {
			        @Override
					public PasswordAuthentication getPasswordAuthentication() {
			            return new PasswordAuthentication(
		            		proxyUsername,
		            		proxyPassword.toCharArray()
	            		);
			        }
			    }
			);
		}
	}

    /** Establece la configuraci&oacute;n para el servidor <i>Proxy</i> seg&uacute;n los valores
     * de configuraci&oacute;n encontrados. */
    public static void setProxySettings() {

    	// Si hay un proxy configurado a nivel de sistema operativo se usa ese y se ignoran
    	// las opciones de AutoFirma
    	if (configureSystemProxy()) {
    		return;
    	}
    	// Si no hay configuracion de sistema operativo, se usa la configuracion del
    	// GUI de autofirma
    	else if (PreferencesManager.getBoolean(PreferencesManager.PREFERENCE_GENERAL_PROXY_SELECTED, false)) {

    		final String proxyHost = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_HOST, null);
    		final String proxyPort = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_PORT, null);
    		final String proxyUsername = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_USERNAME, null);
    		final String cipheredProxyPassword = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_PASSWORD, null);

    		if (proxyHost != null &&
    				!proxyHost.trim().isEmpty() &&
    					proxyPort != null &&
    						!proxyPort.trim().isEmpty()) {

    			LOGGER.info(
					"Establecido Proxy de red desde el GUI de la aplicacion: " + proxyHost + ":" + proxyPort //$NON-NLS-1$ //$NON-NLS-2$
				);

    			System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("http.proxyPort", proxyPort); //$NON-NLS-1$

    			System.setProperty("https.proxyHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("https.proxyPort", proxyPort); //$NON-NLS-1$

    			System.setProperty("ftp.proxHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("ftp.proxyPort", proxyPort); //$NON-NLS-1$

    			System.setProperty("socksProxyHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("socksProxyPort", proxyPort); //$NON-NLS-1$

        		if (proxyUsername != null &&
        				!proxyUsername.trim().trim().isEmpty() &&
        					cipheredProxyPassword != null &&
        						!cipheredProxyPassword.trim().trim().isEmpty()) {

        			final char[] proxyPassword;
					try {
						proxyPassword = decipherPassword(cipheredProxyPassword);
					}
					catch (final Exception e) {
						LOGGER.warning("No se pudo descifrar la contrasena del proxy. No se configurara el usuario y contrasena: " + e); //$NON-NLS-1$
						Authenticator.setDefault(null);
						return;
					}

        			Authenticator.setDefault(
    					new Authenticator() {
	    			        @Override
	    					public PasswordAuthentication getPasswordAuthentication() {
	    			            return new PasswordAuthentication(
    			            		proxyUsername,
    			            		proxyPassword
			            		);
	    			        }
	    			    }
					);
        		}

        		clearOnUncheck = true;
    		}
    		// Indicar que si se quiere usar proxy pero con los valores en blanco se entiende
    		// como limpiar valores que pudiesen estar ya establecidos en Java
    		else {
    			clearJavaProxy();
    		}
    	}
    	// Si no se indica nada en el GUI de AutoFirma, se dejan los valores por defecto de
    	// la JVM tal y como estaban, nunca se sobreescriben, a menos que estos mismos valores
    	// hubiesen sido establecidos con el mismo AutoFirma.
    	else {
    		// Si es establecio el Proxy con AutoFirma y se desmarca, se borra con AutoFirma
    		if (clearOnUncheck) {
    			clearJavaProxy();
    		}
    		// Si se desmarca con AutoFirma pero habia un Proxy establecido externamente, se respeta
    		else {
	    		final String javaProxyHost = System.getProperty("http.proxyHost"); //$NON-NLS-1$
	    		final String javaProxyPort = System.getProperty("http.proxyPort"); //$NON-NLS-1$
	    		// Si hay un proxy establecido a nivel de JVM...
	    		if (javaProxyHost != null &&
	    				javaProxyPort != null &&
	    					!javaProxyHost.trim().isEmpty() &&
	    						!javaProxyPort.trim().isEmpty()) {
	    			LOGGER.info(
						"Se usara el Proxy por defecto de Java para las conexiones de red: " + javaProxyHost + ":" + javaProxyPort //$NON-NLS-1$ //$NON-NLS-2$
					);
	    		}
	    		// Miramos a ver si hay establecido un proxy a nivel de SO (solo para Windows 7 y
	    		// superiores y Linux con GNOME. Si lo hay, las funciones lo aplican
	    		else if (!setDefaultHttpProxy() && !setDefaultHttpsProxy()) {
	    			// Si las funciones no han encontrado y establecido un proxy a nivel de SO, ya no nos
	    			// quedan mas opciones, no se usa proxy
	    			LOGGER.info("No se usara Proxy para las conexiones de red"); //$NON-NLS-1$
	    		}
    		}
    	}
    }

    private static void clearJavaProxy() {
		LOGGER.info("No se usara Proxy para las conexiones de red"); //$NON-NLS-1$
		System.clearProperty("http.proxyHost"); //$NON-NLS-1$
		System.clearProperty("http.proxyPort"); //$NON-NLS-1$
		System.clearProperty("http.nonProxyHosts"); //$NON-NLS-1$
		System.clearProperty("https.proxyHost"); //$NON-NLS-1$
		System.clearProperty("https.proxyPort"); //$NON-NLS-1$
		System.clearProperty("https.nonProxyHosts"); //$NON-NLS-1$
		System.clearProperty("ftp.proxHost"); //$NON-NLS-1$
		System.clearProperty("ftp.proxyPort"); //$NON-NLS-1$
		System.clearProperty("ftp.nonProxyHosts"); //$NON-NLS-1$
		System.clearProperty("socks.proxyHost"); //$NON-NLS-1$
		System.clearProperty("socks.proxyPort"); //$NON-NLS-1$
		System.clearProperty("socks.nonProxyHosts"); //$NON-NLS-1$
		Authenticator.setDefault(null);
    }

    private static final char[] PWD_CIPHER_KEY = new char[] {'8', 'W', '{', 't', '2', 'r', ',', 'B'};

    /** Cifra una contrase&ntilde;a.
     * @param password Contrase&ntilde;a cifrada o <code>null</code> si la contrase&ntilde;a proporcionada es
     *                 nula o vac&iacute;a.
     * @return Contrase&tilde;a cifrada en base 64.
     * @throws GeneralSecurityException Cuando se produce un error durante el cifrado. */
    public static String cipherPassword(final char[] password) throws GeneralSecurityException {
    	if (password == null || password.length < 1) {
    		return null;
    	}
    	return CypherDataManager.cipherData(
			String.valueOf(password).getBytes(StandardCharsets.UTF_8),
			String.valueOf(
				PWD_CIPHER_KEY
			).getBytes(StandardCharsets.UTF_8)
		);
    }

    /** Descifra una contrase&ntilde;a
     * @param cipheredPassword Contrase&ntilde;a cifrada en base 64.
     * @return Contrase&ntilde;a o <code>null</code> si la entrada cifrada era nula o vac&iacute;a.
     * @throws GeneralSecurityException Cuando se produce un error durante el descifrado.
     * @throws IOException Cuando los datos introducidos no son un base 64 v&aacute;lido. */
    public static char[] decipherPassword(final String cipheredPassword) throws GeneralSecurityException, IOException {
    	if (cipheredPassword == null  || cipheredPassword.isEmpty()) {
    		return null;
    	}
    	final byte[] p = CypherDataManager.decipherData(
			cipheredPassword.getBytes(StandardCharsets.UTF_8),
			String.valueOf(
				PWD_CIPHER_KEY
			).getBytes(StandardCharsets.UTF_8)
		);
    	return new String(p, StandardCharsets.UTF_8).toCharArray();
    }
}
