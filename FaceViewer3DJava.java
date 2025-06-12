import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FaceViewer3DJava extends JPanel {
    private static class Point3D {
        double x, y, z;
        Point3D(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
        }
    }

    private final List<Point3D[]> faces = new ArrayList<>();
    private double angle = 0;

    public FaceViewer3DJava(File csvFile) throws IOException {
        setBackground(Color.YELLOW);
        loadCSV(csvFile);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                angle += Math.toRadians(5);
                angle=angle % ( Math.PI * 2.00) ;
                repaint();
            }
        }, 100, 100);
    }

    private void loadCSV(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split(",");
            if (parts.length != 12) continue;
            Point3D[] face = new Point3D[4];
            for (int i = 0; i < 4; i++) {
                double x = Double.parseDouble(parts[i * 3]);
                double y = Double.parseDouble(parts[i * 3 + 1]);
                double z = Double.parseDouble(parts[i * 3 + 2]);
                face[i] = new Point3D(x, y, z);
            }
            faces.add(face);
        }
        reader.close();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2, cy = h / 2;
        double scale = 100;

        // Draw filled black faces first
        for (Point3D[] face : faces) {
            Polygon poly = new Polygon();
            for (Point3D p : face) {
                Point2D proj = project(rotateY(p, angle), scale, cx, cy);
                poly.addPoint((int) proj.x, (int) proj.y);
            }
            g2.setColor(Color.BLACK);
            g2.fillPolygon(poly);
        }

        // Draw white outlines after
        for (Point3D[] face : faces) {
            g2.setColor(Color.WHITE);
            for (int i = 0; i < 4; i++) {
                Point2D p1 = project(rotateY(face[i], angle), scale, cx, cy);
                Point2D p2 = project(rotateY(face[(i + 1) % 4], angle), scale, cx, cy);
                g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        }
    }

    private Point3D rotateY(Point3D p, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        double x = p.x * cos - p.z * sin;
        double z = p.x * sin + p.z * cos;
        return new Point3D(x, p.y, z);
    }

    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    private Point2D project(Point3D p, double scale, int cx, int cy) {
        double zOffset = 5;
        double factor = scale / (p.z + zOffset);
        return new Point2D(cx + p.x * factor, cy - p.y * factor);
    }

    public static void main(String[] args) throws Exception {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        JFrame frame = new JFrame("Face Viewer 3D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new FaceViewer3DJava(chooser.getSelectedFile()));
        frame.setVisible(true);
    }
}
