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

        Metro me1 = new Metro("ME-1", 80);
        me1.setRoute(buildRoute(aeroport, gareCentrale));
        vehicles.add(me1);

        Metro me2 = new Metro("ME-2", 80);
        me2.setRoute(buildRoute(gareCentrale, saintRoch, portMaritime, arsenal, citeAdmin));
        vehicles.add(me2);

        Metro me3 = new Metro("ME-3", 80);
        me3.setRoute(buildRoute(gareCentrale, saintRoch, bellevue));
        vehicles.add(me3);

        Metro me4 = new Metro("ME-4", 80);
        me4.setRoute(buildRoute(gareCentrale, universite, technopole, innovation));
        vehicles.add(me4);

        Metro me5 = new Metro("ME-5", 80);
        me5.setRoute(buildRoute(gareCentrale, republique, victorHugo, prefecture));
        vehicles.add(me5);

        Train tr1 = new Train("TR-1", 200);
        tr1.setRoute(buildRoute(gareCentrale, republique, victorHugo, prefecture, jardinPublic, parcSud));
        vehicles.add(tr1);

        Tram tm1 = new Tram("TM-1", 40);
        tm1.setRoute(buildRoute(universite, gareCentrale, republique, victorHugo, prefecture, jardinPublic, parcSud, egliseNeuve, vieuxBourg, lesAcacias));
        vehicles.add(tm1);

        Tram tm2 = new Tram("TM-2", 40);
        tm2.setRoute(buildRoute(gareCentrale, saintRoch, bellevue, lesLilas, moulinVert, jardinPublic, prefecture, victorHugo, republique, gareCentrale));
        vehicles.add(tm2);

        Tram tm3 = new Tram("TM-3", 40);
        tm3.setRoute(buildRoute(saintRoch, portMaritime));
        vehicles.add(tm3);

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