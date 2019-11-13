/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Achim Kraus (Bosch Software Innovations GmbH) - use SslContextUtil
 ******************************************************************************/
package org.eclipse.californium.tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.elements.util.SslContextUtil;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.pskstore.InMemoryPskStore;

/**
 * This class implements a simple CoAP client for testing purposes. Usage:
 * <p>
 * {@code java -jar SampleClient.jar [-l] METHOD URI [PAYLOAD]}
 * <ul>
 * <li>METHOD: {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}
 * <li>URI: The URI to the remote endpoint or resource}
 * <li>PAYLOAD: The data to send with the request}
 * </ul>
 * Options:
 * <ul>
 * <li>-l: Loop for multiple responses}
 * </ul>
 * Examples:
 * <ul>
 * <li>{@code SampleClient DISCOVER coap://localhost}
 * <li>{@code SampleClient POST coap://someServer.org:5683 my data}
 * </ul>
 */
public class ConsoleClient {

	// the trust store file used for DTLS server authentication
	private static final String TRUST_STORE_LOCATION = "certs/trustStore.jks";
	private static final char[] TRUST_STORE_PASSWORD = "rootPass".toCharArray();

	private static final String KEY_STORE_LOCATION = "certs/keyStore.jks";
	private static final char[] KEY_STORE_PASSWORD = "endPass".toCharArray();
	
	// resource URI path used for discovery
	private static final String DISCOVERY_RESOURCE = "/.well-known/core";

	// indices of command line parameters
	private static final int IDX_METHOD          = 0;
	private static final int IDX_URI             = 1;
	private static final int IDX_PAYLOAD         = 2;

	// exit codes for runtime errors
	private static final int ERR_MISSING_METHOD  = 1;
	private static final int ERR_UNKNOWN_METHOD  = 2;
	private static final int ERR_MISSING_URI     = 3;
	private static final int ERR_BAD_URI         = 4;
	private static final int ERR_REQUEST_FAILED  = 5;
	private static final int ERR_RESPONSE_FAILED = 6;


	// initialize parameters
	static String method = null;
	static URI uri = null;
	static String payload = "";
	static boolean loop = false;

	static boolean usePSK = false;
	static boolean useRaw = true;

	// for coaps
	private static Endpoint dtlsEndpoint;
	
