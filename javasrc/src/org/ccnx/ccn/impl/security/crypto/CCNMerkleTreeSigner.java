package org.ccnx.ccn.impl.security.crypto;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.protocol.ContentObject;


public class CCNMerkleTreeSigner implements CCNAggregatedSigner {
	
	public void signBlocks(
			ContentObject [] contentObjects, 
			PrivateKey signingKey) throws InvalidKeyException, SignatureException, 
											 NoSuchAlgorithmException, IOException {
		
		// Generate the signatures for these objects. This sets the 
		// signatures as a side effect
		// DKS TODO remove side effect behavior.
		CCNMerkleTree tree = 
			new CCNMerkleTree(contentObjects, signingKey);
		Log.info("Signed tree of " + tree.numLeaves() + " leaves, " + tree.nodeCount() + " nodes.");
	}

}