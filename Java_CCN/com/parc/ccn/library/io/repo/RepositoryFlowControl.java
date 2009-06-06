package com.parc.ccn.library.io.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.stream.XMLStreamException;

import com.parc.ccn.CCNBase;
import com.parc.ccn.Library;
import com.parc.ccn.data.ContentName;
import com.parc.ccn.data.ContentObject;
import com.parc.ccn.data.query.BasicNameEnumeratorListener;
import com.parc.ccn.data.query.CCNInterestListener;
import com.parc.ccn.data.query.Interest;
import com.parc.ccn.data.security.SignedInfo.ContentType;
import com.parc.ccn.library.CCNFlowControl;
import com.parc.ccn.library.CCNLibrary;
import com.parc.ccn.library.CCNNameEnumerator;
import com.parc.ccn.network.daemons.repo.RepositoryInfo;

/**
 * Potential replacement for existing repo flow control (RepositoryProtocol)
 * that handles concurrent users better.
 * @author smetters
 *
 */
public class RepositoryFlowControl extends CCNFlowControl implements CCNInterestListener {

	protected static final int ACK_BLOCK_SIZE = 20;
	protected static final int ACK_INTERVAL = 128;

	protected boolean _bestEffort = true;
	protected int _blocksSinceAck = 0;
	protected int _ackInterval = ACK_INTERVAL;
	protected String _repoName = null;
	protected HashSet<Interest> _writeInterests = new HashSet<Interest>();
	protected CCNNameEnumerator _ackne;
	protected RepoAckHandler _ackHandler;

	public Interest handleContent(ArrayList<ContentObject> results,
			Interest interest) {
		
		Interest interestToReturn = null;
		for (ContentObject co : results) {
			if (co.signedInfo().getType() != ContentType.DATA)
				continue;
			RepositoryInfo repoInfo = new RepositoryInfo();
			try {
				repoInfo.decode(co.content());
				switch (repoInfo.getType()) {
				case INFO:
					_repoName = repoInfo.getLocalName();
					//_writeInterest = null;
					synchronized (this) {
						notify();
					}
					break;
				default:
					break;
				}
			} catch (XMLStreamException e) {
				Library.logger().info("XMLStreamException parsing RepositoryInfo: " + e.getMessage() + " from content object " + co.name() + ", skipping.");
			}
		}
		// So far, we seem never to have anything to return.
		return interestToReturn;
	}

	/**
	 * The names returned by NameEnumerator are only the 1 level names
	 * without prefix, but the names we are holding contain the basename
	 * so we reconstruct a full name here.
	 *
	 * @author rasmusse
	 *
	 */
	private class RepoAckHandler implements BasicNameEnumeratorListener {

		public int handleNameEnumerator(ContentName prefix, ArrayList<ContentName> names) {
			Library.logger().info("Enumeration response for " + names.size() + " children of " + prefix + ".");
			for (ContentName name : names)
				ack(new ContentName(prefix, name.component(0)));
			return names.size();
		}
	}

	public RepositoryFlowControl(ContentName name, CCNLibrary library) {
		super(name, library); // this does the first half of handleNameSpace
	}

	@Override
	public void addNameSpace(ContentName name) throws IOException {
		super.addNameSpace(name);
		clearUnmatchedInterests();	// Remove possible leftover interests from "getLatestVersion"
		ContentName repoWriteName = new ContentName(name, CCNBase.REPO_START_WRITE, CCNLibrary.nonce());

		Interest writeInterest = new Interest(repoWriteName);
		_library.expressInterest(writeInterest, this);
		_writeInterests.add(writeInterest);
		if (! _bestEffort) {
			_ackHandler = new RepoAckHandler();
			_ackne = new CCNNameEnumerator(_library, _ackHandler);
		}

		/*
		 * Wait for information to be returned from a repo
		 */
		synchronized (this) {
			boolean interrupted;
			do {
				try {
					interrupted = false;
					wait(getTimeout());
				} catch (InterruptedException e) {
					interrupted = true;
				}
			} while (interrupted);
		}
		if (_repoName == null)
			throw new IOException("No response from a repository");
	}

	/**
	 * Handle acknowledgement packet from the repo
	 * @param co
	 */
	public void ack(ContentName name) {
		synchronized (_holdingArea) {
			Library.logger().fine("Handling ACK " + name);
			if (_holdingArea.get(name) != null) {
				ContentObject co = _holdingArea.get(name);
				Library.logger().fine("CO " + co.name() + " acked");
				_holdingArea.remove(co.name());
				if (_holdingArea.size() < _highwater)
					_holdingArea.notify();
			}
		}
	}

	public void setBestEffort(boolean flag) {
		_bestEffort = flag;
	}

	public boolean flushComplete() {
		return _bestEffort ? true : _holdingArea.size() == 0;
	}

	public void afterPutAction(ContentObject co) throws IOException {
		if (! _bestEffort) {
			if (_holdingArea.size() > _ackInterval) {
				ContentName prefix = getNameSpace(co.name());
				_ackne.cancelPrefix(prefix);
				_ackne.registerPrefix(prefix);
			}
		} else {
			super.afterPutAction(co);
		}
	}

	public void beforeClose() throws IOException {
		_ackInterval = 0;
	}

	public void afterClose() throws IOException {
		// DKS don't actually want to cancel all the interests, only the
		// ones relevant to the data we've finished writing.
		//if (! _bestEffort)
		//	cancelInterests();
		if (!flushComplete()) {
			throw new IOException("Unable to confirm writes are stable: timed out waiting ack for " + _holdingArea.firstKey());
		}
	}

	public void cancelInterests() {
		for (ContentName prefix : _filteredNames) {
			_ackne.cancelPrefix(prefix);
		}
		for (Interest writeInterest : _writeInterests){
			_library.cancelInterest(writeInterest, this);
		}
	}
}
