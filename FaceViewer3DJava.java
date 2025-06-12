import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.Timer;


public class FaceViewer3DJava extends JPanel {
    private List<double[][]> faces = new ArrayList<>();
    private double angle = 0;

    public FaceViewer3DJava(File csvFile) throws IOException {
        setBackground(Color.YELLOW);
        readCSV(csvFile);
        Timer timer = new Timer(500, e -> {
            angle += Math.toRadians(5);
            repaint();
        });
        timer.start();
    }

    private void readCSV(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.trim().split(",");
            if (tokens.length != 12) continue;
            double[][] face = new double[4][3];
            for (int i = 0; i < 4; i++) {
                face[i][0] = Double.parseDouble(tokens[i * 3]);
                face[i][1] = Double.parseDouble(tokens[i * 3 + 1]);
                face[i][2] = Double.parseDouble(tokens[i * 3 + 2]);
            }
            faces.add(face);
        }
        br.close();
    }

    private int[] project(double x, double y, double z, int w, int h) {
        // Rotação em Y
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double xr = x * cosA - z * sinA;
        double zr = x * sinA + z * cosA;

        double scale = 1000 / (zr + 500);  // perspectiva simples
        int sx = (int) (xr * scale + w / 2);
        int sy = (int) (-y * scale + h / 2);
        return new int[]{sx, sy};
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));

        for (double[][] face : faces) {
            int[] xPoints = new int[4];
            int[] yPoints = new int[4];
            for (int i = 0; i < 4; i++) {
                int[] pt = project(face[i][0], face[i][1], face[i][2], w, h);
                xPoints[i] = pt[0];
                yPoints[i] = pt[1];
            }
            g2.fillPolygon(xPoints, yPoints, 4);
        }
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        for (double[][] face : faces) {
            int[] xPoints = new int[4];
            int[] yPoints = new int[4];
            for (int i = 0; i < 4; i++) {
                int[] pt = project(face[i][0], face[i][1], face[i][2], w, h);
                xPoints[i] = pt[0];
                yPoints[i] = pt[1];
            }
            g2.drawPolygon(xPoints, yPoints, 4);
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(null);
            if (res != JFileChooser.APPROVE_OPTION) return;

            try {
                JFrame frame = new JFrame("Face Viewer 3D");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                FaceViewer3DJava panel = new FaceViewer3DJava(chooser.getSelectedFile());
                frame.add(panel);
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Erro ao ler o ficheiro: " + e.getMessage());
            }
        });
    }
}

