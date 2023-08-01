package Main;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class Rdp {
    private final SortedMap<String, SortedMap<String, Integer>> matrizMap;

    private Map<String, Integer> marcado;

    public Rdp(SortedMap<String, SortedMap<String, Integer>> matrizIncidencia, Map<String, Integer> marcadoInicial) {
        this.matrizMap = Collections.unmodifiableSortedMap(matrizIncidencia);
        this.marcado = marcadoInicial;
    }

    public boolean disparar(String transicion) {

        if (!isSensibilizada(transicion)) {
            return false;
        }

        matrizMap.get(transicion)
                .entrySet()
                .forEach(plazaTok -> marcado.merge(plazaTok.getKey(),
                        plazaTok.getValue(),
                        Integer::sum));

        return true;
    }

    public SortedMap<String, SortedMap<String, Integer>> getMatrizMap() {
        return matrizMap;
    }

    public Matrix getMatriz() {
        List<Map<String, Integer>> rows = matrizMap.entrySet().stream()
                .map(x -> x.getValue())
                .collect(Collectors.toList());

        // T count
        int n = rows.size();
        // P count
        int m = rows.get(0).size();

        Matrix matriz = new Matrix(n, m);
        int i = 0;

        for (Map<String, Integer> row : rows) {
            int j = 0;
            for (Integer value : row.values()) {
                matriz.set(i, j, value);
                j++;
            }
            i++;
        }

        return matriz;
    }

    public Map<String, Integer> getMarcado() {
        return marcado;
    }

    public void setMarcado(Map<String, Integer> estado) {
        marcado = new HashMap<String, Integer>(estado);
    }

    public Set<String> getTrancisiones() {
        return matrizMap.keySet();
    }

    public Set<String> getPlazas() {
        return matrizMap.entrySet().iterator().next().getValue().keySet();
    }

    public Set<String> getTransicionesSensibilizadas() {
        return matrizMap.keySet().stream()
                .filter(t -> isSensibilizada(t))
                .collect(Collectors.toSet());
    }

    private boolean isSensibilizada(String transicion) {
        return matrizMap.get(transicion).entrySet().stream()
                .allMatch(
                        marcadoNecesario -> marcado.get(marcadoNecesario.getKey())
                                + marcadoNecesario.getValue() >= 0);
    }
}
