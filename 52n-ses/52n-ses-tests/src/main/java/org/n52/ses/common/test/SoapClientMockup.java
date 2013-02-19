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
package org.n52.ses.common.test;

import java.io.PrintWriter;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapClient;
import org.w3c.dom.Element;

public class SoapClientMockup implements SoapClient {

	@Override
	public int getSoapMonitorPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUsingSoapMonitor() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startSoapMonitor(int monitorPort) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSoapMonitor() {
		// TODO Auto-generated method stub

	}

	@Override
	public PrintWriter getTraceWriter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsingTrace() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTrace(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTraceWriter(PrintWriter arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Element[] send(EndpointReference src, EndpointReference dest,
			String wsaAction, Element[] body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] send(EndpointReference src, EndpointReference dest,
			String wsaAction, Element[] body, Element[] extraHeaders) {
		// TODO Auto-generated method stub
		return null;
	}

}
