package org.example;

import org.example.model.*;
import org.example.network.NetworkGraph;
import org.example.service.RouteService;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // 1. INITIALISATION DU RÉSEAU GLOBAL
        NetworkGraph graph = new NetworkGraph();

        // 2. CRÉATION DES 20 GARES (Basé sur l'Annexe Page 1)
        // Format : ID, Nom, Capacité, Types de transports supportés
        Station gareCentrale = new Station("ST_CENT", "Gare Centrale", 5000, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM,VehicleType.METRO});
        Station saintRoch = new Station("ST_ROCH", "Saint-Roch", 3500, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station republique = new Station("ST_REP", "République", 3000, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station universite = new Station("ST_UNIV", "Université", 2500, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station aeroport = new Station("ST_AERO", "Aéroport", 6000,     new VehicleType[]{VehicleType.METRO});
        Station bellevue = new Station("ST_BELL", "Bellevue", 1500, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station victorHugo = new Station("ST_HUGO", "Victor Hugo", 1200, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station prefecture = new Station("ST_PREF", "Préfecture", 1800, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station portMaritime = new Station("ST_PORT", "Port Maritime", 2200, new VehicleType[]{VehicleType.TRAM, VehicleType.METRO});
        Station technopole = new Station("ST_TECH", "Technopôle", 2000, new VehicleType[]{VehicleType.METRO});
        Station lesLilas = new Station("ST_LILAS", "Les Lilas", 800, new VehicleType[]{VehicleType.TRAM});
        Station moulinVert = new Station("ST_MOUL", "Moulin Vert", 700, new VehicleType[]{VehicleType.TRAM});
        Station jardinPublic = new Station("ST_JARD", "Jardin Public", 1000, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM});
        Station parcSud = new Station("ST_PARC", "Parc Sud", 1200, new VehicleType[]{VehicleType.TRAIN, VehicleType.TRAM, VehicleType.METRO});
        Station egliseNeuve = new Station("ST_EGL", "Église Neuve", 900, new VehicleType[]{VehicleType.TRAM});
        Station vieuxBourg = new Station("ST_BOURG", "Vieux Bourg", 700, new VehicleType[]{VehicleType.TRAM});
        Station lesAcacias = new Station("ST_ACAC", "Les Acacias", 1000, new VehicleType[]{VehicleType.TRAM});
        Station innovation = new Station("ST_INNO", "Innovation", 1800, new VehicleType[]{VehicleType.METRO});
        Station citeAdmin = new Station("ST_CITE", "Cité Administrative", 1500, new VehicleType[]{VehicleType.METRO});
        Station arsenal = new Station("ST_ARS", "Arsenal", 1400, new VehicleType[]{VehicleType.METRO});

        // Ajout de toutes les gares au graphique du réseau
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

        // 3. CRÉATION DES CONNEXIONS (Basé sur la Carte du Réseau Page 2)
        // Axe Nord
        graph.addConnection(new Connection(aeroport, gareCentrale, 12.0));

        // Branche Ouest (Saint-Roch)
        graph.addConnection(new Connection(saintRoch, gareCentrale, 4.0));
        graph.addConnection(new Connection(saintRoch, bellevue, 3.0));
        graph.addConnection(new Connection(bellevue, lesLilas, 2.0));
        graph.addConnection(new Connection(lesLilas, moulinVert, 2.0));

        // Ramification Port Maritime depuis Saint-Roch
        graph.addConnection(new Connection(saintRoch, portMaritime, 7.0));
        graph.addConnection(new Connection(portMaritime, arsenal, 3.0));
        graph.addConnection(new Connection(arsenal, citeAdmin, 3.0));

        // Axe Central (Gare Centrale vers le Sud)
        graph.addConnection(new Connection(gareCentrale, republique, 3.0));
        graph.addConnection(new Connection(republique, victorHugo, 2.0));
        graph.addConnection(new Connection(victorHugo, prefecture, 1.0));
        graph.addConnection(new Connection(prefecture, jardinPublic, 2.0));

        // Jonction entre la ligne Ouest et l'axe Central
        graph.addConnection(new Connection(moulinVert, jardinPublic, 2.0));

        // Prolongation Axe Central vers le Sud extrême
        graph.addConnection(new Connection(jardinPublic, parcSud, 3.0));
        graph.addConnection(new Connection(parcSud, egliseNeuve, 2.0));
        graph.addConnection(new Connection(egliseNeuve, vieuxBourg, 2.0));
        graph.addConnection(new Connection(vieuxBourg, lesAcacias, 1.0));

        // Ligne Est (Université & Technopôle)
        graph.addConnection(new Connection(gareCentrale, universite, 5.0));
        graph.addConnection(new Connection(universite, technopole, 3.0));
        graph.addConnection(new Connection(technopole, innovation, 2.0));
        graph.addConnection(new Connection(innovation, lesAcacias, 3.0));


        // 4. INSTANCIATION DU SERVICE DE NAVIGATION (GPS)
        RouteService router = new RouteService(graph);

        System.out.println("=== SIMULATEUR DE TRANSPORT : RÉSEAU CHARGÉ ===");
        System.out.println("Nombre de stations enregistrées : " + graph.getStations().size());
        System.out.println("Nombre de connexions enregistrées : " + graph.getConnections().size());
        System.out.println("==============================================\n");


        // 5. ZONE DE TESTS DE TRAJETS OPTIMAUX (DIJKSTRA)

        // Test 1 : Trajet Métro depuis l'Aéroport jusqu'à la Gare Centrale
        System.out.println("--- TEST 1 : Voyage Aéroport → Gare Centrale ---");
        ArrayList<Station> route1 = router.findRoute(portMaritime,innovation, VehicleType.TRAM);
        System.out.print("Itinéraire (METRO) : ");
        for (Station s : route1) {
            System.out.print(s.getName() + " -> ");
        }
        System.out.println("Arrivée !");
        System.out.println();


    // Test 2 : Trajet depuis Moulin Vert jusqu'à Les Acacias
        System.out.println("--- TEST 2 : Voyage Moulin Vert → Les Acacias ---");
        ArrayList<Station> route2 = router.findRoute(saintRoch, jardinPublic, VehicleType.TRAM);
        System.out.print("Itinéraire (TRAM) : ");
        for (Station s : route2) {
            System.out.print(s.getName() + " -> ");
        }
        System.out.println("Arrivée !");
        System.out.println();


        // Test 3 : Trajet longue distance Gare Centrale → Les Acacias via la Ligne Est (Université)
        System.out.println("--- TEST 3 : Voyage Gare Centrale → Les Acacias ---");
        ArrayList<Station> routeEst = router.findRoute(citeAdmin, innovation, VehicleType.METRO);
        System.out.print("Itinéraire (TRAM) : ");
        for (Station s : routeEst) {
            System.out.print(s.getName() + " -> ");
        }
        System.out.println("Arrivée !");
        System.out.println();


        // Test 4 : Suivi d'un passager réel sur le réseau
        System.out.println("--- TEST 4 : Cycle de vie initial d'un passager ---");
        Passenger p1 = new Passenger("PASS_001", moulinVert, lesAcacias);
        // On attribue l'itinéraire calculé au test 2 au passager
        p1.setRoute(route2);

        System.out.println("Passager ID : " + p1.getId());
        System.out.println("Départ programmé de : " + p1.getOrigin().getName());
        System.out.println("Destination finale : " + p1.getDestination().getName());
        System.out.println("Étape actuelle (Index) : " + p1.getRouteIndex());
        if (p1.getNextStop() != null) {
            System.out.println("Prochain arrêt ciblé : " + p1.getNextStop().getName());
        }
        System.out.println("==============================================");
    }
}