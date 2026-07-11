package com.programacion.gui;

import com.programacion.model.ImageLayer;
import com.programacion.model.LayerManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Panel de control del visor: permite cargar imágenes, editar el color,
 * la transparencia (alpha) y la profundidad (Z) de la capa actualmente
 * seleccionada, y eliminarla.
 *
 * <p>Cubre las tareas 3.1 a 3.5, 3.8 y 3.9 de la repartición:</p>
 * <ul>
 *   <li>3.1 Panel de controles (JPanel).</li>
 *   <li>3.2 Botón "Cargar Imagen" (JFileChooser vía {@link LayerManager}).</li>
 *   <li>3.3 Selector de color (JColorChooser).</li>
 *   <li>3.4 Slider de transparencia 0-100 → alpha 0.0-1.0.</li>
 *   <li>3.5 Slider de profundidad Z (-10.0 a 10.0).</li>
 *   <li>3.8 Botón "Eliminar Capa".</li>
 *   <li>3.9 Sincronización con la capa seleccionada en {@link LayerListPanel}
 *       y disparo de un callback de repintado para el panel OpenGL (stub
 *       hasta que Mateo entregue el GLRenderer real).</li>
 * </ul>
 */
public class ControlPanel extends JPanel {

    private final LayerManager layerManager;
    private final Runnable onRenderRequest;

    private ImageLayer capaActiva;
    private boolean sincronizando = false;

    private JLabel lblCapaActiva;
    private JButton btnCargar;
    private JButton btnColor;
    private ColorSwatch colorSwatch;
    private JSlider sliderAlpha;
    private JLabel lblAlphaValor;
    private JSlider sliderZ;
    private JLabel lblZValor;
    private JButton btnEliminar;

    /**
     * @param layerManager    gestor compartido de capas.
     * @param layerListPanel  tabla de capas con la que este panel se sincroniza
     *                        (selección de fila → controles activos).
     * @param onRenderRequest callback invocado cada vez que un control modifica
     *                        una capa; en esta entrega es un stub que Mateo
     *                        conectará más adelante a {@code GLCanvas.repaint()}.
     */
    public ControlPanel(LayerManager layerManager, LayerListPanel layerListPanel, Runnable onRenderRequest) {
        this.layerManager = layerManager;
        this.onRenderRequest = onRenderRequest != null ? onRenderRequest : () -> { /* stub sin operación */ };

        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(4, 4, 4, 4));

        add(construirCabecera(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);

        // Tarea 3.9: la tabla notifica selección -> este panel se sincroniza.
        if (layerListPanel != null) {
            layerListPanel.addCapaSelectionListener(this::setCapaActiva);
        }

        setCapaActiva(null);
    }

    /** Constructor de conveniencia sin callback de render (usa un stub interno). */
    public ControlPanel(LayerManager layerManager, LayerListPanel layerListPanel) {
        this(layerManager, layerListPanel, null);
    }

    // ------------------------------------------------------------------
    // Construcción de UI
    // ------------------------------------------------------------------

    private JPanel construirCabecera() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("\uD83C\uDF9B\uFE0F  Panel de Control");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setForeground(new Color(0x2B2B3D));
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        lblCapaActiva = new JLabel("Selecciona una capa para editarla");
        lblCapaActiva.setFont(lblCapaActiva.getFont().deriveFont(Font.PLAIN, 11.5f));
        lblCapaActiva.setForeground(LayerListPanel.COLOR_TEXT_MUTED);
        lblCapaActiva.setAlignmentX(LEFT_ALIGNMENT);

