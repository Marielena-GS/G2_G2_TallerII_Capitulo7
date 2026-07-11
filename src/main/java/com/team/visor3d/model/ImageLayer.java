package com.team.visor3d.model;

import java.awt.Color;

/**
 * Representa una capa de imagen en el visor 3D.
 * Contiene la información necesaria para el renderizado y la gestión de la capa.
 */
public class ImageLayer {
    private String id;
    private String ruta;
    private int textureID;
    private float posZ;
    private Color colorRGB;
    private float alpha;
    private String nombre;

    /**
     * Constructor por defecto.
     * Crea una nueva capa con valores predeterminados (alpha 1.0, z=0, color blanco).
     */
    public ImageLayer() {
        this.colorRGB = Color.WHITE;
        this.alpha = 1.0f;
        this.posZ = 0.0f;
    }

    /**
     * Constructor con parámetros principales.
     * 
     * @param id Identificador único de la capa.
     * @param ruta Ruta del archivo de imagen en el disco.
     * @param nombre Nombre descriptivo de la capa.
     */
    public ImageLayer(String id, String ruta, String nombre) {
        this();
        this.id = id;
        this.ruta = ruta;
        this.nombre = nombre;
    }

    /**
     * @return El identificador de la capa.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id El nuevo identificador de la capa.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return La ruta del archivo.
     */
    public String getRuta() {
        return ruta;
    }

    /**
     * @param ruta La nueva ruta del archivo.
     */
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    /**
     * @return El ID de textura para OpenGL.
     */
    public int getTextureID() {
        return textureID;
    }

    /**
     * @param textureID El nuevo ID de textura.
     */
    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }

    /**
     * @return La posición en el eje Z.
     */
    public float getPosZ() {
        return posZ;
    }

    /**
     * @param posZ La nueva posición en el eje Z.
     */
    public void setPosZ(float posZ) {
        this.posZ = posZ;
    }

    /**
     * @return El color de tinte de la capa.
     */
    public Color getColorRGB() {
        return colorRGB;
    }

    /**
     * @param colorRGB El nuevo color de tinte.
     */
    public void setColorRGB(Color colorRGB) {
        this.colorRGB = colorRGB;
    }

    /**
     * @return El nivel de transparencia (0.0 a 1.0).
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * @param alpha El nuevo nivel de transparencia (0.0 a 1.0).
     */
    public void setAlpha(float alpha) {
        if (alpha < 0.0f) alpha = 0.0f;
        if (alpha > 1.0f) alpha = 1.0f;
        this.alpha = alpha;
    }

    /**
     * @return El nombre descriptivo de la capa.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre El nuevo nombre de la capa.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}