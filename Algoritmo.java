package org.example;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import com.google.common.collect.Lists;

public class Algorithm {
	
	public Algorithm() {}
	
	List<String> subjects = new ArrayList<String>();
	List<String> confirmed = new ArrayList<String>();
	List<String> couples = Lists.newArrayList();
	List<String> conditions = Lists.newArrayList();
	List<String> choosenCase = new ArrayList<String>();
	List<String> confidence = new ArrayList<String>();
	
	public void EntityLinking() {
		
		JFrame frame = new JFrame();
		FileDialog ontology = new FileDialog( frame, "Choose the ontology", FileDialog.LOAD);
		ontology.setDirectory("C:\\");
		ontology.setFile("*.owl");
		ontology.setVisible(true);
		String filename = ontology.getFile();
		if (filename == null) {
			System.out.println("Error opening file");
			return;
		}
		
		FileDialog oracleFile = new FileDialog( frame, "Choose the oracle", FileDialog.LOAD);
		oracleFile.setDirectory("C:\\");
		oracleFile.setFile("*.owl");
		oracleFile.setVisible(true);
		String oracleFilename = oracleFile.getFile();
		if (oracleFilename == null) {
			System.out.println("Error opening file");
			return;
		}
		
		Model model = new LinkedHashModel();
		Model oracle = new LinkedHashModel();
		RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
		RDFParser rdfOracleParser = Rio.createParser(RDFFormat.RDFXML);
		rdfParser.setRDFHandler(new StatementCollector(model));
		rdfOracleParser.setRDFHandler(new StatementCollector(oracle));
		FileInputStream ontologyfile=null;
		FileInputStream oraclefile=null;
		try {
			ontologyfile=new FileInputStream(ontology.getDirectory()+filename);
			oraclefile=new FileInputStream(oracleFile.getDirectory()+oracleFilename);
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			   rdfParser.parse(ontologyfile, "");
			   rdfOracleParser.parse(oraclefile, "");
			}
			catch (IOException e) {
			  // handle IO problems (e.g. the file could not be read)
			}
			catch (RDFParseException e) {
			  // handle unrecoverable parse error
			}
			catch (RDFHandlerException e) {
			  // handle a problem encountered by the RDFHandler
			}
			finally {
			  try {
				ontologyfile.close();
				oraclefile.close();
			  } 
			  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
			}
		ValueFactory factory = SimpleValueFactory.getInstance();
		String prefix="http://xmlns.com/foaf/0.1#";
		JFrame frame2 = new JFrame();
		String inputString = JOptionPane.showInputDialog(frame2,"Enter the entity name:",null);
		IRI entity = factory.createIRI(prefix+inputString);
		for (Statement statement1: model.filter(entity, OWL.SAMEAS, null)) {
			subjects.add(statement1.getObject().toString().split("#")[1]);
		}
		for (Statement statement2: model.filter(null, OWL.SAMEAS, entity)) {
			subjects.add(statement2.getSubject().toString().split("#")[1]);
		}
		for(String sub : subjects) {
			confidence.add(sub);
			confidence.add(randomConfidence());
		}
		String[] predicateToString;
		boolean symmcheck;
		for(String element : subjects) {
			IRI B = factory.createIRI(prefix+element);
			//System.out.println("Evaluating "+element+"...");
			for (Statement statement: model.filter(B, null, null)) {
				IRI predicate=statement.getPredicate();
				IRI C= (IRI) statement.getObject();
				if(isIn(subjects,C.toString().split("#")[1])) {
					//System.out.print("Found statement: "+B+"---"+predicate+"---"+C);
					predicateToString=predicate.toString().split("#");
					symmcheck=false;
					switch (predicateToString[1]) {
		            	case "FunctionalProperty":  
		            		for (Statement s1: model.filter(B, predicate,entity)) {
		            			//georgeWBush===C
		            			String c =C.toString().split("#")[1];
		            			if(!confirmed(c)) confirmed.add(c);
		            			for (Statement s3 : model.filter(C, null, null)) {
		            				if(!Objects.equals(s3.getPredicate(),OWL.SAMEAS)) {
		            					oracle.add(s3);
		            				}
		            			}
		            		}
		            	case "InverseFunctionalProperty":
		            		for (Statement s2: model.filter(entity, predicate, C)) {
		            			//georgeWBush===B
		            			// posso dire con certezza che georgeWBush sameAS B, e posso estendere
		            			// la conoscenza dell'oracolo con le datatypeProperty di B
		            			String b = B.toString().split("#")[1];
		            			if(!confirmed(b)) confirmed.add(b);
		            			for (Statement s4 : model.filter(B, null, null)) {
		            				if(!Objects.equals(s4.getPredicate(),OWL.SAMEAS)) {
		            					oracle.add(s4);
		            				}
		            			}
		            		}
		            	case "SymmetricProperty": symmcheck=true;
		            		
		            default: break;
					}
					if(!symmcheck) {
						String subj = B.toString().split("#")[1];
	            		String obj = C.toString().split("#")[1];
	            		String cond = subj+"@"+obj;
	            		conditions.add(cond);
					}
				}
			}
		}
		String X,Y;
		for(String k : conditions) {
			X=k.split("@")[0];
			Y=k.split("@")[1];
			if(isIn(confirmed,X)) {
				subjects.remove(Y);
			}
			if(isIn(confirmed,Y)) {
				subjects.remove(X);
			}
		}
		for(String s : confirmed) {
			subjects.remove(s);
			System.out.println(entity+" sameAs "+s);
		}

		int c;
		IRI iri;
		for(String e : subjects) {
			iri=factory.createIRI(prefix+e);
			c=whatIf(entity,iri,model,oracle);
			couples.add(e);
			couples.add(Integer.toString(c));
		}
		List<String> tempCouples=new ArrayList<String>();
		for(String s: couples) {
			tempCouples.add(s);
		}
		List<String> orderedCouples =order(couples);
		couples=tempCouples;
		List<String> firstCandidate =build(couples);
		if(firstCandidate.size()==2) {
			choosenCase.add(firstCandidate.get(0));
			choosenCase.add(firstCandidate.get(1));
			subjects.remove(firstCandidate.get(0));
		}
		else {
			List<String> candidate= chooseCandidate(firstCandidate);
			choosenCase.add(candidate.get(0));
			choosenCase.add(candidate.get(1));
			subjects.remove(candidate.get(0));
		}
		for(String candidate: subjects) {
			if(consistent(candidate)) {
				choosenCase.add(candidate);
				int value=getIntegerValue(candidate);
				choosenCase.add(Integer.toString(value));
			}
		}
		List<String> output = Lists.newArrayList();
		output=order(choosenCase);
		int size = oracle.filter(entity,null,null).size();
		int value;
		for(int i=0;i<output.size();i+=2){
			value=Integer.parseInt(output.get(i+1));
			if (size>0) {
			System.out.println("Entity "+output.get(i)+" is linked to "+entity+
	    			" with "+output.get(i+1)+" statements ("+(100*value)/size+"%)");
			}
		}
	}

