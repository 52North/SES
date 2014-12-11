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
package org.n52.ses.wsn.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.apache.xmlbeans.XmlException;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.wsn.NotificationMessageImpl;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class NotificationMessageImplSerializationTest {

	@Test
	public void testSerialization() throws XmlException, IOException, SoapFault, ClassNotFoundException, SAXException {
		NotificationMessage n = createMessage();
		
		NotificationMessageImpl m = new NotificationMessageImpl(n);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(m);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object om = ois.readObject();
		
		Assert.assertTrue(om instanceof NotificationMessageImpl);
		
		NotificationMessageImpl onm = (NotificationMessageImpl) om;
		
		Assert.assertTrue(m.equals(onm));
		
		NotificationMessage nm1 = (NotificationMessage) m.getNotificationMessage();
		NotificationMessage nm2 = (NotificationMessage) onm.getNotificationMessage();
		
		Iterator<?> it = nm1.getMessageContentNames().iterator();
		while (it.hasNext()) {
			QName qn = (QName) it.next();
			String nm1Content = XmlUtils.toString(nm1.getMessageContent(qn));
			String nm2Content = XmlUtils.toString(nm2.getMessageContent(qn));
			
			Diff myDiff = new Diff(nm1Content, nm2Content);
			
			Assert.assertTrue(myDiff.identical());
		}
		
	}
	
	private NotificationMessage createMessage() throws IOException, SAXException {
		SimpleNotificationMessage result = new SimpleNotificationMessage();
		result.addMessageContent(createAIXMBasicMessage());
		return result;
	}

	private Element createAIXMBasicMessage() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("aixmBasicMessage.xml")).getDocumentElement();
	}
	
}