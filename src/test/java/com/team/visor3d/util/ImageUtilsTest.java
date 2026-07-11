package com.team.visor3d.util;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class ImageUtilsTest {

    @Test
    public void testRedimensionarImagen_CorrectDimensions() {
        // Preparar
        BufferedImage imgOriginal = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        int nuevoAncho = 200;
        int nuevoAlto = 150;

        // Ejecutar
        BufferedImage imgRedimensionada = ImageUtils.redimensionarImagen(imgOriginal, nuevoAncho, nuevoAlto);

        // Validar
        assertNotNull(imgRedimensionada, "La imagen devuelta no debería ser nula");
        assertEquals(nuevoAncho, imgRedimensionada.getWidth(), "El ancho no coincide con lo esperado");
        assertEquals(nuevoAlto, imgRedimensionada.getHeight(), "El alto no coincide con lo esperado");
    }

    @Test
    public void testRedimensionarImagen_NullImage_ThrowsException() {
        // Ejecutar y Validar
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageUtils.redimensionarImagen(null, 100, 100);
        });
        
        assertTrue(exception.getMessage().contains("nula"), "El mensaje de error no es el esperado");
    }

    @Test
    public void testRedimensionarImagen_InvalidDimensions_ThrowsException() {
        BufferedImage imgOriginal = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        // Ejecutar y Validar
        assertThrows(IllegalArgumentException.class, () -> {
            ImageUtils.redimensionarImagen(imgOriginal, 0, 100);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ImageUtils.redimensionarImagen(imgOriginal, 100, -5);
        });
    }

    @Test
    public void testGenerarThumbnail_Returns100x100() {
        // Preparar
        BufferedImage imgOriginal = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

        // Ejecutar
        BufferedImage thumbnail = ImageUtils.generarThumbnail(imgOriginal);

        // Validar
        assertNotNull(thumbnail);
        assertEquals(100, thumbnail.getWidth(), "El thumbnail debe tener un ancho de 100");
        assertEquals(100, thumbnail.getHeight(), "El thumbnail debe tener un alto de 100");
    }
}