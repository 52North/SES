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

import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.muse.ws.resource.WsResourceCapability;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.muse.ws.resource.metadata.MetadataDescriptor;
import org.apache.muse.ws.resource.properties.ResourcePropertyCollection;
import org.apache.muse.ws.resource.properties.get.faults.InvalidResourcePropertyQNameFault;
import org.apache.muse.ws.resource.properties.listeners.PropertyChangeApprover;
import org.apache.muse.ws.resource.properties.listeners.PropertyChangeListener;
import org.apache.muse.ws.resource.properties.listeners.PropertyReadListener;
import org.apache.muse.ws.resource.properties.schema.ResourcePropertiesSchema;
import org.apache.muse.ws.resource.properties.set.SetRequest;
import org.apache.muse.ws.resource.properties.set.faults.InvalidModificationFault;
import org.apache.muse.ws.resource.properties.set.faults.SetResourcePropertyRequestFailedFault;
import org.apache.muse.ws.resource.properties.set.faults.UnableToModifyResourcePropertyFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResourcePropertyCollectionMockup implements
		ResourcePropertyCollection {

	@Override
	public Element[] getMultipleResourceProperties(QName[] arg0)
			throws InvalidResourcePropertyQNameFault, BaseFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] getResourceProperty(QName arg0)
			throws InvalidResourcePropertyQNameFault, BaseFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyAsObject(QName arg0, Class arg1)
			throws InvalidResourcePropertyQNameFault, BaseFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getResourcePropertyDocument() throws BaseFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element putResourcePropertyDocument(Element arg0) throws BaseFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResourceProperties(SetRequest arg0)
			throws InvalidResourcePropertyQNameFault, InvalidModificationFault,
			SetResourcePropertyRequestFailedFault,
			UnableToModifyResourcePropertyFault, BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteResourceProperty(QName arg0) throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertResourceProperty(QName arg0, Object[] arg1)
			throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateResourceProperty(QName arg0, Object[] arg1)
			throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertOrUpdate(QName arg0, Object arg1) throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertOrUpdate(QName arg0, Object[] arg1) throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteResourceProperty(QName arg0, Object arg1)
			throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getSecurityToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertResourceProperty(QName arg0, Object[] arg1, Object arg2)
			throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateResourceProperty(QName arg0, Object[] arg1, Object arg2)
			throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChangeApprover(PropertyChangeApprover arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addReadListener(PropertyReadListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<?> getChangeApprovers(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<?> getChangeListeners(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<?> getReadListeners(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeChangeApprover(PropertyChangeApprover arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeChangeListener(PropertyChangeListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeReadListener(PropertyReadListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyMetadata() throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public MetadataDescriptor getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMetadata(MetadataDescriptor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void validateMetadata() throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCapability(WsResourceCapability arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public WsResourceCapability getCapability(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourcePropertiesSchema getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPropertyDefinition(QName arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSchema(ResourcePropertiesSchema arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void validateSchema() throws BaseFault {
		// TODO Auto-generated method stub

	}

	@Override
	public Element toXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element toXML(Document arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
