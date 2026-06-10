package display;

import model.Connection;
import model.Passenger;
import model.PassengerState;
import model.Station;
import model.Vehicle;
import model.VehicleType;
import network.NetworkGraph;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwingDisplay extends JPanel {

    private final NetworkGraph graph;
    private final List<Vehicle> vehicles;
    private final List<Passenger> passengers;
    private final Map<String, Point> positions = new HashMap<>();

    private String time = "00:00";

    // couleurs
    private static final Color BG       = new Color(24, 26, 32);
    private static final Color LINE     = new Color(90, 95, 105);
    private static final Color BOX_FILL = new Color(40, 44, 54);
    private static final Color BOX_EDGE = new Color(120, 126, 140);
    private static final Color TEXT     = new Color(225, 228, 235);
    private static final Color METRO_C  = new Color(70, 130, 255);
    private static final Color TRAIN_C  = new Color(235, 80, 80);
    private static final Color TRAM_C   = new Color(70, 200, 110);

    private static final int BOX_W = 116;
    private static final int BOX_H = 30;
    private static final int MAP_W = 760;

    public SwingDisplay(NetworkGraph graph, List<Vehicle> vehicles, List<Passenger> passengers) {
        this.graph = graph;
        this.vehicles = vehicles;
        this.passengers = passengers;
        buildPositions();
        setPreferredSize(new Dimension(MAP_W + 320, 880));
        setBackground(BG);

        JFrame frame = new JFrame("Reseau de transport");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Positions (x,y) des centres de gare, reproduisant le schema de l'annexe.
    private void buildPositions() {
        int A = 110, B = 290, C = 470, D = 650;
        put("ST_AERO",  C, 50);
        put("ST_ROCH",  B, 140);
        put("ST_CENT",  C, 140);
        put("ST_BELL",  A, 240);
        put("ST_PORT",  B, 240);
        put("ST_REP",   C, 240);
        put("ST_UNIV",  D, 240);
        put("ST_LILAS", A, 340);
        put("ST_ARS",   B, 340);
        put("ST_HUGO",  C, 340);
        put("ST_TECH",  D, 340);
        put("ST_MOUL",  A, 440);
        put("ST_CITE",  B, 440);
        put("ST_PREF",  C, 440);
        put("ST_INNO",  D, 440);
        put("ST_JARD",  C, 540);
        put("ST_PARC",  C, 620);
        put("ST_EGL",   C, 700);
        put("ST_BOURG", C, 770);
        put("ST_ACAC",  C, 840);
    }

    private void put(String id, int x, int y) {
        positions.put(id, new Point(x, y));
    }

    // appelee par Main apres chaque tick
    public void refresh(String time) {
        this.time = time;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawConnections(g2);
        drawStations(g2);
        drawVehicles(g2);
        drawDashboard(g2);
    }

    private void drawConnections(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(LINE);
        for (Connection c : graph.getConnections()) {
            Point a = positions.get(c.getStationA().getId());
            Point b = positions.get(c.getStationB().getId());
            if (a == null || b == null) continue;

            if (isElbow(c)) {
                drawElbow(g2, a, b);     // trace orthogonal (coude en S)
            } else {
                g2.drawLine(a.x, a.y, b.x, b.y);
            }
        }
    }

    // Les connexions qui tomberaient en diagonale -> on les trace en coude.
    private boolean isElbow(Connection c) {
        return involves(c, "ST_MOUL", "ST_JARD")     // Moulin Vert -> Jardin Public
                || involves(c, "ST_INNO", "ST_ACAC")     // Innovation -> Les Acacias
                || involves(c, "ST_ROCH", "ST_BELL")     // Saint-Roch -> Bellevue
                || involves(c, "ST_CENT", "ST_UNIV");    // Gare Centrale -> Universite
    }

    private boolean involves(Connection c, String id1, String id2) {
        String a = c.getStationA().getId();
        String b = c.getStationB().getId();
        return (a.equals(id1) && b.equals(id2)) || (a.equals(id2) && b.equals(id1));
    }

    // Coude en S : on descend a mi-hauteur entre les deux gares, on file
    // horizontalement la (entre les rangees, donc sans traverser de boite),
    // puis on redescend jusqu'a la gare du bas.
    private void drawElbow(Graphics2D g2, Point a, Point b) {
        Point haut = (a.y <= b.y) ? a : b;
        Point bas  = (a.y <= b.y) ? b : a;

        int midY = (haut.y + bas.y) / 2;   // hauteur intermediaire, hors des boites

        g2.drawLine(haut.x, haut.y, haut.x, midY);   // descente sur la colonne du haut
        g2.drawLine(haut.x, midY, bas.x, midY);      // horizontal entre les rangees
        g2.drawLine(bas.x, midY, bas.x, bas.y);      // descente sur la colonne du bas
    }

    private void drawStations(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (Station s : graph.getStations()) {
            Point p = positions.get(s.getId());
            if (p == null) continue;

            int x = p.x - BOX_W / 2;
            int y = p.y - BOX_H / 2;

            g2.setColor(BOX_FILL);
            g2.fillRoundRect(x, y, BOX_W, BOX_H, 8, 8);
            g2.setColor(BOX_EDGE);
            g2.drawRoundRect(x, y, BOX_W, BOX_H, 8, 8);

            // nom
            g2.setColor(TEXT);
            g2.drawString(s.getName(), x + 8, y + 19);

            // compteur de passagers en attente, colore selon la charge
            int count = s.getWaiting().size();
            String n = String.valueOf(count);
            g2.setColor(loadColor(count));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            int nw = g2.getFontMetrics().stringWidth(n);
            g2.drawString(n, x + BOX_W - nw - 8, y + 20);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        }
    }

    private void drawVehicles(Graphics2D g2) {
        Map<String, Integer> stacked = new HashMap<>();   // pour empiler ceux a quai
        for (Vehicle v : vehicles) {
            Point pos = vehiclePosition(v, stacked);
            if (pos == null) continue;

            g2.setColor(colorFor(v.getType()));
            g2.fillOval(pos.x - 7, pos.y - 7, 14, 14);
            g2.setColor(Color.BLACK);
            g2.drawOval(pos.x - 7, pos.y - 7, 14, 14);

            // id + nombre de passagers a bord
            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(v.getId() + " (" + v.getPassengerCount() + ")", pos.x + 10, pos.y + 4);
        }
    }

    // position pixel d'un vehicule : interpolee si en transit, empilee pres de la gare si a quai
    private Point vehiclePosition(Vehicle v, Map<String, Integer> stacked) {
        List<Station> route = v.getRoute();
        if (route.isEmpty()) return null;

        if (v.isParked()) {
            Station here = v.getCurrentStation();
            Point base = positions.get(here.getId());
            if (base == null) return null;
            int k = stacked.getOrDefault(here.getId(), 0);
            stacked.put(here.getId(), k + 1);
            return new Point(base.x - BOX_W / 2 - 12, base.y - 10 + k * 14);
        }

        Station from = route.get(v.getRouteIndex());
        Station to   = v.getNextStation();
        if (to == null) return null;
        Point pf = positions.get(from.getId());
        Point pt = positions.get(to.getId());
        if (pf == null || pt == null) return null;

        double travel = distance(from, to) / v.getSpeed();
        double ratio = (travel <= 0) ? 1.0 : (travel - v.getTimeUntilNextStation()) / travel;
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;

        int x = (int) (pf.x + ratio * (pt.x - pf.x));
        int y = (int) (pf.y + ratio * (pt.y - pf.y));
        return new Point(x, y);
    }

    // ----- tableau de bord sur le cote droit -----
    private void drawDashboard(Graphics2D g2) {
        int x = MAP_W + 20;
        int y = 40;

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

        g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("TABLEAU DE BORD", x, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        y += 24; g2.drawString("Heure : " + time, x, y);
        y += 26; g2.drawString("Passagers : " + passengers.size(), x, y);
        y += 20; g2.drawString("  en attente : " + waiting, x, y);
        y += 20; g2.drawString("  a bord : " + onboard, x, y);
        y += 20; g2.drawString("  arrives : " + arrived, x, y);

        y += 28; g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("A bord par type", x, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        y += 20; g2.setColor(METRO_C); g2.drawString("  metro : " + metro, x, y);
        y += 20; g2.setColor(TRAIN_C); g2.drawString("  train : " + train, x, y);
        y += 20; g2.setColor(TRAM_C);  g2.drawString("  tram : " + tram, x, y);

        y += 32; g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("SUIVIS", x, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (Passenger p : passengers) {
            if (!p.isTracked()) continue;
            y += 38;
            g2.setColor(TEXT);
            g2.drawString(p.getId() + " [" + p.getState() + "]", x, y);
            y += 16;
            g2.setColor(new Color(170, 175, 185));
            g2.drawString("  " + trackedDetail(p), x, y);
        }

        // legende
        y = 840;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(METRO_C); g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT); g2.drawString("Metro", x + 18, y);
        y += 18;
        g2.setColor(TRAIN_C); g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT); g2.drawString("Train", x + 18, y);
        y += 18;
        g2.setColor(TRAM_C); g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT); g2.drawString("Tram", x + 18, y);
    }

    private String trackedDetail(Passenger p) {
        String pos, eta;
        if (p.getState() == PassengerState.WAITING) {
            pos = "attend a " + p.getCurrentStation().getName();
        } else if (p.getState() == PassengerState.ON_BOARD) {
            var leg = p.getCurrentLeg();
            pos = "dans " + (leg != null ? leg.getVehicleId() : "?")
                    + " -> " + (leg != null ? leg.getTo().getName() : "?");
        } else {
            pos = "arrive a " + p.getDestination().getName();
        }
        if (p.getState() == PassengerState.ARRIVED) eta = "";
        else if (p.getPlan().isEmpty()) eta = " (aucun trajet)";
        else eta = " ETA " + hhmm(p.getPlan().get(p.getPlan().size() - 1).getAlightTime());
        return pos + eta;
    }

    private Color loadColor(int count) {
        if (count == 0) return new Color(120, 126, 140);  // gris
        if (count < 10) return TRAM_C;                    // vert
        if (count < 30) return new Color(235, 200, 60);   // jaune
        return TRAIN_C;                                   // rouge
    }

    private Color colorFor(VehicleType type) {
        switch (type) {
            case TRAIN: return TRAIN_C;
            case METRO: return METRO_C;
            case TRAM:  return TRAM_C;
            default:    return Color.GRAY;
        }
    }

    private double distance(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) return c.getDistance();
        }
        return 1.0;
    }

    private String hhmm(int minute) {
        int h = (minute / 60) % 24;
        int m = minute % 60;
        return String.format("%02d:%02d", h, m);
    }
}