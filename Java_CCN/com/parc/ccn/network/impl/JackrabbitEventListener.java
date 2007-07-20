package com.parc.ccn.network.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import com.parc.ccn.Library;
import com.parc.ccn.data.ContentName;
import com.parc.ccn.data.ContentObject;
import com.parc.ccn.data.query.CCNQueryListener;
import com.parc.ccn.data.query.CCNQueryDescriptor;
import com.parc.ccn.data.security.ContentAuthenticator;

/**
 * Wraps a CCNQueryListener and links it to Jackrabbit events.
 * @author smetters
 *
 */
class JackrabbitEventListener implements EventListener {

	protected JackrabbitCCNRepository _repository;
	protected CCNQueryListener _listener;
	protected int _events;

	public JackrabbitEventListener(JackrabbitCCNRepository repository,
									CCNQueryListener l, int events) {
		if (null == repository) 
			throw new IllegalArgumentException("JackrabbitEventListener: repository cannot be null!");
		_repository = repository;
		if (null == l) 
			throw new IllegalArgumentException("JackrabbitEventListener: listener cannot be null!");
		_listener = l;
		_events = events;
	}
	
	JackrabbitCCNRepository repository() { return _repository; }
	
	public int events() { return _events; }
	public CCNQueryListener listener() { return _listener; }
	public CCNQueryDescriptor queryDescriptor() { return _listener.getQuery(); }

	public ContentName queryName() {
		CCNQueryDescriptor descriptor = _listener.getQuery();
		if (null == descriptor)
			return null;
		return descriptor.name();
	}
	
	public ContentAuthenticator queryAuthenticator() {
		CCNQueryDescriptor descriptor = _listener.getQuery();
		if (null == descriptor)
			return null;
		return descriptor.authenticator();
	}
	
	public CCNQueryListener.CCNQueryType queryType() {
		CCNQueryDescriptor descriptor = _listener.getQuery();
		if (null == descriptor)
			return null;
		return descriptor.type();
	}
	
	public void onEvent(EventIterator events) {
		
		ArrayList<ContentObject> nodesFound = new ArrayList<ContentObject>();
		
		while (events.hasNext()) {
			
			Event event = events.nextEvent();

			// Start by handling all events together
			switch(event.getType()) {
			case Event.NODE_ADDED:
				// there seems to be a bug in jackrabbit that causes the 
				// NODE_ADDED event not to be delivered reliably.
				// We _do_ seem to get, however, a notification that the
				// jcr:primaryType property is being set. This should only
				// happen once (at node creation time),
				// at least in our application, so we'll use that instead
			case Event.PROPERTY_ADDED:
			case Event.PROPERTY_CHANGED:
			case Event.PROPERTY_REMOVED:
					try {
						Node affectedNode;
						try {
							affectedNode = repository().getNode(event.getPath());
							ContentObject co = repository().getContentObject(affectedNode);
							if (_listener.matchesQuery(co)) {
								nodesFound.add(co);
							}
						} catch (PathNotFoundException e) {
							Library.logger().warning("Cannot find node corresponding to generated event at path: " + event.getPath());
							Library.logStackTrace(Level.WARNING, e);
							continue;
						} catch (RepositoryException e) {
							Library.logger().warning("Error retrieving node corresponding to generated event at path: " + event.getPath());
							Library.logStackTrace(Level.WARNING, e);
							continue;
						} catch (IOException e) {
							Library.logger().warning("Error retrieving content object information corresponding to generated event at path: " + event.getPath());
							Library.logStackTrace(Level.WARNING, e);
							continue;					
						}
					} catch (RepositoryException e) {
						Library.logger().warning("Error: can't even retrieve path associated with event " + event);
						Library.logStackTrace(Level.WARNING, e);
						continue;
					}
			}
		}
		_listener.handleResults(nodesFound);
	}

	public String getQueryPath() {
		// Needs to combine both the name in the query
		// and the query type into an effective jackrabbit
		// query path. Could also incorporate publisher features,
		// etc...
		// TODO Auto-generated method stub
		return null;
	}
}
