package org.example;

import org.example.display.AsciiDisplay;
import org.example.model.*;
import org.example.network.NetworkGraph;
import org.example.observer.SimulationObservable;
import org.example.service.PassengerService;
import org.example.service.RouteService;
import org.example.simulation.Scheduler;
import org.example.simulation.SimulationEngine;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // --- RÉSEAU ---
        NetworkGraph graph = new NetworkGraph();

        Station gareCentrale  = new Station("ST_CENT",  "Gare Centrale",       5000, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station saintRoch     = new Station("ST_ROCH",  "Saint-Roch",          3500, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station republique    = new Station("ST_REP",   "République",          3000, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station universite    = new Station("ST_UNIV",  "Université",          2500, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station aeroport      = new Station("ST_AERO",  "Aéroport",            6000, new VehicleType[]{VehicleType.METRO});
        Station bellevue      = new Station("ST_BELL",  "Bellevue",            1500, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station victorHugo    = new Station("ST_HUGO",  "Victor Hugo",         1200, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station prefecture    = new Station("ST_PREF",  "Préfecture",          1800, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station portMaritime  = new Station("ST_PORT",  "Port Maritime",       2200, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station technopole    = new Station("ST_TECH",  "Technopôle",          2000, new VehicleType[]{VehicleType.METRO});
        Station lesLilas      = new Station("ST_LILAS", "Les Lilas",            800, new VehicleType[]{VehicleType.TRAM});
        Station moulinVert    = new Station("ST_MOUL",  "Moulin Vert",          700, new VehicleType[]{VehicleType.TRAM});
        Station jardinPublic  = new Station("ST_JARD",  "Jardin Public",       1000, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM});
        Station parcSud       = new Station("ST_PARC",  "Parc Sud",            1200, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station egliseNeuve   = new Station("ST_EGL",   "Église Neuve",         900, new VehicleType[]{VehicleType.TRAM});
        Station vieuxBourg    = new Station("ST_BOURG", "Vieux Bourg",          700, new VehicleType[]{VehicleType.TRAM});
        Station lesAcacias    = new Station("ST_ACAC",  "Les Acacias",         1000, new VehicleType[]{VehicleType.TRAM});
        Station innovation    = new Station("ST_INNO",  "Innovation",          1800, new VehicleType[]{VehicleType.METRO});
        Station citeAdmin     = new Station("ST_CITE",  "Cité Administrative", 1500, new VehicleType[]{VehicleType.METRO});
        Station arsenal       = new Station("ST_ARS",   "Arsenal",             1400, new VehicleType[]{VehicleType.METRO});

        graph.addStation(gareCentrale); graph.addStation(saintRoch);   graph.addStation(republique);
        graph.addStation(universite);   graph.addStation(aeroport);    graph.addStation(bellevue);
        graph.addStation(victorHugo);   graph.addStation(prefecture);  graph.addStation(portMaritime);
        graph.addStation(technopole);   graph.addStation(lesLilas);    graph.addStation(moulinVert);
        graph.addStation(jardinPublic); graph.addStation(parcSud);     graph.addStation(egliseNeuve);
        graph.addStation(vieuxBourg);   graph.addStation(lesAcacias);  graph.addStation(innovation);
        graph.addStation(citeAdmin);    graph.addStation(arsenal);

        graph.addConnection(new Connection(aeroport,     gareCentrale, 12.0));
        graph.addConnection(new Connection(saintRoch,    gareCentrale,  4.0));
        graph.addConnection(new Connection(saintRoch,    bellevue,      3.0));
        graph.addConnection(new Connection(bellevue,     lesLilas,      2.0));
        graph.addConnection(new Connection(lesLilas,     moulinVert,    2.0));
        graph.addConnection(new Connection(saintRoch,    portMaritime,  7.0));
        graph.addConnection(new Connection(portMaritime, arsenal,       3.0));
        graph.addConnection(new Connection(arsenal,      citeAdmin,     3.0));
        graph.addConnection(new Connection(gareCentrale, republique,    3.0));
        graph.addConnection(new Connection(republique,   victorHugo,    2.0));
        graph.addConnection(new Connection(victorHugo,   prefecture,    1.0));
        graph.addConnection(new Connection(prefecture,   jardinPublic,  2.0));
        graph.addConnection(new Connection(moulinVert,   jardinPublic,  2.0));
        graph.addConnection(new Connection(jardinPublic, parcSud,       3.0));
        graph.addConnection(new Connection(parcSud,      egliseNeuve,   2.0));
        graph.addConnection(new Connection(egliseNeuve,  vieuxBourg,    2.0));
        graph.addConnection(new Connection(vieuxBourg,   lesAcacias,    1.0));
        graph.addConnection(new Connection(gareCentrale, universite,    5.0));
        graph.addConnection(new Connection(universite,   technopole,    3.0));
        graph.addConnection(new Connection(technopole,   innovation,    2.0));
        graph.addConnection(new Connection(innovation,   lesAcacias,    3.0));

        // --- OBSERVER ---
        SimulationObservable observable = new SimulationObservable();
        AsciiDisplay display = new AsciiDisplay();
        observable.addObserver(display);

        // --- SERVICES ---
        RouteService routeService     = new RouteService(graph);
        PassengerService passengerService = new PassengerService(routeService);

        // --- VÉHICULES via Factory ---
        Vehicle metro1 = VehicleFactory.create(VehicleType.METRO, 80);
        Vehicle metro2 = VehicleFactory.create(VehicleType.METRO, 80);
        Vehicle tram1  = VehicleFactory.create(VehicleType.TRAM,  40);
        Vehicle tram2  = VehicleFactory.create(VehicleType.TRAM,  40);

        // --- ROUTES des véhicules ---
        ArrayList<Station> routeMetro1 = new ArrayList<Station>();
        routeMetro1.add(aeroport); routeMetro1.add(gareCentrale);
        routeMetro1.add(saintRoch); routeMetro1.add(portMaritime);
        routeMetro1.add(arsenal); routeMetro1.add(citeAdmin);

        ArrayList<Station> routeMetro2 = new ArrayList<Station>();
        routeMetro2.add(gareCentrale); routeMetro2.add(universite);
        routeMetro2.add(technopole); routeMetro2.add(innovation);

        ArrayList<Station> routeTram1 = new ArrayList<Station>();
        routeTram1.add(gareCentrale); routeTram1.add(saintRoch);
        routeTram1.add(bellevue); routeTram1.add(lesLilas); routeTram1.add(moulinVert);

        ArrayList<Station> routeTram2 = new ArrayList<Station>();
        routeTram2.add(moulinVert); routeTram2.add(jardinPublic);
        routeTram2.add(parcSud); routeTram2.add(egliseNeuve);
        routeTram2.add(vieuxBourg); routeTram2.add(lesAcacias);

        // --- SCHEDULER ---
        Scheduler scheduler = new Scheduler(graph, observable);
        scheduler.registerVehicle(metro1, routeMetro1);
        scheduler.registerVehicle(metro2, routeMetro2);
        scheduler.registerVehicle(tram1,  routeTram1);
        scheduler.registerVehicle(tram2,  routeTram2);

        // --- PASSAGERS ---
        passengerService.spawnPassenger(aeroport,    citeAdmin,   VehicleType.METRO, observable);
        passengerService.spawnPassenger(gareCentrale, innovation,  VehicleType.METRO, observable);
        passengerService.spawnPassenger(gareCentrale, moulinVert,  VehicleType.TRAM,  observable);
        passengerService.spawnPassenger(moulinVert,   lesAcacias,  VehicleType.TRAM,  observable);

        // --- DÉMARRAGE via Facade ---
        SimulationEngine engine = new SimulationEngine(graph, scheduler, display, observable);
        engine.start();
    }
}