        header.add(titulo);
        header.add(Box.createVerticalStrut(2));
        header.add(lblCapaActiva);
        return header;
    }

    private JComponent construirCuerpo() {
        JPanel cuerpo = new JPanel();
        cuerpo.setOpaque(false);
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));

        cuerpo.add(construirSeccionCargar());
        cuerpo.add(Box.createVerticalStrut(14));
        cuerpo.add(construirSeccionColor());
        cuerpo.add(Box.createVerticalStrut(14));
        cuerpo.add(construirSeccionAlpha());
        cuerpo.add(Box.createVerticalStrut(14));
        cuerpo.add(construirSeccionZ());
        cuerpo.add(Box.createVerticalStrut(18));
        cuerpo.add(construirSeccionEliminar());
        cuerpo.add(Box.createVerticalGlue());
        return cuerpo;
    }

    private JPanel construirTarjeta(String tituloTexto) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LayerListPanel.COLOR_BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getMaximumSize().height));

        if (tituloTexto != null) {
            JLabel lbl = new JLabel(tituloTexto);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
            lbl.setForeground(new Color(0x3D3D52));
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            tarjeta.add(lbl);
            tarjeta.add(Box.createVerticalStrut(8));
        }
        return tarjeta;
    }

    private JPanel construirSeccionCargar() {
        JPanel tarjeta = construirTarjeta(null);

        btnCargar = crearBotonPrimario("\uD83D\uDCC2  Cargar Imagen", LayerListPanel.COLOR_ACCENT);
        btnCargar.addActionListener(e -> doCargarImagen());
        btnCargar.setAlignmentX(LEFT_ALIGNMENT);
        btnCargar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel ayuda = new JLabel("Formatos soportados: PNG, JPG, BMP");
        ayuda.setFont(ayuda.getFont().deriveFont(10.5f));
        ayuda.setForeground(LayerListPanel.COLOR_TEXT_MUTED);
        ayuda.setAlignmentX(LEFT_ALIGNMENT);
        ayuda.setBorder(new EmptyBorder(6, 2, 0, 0));

        tarjeta.add(btnCargar);
        tarjeta.add(ayuda);
        return tarjeta;
    }

    private JPanel construirSeccionColor() {
        JPanel tarjeta = construirTarjeta("\uD83C\uDFA8 Color de la capa");

        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);

        colorSwatch = new ColorSwatch();
        colorSwatch.setPreferredSize(new Dimension(40, 40));
        colorSwatch.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (colorSwatch.isEnabled()) doElegirColor();
            }
        });

        btnColor = crearBotonSecundario("Elegir color…");
        btnColor.addActionListener(e -> doElegirColor());

        fila.add(colorSwatch, BorderLayout.WEST);
        fila.add(btnColor, BorderLayout.CENTER);
        tarjeta.add(fila);
        return tarjeta;
    }

    private JPanel construirSeccionAlpha() {
        JPanel tarjeta = construirTarjeta("\uD83D\uDCA7 Transparencia (Alpha)");

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.setAlignmentX(LEFT_ALIGNMENT);
        lblAlphaValor = new JLabel("100%");
        lblAlphaValor.setFont(lblAlphaValor.getFont().deriveFont(Font.BOLD, 12f));
        lblAlphaValor.setForeground(LayerListPanel.COLOR_ALPHA.darker());
        filaTitulo.add(lblAlphaValor, BorderLayout.EAST);

        sliderAlpha = new JSlider(0, 100, 100);
        sliderAlpha.setOpaque(false);
        sliderAlpha.setAlignmentX(LEFT_ALIGNMENT);
        sliderAlpha.setMajorTickSpacing(25);
        sliderAlpha.setPaintTicks(true);
        sliderAlpha.addChangeListener(e -> {
            lblAlphaValor.setText(sliderAlpha.getValue() + "%");
            if (sincronizando || capaActiva == null) return;
            capaActiva.setAlpha(sliderAlpha.getValue() / 100f);
            notificarCambio();
        });

        tarjeta.add(filaTitulo);
        tarjeta.add(sliderAlpha);
        return tarjeta;
    }

    private JPanel construirSeccionZ() {
        JPanel tarjeta = construirTarjeta("\uD83D\uDCD0 Profundidad (Eje Z)");

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.setAlignmentX(LEFT_ALIGNMENT);
        lblZValor = new JLabel("0.0");
        lblZValor.setFont(lblZValor.getFont().deriveFont(Font.BOLD, 12f));
        lblZValor.setForeground(LayerListPanel.COLOR_Z.darker());
        filaTitulo.add(lblZValor, BorderLayout.EAST);

        // Rango -10.0 a 10.0 representado en décimas: -100..100
        sliderZ = new JSlider(-100, 100, 0);
        sliderZ.setOpaque(false);
        sliderZ.setAlignmentX(LEFT_ALIGNMENT);
        sliderZ.setMajorTickSpacing(50);
        sliderZ.setPaintTicks(true);

        Hashtable<Integer, JLabel> etiquetas = new Hashtable<>();
        etiquetas.put(-100, new JLabel("-10"));
        etiquetas.put(0, new JLabel("0"));
        etiquetas.put(100, new JLabel("+10"));
        sliderZ.setLabelTable(etiquetas);
        sliderZ.setPaintLabels(true);

        sliderZ.addChangeListener(e -> {
            float valor = sliderZ.getValue() / 10f;
            lblZValor.setText(String.format(Locale.US, "%.1f", valor));
            if (sincronizando || capaActiva == null) return;
            capaActiva.setPosZ(valor);
            notificarCambio();
        });

        tarjeta.add(filaTitulo);
        tarjeta.add(sliderZ);
        return tarjeta;
    }

    private JPanel construirSeccionEliminar() {
        JPanel tarjeta = new JPanel();
        tarjeta.setOpaque(false);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);

        btnEliminar = crearBotonPrimario("\uD83D\uDDD1\uFE0F  Eliminar Capa", LayerListPanel.COLOR_DANGER);
        btnEliminar.addActionListener(e -> doEliminarCapa());
        btnEliminar.setAlignmentX(LEFT_ALIGNMENT);
        btnEliminar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        tarjeta.add(btnEliminar);
        return tarjeta;
    }

    private JButton crearBotonPrimario(String texto, Color color) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondo = isEnabled() ? color : new Color(0xD5D5E3);
                if (getModel().isRollover() && isEnabled()) fondo = fondo.darker();
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setForeground(Color.WHITE);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12.5f));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private JButton crearBotonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBackground(Color.WHITE);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LayerListPanel.COLOR_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        return boton;
    }

    // ------------------------------------------------------------------
    // Acciones (Tareas 3.2, 3.3, 3.8)
    // ------------------------------------------------------------------

    private void doCargarImagen() {
        // LayerManager.cargarCapaDesdeArchivo() ya abre el JFileChooser,
        // valida el formato y notifica a sus propios listeners (Tarea 2.3/2.4).
        ImageLayer nueva = layerManager.cargarCapaDesdeArchivo();
        if (nueva != null) {
            setCapaActiva(nueva);
        }
    }

    private void doElegirColor() {
        if (capaActiva == null) return;
        Color elegido = JColorChooser.showDialog(this, "Selecciona un color para la capa", capaActiva.getColorRGB());
        if (elegido != null) {
            capaActiva.setColorRGB(elegido);
            colorSwatch.setColor(elegido);
            notificarCambio();
        }
    }

    private void doEliminarCapa() {
        if (capaActiva == null) return;
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar la capa \"" + capaActiva.getNombre() + "\"? Esta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            layerManager.eliminarCapa(capaActiva.getId());
            setCapaActiva(null);
            onRenderRequest.run();
        }
    }

    private void notificarCambio() {
        if (capaActiva == null) return;
        layerManager.actualizarCapa(capaActiva);
        onRenderRequest.run(); // Tarea 3.9: dispara repaint() del panel OpenGL (stub).
    }

    // ------------------------------------------------------------------
    // Sincronización con la capa seleccionada (Tarea 3.9)
    // ------------------------------------------------------------------

    /** Actualiza todos los controles para reflejar los valores de la capa dada (o los deshabilita si es null). */
    public void setCapaActiva(ImageLayer capa) {
        this.capaActiva = capa;
        sincronizando = true;
        try {
            boolean hay = capa != null;
            colorSwatch.setEnabled(hay);
            btnColor.setEnabled(hay);
            sliderAlpha.setEnabled(hay);
            sliderZ.setEnabled(hay);
            btnEliminar.setEnabled(hay);

            if (hay) {
                lblCapaActiva.setText("\u270F\uFE0F Editando: " + capa.getNombre());
                sliderAlpha.setValue(Math.round(capa.getAlpha() * 100));
                sliderZ.setValue(Math.round(capa.getPosZ() * 10));
                lblAlphaValor.setText(Math.round(capa.getAlpha() * 100) + "%");
                lblZValor.setText(String.format(Locale.US, "%.1f", capa.getPosZ()));
                colorSwatch.setColor(capa.getColorRGB());
            } else {
                lblCapaActiva.setText("Selecciona una capa para editarla");
                sliderAlpha.setValue(100);
                sliderZ.setValue(0);
                lblAlphaValor.setText("--");
                lblZValor.setText("--");
                colorSwatch.setColor(new Color(0xEDEDF6));
            }
        } finally {
            sincronizando = false;
        }
    }

    /** @return la capa que actualmente se está editando, o {@code null} si ninguna. */
    public ImageLayer getCapaActiva() {
        return capaActiva;
    }

    // ------------------------------------------------------------------
    // Componente auxiliar: preview de color
    // ------------------------------------------------------------------

    private static class ColorSwatch extends JComponent {
        private Color color = new Color(0xEDEDF6);

        void setColor(Color c) {
            this.color = (c != null) ? c : new Color(0xEDEDF6);
            repaint();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color pintura = isEnabled() ? color : new Color(0xEDEDF6);
            g2.setColor(pintura);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }
}
