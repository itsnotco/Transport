/*
 * Interface graphique Swing de la simulation.
 * Affiche la carte du réseau (connexions, stations, véhicules, passager suivi)
 * et un tableau de bord latéral (statistiques, journal d'activité, incidents).
 * Implémente SimulationObserver pour recevoir les événements de mouvement via l'EventBus.
 * L'avancement du temps est délégué au callback onStep, déclenché par la touche ENTRÉE.
 */
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
import simulation.IncidentManager;

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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SwingDisplay extends JPanel implements SimulationObserver {

    private final NetworkGraph graph;
    private final List<Vehicle> vehicles;
    private final List<Passenger> passengers;
    private final IncidentManager incidents;
    // Table de correspondance identifiant de station → coordonnées pixel dans la fenêtre.
    private final Map<String, Point> positions = new HashMap<>();

    private String time = "00:00";
    private int totalArrivals = 0;
    private int currentMinute = 0;

    private Runnable onStep;

    // Taille maximale du journal d'activité affiché dans le tableau de bord.
    private static final int MAX_ACTIVITY = 5;
    private final LinkedList<String> recentActivity = new LinkedList<>();

    // Palette de couleurs fixe pour le fond sombre et les différents types de véhicules.
    private static final Color BG = new Color(24, 26, 32);
    private static final Color LINE = new Color(90, 95, 105);
    private static final Color BOX_FILL = new Color(40, 44, 54);
    private static final Color BOX_EDGE = new Color(120, 126, 140);
    private static final Color TEXT = new Color(225, 228, 235);
    private static final Color METRO_C = new Color(70, 130, 255);
    private static final Color TRAIN_C = new Color(235, 80, 80);
    private static final Color TRAM_C = new Color(70, 200, 110);
    private static final Color TRACKED_C = new Color(255, 220, 50);
    private static final Color TRACKED_RING = new Color(255, 255, 255);
    private static final Color INCIDENT_C = new Color(235, 80, 80);
    private static final Color OK_C = new Color(70, 200, 110);

    private static final int BOX_W = 116;
    private static final int BOX_H = 30;
    private static final int MAP_W = 760;

    public SwingDisplay(NetworkGraph graph, List<Vehicle> vehicles,
                        List<Passenger> passengers, IncidentManager incidents) {
        this.graph = graph;
        this.vehicles = vehicles;
        this.passengers = passengers;
        this.incidents = incidents;
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

    // Reçoit les notifications du VehicleScheduler (pattern Observer) et les enregistre dans le journal d'activité.
    @Override
    public void onEvent(SimulationEvent e) {
        String line;
        if (e.getKind() == SimulationEvent.Kind.DEPARTURE) {
            line = e.getVehicle().getId() + " : depart vers " + e.getTo().getName();
        } else {
            line = e.getVehicle().getId() + " : arrive a " + e.getTo().getName();
        }
        recentActivity.addFirst(line);
        // Maintient la liste à MAX_ACTIVITY éléments en supprimant les entrées les plus anciennes.
        while (recentActivity.size() > MAX_ACTIVITY) {
            recentActivity.removeLast();
        }
        repaint();
    }

    // Branche le callback de pas de simulation et active l'écoute clavier (ENTRÉE = +1 min, Q = quitter).
    public void setOnStep(Runnable onStep) {
        this.onStep = onStep;
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (SwingDisplay.this.onStep != null) {
                        SwingDisplay.this.onStep.run();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                    System.exit(0);
                }
            }
        });

        // Le clic souris remet le focus sur le panneau pour que les raccourcis clavier fonctionnent.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    // Définit manuellement les coordonnées pixel de chaque station pour reproduire la topologie du réseau.
    private void buildPositions() {
        int A = 110, B = 290, C = 470, D = 650;
        put("ST_AERO", C, 50);
        put("ST_ROCH", B, 140);
        put("ST_CENT", C, 140);
        put("ST_BELL", A, 240);
        put("ST_PORT", B, 240);
        put("ST_REP", C, 240);
        put("ST_UNIV", D, 240);
        put("ST_LILAS", A, 340);
        put("ST_ARS", B, 340);
        put("ST_HUGO", C, 340);
        put("ST_TECH", D, 340);
        put("ST_MOUL", A, 440);
        put("ST_CITE", B, 440);
        put("ST_PREF", C, 440);
        put("ST_INNO", D, 440);
        put("ST_JARD", C, 540);
        put("ST_PARC", C, 620);
        put("ST_EGL", C, 700);
        put("ST_BOURG", C, 770);
        put("ST_ACAC", C, 840);
    }

    private void put(String id, int x, int y) {
        positions.put(id, new Point(x, y));
    }

    public void refresh(String time, int totalArrivals, int currentMinute) {
        this.time = time;
        this.totalArrivals = totalArrivals;
        this.currentMinute = currentMinute;
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
        drawTrackedPassengers(g2);
        drawDashboard(g2);
    }

    // Dessine les tronçons en rouge si bloqués par un incident, gris sinon.
    private void drawConnections(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2.5f));
        for (Connection c : graph.getConnections()) {
            Point[] pts = pathPoints(c.getStationA(), c.getStationB());
            if (pts == null) continue;
            g2.setColor(c.isBlocked() ? INCIDENT_C : LINE);
            for (int i = 0; i < pts.length - 1; i++) {
                g2.drawLine(pts[i].x, pts[i].y, pts[i + 1].x, pts[i + 1].y);
            }
        }
        g2.setStroke(new BasicStroke(2.5f));
    }

    // Dessine chaque station sous forme de boîte arrondie avec le nombre de passagers en attente colorisé.
    private void drawStations(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (Station s : graph.getStations()) {
            Point p = positions.get(s.getId());
            if (p == null) continue;

            int x = p.x - BOX_W / 2;
            int y = p.y - BOX_H / 2;

            g2.setColor(BOX_FILL);
            g2.fillRoundRect(x, y, BOX_W, BOX_H, 8, 8);
            // Bordure rouge épaisse si la station est fermée, gris fin sinon.
            if (s.isClosed()) {
                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(INCIDENT_C);
            } else {
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(BOX_EDGE);
            }
            g2.drawRoundRect(x, y, BOX_W, BOX_H, 8, 8);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(TEXT);
            g2.drawString(s.getName(), x + 8, y + 19);

            int count = s.getWaiting().size();
            String n = String.valueOf(count);
            // Couleur du compteur : vert < 10, jaune 10-29, rouge ≥ 30 passagers en attente.
            g2.setColor(loadColor(count));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            int nw = g2.getFontMetrics().stringWidth(n);
            g2.drawString(n, x + BOX_W - nw - 8, y + 20);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        }
    }

    // Positionne chaque véhicule sur la carte ; les véhicules à la même station sont décalés verticalement.
    private void drawVehicles(Graphics2D g2) {
        Map<String, Integer> stacked = new HashMap<>();
        for (Vehicle v : vehicles) {
            Point pos = vehiclePosition(v, stacked);
            if (pos == null) continue;

            g2.setColor(colorFor(v.getType()));
            g2.fillOval(pos.x - 7, pos.y - 7, 14, 14);
            // Cercle rouge épais si hors service, noir fin sinon.
            if (v.getState() == model.VehicleState.OUT_OF_SERVICE) {
                g2.setColor(INCIDENT_C);
                g2.setStroke(new BasicStroke(2.5f));
            } else {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawOval(pos.x - 7, pos.y - 7, 14, 14);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(TEXT);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(v.getId() + " (" + v.getPassengerCount() + ")", pos.x + 10, pos.y + 4);
        }
    }

    // Dessine un anneau blanc + point jaune sur la position du passager suivi (station ou véhicule).
    private void drawTrackedPassengers(Graphics2D g2) {
        for (Passenger p : passengers) {
            if (!p.isTracked()) continue;

            Point pos = trackedPosition(p);
            if (pos == null) continue;

            g2.setColor(TRACKED_RING);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(pos.x - 9, pos.y - 9, 18, 18);

            g2.setColor(TRACKED_C);
            g2.fillOval(pos.x - 6, pos.y - 6, 12, 12);

            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString(p.getId(), pos.x + 12, pos.y + 4);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // Résout la position pixel du passager suivi : sur le véhicule si ON_BOARD, sur la station sinon.
    private Point trackedPosition(Passenger p) {
        if (p.getState() == PassengerState.ON_BOARD) {
            JourneyLeg leg = p.getCurrentLeg();
            if (leg == null) return null;
            for (Vehicle v : vehicles) {
                if (v.getId().equals(leg.getVehicleId())) {
                    return vehiclePosition(v, new HashMap<>());
                }
            }
            return null;
        } else if (p.getState() == PassengerState.WAITING) {
            return positions.get(p.getCurrentStation().getId());
        } else {
            return positions.get(p.getDestination().getId());
        }
    }

    private void drawDashboard(Graphics2D g2) {
        int x = MAP_W + 20;
        int y = 40;

        int waiting = 0, onboard = 0;
        for (Passenger p : passengers) {
            if (p.getState() == PassengerState.WAITING) waiting++;
            else if (p.getState() == PassengerState.ON_BOARD) onboard++;
        }
        // Calcule le nombre de passagers par type de véhicule pour les statistiques détaillées.
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
        y += 24;
        g2.drawString("Heure : " + time, x, y);
        y += 26;
        g2.drawString("Passagers : " + passengers.size(), x, y);
        y += 20;
        g2.drawString("  en attente : " + waiting, x, y);
        y += 20;
        g2.drawString("  a bord : " + onboard, x, y);
        y += 20;
        g2.drawString("  arrives (total) : " + totalArrivals, x, y);

        y += 28;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("A bord par type", x, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        y += 20;
        g2.setColor(METRO_C);
        g2.drawString("  metro : " + metro, x, y);
        y += 20;
        g2.setColor(TRAIN_C);
        g2.drawString("  train : " + train, x, y);
        y += 20;
        g2.setColor(TRAM_C);
        g2.drawString("  tram : " + tram, x, y);

        y += 32;
        g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("ACTIVITE RECENTE", x, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        if (recentActivity.isEmpty()) {
            y += 18;
            g2.setColor(new Color(150, 155, 165));
            g2.drawString("  (en attente...)", x, y);
        } else {
            for (String line : recentActivity) {
                y += 16;
                g2.setColor(new Color(170, 175, 185));
                g2.drawString("  " + line, x, y);
            }
        }

        y += 32;
        g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("SUIVI", x, y);

        for (Passenger p : passengers) {
            if (!p.isTracked()) continue;

            y += 20;
            g2.setColor(TRACKED_C);
            g2.fillOval(x, y - 10, 10, 10);
            g2.setColor(TRACKED_RING);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y - 10, 10, 10);
            g2.setStroke(new BasicStroke(1f));

            g2.setColor(TRACKED_C);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(p.getId() + "  →  " + p.getDestination().getName(), x + 14, y);

            y += 14;
            g2.setColor(new Color(170, 175, 185));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("  " + trackedState(p), x, y);

            String eta = trackedEta(p);
            if (!eta.isEmpty()) {
                y += 13;
                g2.setColor(new Color(130, 200, 130));
                g2.drawString("  " + eta, x, y);
            }
        }

        y += 32;
        g2.setColor(TEXT);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("INCIDENTS", x, y);

        List<IncidentManager.Incident> active =
                (incidents == null) ? Collections.emptyList() : incidents.getActive();

        y += 18;
        if (active.isEmpty()) {
            g2.setColor(OK_C);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.fillOval(x, y - 10, 10, 10);
            g2.drawString("  Reseau nominal", x + 14, y);
        } else {
            g2.setColor(INCIDENT_C);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("  " + active.size() + " actif(s)", x + 14, y);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            for (IncidentManager.Incident i : active) {
                y += 16;
                g2.setColor(INCIDENT_C);
                g2.fillOval(x, y - 9, 8, 8);
                g2.setColor(new Color(225, 180, 180));
                int rem = i.remaining(currentMinute);
                // Affiche la description de l'incident et le temps restant avant résolution.
                g2.drawString("  " + i.describe() + "  (" + rem + " min)", x + 14, y);
            }
        }

        y = 840;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(METRO_C);
        g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT);
        g2.drawString("Metro", x + 18, y);
        y += 18;
        g2.setColor(TRAIN_C);
        g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT);
        g2.drawString("Train", x + 18, y);
        y += 18;
        g2.setColor(TRAM_C);
        g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT);
        g2.drawString("Tram", x + 18, y);
        y += 18;
        g2.setColor(TRACKED_C);
        g2.fillOval(x, y - 10, 12, 12);
        g2.setColor(TEXT);
        g2.drawString("Suivi", x + 18, y);

        y += 30;
        g2.setColor(new Color(150, 155, 165));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("ENTREE = +1 min   |   Q = quitter", x, y);
    }

    private String trackedState(Passenger p) {
        if (p.getState() == PassengerState.WAITING) {
            return "Attend à " + p.getCurrentStation().getName();
        } else if (p.getState() == PassengerState.ON_BOARD) {
            JourneyLeg leg = p.getCurrentLeg();
            if (leg != null) return "Dans " + leg.getVehicleId() + "  →  " + leg.getTo().getName();
            return "En transit (?)";
        } else {
            return "Arrivé à " + p.getDestination().getName();
        }
    }

    // Calcule l'ETA en lisant l'heure de descente du dernier tronçon du plan du passager.
    private String trackedEta(Passenger p) {
        if (p.getState() == PassengerState.ARRIVED) return "";
        List<JourneyLeg> plan = p.getPlan();
        if (plan == null || plan.isEmpty()) return "Aucun trajet disponible";
        return "ETA : " + hhmm(plan.get(plan.size() - 1).getAlightTime());
    }

    // Calcule la position pixel d'un véhicule : à quai avec offset d'empilement, ou interpolée sur le trajet.
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
        Station to = v.getNextStation();
        if (to == null) return null;

        Point[] pts = pathPoints(from, to);
        if (pts == null) return null;

        double travel = distance(from, to) / v.getSpeed();
        // ratio indique la progression entre 0.0 (départ) et 1.0 (arrivée) pour l'interpolation.
        double ratio = travel <= 0 ? 1.0 : (travel - v.getTimeUntilNextStation()) / travel;
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;

        return pointAlong(pts, ratio);
    }

    // Retourne les points de passage du chemin affiché : direct ou avec un coude pour certains tronçons obliques.
    private Point[] pathPoints(Station from, Station to) {
        Point a = positions.get(from.getId());
        Point b = positions.get(to.getId());
        if (a == null || b == null) return null;

        boolean down = involvesIds(from, to, "ST_MOUL", "ST_JARD")
                || involvesIds(from, to, "ST_INNO", "ST_ACAC");
        boolean side = involvesIds(from, to, "ST_ROCH", "ST_BELL")
                || involvesIds(from, to, "ST_CENT", "ST_UNIV");

        if (!down && !side) return new Point[]{a, b};

        // Ajoute un point intermédiaire (coude) pour éviter les diagonales sur certains tronçons.
        Point haut = a.y <= b.y ? a : b;
        Point bas = a.y <= b.y ? b : a;
        Point corner = down ? new Point(haut.x, bas.y) : new Point(bas.x, haut.y);
        return new Point[]{a, corner, b};
    }

    private boolean involvesIds(Station from, Station to, String id1, String id2) {
        String a = from.getId(), b = to.getId();
        return (a.equals(id1) && b.equals(id2)) || (a.equals(id2) && b.equals(id1));
    }

    // Calcule le point exact sur un chemin multi-segments en fonction d'un ratio de progression.
    private Point pointAlong(Point[] pts, double ratio) {
        double total = 0;
        for (int i = 0; i < pts.length - 1; i++) total += segLength(pts[i], pts[i + 1]);
        if (total == 0) return pts[0];

        double target = ratio * total;
        double acc = 0;
        for (int i = 0; i < pts.length - 1; i++) {
            double seg = segLength(pts[i], pts[i + 1]);
            if (acc + seg >= target) {
                double r = seg == 0 ? 0 : (target - acc) / seg;
                int x = (int) (pts[i].x + r * (pts[i + 1].x - pts[i].x));
                int y = (int) (pts[i].y + r * (pts[i + 1].y - pts[i].y));
                return new Point(x, y);
            }
            acc += seg;
        }
        return pts[pts.length - 1];
    }

    private double segLength(Point a, Point b) {
        int dx = b.x - a.x, dy = b.y - a.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Retourne une couleur selon la charge de la station : gris = vide, vert = faible, jaune = moyen, rouge = élevé.
    private Color loadColor(int count) {
        if (count == 0) return new Color(120, 126, 140);
        if (count < 10) return TRAM_C;
        if (count < 30) return new Color(235, 200, 60);
        return TRAIN_C;
    }

    private Color colorFor(VehicleType type) {
        switch (type) {
            case TRAIN:
                return TRAIN_C;
            case METRO:
                return METRO_C;
            case TRAM:
                return TRAM_C;
            default:
                return Color.GRAY;
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