	/*
	 * Main method of this client.
	 */
	public static void main(String[] args) throws IOException, GeneralSecurityException {
		
		// display help if no parameters specified
		if (args.length == 0) {
			printInfo();
			return;
		}
		
		// input parameters
		int idx = 0;
		for (String arg : args) {
			if (arg.startsWith("-")) {
				if (arg.equals("-l")) {
					loop = true;
				} else if (arg.equals("-psk")) {
					usePSK = true;
				} else if (arg.equals("-cert")) {
					useRaw = false;
				} else {
					System.out.println("Unrecognized option: " + arg);
				}
			} else {
				switch (idx) {
				case IDX_METHOD:
					method = arg.toUpperCase();
					break;
				case IDX_URI:
					try {
						uri = new URI(arg);
					} catch (URISyntaxException e) {
						System.err.println("Failed to parse URI: " + e.getMessage());
						System.exit(ERR_BAD_URI);
					}
					break;
				case IDX_PAYLOAD:
					payload = arg;
					break;
				default:
					System.out.println("Unexpected argument: " + arg);
				}
				++idx;
			}
		}

		// check if mandatory parameters specified
		if (method == null) {
			System.err.println("Method not specified");
			System.exit(ERR_MISSING_METHOD);
		}
		if (uri == null) {
			System.err.println("URI not specified");
			System.exit(ERR_MISSING_URI);
		}
		
		// create request according to specified method
		Request request = newRequest(method);

		// set request URI
		if (method.equals("DISCOVER") && (uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
			// add discovery resource path to URI
			try {
				uri = new URI(uri.getScheme(), uri.getAuthority(), DISCOVERY_RESOURCE, uri.getQuery());
				
			} catch (URISyntaxException e) {
				System.err.println("Failed to parse URI: " + e.getMessage());
				System.exit(ERR_BAD_URI);
			}
		}
		
		request.setURI(uri);
		request.setPayload(payload);
		request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
		
		if (request.getScheme().equals(CoAP.COAP_SECURE_URI_SCHEME)) {
		

			DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder();
			builder.setAddress(new InetSocketAddress(0));
			if (usePSK) {
				InMemoryPskStore pskStore = new InMemoryPskStore();
				pskStore.addKnownPeer(request.getDestinationContext().getPeerAddress(),
						System.console().readLine("PSK Identity: "),
						new String(System.console().readPassword("Secret Key (input hidden): ")).getBytes());
				builder.setPskStore(pskStore);
			} else {
				// load trust store
				Certificate[] trustedCertificates = SslContextUtil.loadTrustedCertificates(
						SslContextUtil.CLASSPATH_SCHEME + TRUST_STORE_LOCATION, "root",
						TRUST_STORE_PASSWORD);
				builder.setTrustStore(trustedCertificates);
				SslContextUtil.Credentials credentials = SslContextUtil.loadCredentials(
						SslContextUtil.CLASSPATH_SCHEME + KEY_STORE_LOCATION, "client", KEY_STORE_PASSWORD,
						KEY_STORE_PASSWORD);
				CertificateType type = useRaw ? CertificateType.RAW_PUBLIC_KEY : CertificateType.X_509;
				builder.setIdentity(credentials.getPrivateKey(), credentials.getCertificateChain(), type);
			}

			DTLSConnector dtlsconnector = new DTLSConnector(builder.build(), null);
			CoapEndpoint.Builder endpointBuilder = new CoapEndpoint.Builder();
			endpointBuilder.setConnector(dtlsconnector);

			dtlsEndpoint = endpointBuilder.build();
			dtlsEndpoint.start();
			EndpointManager.getEndpointManager().setDefaultEndpoint(dtlsEndpoint);
		}
		
		// execute request
		try {
			request.send();

			// loop for receiving multiple responses
			do {
	
				// receive response
				Response response = null;
				try {
					response = request.waitForResponse();
				} catch (InterruptedException e) {
					System.err.println("Failed to receive response: " + e.getMessage());
					System.exit(ERR_RESPONSE_FAILED);
				}
	
				// output response
	
				if (response != null) {
	
					System.out.println(Utils.prettyPrint(response));
					System.out.println("Time elapsed (ms): " + response.getRTT());
	
					// check of response contains resources
					if (response.getOptions().isContentFormat(MediaTypeRegistry.APPLICATION_LINK_FORMAT)) {
	
						String linkFormat = response.getPayloadString();
	
						// output discovered resources
						System.out.println("\nDiscovered resources:");
						System.out.println(linkFormat);
	
					} else {
						// check if link format was expected by client
						if (method.equals("DISCOVER")) {
							System.out.println("Server error: Link format not specified");
						}
					}
	
				} else {
					// no response received	
					System.err.println("Request timed out");
					break;
				}
	
			} while (loop);
			
		} catch (Exception e) {
			System.err.println("Failed to execute request: " + e.getMessage());
			System.exit(ERR_REQUEST_FAILED);
		}
	}

	/*
	 * Outputs user guide of this program.
	 */
	public static void printInfo() {
		System.out.println("Californium (Cf) Console Client");
		System.out.println("(c) 2014, Institute for Pervasive Computing, ETH Zurich");
		System.out.println();
		System.out.println("Usage: " + ConsoleClient.class.getSimpleName() + " [-l] METHOD URI [PAYLOAD]");
		System.out.println("  METHOD  : {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}");
		System.out.println("  URI     : The CoAP URI of the remote endpoint or resource");
		System.out.println("            A coaps URI will automatically use CoAP over DTLS");
		System.out.println("  PAYLOAD : The data to send with the request");
		System.out.println("Options:");
		System.out.println("  -l      : Loop for multiple responses");
		System.out.println("           (automatic for OBSERVE and separate responses)");
		System.out.println("  -psk    : Use a pre-shared secrest for DTLS (is prompted)");
		System.out.println("  -cert   : Use full X.509 certificates instead of raw public keys");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  " + ConsoleClient.class.getSimpleName() + " DISCOVER coap://localhost");
		System.out.println("  " + ConsoleClient.class.getSimpleName() + " PUT coap://iot.eclipse.org:5683/large-put my data");
	}

	/*
	 * Instantiates a new request based on a string describing a method.
	 * 
	 * @return A new request object, or null if method not recognized
	 */
	private static Request newRequest(String method) {
		if (method.equals("GET")) {
			return Request.newGet();
		} else if (method.equals("POST")) {
			return Request.newPost();
		} else if (method.equals("PUT")) {
			return Request.newPut();
		} else if (method.equals("DELETE")) {
			return Request.newDelete();
		} else if (method.equals("DISCOVER")) {
			return Request.newGet();
		} else if (method.equals("OBSERVE")) {
			Request request = Request.newGet();
			request.setObserve();
			loop = true;
			return request;
		} else {
			System.err.println("Unknown method: " + method);
			System.exit(ERR_UNKNOWN_METHOD);
			return null;
		}
	}

}