	private List<String> chooseCandidate(List<String> orderedCouples) {
		List<String> arr = Lists.newArrayList();
		String candidate=orderedCouples.get(0);
		double confidence=getConfidence(orderedCouples.get(1));
		double c;
		for(int i=3;i<orderedCouples.size()-2;i+=2) {
			c=getConfidence(orderedCouples.get(i));
			if(c>confidence) {
				confidence=c;
				candidate=orderedCouples.get(i-1);
			}
		}
		arr.add(candidate);
		arr.add(Integer.toString(getIntegerValue(candidate)));
		return arr;
	}

	private List<String> build(List<String> orderedCouples) {
		List<String> returnArray = new ArrayList<String>();
		returnArray.add(orderedCouples.get(0));
		returnArray.add(orderedCouples.get(1));
		int value=Integer.parseInt(orderedCouples.get(1));
		for(int i=3;i<orderedCouples.size()-2;i+=2) {
			if(Integer.parseInt(orderedCouples.get(i))==value) {
				returnArray.add(orderedCouples.get(i-1));
				returnArray.add(orderedCouples.get(i));
			}
			else break;
		}
		return returnArray;
	}

	private List<String> order(List<String> arr) {
		List<String> temparr=arr;
		List<String> returnArray = new ArrayList<String>();
		int position;
		int csize=temparr.size();
		String str1,str2;
		for(int k=0;k<csize;k++) {
			if(temparr.size()>1) {
				position=max(temparr);
				str1=temparr.get(position-1);
				str2=temparr.get(position);
				returnArray.add(str1);
				returnArray.add(str2);
				temparr.remove(position);
				temparr.remove(position-1);
			}
		}
		return returnArray;
	}

	private double getConfidence(String z) {
		for(int i=0;i<confidence.size();i+=2){
			if(Objects.equals(confidence.get(i),z)) return Double.parseDouble(confidence.get(i+1));
		}
		return -1;
	}

	private int getIntegerValue(String z) {
		for(int i=0;i<couples.size();i+=2){
			if(Objects.equals(couples.get(i),z)) return Integer.valueOf(couples.get(i+1));
		}
		return -1;
	}

	private boolean consistent(String z) {
		String c1,c2;
		for(String elem : choosenCase) {
			c1=elem+"@"+z;
			c2=z+"@"+elem;
			if(isIn(conditions,c1)) return false;
			if(isIn(conditions,c2)) return false;
		}
		return true;
	}

	private String randomConfidence() {
		Random rand = new Random();
		double n = rand.nextDouble();
		String p = Double.toString(n);
		String n1="";
		n1+=p.charAt(0);
		n1+=p.charAt(1);
		n1+=p.charAt(2);
		return n1;
	}

	private int max(List<String> c) {
		int max=-1;
		int pos=1;
		int v;
		for(int i=1;i<c.size();i+=2) {
			v=Integer.parseInt(c.get(i));
			if(v>max) {
				max=v;
				pos=i;
			}
		}
		return pos;
	}

	private int whatIf(IRI entity,IRI B, Model model, Model oracle) {
		int counter=0;
		for(Statement s:model.filter(B, null, null)) {
			if(oracle.contains(entity, s.getPredicate(), s.getObject()) && !Objects.equals(s.getObject().toString(),"http://www.w3.org/2002/07/owl#NamedIndividual")) counter+=1;
		}
		return counter;
	}

	private boolean confirmed(String element) {
		for(String subject : confirmed) {
			if(Objects.equals(element,subject)) {
				return true;
			}
		}
		return false;
	}

	private boolean isIn(List<String> subjects2, String object) {
		for(String subject : subjects2) {
			if(Objects.equals(object,subject)) return true;
		}
		return false;
	}
}