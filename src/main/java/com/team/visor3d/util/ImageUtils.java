package com.team.visor3d.util;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utilidades para cargar, redimensionar y procesar imágenes 
 * en el contexto del renderizado 3D y vista previa en UI.
 */
public final class ImageUtils {

    // Previene la instanciación de esta clase utilitaria
    private ImageUtils() {
    }

    /**
     * Carga una imagen en formato BufferedImage desde el sistema de archivos.
     * Valida que la extensión sea PNG, JPG o BMP.
     * 
     * @param ruta La ruta absoluta o relativa al archivo de imagen.
     * @return BufferedImage instanciado con los píxeles de la imagen.
     * @throws IOException Si ocurre un problema leyendo el archivo o si el formato no está soportado.
     */
    public static BufferedImage cargarImagen(String ruta) throws IOException {
        File archivo = new File(ruta);
        if (!archivo.exists() || !archivo.isFile()) {
            throw new IOException("El archivo no existe o no es un archivo válido: " + ruta);
        }
        
        String name = archivo.getName().toLowerCase();
        if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp"))) {
            throw new IOException("Formato de archivo no soportado. Se esperaba PNG, JPG o BMP.");
        }
        
        BufferedImage img = ImageIO.read(archivo);
        if (img == null) {
            throw new IOException("No se pudo leer la imagen (contenido corrupto o formato irreconocible).");
        }
        return img;
    }

    /**
     * Redimensiona una imagen al ancho y alto indicados usando SCALE_SMOOTH.
     * 
     * @param imagenOriginal La imagen original que se va a redimensionar.
     * @param ancho El nuevo ancho en píxeles.
     * @param alto El nuevo alto en píxeles.
     * @return Una nueva instancia de BufferedImage redimensionada.
     * @throws IllegalArgumentException Si la imagen es nula o si el ancho o alto son <= 0.
     */
    public static BufferedImage redimensionarImagen(BufferedImage imagenOriginal, int ancho, int alto) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("La imagen original no puede ser nula.");
        }
        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("Las dimensiones deben ser mayores a cero.");
        }
        
        Image tmp = imagenOriginal.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        BufferedImage imagenRedimensionada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = imagenRedimensionada.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return imagenRedimensionada;
    }

    /**
     * Genera una miniatura (thumbnail) de la imagen indicada.
     * Por diseño para la interfaz, genera un thumbnail de 100x100.
     * 
     * @param imagenOriginal La imagen sobre la que se calculará la miniatura.
     * @return El thumbnail resultante de 100x100 px.
     */
    public static BufferedImage generarThumbnail(BufferedImage imagenOriginal) {
        return redimensionarImagen(imagenOriginal, 100, 100);
    }
}