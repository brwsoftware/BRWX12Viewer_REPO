package com.brwsoftware.brwx12viewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.brwsoftware.brwx12library.X12ConverterXml;
import com.brwsoftware.brwx12library.X12Exception;
import com.brwsoftware.brwx12library.X12Schema;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class X12TransformManager {

	//Singleton implementation
	private static X12TransformManager instance;
	private X12TransformManager() {		
	}
    public static X12TransformManager getInstance(){
        if(instance == null){
        	instance = new X12TransformManager();
        }
        return instance;
    }
    
	public static final class X12Transform {
		private final SimpleStringProperty tsid = new SimpleStringProperty();
		private final SimpleStringProperty impl = new SimpleStringProperty();
		private final SimpleStringProperty schemaPathName = new SimpleStringProperty();
		private final SimpleStringProperty xsltPathName = new SimpleStringProperty();
		
		private X12Schema x12Schema;
		private Transformer transformer;
		
		public X12Transform() {			
		}

		public String getTSID() {
			return tsid.get();
		}

		public void setTSID(String tsid) {
			this.tsid.set(tsid);
		}
		
		public StringProperty tsidProperty() {
			return tsid;
		}
		
		public String getImpl() {
			return impl.get();
		}

		public void setImpl(String impl) {
			this.impl.set(impl);
		}
		
		public StringProperty implProperty() {
			return impl;
		}

		public String getSchemaPathName() {
			return schemaPathName.get();
		}

		public void setSchemaPathName(String schemaPathName) {
			this.schemaPathName.set(schemaPathName);
		}
		
		public StringProperty schemaPathNameProperty() {
			return schemaPathName;
		}
		
		public String getXsltPathName() {
			return xsltPathName.get();
		}

		public void setXsltPathName(String xsltPathName) {
			this.xsltPathName.set(xsltPathName);
		}
		
		public StringProperty xsltPathNameProperty() {
			return xsltPathName;
		}
		
		public X12Schema getX12Schema() throws XMLStreamException, X12Exception, FileNotFoundException, IOException {
			if(x12Schema == null) {
				try (FileInputStream theSchema = new FileInputStream(getSchemaPathName());) {
					X12Schema tmp = new X12Schema();
					tmp.addTransactionSet(theSchema);
					x12Schema = tmp;
				} 
			}
			return x12Schema;
		}

		public Transformer getTransformer() throws FileNotFoundException, IOException, TransformerConfigurationException {
			if(transformer == null) {
				try (FileInputStream theXslt = new FileInputStream(getXsltPathName());){
					StreamSource theXsltSource = new StreamSource(theXslt);
					
					TransformerFactory transFactory= TransformerFactory.newInstance();
					transFactory.setURIResolver(new URIResolver() {
						public Source resolve(String href, String base) throws TransformerException {
							String fileName = getXsltPathName();
							String theDir = fileName.toString().substring(0, fileName.lastIndexOf(File.separator));

							File f = new File(theDir, href);
							return new StreamSource(f.toString());
						}
					});			

					Transformer tmp = transFactory.newTransformer(theXsltSource);
					transformer = tmp;
				}
			}
				
			return transformer;
		}
		
		void transformHtml(InputStream inX12, OutputStream outHtml) throws TransformerException, XMLStreamException, IOException, X12Exception {
			try (ByteArrayOutputStream theXml = new ByteArrayOutputStream();) {
				
				X12Schema x12Schema = getX12Schema();

				X12ConverterXml theConverter = new X12ConverterXml();

				theConverter.convert(inX12, theXml, x12Schema);
				
				StreamSource theXmlSource = new StreamSource(new ByteArrayInputStream(theXml.toByteArray())); 

				Transformer transformer = getTransformer();
				transformer.transform(theXmlSource, new StreamResult(outHtml));
			}
		}
	}
	
	private class Key {
		private String key;
		
		public Key(X12Transform obj) {
			makeKey(obj.getTSID(), obj.getImpl());			
		}
		public Key(String tsid, String impl) {
			makeKey(tsid, impl);
		}
		private void makeKey(String tsid, String impl) {
			key = String.format("%s_%s", tsid, impl);
		}
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			return key.equals(((Key)obj).key);
		}
	}
	
	private final HashMap<Key, X12Transform> mapX12Transform = new HashMap<Key, X12Transform>();
	
	public void add(X12Transform obj) {
		mapX12Transform.put(new Key(obj), obj);
	}
	
	public void remove(String tsid, String impl) {
		mapX12Transform.remove(new Key(tsid, impl));
	}
	
	private boolean has(String tsid, String impl) {
		return mapX12Transform.containsKey(new Key(tsid, impl));
	}
	
	private X12Transform get(String tsid, String impl) {
		return mapX12Transform.get(new Key(tsid, impl));
	}
	
	private void loadItem(XMLStreamReader reader) throws XMLStreamException {
		X12Transform x12Transform = new X12Transform();
		
		x12Transform.setTSID(reader.getAttributeValue(null, "tsid"));
		x12Transform.setImpl(reader.getAttributeValue(null, "impl"));
		x12Transform.setSchemaPathName(reader.getAttributeValue(null, "schema"));
		x12Transform.setXsltPathName(reader.getAttributeValue(null, "xslt"));
		
		add(x12Transform);
	}

	public boolean hasBestMatch(String tsid, String impl) {
		X12Transform trfmX12 = getBestMatch(tsid, impl);
		return (trfmX12 != null);
	}
	
	public X12Transform getBestMatch(String tsid, String impl) {
		X12Transform trfmX12 = null;
		
		if(has(tsid, impl)) {
			trfmX12 = get(tsid, impl);
		}
		else if (impl != null && impl.length() > 10) {
			String implShort = impl.substring(0, 10);
			if(has(tsid, implShort)) {
				trfmX12 = get(tsid, implShort);
			}
		}
		
		if(trfmX12 == null) {
			if(has(tsid, null)) {
				trfmX12 = get(tsid, null);
			}
		}
		return trfmX12;
	}

	public void loadFromXml(String xmlPathName) throws XMLStreamException, IOException {
		loadFromXml(new File(xmlPathName));
	}

	public void loadFromXml(File theFile) throws XMLStreamException, IOException {
		mapX12Transform.clear();
		try (FileInputStream theStream = new FileInputStream(theFile)) {
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(theStream);
			boolean hasRoot = false;
			String localName;
			while (reader.hasNext()) {
				int eventType = reader.next();
				switch (eventType) {
				case XMLStreamReader.START_ELEMENT:
					localName = reader.getLocalName();
					if(localName.compareToIgnoreCase("x12transform") == 0) {
						hasRoot = true;
					}
					else if(hasRoot && localName.compareToIgnoreCase("item") == 0) {
						loadItem(reader);
					}
					break;
				}
			}
			reader.close();
		}
	}
	
	public void saveAsXml(File theFile) throws FileNotFoundException, IOException, XMLStreamException, FactoryConfigurationError {
		try (FileOutputStream theStream = new FileOutputStream(theFile)) {
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(theStream);
			
			writer.writeStartDocument();
			writer.writeStartElement("x12transform");
			
			for(X12Transform trfm : mapX12Transform.values()) {
				writer.writeStartElement("item");
				writer.writeAttribute("tsid", trfm.getTSID());
				writer.writeAttribute("impl", trfm.getImpl());
				writer.writeAttribute("schema", trfm.getSchemaPathName());
				writer.writeAttribute("xslt", trfm.getXsltPathName());
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.close();
		}
	}

	public ArrayList<X12Transform> getArrayList() {
		return new ArrayList<X12Transform>(mapX12Transform.values());
	}
}
