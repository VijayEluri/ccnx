package org.ccnx.ccn.impl.encoding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.ccnx.ccn.impl.support.Log;


public class BinaryXMLDictionary {
	
	// Should not necessarily tie this to CCN...
	protected static String DEFAULT_DICTIONARY_RESNAME = "tagname.csvdict";
	
	protected String _dictionaryFileName;
	protected HashMap<String,Long> _encodingDictionary = new HashMap<String,Long>();
	protected HashMap<Long,String> _decodingDictionary = new HashMap<Long,String>();
	
	protected static BinaryXMLDictionary DEFAULT_DICTIONARY = null;
	
	static {
		DEFAULT_DICTIONARY = new BinaryXMLDictionary();
	}
	
	public static BinaryXMLDictionary getDefaultDictionary() {
		return DEFAULT_DICTIONARY;
	}
	
	public BinaryXMLDictionary(String dictionaryFile) throws IOException {
		loadDictionaryFile(dictionaryFile);
	}

	public BinaryXMLDictionary() {
		try {
			loadDictionaryFile(DEFAULT_DICTIONARY_RESNAME);
		} catch (IOException fe) {
			Log.warning("Cannot parse default CCN encoding dictionary: " + DEFAULT_DICTIONARY_RESNAME + ":" + 
					fe.getMessage());
			
		}
	}
	
	public long encodeTag(String tag) {
		Long value = _encodingDictionary.get(tag);
		if (null == value)
			return -1;
		return value.longValue();
	}
	
	public String decodeTag(long tagVal) {
		String tag = _decodingDictionary.get(Long.valueOf(tagVal));
		return tag;
	}
	
	// DKS TODO -- do attributes use the same dictionary entries?
	public long encodeAttr(String attr) {
		Long value = _encodingDictionary.get(attr);
		if (null == value)
			return -1;
		return value.longValue();
	}
	
	public String decodeAttr(long tagVal) {
		String tag = _decodingDictionary.get(Long.valueOf(tagVal));
		return tag;
	}

	protected void loadDictionaryFile(String dictionaryFile) throws IOException {
		
		if (null == dictionaryFile) 
			throw new IOException("BinaryXMLDictionary: dictionary file name cannot be null!");
		
		InputStream in = getClass().getResourceAsStream(dictionaryFile);
		
		if (null == in) {
			throw new IOException("BinaryXMLDictionary: getResourceAsStream cannot open resource file: " + dictionaryFile + ".");
		}
		BufferedReader reader = 
			new BufferedReader(new InputStreamReader(in));
		
		String line = null;
		
		while (reader.ready()) {
			line = reader.readLine();
			String [] parts = line.split(",");
			
			// Format: <num>,<name>[,<modifier>]  where <modifier> is one of Deprecated or Obsolete
			if (parts.length > 3) {
				if (parts.length != 0) // if 0, just empty line
					Log.info("Dictionary: " + dictionaryFile + ":  Cannot parse dictionary line: " + line);
				continue;
			} 
			
			if ((parts.length == 3) && ((parts[2].equals("Deprecated") || (parts[2].equals("Obsolete"))))) {
				continue; // skip old stuff
			}
			Long value = Long.valueOf(parts[0]);
			String tag = parts[1];
			
			_encodingDictionary.put(tag, value);
			_decodingDictionary.put(value, tag);
		}
		
	}
}
