package display;

import model.Connection;
import model.Passenger;
import model.PassengerState;
import model.Station;
import model.Vehicle;
import model.VehicleType;
import network.NetworkGraph;
import observer.SimulationEvent;
import observer.SimulationObserver;
import service.JourneyLeg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsciiDisplay implements SimulationObserver {

    // ---- couleurs ANSI ----
    private static final String[] ANSI = {
            "",              // 0 defaut
            "\u001B[91m",    // 1 rouge
            "\u001B[92m",    // 2 vert
            "\u001B[93m",    // 3 jaune
            "\u001B[94m",    // 4 bleu
            "\u001B[90m",    // 5 gris (lignes/boites)
            "\u001B[96m",    // 6 cyan
            "\u001B[1m"      // 7 gras
    };
    private static final String RESET = "\u001B[0m";

    // ---- journal des derniers mouvements (alimente par l'Observer) ----
    private static class Ev {
        final String s;
        final int c;

        Ev(String s, int c) {
            this.s = s;
            this.c = c;
        }
    }

    private final List<Ev> recent = new ArrayList<>();

    public void printTime(String time) {
        System.out.println("\n" + ANSI[7] + "================  t = " + time + "  ================" + RESET);
    }

    @Override
    public void onEvent(SimulationEvent e) {
        Vehicle v = e.getVehicle();
        int color = colorIndex(v.getType());
        String s;
        if (e.getKind() == SimulationEvent.Kind.DEPARTURE) {
            s = v.getId() + "  " + e.getFrom().getName() + " -> " + e.getTo().getName();
        } else {
            s = v.getId() + "  arrive a " + e.getTo().getName();
        }
        recent.add(new Ev(s, color));
        while (recent.size() > 6) {
            recent.remove(0);
        }
    }

    public void printJourney(Passenger p, List<JourneyLeg> legs, int now) {
        System.out.println("\n=== PLAN " + p.getId() + " | "
                + p.getOrigin().getName() + " -> " + p.getDestination().getName()
                + " (calcule a " + hhmm(now) + ") ===");
        if (legs.isEmpty()) {
            System.out.println("  Aucun trajet trouve (ou deja a destination).");
            return;
        }
        int prevTime = now;
        String prevVeh = null;
        for (JourneyLeg leg : legs) {
            if (prevVeh == null) {
                int wait = leg.getBoardTime() - now;
                if (wait > 0) System.out.println("  Attente " + wait + " min a " + leg.getFrom().getName());
            } else {
                int wait = leg.getBoardTime() - prevTime;
                System.out.println("  Correspondance a " + leg.getFrom().getName()
                        + (wait > 0 ? " (attente " + wait + " min)" : ""));
            }
            System.out.println("  [" + leg.getType() + " " + leg.getVehicleId() + "] "
                    + leg.getFrom().getName() + " " + hhmm(leg.getBoardTime())
                    + " -> " + leg.getTo().getName() + " " + hhmm(leg.getAlightTime()));
            prevTime = leg.getAlightTime();
            prevVeh = leg.getVehicleId();
        }
        int arrival = legs.get(legs.size() - 1).getAlightTime();
        System.out.println("  => Arrivee prevue " + hhmm(arrival) + " | Duree " + (arrival - now) + " min");
    }

    // ============================================================
    //  CARTE DU RESEAU + passagers + vehicules
    // ============================================================
    private static final int W = 96, H = 60;
    private char[][] ch;
    private int[][] col;

    public void printMap(NetworkGraph graph, List<Vehicle> vehicles) {
        ch = new char[H][W];
        col = new int[H][W];
        for (int r = 0; r < H; r++) {
            for (int c = 0; c < W; c++) {
                ch[r][c] = ' ';
            }
        }

        // === connexions verticales (col, rangee debut, rangee fin) ===
        // Axe central (col 60), boites a 0,6,12,18,24,30,36,42,48,54
        vseg(60, 3, 5);
        vseg(60, 9, 11);
        vseg(60, 15, 17);
        vseg(60, 21, 23);
        vseg(60, 27, 29);
        vseg(60, 33, 35);
        vseg(60, 39, 41);
        vseg(60, 45, 47);
        vseg(60, 51, 53);
        // Branche B (col 36)
        vseg(36, 9, 11);
        vseg(36, 15, 17);
        vseg(36, 21, 23);
        // Branche A (col 12)
        vseg(12, 15, 17);
        vseg(12, 21, 23);
        // Branche D (col 84)
        vseg(84, 15, 17);
        vseg(84, 21, 23);

        // === connexions horizontales / coudes ===
        // Saint-Roch <-> Gare Centrale
        hseg(7, 46, 49);
        // Gare Centrale -> Universite : droite puis descente
        hseg(7, 70, 84);
        set(84, 7, '+', 5);
        vseg(84, 8, 11);
        // Saint-Roch -> Bellevue : gauche puis descente
        hseg(7, 12, 25);
        set(12, 7, '+', 5);
        vseg(12, 8, 11);
        // Moulin Vert -> Jardin Public : descente puis vers le centre
        vseg(12, 27, 31);
        set(12, 31, '+', 5);
        hseg(31, 13, 49);
        // Innovation -> Les Acacias : grande descente a droite puis retour
        vseg(84, 27, 55);
        set(84, 55, '+', 5);
        hseg(55, 70, 83);

        // === gares (id, left, top) ===
        Map<String, int[]> centers = new HashMap<>();
        box(graph, centers, "ST_AERO", 50, 0);
        box(graph, centers, "ST_ROCH", 26, 6);
        box(graph, centers, "ST_CENT", 50, 6);
        box(graph, centers, "ST_BELL", 2, 12);
        box(graph, centers, "ST_PORT", 26, 12);
        box(graph, centers, "ST_REP", 50, 12);
        box(graph, centers, "ST_UNIV", 74, 12);
        box(graph, centers, "ST_LILAS", 2, 18);
        box(graph, centers, "ST_ARS", 26, 18);
        box(graph, centers, "ST_HUGO", 50, 18);
        box(graph, centers, "ST_TECH", 74, 18);
        box(graph, centers, "ST_MOUL", 2, 24);
        box(graph, centers, "ST_CITE", 26, 24);
        box(graph, centers, "ST_PREF", 50, 24);
        box(graph, centers, "ST_INNO", 74, 24);
        box(graph, centers, "ST_JARD", 50, 30);
        box(graph, centers, "ST_PARC", 50, 36);
        box(graph, centers, "ST_EGL", 50, 42);
        box(graph, centers, "ST_BOURG", 50, 48);
        box(graph, centers, "ST_ACAC", 50, 54);

        // === vehicules EN MOUVEMENT, places sur leur segment ===
        for (Vehicle v : vehicles) {
            if (v.isParked()) continue;
            List<Station> route = v.getRoute();
            if (route.isEmpty()) continue;
            Station from = route.get(v.getRouteIndex());
            Station to = v.getNextStation();
            if (to == null) continue;
            int[] cf = centers.get(from.getId());
            int[] ct = centers.get(to.getId());
            if (cf == null || ct == null) continue;

            double travel = distance(graph, from, to) / v.getSpeed();
            double ratio = (travel <= 0) ? 0.5 : (travel - v.getTimeUntilNextStation()) / travel;
            if (ratio < 0) ratio = 0;
            if (ratio > 1) ratio = 1;

            drawMarker(cf, ct, ratio, v.getType());
        }

        // === impression ===
        System.out.println();
        for (int r = 0; r < H; r++) {
            printRow(r);
        }
        System.out.println(ANSI[5] + "  [Gare        nnn]  nnn = passagers en attente" + RESET);
        System.out.println("  Vehicules en deplacement : " + ANSI[4] + "M metro" + RESET + "   "
                + ANSI[1] + "R train" + RESET + "   " + ANSI[2] + "T tram" + RESET);
    }

    // Place un marqueur de vehicule : point interpole, sinon milieu du segment.
    private void drawMarker(int[] cf, int[] ct, double ratio, VehicleType type) {
        int x = (int) Math.round(cf[0] + ratio * (ct[0] - cf[0]));
        int y = (int) Math.round(cf[1] + ratio * (ct[1] - cf[1]));
        if (!placeMarker(x, y, type)) {
            int mx = (cf[0] + ct[0]) / 2;
            int my = (cf[1] + ct[1]) / 2;
            placeMarker(mx, my, type);
        }
    }

    // Dessine seulement si la case est une ligne/coude/espace (jamais sur un nom de gare).
    private boolean placeMarker(int x, int y, VehicleType type) {
        if (y < 0 || y >= H || x < 0 || x >= W) return false;
        char here = ch[y][x];
        if (here == ' ' || here == '|' || here == '-' || here == '+'
                || here == 'M' || here == 'R' || here == 'T') {
            set(x, y, markerChar(type), colorIndex(type));
            return true;
        }
        return false;
    }

    // ============================================================
    //  TABLEAU DE BORD
    // ============================================================
    public void printDashboard(String time, List<Passenger> passengers, List<Vehicle> vehicles) {
        int waiting = 0, onboard = 0, arrived = 0;
        for (Passenger p : passengers) {
            if (p.getState() == PassengerState.WAITING) waiting++;
            else if (p.getState() == PassengerState.ON_BOARD) onboard++;
            else arrived++;
        }

        int metro = 0, train = 0, tram = 0;
        for (Vehicle v : vehicles) {
            if (v.getType() == VehicleType.METRO) metro += v.getPassengerCount();
            else if (v.getType() == VehicleType.TRAIN) train += v.getPassengerCount();
            else tram += v.getPassengerCount();
        }

        int w = 60;
        System.out.println();
        line('=', w);
        dash(w, " TABLEAU DE BORD   -   heure simulee : " + time, 7);
        line('-', w);
        dash(w, " Passagers : " + passengers.size() + " total", 0);
        dash(w, "   en attente : " + waiting + "    a bord : " + onboard + "    arrives : " + arrived, 0);
        dash(w, "   a bord par type :  metro " + metro + "   train " + train + "   tram " + tram, 0);
        line('-', w);
        dash(w, " SUIVIS", 7);
        for (Passenger p : passengers) {
            if (p.isTracked()) dash(w, " " + trackedLine(p), 0);
        }
        line('-', w);
        dash(w, " DERNIERS MOUVEMENTS", 7);
        if (recent.isEmpty()) {
            dash(w, "   (aucun)", 5);
        } else {
            for (Ev e : recent) dash(w, "   " + e.s, e.c);
        }
        line('=', w);
    }

    private String trackedLine(Passenger p) {
        String pos, eta;
        if (p.getState() == PassengerState.WAITING) {
            pos = "attend a " + p.getCurrentStation().getName();
        } else if (p.getState() == PassengerState.ON_BOARD) {
            JourneyLeg leg = p.getCurrentLeg();
            pos = "dans " + (leg != null ? leg.getVehicleId() : "?")
                    + " -> " + (leg != null ? leg.getTo().getName() : "?");
        } else {
            pos = "arrive a " + p.getDestination().getName();
        }
        if (p.getState() == PassengerState.ARRIVED) eta = "";
        else if (p.getPlan().isEmpty()) eta = "  (aucun trajet)";
        else eta = "  ETA " + hhmm(p.getPlan().get(p.getPlan().size() - 1).getAlightTime());
        return p.getId() + " [" + p.getState() + "] " + pos + eta;
    }

    private void line(char c, int w) {
        System.out.println(ANSI[5] + "+" + repeat(c, w + 2) + "+" + RESET);
    }

    private void dash(int w, String content, int color) {
        String padded = pad(content, w);
        String body = (color == 0) ? padded : ANSI[color] + padded + RESET;
        System.out.println(ANSI[5] + "|" + RESET + " " + body + " " + ANSI[5] + "|" + RESET);
    }

    private String pad(String s, int w) {
        if (s.length() > w) return s.substring(0, w);
        StringBuilder b = new StringBuilder(s);
        while (b.length() < w) b.append(' ');
        return b.toString();
    }

    private String repeat(char c, int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) b.append(c);
        return b.toString();
    }

    // ---------------- helpers carte ----------------

    private void vseg(int col, int r1, int r2) {
        for (int r = r1; r <= r2; r++) set(col, r, '|', 5);
    }

    private void hseg(int row, int c1, int c2) {
        for (int c = c1; c <= c2; c++) set(c, row, '-', 5);
    }

    private void box(NetworkGraph graph, Map<String, int[]> centers, String id, int left, int top) {
        Station s = stationById(graph, id);
        if (s == null) return;
        int count = s.getWaiting().size();

        set(left, top, '+', 5);
        set(left + 19, top, '+', 5);
        set(left, top + 2, '+', 5);
        set(left + 19, top + 2, '+', 5);
        for (int c = left + 1; c <= left + 18; c++) {
            set(c, top, '-', 5);
            set(c, top + 2, '-', 5);
        }
        set(left, top + 1, '|', 5);
        set(left + 19, top + 1, '|', 5);

        text(left + 2, top + 1, shortName(id), 0);
        text(left + 15, top + 1, String.format("%3d", count), loadColor(count));

        centers.put(id, new int[]{left + 10, top + 1});
    }

    private int loadColor(int count) {
        if (count == 0) return 5;
        if (count < 10) return 2;
        if (count < 30) return 3;
        return 1;
    }

    private int colorIndex(VehicleType t) {
        if (t == VehicleType.METRO) return 4;
        if (t == VehicleType.TRAIN) return 1;
        return 2;
    }

    private char markerChar(VehicleType t) {
        if (t == VehicleType.METRO) return 'M';
        if (t == VehicleType.TRAIN) return 'R';
        return 'T';
    }

    private void set(int x, int y, char c, int color) {
        if (y >= 0 && y < H && x >= 0 && x < W) {
            ch[y][x] = c;
            col[y][x] = color;
        }
    }

    private void text(int x, int y, String s, int color) {
        for (int i = 0; i < s.length(); i++) set(x + i, y, s.charAt(i), color);
    }

    private void printRow(int r) {
        int last = W - 1;
        while (last >= 0 && ch[r][last] == ' ') last--;
        if (last < 0) {
            System.out.println();
            return;
        }

        StringBuilder sb = new StringBuilder();
        int cur = 0;
        for (int c = 0; c <= last; c++) {
            int d = col[r][c];
            if (d != cur) {
                sb.append(RESET);
                if (d != 0) sb.append(ANSI[d]);
                cur = d;
            }
            sb.append(ch[r][c]);
        }
        if (cur != 0) sb.append(RESET);
        System.out.println(sb);
    }

    private Station stationById(NetworkGraph graph, String id) {
        for (Station s : graph.getStations()) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    private double distance(NetworkGraph graph, Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) return c.getDistance();
        }
        return 1.0;
    }

    private String shortName(String id) {
        switch (id) {
            case "ST_CENT":
                return "Gare Centr.";
            case "ST_ROCH":
                return "Saint-Roch";
            case "ST_REP":
                return "Republique";
            case "ST_UNIV":
                return "Universite";
            case "ST_AERO":
                return "Aeroport";
            case "ST_BELL":
                return "Bellevue";
            case "ST_HUGO":
                return "Victor Hugo";
            case "ST_PREF":
                return "Prefecture";
            case "ST_PORT":
                return "Port Marit.";
            case "ST_TECH":
                return "Technopole";
            case "ST_LILAS":
                return "Les Lilas";
            case "ST_MOUL":
                return "Moulin Vert";
            case "ST_JARD":
                return "Jardin Pub.";
            case "ST_PARC":
                return "Parc Sud";
            case "ST_EGL":
                return "Eglise Nve";
            case "ST_BOURG":
                return "Vieux Bourg";
            case "ST_ACAC":
                return "Les Acacias";
            case "ST_INNO":
                return "Innovation";
            case "ST_CITE":
                return "Cite Admin.";
            case "ST_ARS":
                return "Arsenal";
            default:
                return id;
        }
    }

    private String hhmm(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}