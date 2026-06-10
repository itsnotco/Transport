package factory;

import model.*;
import network.NetworkGraph;

import java.util.Set;

public class GraphFactory {

    public static NetworkGraph create() {

        Station gareCentrale = new Station("ST_CENT", "Gare Centrale", 5000, VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO);
        Station saintRoch = new Station("ST_ROCH", "Saint-Roch", 3500, VehicleType.TRAM, VehicleType.METRO);
        Station republique = new Station("ST_REP", "République", 3000, VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO);
        Station universite = new Station("ST_UNIV", "Université", 2500, VehicleType.TRAM, VehicleType.METRO);
        Station aeroport = new Station("ST_AERO", "Aéroport", 6000, VehicleType.METRO);
        Station bellevue = new Station("ST_BELL", "Bellevue", 1500, VehicleType.TRAM, VehicleType.METRO);
        Station victorHugo = new Station("ST_HUGO", "Victor Hugo", 1200, VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO);
        Station prefecture = new Station("ST_PREF", "Préfecture", 1800, VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO);
        Station portMaritime = new Station("ST_PORT", "Port Maritime", 2200, VehicleType.TRAM, VehicleType.METRO);
        Station technopole = new Station("ST_TECH", "Technopôle", 2000, VehicleType.METRO, VehicleType.TRAM);
        Station lesLilas = new Station("ST_LILAS", "Les Lilas", 800, VehicleType.TRAM);
        Station moulinVert = new Station("ST_MOUL", "Moulin Vert", 700, VehicleType.TRAM);
        Station jardinPublic = new Station("ST_JARD", "Jardin Public", 1000, VehicleType.TRAIN, VehicleType.TRAM);
        Station parcSud = new Station("ST_PARC", "Parc Sud", 1200, VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO);
        Station egliseNeuve = new Station("ST_EGL", "Église Neuve", 900, VehicleType.TRAM);
        Station vieuxBourg = new Station("ST_BOURG", "Vieux Bourg", 700, VehicleType.TRAM);
        Station lesAcacias = new Station("ST_ACAC", "Les Acacias", 1000, VehicleType.TRAM);
        Station innovation = new Station("ST_INNO", "Innovation", 1800, VehicleType.METRO, VehicleType.TRAM);
        Station citeAdmin = new Station("ST_CITE", "Cité Administrative", 1500, VehicleType.METRO);
        Station arsenal = new Station("ST_ARS", "Arsenal", 1400, VehicleType.METRO);

        NetworkGraph graph = new NetworkGraph();

        graph.addStation(gareCentrale);
        graph.addStation(saintRoch);
        graph.addStation(republique);
        graph.addStation(universite);
        graph.addStation(aeroport);
        graph.addStation(bellevue);
        graph.addStation(victorHugo);
        graph.addStation(prefecture);
        graph.addStation(portMaritime);
        graph.addStation(technopole);
        graph.addStation(lesLilas);
        graph.addStation(moulinVert);
        graph.addStation(jardinPublic);
        graph.addStation(parcSud);
        graph.addStation(egliseNeuve);
        graph.addStation(vieuxBourg);
        graph.addStation(lesAcacias);
        graph.addStation(innovation);
        graph.addStation(citeAdmin);
        graph.addStation(arsenal);

        graph.addConnection(new Connection(aeroport, gareCentrale, 12.0));
        graph.addConnection(new Connection(gareCentrale, saintRoch, 4.0));
        graph.addConnection(new Connection(saintRoch, bellevue, 3.0));
        graph.addConnection(new Connection(bellevue, lesLilas, 2.0));
        graph.addConnection(new Connection(lesLilas, moulinVert, 2.0));
        graph.addConnection(new Connection(saintRoch, portMaritime, 7.0));  
        graph.addConnection(new Connection(portMaritime, arsenal, 3.0));
        graph.addConnection(new Connection(arsenal, citeAdmin, 3.0));
        graph.addConnection(new Connection(gareCentrale, republique, 3.0));
        graph.addConnection(new Connection(republique, victorHugo, 2.0));
        graph.addConnection(new Connection(victorHugo, prefecture, 1.0));
        graph.addConnection(new Connection(prefecture, jardinPublic, 2.0));
        graph.addConnection(new Connection(moulinVert, jardinPublic, 2.0));
        graph.addConnection(new Connection(jardinPublic, parcSud, 3.0));
        graph.addConnection(new Connection(parcSud, egliseNeuve, 2.0));
        graph.addConnection(new Connection(egliseNeuve, vieuxBourg, 2.0));
        graph.addConnection(new Connection(vieuxBourg, lesAcacias, 1.0));
        graph.addConnection(new Connection(gareCentrale, universite, 5.0));
        graph.addConnection(new Connection(universite, technopole, 3.0));
        graph.addConnection(new Connection(technopole, innovation, 2.0));
        graph.addConnection(new Connection(innovation, lesAcacias, 3.0));

        return graph;
    }
}