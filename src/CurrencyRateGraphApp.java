import javax.swing.*;
import java.awt.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

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

    public void readDataFromJSON() {
        rates = fetchUSDHUFValues(LocalDate.of(2015, 1, 1), LocalDate.of(2016, 1, 1));
    }

    public double[] getRates() {
        return rates;
    }

    public static double[] fetchUSDHUFValues(LocalDate startDate, LocalDate endDate) {
        List<Double> usdHufList = new ArrayList<>();

        try {
            String urlString = buildURL(startDate, endDate);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseData = new JSONObject(response.toString());
            JSONObject quotes = responseData.getJSONObject("quotes");

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                String formattedDate = currentDate.toString();
                if (quotes.has(formattedDate)) {
                    JSONObject currencyData = quotes.getJSONObject(formattedDate);
                    if (currencyData.has("USDHUF")) {
                        usdHufList.add(currencyData.getDouble("USDHUF"));
                    }
                }
                currentDate = currentDate.plusDays(1);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        double[] usdHufArray = new double[usdHufList.size()];
        for (int i = 0; i < usdHufList.size(); i++) {
            usdHufArray[i] = usdHufList.get(i);
        }
        return usdHufArray;
    }

    private static String buildURL(LocalDate startDate, LocalDate endDate) {
        return "http://api.currencylayer.com/timeframe?start_date=" + startDate +
                "&end_date=" + endDate + "&currencies=HUF&source=USD&access_key=356e06cecfcfb88b593c6734b7f6e11b";
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
        String filePath = "C:\\Users\\tamas\\IdeaProjects\\CurrencyRateGraphApp\\src\\currency_rates.json";
        model.readDataFromJSON();
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
