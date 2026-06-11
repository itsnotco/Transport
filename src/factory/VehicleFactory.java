package factory;

import model.*;
import network.NetworkGraph;

import java.util.ArrayList;
import java.util.List;

public class VehicleFactory {

    public static List<Vehicle> create(NetworkGraph graph) {

        Station gareCentrale = StationFinder.find(graph, "ST_CENT");
        Station aeroport = StationFinder.find(graph, "ST_AERO");
        Station saintRoch = StationFinder.find(graph, "ST_ROCH");
        Station portMaritime = StationFinder.find(graph, "ST_PORT");
        Station arsenal = StationFinder.find(graph, "ST_ARS");
        Station citeAdmin = StationFinder.find(graph, "ST_CITE");
        Station bellevue = StationFinder.find(graph, "ST_BELL");
        Station universite = StationFinder.find(graph, "ST_UNIV");
        Station technopole = StationFinder.find(graph, "ST_TECH");
        Station innovation = StationFinder.find(graph, "ST_INNO");
        Station republique = StationFinder.find(graph, "ST_REP");
        Station victorHugo = StationFinder.find(graph, "ST_HUGO");
        Station prefecture = StationFinder.find(graph, "ST_PREF");
        Station jardinPublic = StationFinder.find(graph, "ST_JARD");
        Station parcSud = StationFinder.find(graph, "ST_PARC");
        Station egliseNeuve = StationFinder.find(graph, "ST_EGL");
        Station vieuxBourg = StationFinder.find(graph, "ST_BOURG");
        Station lesAcacias = StationFinder.find(graph, "ST_ACAC");
        Station lesLilas = StationFinder.find(graph, "ST_LILAS");
        Station moulinVert = StationFinder.find(graph, "ST_MOUL");

        List<Vehicle> vehicles = new ArrayList<>();

        // =================== METROS ===================

        // ME-1 : branche Aeroport (sens 1, depart Aeroport)
        Metro me1 = new Metro("ME-1", 600);
        me1.setRoute(buildRoute(aeroport, gareCentrale, aeroport));
        vehicles.add(me1);

        // ME-2 : branche Port Maritime -> Cite Administrative (depart GC)
        Metro me2 = new Metro("ME-2", 600);
        me2.setRoute(buildRoute(gareCentrale, saintRoch, portMaritime, arsenal, citeAdmin, arsenal, portMaritime, saintRoch, gareCentrale));
        vehicles.add(me2);

        // ME-3 : branche Bellevue
        Metro me3 = new Metro("ME-3", 600);
        me3.setRoute(buildRoute(gareCentrale, saintRoch, bellevue, saintRoch, gareCentrale));
        vehicles.add(me3);

        // ME-4 : branche Universite -> Innovation (depart GC)
        Metro me4 = new Metro("ME-4", 600);
        me4.setRoute(buildRoute(gareCentrale, universite, technopole, innovation, technopole, universite, gareCentrale));
        vehicles.add(me4);

        // ME-5 : branche Republique -> Prefecture
        Metro me5 = new Metro("ME-5", 600);
        me5.setRoute(buildRoute(gareCentrale, republique, victorHugo, prefecture, victorHugo, republique, gareCentrale));
        vehicles.add(me5);

        // ME-6 : double la branche Aeroport, depart decale a Gare Centrale
        Metro me6 = new Metro("ME-6", 600);
        me6.setRoute(buildRoute(gareCentrale, aeroport, gareCentrale));
        vehicles.add(me6);

        // ME-7 : double Port Maritime -> Cite Administrative, depart decale a Cite Administrative
        Metro me7 = new Metro("ME-7", 600);
        me7.setRoute(buildRoute(citeAdmin, arsenal, portMaritime, saintRoch, gareCentrale, saintRoch, portMaritime, arsenal, citeAdmin));
        vehicles.add(me7);

        // ME-8 : double Universite -> Innovation, depart decale a Innovation
        Metro me8 = new Metro("ME-8", 600);
        me8.setRoute(buildRoute(innovation, technopole, universite, gareCentrale, universite, technopole, innovation));
        vehicles.add(me8);

        // =================== TRAINS ===================

        // TR-1 : axe central GC -> Parc Sud (depart GC)
        Train tr1 = new Train("TR-1", 750);
        tr1.setRoute(buildRoute(gareCentrale, republique, victorHugo, prefecture, jardinPublic, parcSud, jardinPublic, prefecture, victorHugo, republique, gareCentrale));
        vehicles.add(tr1);

        // TR-2 : double l'axe central, depart decale a Parc Sud
        Train tr2 = new Train("TR-2", 750);
        tr2.setRoute(buildRoute(parcSud, jardinPublic, prefecture, victorHugo, republique, gareCentrale, republique, victorHugo, prefecture, jardinPublic, parcSud));
        vehicles.add(tr2);

        // =================== TRAMS ===================

        // TM-1 : grande boucle, sens 1, depart GC
        Tram tm1 = new Tram("TM-1", 200);
        tm1.setRoute(buildRoute(gareCentrale, republique, victorHugo, prefecture, jardinPublic, parcSud, egliseNeuve, vieuxBourg, lesAcacias, innovation, technopole, universite, gareCentrale));
        vehicles.add(tm1);

        // TM-2 : petite boucle ouest, sens 1, depart GC
        Tram tm2 = new Tram("TM-2", 200);
        tm2.setRoute(buildRoute(gareCentrale, saintRoch, bellevue, lesLilas, moulinVert, jardinPublic, prefecture, victorHugo, republique, gareCentrale));
        vehicles.add(tm2);

        // TM-3 : navette Saint-Roch <-> Port Maritime
        Tram tm3 = new Tram("TM-3", 200);
        tm3.setRoute(buildRoute(saintRoch, portMaritime, saintRoch));
        vehicles.add(tm3);

        // TM-4 : petite boucle ouest, sens inverse, depart decale a Moulin Vert
        Tram tm4 = new Tram("TM-4", 200);
        tm4.setRoute(buildRoute(moulinVert, lesLilas, bellevue, saintRoch, gareCentrale, republique, victorHugo, prefecture, jardinPublic, moulinVert));
        vehicles.add(tm4);

        // TM-5 : grande boucle, sens inverse, depart decale aux Acacias
        Tram tm5 = new Tram("TM-5", 200);
        tm5.setRoute(buildRoute(lesAcacias, vieuxBourg, egliseNeuve, parcSud, jardinPublic, prefecture, victorHugo, republique, gareCentrale, universite, technopole, innovation, lesAcacias));
        vehicles.add(tm5);

        return vehicles;
    }

    private static List<Station> buildRoute(Station... stations) {
        List<Station> route = new ArrayList<>();
        for (Station s : stations) {
            route.add(s);
        }
        return route;
    }
}