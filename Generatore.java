package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

public class Generator {
	
	public Generator() {}
	
	public void generateOntology() throws IOException {
		
		String prefix="http://xmlns.com/foaf/0.1#";
		String objpre="http://www.w3.org/2002/07/owl#";
		Random rand = new Random();
		int  n = rand.nextInt(10) + 3;
		Model model = new LinkedHashModel();
		ValueFactory factory = SimpleValueFactory.getInstance();
		IRI georgeWBush = factory.createIRI(prefix+"George_W_Bush");
		List<String> entities = new ArrayList<String>();
		List<String> objectProperties = new ArrayList<String>();
		List<String> facts = new ArrayList<String>();
		for(int i=1;i<=n+1;i++) {
			IRI entity = factory.createIRI(prefix+"Bush"+i);
			entities.add(entity.toString());
			model.add(georgeWBush, OWL.SAMEAS, entity);
		}
		int  r = rand.nextInt(20) + n;
		int t;
		String pname;
		IRI pnameiri,ptypeiri;
		MyStringRandomGen msr = new MyStringRandomGen();
		for(int j=1;j<=r+1;j++) {
			t = rand.nextInt(3) + 1;
	        pname=msr.generateRandomString();
	        pnameiri = factory.createIRI(prefix+pname);
	        objectProperties.add(pnameiri.toString());
			switch(t) {
				case 1: {
					ptypeiri= factory.createIRI(objpre+"FunctionalProperty");
					model.add(pnameiri,RDF.TYPE,ptypeiri);
				}
				case 2: {
					ptypeiri= factory.createIRI(objpre+"InverseFunctionalProperty");
					model.add(pnameiri,RDF.TYPE,ptypeiri);
				}
				case 3: {
					ptypeiri= factory.createIRI(objpre+"SymmetricProperty");
					model.add(pnameiri,RDF.TYPE,ptypeiri);
				}
			default: ;
			List<String> temp = new ArrayList<String>();
			temp.add(pick(entities));
			temp.add(pick(entities));
			IRI t1 = factory.createIRI(temp.get(0));
			IRI t2 = factory.createIRI(temp.get(1));
			model.add(t1,pnameiri,t2);
			}
		}
		int f = rand.nextInt(n) * 3;
		String fname,temp2,oname;
		IRI fnameIri,onameIri,t1,t2;
		for(int y=0;y<=f+1;y++){
			fname=msr.generateRandomString();
			oname=msr.generateRandomString();
			fnameIri=factory.createIRI(prefix+fname);
			onameIri=factory.createIRI(prefix+oname);
			temp2=pick(entities);
			t1=factory.createIRI(temp2);
			facts.add(fnameIri.toString());
			model.add(t1,fnameIri,onameIri);
		}
		
		Model oracle = new LinkedHashModel();
		for(Statement s: model) {
			if(isIn(entities,s.getSubject()) && !isIn(entities,s.getObject())) {
				int p = rand.nextInt(100);
				if(p>49) oracle.add(georgeWBush,s.getPredicate(),s.getObject());
			}
		}
		System.out.println("Oracle size: "+oracle.size());
		FileOutputStream out = new FileOutputStream("E:\\Desktop\\Ontologies\\ontology.owl");
		FileOutputStream ora = new FileOutputStream("E:\\Desktop\\Ontologies\\oracle.owl");
		RDFWriter writer = Rio.createWriter(RDFFormat.RDFXML, out);
		RDFWriter writer2 = Rio.createWriter(RDFFormat.RDFXML, ora);
		try {
		  writer.startRDF();
		  writer2.startRDF();
		  for (Statement st: model) {
		    writer.handleStatement(st);
		  }
		  for (Statement s: oracle) {
			    writer2.handleStatement(s);
			  }
		  writer.endRDF();
		  writer2.endRDF();
		}
		catch (RDFHandlerException e) {
		 // oh no, do something!
		}
		finally {
		  out.close();
		  ora.close();
		}
	}

	private String pick(List<String> e) {
		Random rand = new Random();
		int i = rand.nextInt(e.size());
		return e.get(i);
	}

	private <T> boolean isIn(List<String> e, T r) {
		for(String s : e) {
			if(Objects.equals(s,r.toString())) return true;
		}
		return false;
	}

	private List<String> pick(List<String> e, List<String> p) {
		Random rand = new Random();
		int i,x;
		do {
			i = rand.nextInt(e.size());
			x = rand.nextInt(e.size());
		} while(i==x);
		int j= rand.nextInt(p.size());
		List<String> output = new ArrayList<String>();
		output.add(e.get(i));
		output.add(p.get(j));
		output.add(e.get(x));
		return output;
	}
	
}
