package Importador;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Main.Rdp;

public class ImportadorPIPE implements IImportador {

    @Override
    public Rdp importar(String filename) {

        File xmlFile = new File(filename);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;

        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<String> plazas = getPlazas(doc);
        List<String> transiciones = getTransiciones(doc);

        Map<String, int[]> matrizIncidencia = new HashMap<String, int[]>();

        rellenarMatriz(doc, matrizIncidencia, plazas, transiciones);

        int[] estadoInicial = getEstadoInicial(doc, plazas);

        return new Rdp(plazas, transiciones, matrizIncidencia, estadoInicial);
    }

    private List<String> getPlazas(Document doc) {

        List<String> plazas = new ArrayList<String>();

        NodeList plazasNodes = doc.getElementsByTagName("place");

        for (int i = 0; i < plazasNodes.getLength(); i++) {

            Element plaza = (Element) plazasNodes.item(i);

            String plazaId = plaza.getAttribute("id");

            plazas.add(plazaId);
        }

        return plazas;
    }

    private int[] getEstadoInicial(Document doc, List<String> plazas) {

        int[] estado = new int[plazas.size()];

        NodeList plazasNodes = doc.getElementsByTagName("place");

        for (int i = 0; i < plazasNodes.getLength(); i++) {

            Element plaza = (Element) plazasNodes.item(i);

            Element initialMarking = (Element) plaza
                    .getElementsByTagName("initialMarking")
                    .item(0);

            estado[i] = Integer.parseInt(initialMarking
                    .getElementsByTagName("value")
                    .item(0)
                    .getTextContent()
                    .split(",")[1]);
        }

        return estado;
    }

    private List<String> getTransiciones(Document doc) {

        List<String> transiciones = new ArrayList<String>();

        NodeList transicionesNodes = doc.getElementsByTagName("transition");

        for (int i = 0; i < transicionesNodes.getLength(); i++) {

            Element transicion = (Element) transicionesNodes.item(i);

            String transicionId = transicion.getAttribute("id");

            transiciones.add(transicionId);
        }

        return transiciones;
    }

    private void rellenarMatriz(
            Document doc,
            Map<String, int[]> matrizIncidencia,
            List<String> plazas,
            List<String> transiciones) {

        transiciones.stream().forEach(t -> matrizIncidencia.put(t, new int[plazas.size()]));

        NodeList arcos = doc.getElementsByTagName("arc");

        for (int i = 0; i < arcos.getLength(); i++) {

            Element arco = (Element) arcos.item(i);

            String source = arco.getAttribute("source");

            String target = arco.getAttribute("target");

            Element inscription = (Element) arco
                    .getElementsByTagName("inscription")
                    .item(0);

            int weight = Integer.parseInt(inscription
                    .getElementsByTagName("value")
                    .item(0)
                    .getTextContent()
                    .split(",")[1]);

            int plazaIndex;
            String transicion;

            // Si source es una plaza, es una plaza de entrada
            // Y target una transicion
            if (plazas.contains(source)) {
                plazaIndex = plazas.indexOf(source);
                transicion = target;
                // Signo negativo porque es plaza de entrada
                weight *= -1;
            }
            // Source es una transicion,
            // Target es plaza de salida
            else {
                plazaIndex = plazas.indexOf(target);
                transicion = source;
            }

            matrizIncidencia.get(transicion)[plazaIndex] = weight;
        }
    }
}
