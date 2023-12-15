import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.json.*;

public class CurrencyRateGraphApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CurrencyRateModel model = new CurrencyRateModel();
            CurrencyRateView view = new CurrencyRateView(model);
            new CurrencyRateController(view, model);
        });
    }
}

class CurrencyRateModel {
    private double[] rates;

    public void readDataFromJSONFile(String filename) {
        StringBuilder jsonData = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                jsonData.append(line);
            }
            br.close();

            JSONObject jsonObject = new JSONObject(jsonData.toString());
            JSONArray ratesArray = jsonObject.getJSONArray("rates");
            rates = new double[ratesArray.length()];

            for (int i = 0; i < ratesArray.length(); i++) {
                rates[i] = ratesArray.getDouble(i);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public double[] getRates() {
        return rates;
    }
}

class CurrencyRateView {
    private JFrame frame;
    private CurrencyRateModel model;
    private GraphPanel graphPanel;

    public CurrencyRateView(CurrencyRateModel model) {
        this.model = model;
        frame = new JFrame("Currency Rate Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        graphPanel = new GraphPanel();
        frame.add(graphPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    public void updateGraph() {
        graphPanel.setRates(model.getRates());
        graphPanel.repaint();
    }
}

class CurrencyRateController {
    private CurrencyRateView view;
    private CurrencyRateModel model;

    public CurrencyRateController(CurrencyRateView view, CurrencyRateModel model) {
        this.view = view;
        this.model = model;
        String filePath = "C:\\Users\\tamas\\Documents\\Java\\currency_rates.json"; /* C:/path/to/your/directory/ */
        model.readDataFromJSONFile(filePath);
        view.updateGraph();
    }
}

class GraphPanel extends JPanel {
    private double[] rates;

    public void setRates(double[] rates) {
        this.rates = rates;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (rates != null && rates.length > 1) {
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            int margin = 20;
            int graphWidth = width - 2 * margin;
            int graphHeight = height - 2 * margin;

            double maxRate = getMaxRate();
            double minRate = getMinRate();

            g2d.setColor(Color.BLUE);
            for (int i = 0; i < rates.length - 1; i++) {
                int x1 = i * graphWidth / (rates.length - 1) + margin;
                int x2 = (i + 1) * graphWidth / (rates.length - 1) + margin;
                int y1 = (int) ((maxRate - rates[i]) * graphHeight / (maxRate - minRate)) + margin;
                int y2 = (int) ((maxRate - rates[i + 1]) * graphHeight / (maxRate - minRate)) + margin;
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private double getMaxRate() {
        double max = rates[0];
        for (double rate : rates) {
            if (rate > max) {
                max = rate;
            }
        }
        return max;
    }

    private double getMinRate() {
        double min = rates[0];
        for (double rate : rates) {
            if (rate < min) {
                min = rate;
            }
        }
        return min;
    }
}
