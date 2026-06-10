package display;

import model.Connection;
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
    private final Map<String, Point> positions = new HashMap<>();

    public SwingDisplay(NetworkGraph graph, List<Vehicle> vehicles) {
        this.graph = graph;
        this.vehicles = vehicles;
        buildPositions();
        setPreferredSize(new Dimension(760, 820));
        setBackground(Color.WHITE);

        JFrame frame = new JFrame("Reseau de transport");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Positions (x,y) de chaque gare, reproduisant le schema de l'annexe.
    // 4 colonnes (A gauche -> D droite) et des lignes par niveau.
    private void buildPositions() {
        int A = 90, B = 250, C = 420, D = 610;
        put("ST_AERO",  C, 40);
        put("ST_ROCH",  B, 120);
        put("ST_CENT",  C, 120);
        put("ST_BELL",  A, 200);
        put("ST_PORT",  B, 200);
        put("ST_REP",   C, 200);
        put("ST_UNIV",  D, 200);
        put("ST_LILAS", A, 280);
        put("ST_ARS",   B, 280);
        put("ST_HUGO",  C, 280);
        put("ST_TECH",  D, 280);
        put("ST_MOUL",  A, 360);
        put("ST_CITE",  B, 360);
        put("ST_PREF",  C, 360);
        put("ST_JARD",  C, 440);
        put("ST_INNO",  D, 440);
        put("ST_PARC",  C, 520);
        put("ST_EGL",   C, 590);
        put("ST_BOURG", C, 660);
        put("ST_ACAC",  C, 730);
    }

    private void put(String id, int x, int y) {
        positions.put(id, new Point(x, y));
    }

    // Appelee par Main apres chaque tick pour rafraichir l'affichage
    public void refresh() {
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
    }

    private void drawConnections(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2f));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

        for (Connection c : graph.getConnections()) {
            Point a = positions.get(c.getStationA().getId());
            Point b = positions.get(c.getStationB().getId());
            if (a == null || b == null) continue;

            g2.setColor(new Color(180, 180, 180));
            g2.drawLine(a.x, a.y, b.x, b.y);

            int mx = (a.x + b.x) / 2;
            int my = (a.y + b.y) / 2;
            g2.setColor(new Color(120, 120, 120));
            g2.drawString(String.valueOf((int) c.getDistance()), mx + 3, my);
        }
    }

    private void drawStations(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Station s : graph.getStations()) {
            Point p = positions.get(s.getId());
            if (p == null) continue;

            String name = s.getName();
            int w = g2.getFontMetrics().stringWidth(name) + 12;
            int h = 20;
            int x = p.x - w / 2;
            int y = p.y - h / 2;

            g2.setColor(new Color(235, 240, 250));
            g2.fillRect(x, y, w, h);
            g2.setColor(new Color(90, 90, 90));
            g2.drawRect(x, y, w, h);
            g2.setColor(Color.BLACK);
            g2.drawString(name, x + 6, y + 14);
        }
    }

    private void drawVehicles(Graphics2D g2) {
        // compteur de vehicules a quai par gare, pour les empiler sans chevauchement
        Map<String, Integer> stacked = new HashMap<>();

        for (Vehicle v : vehicles) {
            Point pos = vehiclePosition(v, stacked);
            if (pos == null) continue;

            g2.setColor(colorFor(v.getType()));
            g2.fillOval(pos.x - 6, pos.y - 6, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawOval(pos.x - 6, pos.y - 6, 12, 12);

            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            // ICI : ajouter le nombre de passagers quand ils seront implementes,
            // ex: g2.drawString(v.getId() + " (" + v.getPassengerCount() + ")", ...)
            g2.drawString(v.getId(), pos.x + 8, pos.y + 4);
        }
    }

    // Position pixel d'un vehicule : a sa gare si gare, interpolee si en transit
    private Point vehiclePosition(Vehicle v, Map<String, Integer> stacked) {
        List<Station> route = v.getRoute();
        if (route.isEmpty()) return null;

        if (v.isParked()) {
            Station here = v.getCurrentStation();
            Point base = positions.get(here.getId());
            if (base == null) return null;
            int n = stacked.getOrDefault(here.getId(), 0);
            stacked.put(here.getId(), n + 1);
            return new Point(base.x + 14 + n * 16, base.y - 14);
        }

        // en transit : on interpole entre la gare quittee et la suivante
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

    private double distance(Station a, Station b) {
        for (Connection c : graph.getConnections()) {
            if (c.involves(a) && c.involves(b)) {
                return c.getDistance();
            }
        }
        return 1.0;
    }

    private Color colorFor(VehicleType type) {
        switch (type) {
            case TRAIN: return new Color(200, 60, 60);   // rouge
            case METRO: return new Color(60, 90, 200);   // bleu
            case TRAM:  return new Color(50, 160, 80);   // vert
            default:    return Color.GRAY;
        }
    }
}