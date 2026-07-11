package com.team.visor3d.model;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Gestiona la lista de capas (ImageLayer) del visor 3D.
 * Implementa un mecanismo simple de observación para que otros componentes
 * (como ControlPanel o GLRenderer) escuchen cambios en las capas.
 */
public class LayerManager {
    private final List<ImageLayer> capas;
    private final List<Consumer<List<ImageLayer>>> listeners;

    /**
     * Inicializa el gestor de capas con listas vacías.
     */
    public LayerManager() {
        this.capas = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Añade un suscriptor a los cambios del estado de las capas.
     * 
     * @param listener El callback a ser ejecutado cuando haya cambios.
     */
    public void addLayerChangeListener(Consumer<List<ImageLayer>> listener) {
        this.listeners.add(listener);
    }

    /**
     * Elimina un suscriptor.
     * 
     * @param listener El callback a remover.
     */
    public void removeLayerChangeListener(Consumer<List<ImageLayer>> listener) {
        this.listeners.remove(listener);
    }

    /**
     * Notifica a todos los suscriptores sobre los cambios actuales.
     */
    private void notificarCambios() {
        List<ImageLayer> copiaInmutable = getCapas();
        for (Consumer<List<ImageLayer>> listener : listeners) {
            listener.accept(copiaInmutable);
        }
    }

    /**
     * Agrega una nueva capa.
     * 
     * @param capa La capa a ser agregada.
     */
    public void agregarCapa(ImageLayer capa) {
        if (capa != null) {
            this.capas.add(capa);
            notificarCambios();
        }
    }

    /**
     * Elimina una capa según su identificador.
     * 
     * @param id Identificador de la capa a eliminar.
     * @return true si la capa existía y se eliminó, false caso contrario.
     */
    public boolean eliminarCapa(String id) {
        boolean eliminada = this.capas.removeIf(capa -> capa.getId().equals(id));
        if (eliminada) {
            notificarCambios();
        }
        return eliminada;
    }

    /**
     * Busca una capa por su identificador.
     * 
     * @param id El identificador a buscar.
     * @return Un Optional que contiene la capa, si fue encontrada.
     */
    public Optional<ImageLayer> getCapaPorId(String id) {
        return this.capas.stream()
                .filter(capa -> capa.getId().equals(id))
                .findFirst();
    }

    /**
     * Retorna todas las capas registradas actualmente.
     * 
     * @return Una vista inmutable de la lista de capas.
     */
    public List<ImageLayer> getCapas() {
        return Collections.unmodifiableList(this.capas);
    }

    /**
     * Actualiza la información de una capa existente (debe coincidir el id).
     * 
     * @param capaActualizada El objeto capa con los nuevos datos.
     */
    public void actualizarCapa(ImageLayer capaActualizada) {
        if (capaActualizada == null) return;
        
        for (int i = 0; i < capas.size(); i++) {
            if (capas.get(i).getId().equals(capaActualizada.getId())) {
                capas.set(i, capaActualizada);
                notificarCambios();
                return;
            }
        }
    }

    /**
     * Solicita al usuario seleccionar una imagen a través de un JFileChooser
     * y, si es válida (PNG, JPG, BMP), la añade al gestor de capas.
     * 
     * @return La nueva capa creada, o null si se canceló la operación o el archivo no es válido.
     */
    public ImageLayer cargarCapaDesdeArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Capa de Imagen");
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Imágenes soportadas (PNG, JPG, BMP)", "png", "jpg", "jpeg", "bmp");
        fileChooser.addChoosableFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String name = file.getName().toLowerCase();
            
            // Validar que la extensión es la correcta
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
                ImageLayer nuevaCapa = new ImageLayer(
                        UUID.randomUUID().toString(),
                        file.getAbsolutePath(),
                        file.getName()
                );
                agregarCapa(nuevaCapa);
                return nuevaCapa;
            }
        }
        return null;
    }
}