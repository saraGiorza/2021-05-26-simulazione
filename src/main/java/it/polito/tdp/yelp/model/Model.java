package it.polito.tdp.yelp.model;

import java.util.*;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {

	private YelpDao dao = new YelpDao();
	private SimpleDirectedWeightedGraph<Business, DefaultWeightedEdge> grafo;

	public List<String> citta() {
		List<String> citta = dao.getCities();
		Collections.sort(citta);
		return citta;
	}

	public void creaGrafo(String citta, int anno) {
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		// vertici
		List<Business> vertici = dao.getLocali(citta, anno);
		Graphs.addAllVertices(grafo, vertici);
		
		//archi
		for(Business b1: grafo.vertexSet()) {
			for(Business b2: grafo.vertexSet()) {
				if(!b1.equals(b2)) {
					float media1 = dao.getMediaReviews(b1.getBusinessId(), anno);
					float media2 = dao.getMediaReviews(b2.getBusinessId(), anno);
					float differenza = media1-media2;
					
					//si aggiunge l'arco solo se la media e' positiva
					//in questo modo non si crea l'arco doppio
					if(differenza > 0) {
						Graphs.addEdge(grafo, b2, b1, differenza);						
					}					
				}
			}
		}
	}

	public String infoGrafo() {
		// se il grafo non è stato creato ritorna stringa vuota
		try {
			return "Grafo creato con " + grafo.vertexSet().size() + " vertici " + grafo.edgeSet().size() + " archi";
		} catch (NullPointerException npe) {
			return "";
		}
	}
	
	public String migliore() {
		
		float best = 0;
		Business locale = null;
		for(Business b: grafo.vertexSet()) {
			float media = 0;
			Set<DefaultWeightedEdge> entranti = grafo.incomingEdgesOf(b);
			Set<DefaultWeightedEdge> uscenti = grafo.outgoingEdgesOf(b);
			
			for(DefaultWeightedEdge e: entranti) {
				media+= grafo.getEdgeWeight(e);				
			}
			for(DefaultWeightedEdge u: uscenti) {
				media-= grafo.getEdgeWeight(u);				
			}
			
			if(media > best) {
				best = media;
				locale = b;
			}			
		}
		
		return locale.getBusinessName();
	}

}
