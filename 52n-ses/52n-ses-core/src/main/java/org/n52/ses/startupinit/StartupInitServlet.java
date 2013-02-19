/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.ses.startupinit;

import static org.apache.http.entity.ContentType.TEXT_XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.common.SESProperties;


/**
 * Servlet class that wakes up the SES after a (re-)start.
 *
 */
public class StartupInitServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WakeUpThread thread;

	@Override
	public void init() throws ServletException {
		super.init();

		/*
		 * contact the SES to wake it up
		 */
		SESProperties parameters = new SESProperties();
		InputStream propStream = getServletContext().getResourceAsStream("/WEB-INF/classes/" +ConfigurationRegistry.CONFIG_FILE);
		try {
			parameters.load(propStream);
		} catch (IOException e) {
			log(e.getMessage(), e);
		}

		String sesurl = "http://localhost:8080/ses/services/Broker";
		
		try {
			sesurl = parameters.getProperty(ConfigurationRegistry.SES_INSTANCE).trim();
		} catch (Exception e) {
			/*empty*/
		}
		
		int time = 1000;

		try {
			time = Integer.parseInt(parameters.getProperty(ConfigurationRegistry.TIME_TO_WAKEUP));
		} catch (NumberFormatException e) {
			/*empty*/
		}

		log("##Startup Init Wakeup## contacting "+ sesurl);

		this.thread = new WakeUpThread(sesurl, time);
		this.thread.start();

	}


	

	@Override
	public void destroy() {
		this.thread.setRunning(false);
		
		super.destroy();
	}

	public static String getGetCapabilitiesRequest(String sesurl) throws IOException {
		InputStream capsstream = StartupInitServlet.class.getResourceAsStream(
				"/sesconfig/wakeup_capabilities_start.xml");

		BufferedReader br = new BufferedReader(new InputStreamReader(capsstream));
		StringBuilder sb = new StringBuilder();
		while (br.ready()) {
			sb.append(br.readLine());
		}
		br.close();
		
		sb.append(sesurl);
		
		capsstream = StartupInitServlet.class.getResourceAsStream(
				"/sesconfig/wakeup_capabilities_end.xml");
		br = new BufferedReader(new InputStreamReader(capsstream));
		while (br.ready()) {
			sb.append(br.readLine());
		}
		br.close();
		
		return sb.toString();
	}


	private class WakeUpThread extends Thread {

		private String sesurl;
		private boolean running;
		private int wakeUpTime;
		private boolean firstRun = true;

		public WakeUpThread(String url, int time) {
			this.wakeUpTime = time;
			this.sesurl = url;
			this.running = true;
			this.setName("SES-WakeUp-Thread");
		}


		public void setRunning(boolean running) {
			this.running = running;
		}


		@Override
		public void run() {
			int errors = 0;
			while(this.running && errors < 10) {
				errors++;
				try {
					if (sendWakeUpPost()) {
						sendWakeUpNotification();
					}
				} catch (IOException e) {
					log(e.getMessage(), e);
				} catch (InterruptedException e) {
					log(e.getMessage(), e);
				} catch (XmlException e) {
					log(e.getMessage(), e);
				} catch (HttpClientException e) {
					log(e.getMessage(), e);
				}
			}
			
		}
		


		private boolean sendWakeUpPost() throws IOException, InterruptedException, XmlException {
			if (this.firstRun) {
				Thread.sleep(this.wakeUpTime);
				this.firstRun = false;
			}
			
			// send data
			int responseCode = 500;
			int errors = 0;
			while (responseCode != 200 && errors < 5) {
				errors++;
				try {
					log("WakeUp Try #"+errors);
					Thread.sleep(1000);
					
		            HttpClient httpClient = new SimpleHttpClient();
                    String payload = getGetCapabilitiesRequest(this.sesurl);
                    
                    HttpResponse response = httpClient.executePost(this.sesurl, payload, TEXT_XML);
                    responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                        continue;
                    }
                    
                    if (response.getEntity() == null) {
                        log("No content! Retry...");
                        continue;
                    }
                    
                    InputStream contentStream = response.getEntity().getContent();
                    XmlObject responseContent = XmlObject.Factory.parse(contentStream);
                    log("##response from SES:## "+ responseContent.xmlText());
                    setRunning(false);
                    return true;
                }
                catch (HttpClientException e) {
                    log(Thread.currentThread().getName()+"] Wakeup failed. Retrying... Exception was: " + e.getMessage());
                }
			}

			return false;
		}
		
		

        private void sendWakeUpNotification() throws HttpClientException, IOException {
            /*
             * send an initial wakeup notification -> main resources get initialized
             */
        	 HttpClient httpClient = new SimpleHttpClient();
             HttpResponse response = httpClient.executePost(this.sesurl, readNotification(), TEXT_XML);
             int responseCode = response.getStatusLine().getStatusCode();
             if (responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
            	 log("Could not send initial notification (HTTP response code "+ responseCode + ").");
             } else {
                 log("Succesfully send initial notification.");
             }
        }


		private String readNotification() throws IOException {
            BufferedReader capsstream = new BufferedReader(new InputStreamReader(
            		StartupInitServlet.class.getResourceAsStream(
                                "/sesconfig/wakeup_notification.xml")));

            StringBuilder sb = new StringBuilder();
            while (capsstream.ready()) {
                sb.append(capsstream.readLine());
            }
            capsstream.close();
            
            //send service URL (in <wsa.To> element)
            return sb.toString().replace("${ses_host}", this.sesurl).
                    replace("${now}", new DateTime().toString(ISODateTimeFormat.dateTime()));
		}


	}
	
}
