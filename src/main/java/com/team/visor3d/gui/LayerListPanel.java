package com.programacion.gui;

import com.programacion.model.ImageLayer;
import com.programacion.model.LayerManager;
import com.programacion.util.ImageUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Panel que muestra, en una tabla moderna e interactiva, todas las capas
 * (ImageLayer) registradas en el {@link LayerManager}.
 *
 * <p>Responsabilidades (Tarea 3.6 - 3.7):</p>
 * <ul>
 *   <li>Renderizar Nombre, Z, Alpha y un preview de Color por cada capa.</li>
 *   <li>Permitir seleccionar una capa activa haciendo click en la fila.</li>
 *   <li>Notificar la selección a quien esté interesado (normalmente
 *       {@link ControlPanel}) mediante un listener sencillo.</li>
 * </ul>
 *
 * El panel se mantiene sincronizado automáticamente con el LayerManager:
 * cada vez que se agrega, elimina o actualiza una capa desde cualquier
 * parte de la aplicación, la tabla se refresca sola.
 */
public class LayerListPanel extends JPanel {

    // ---- Paleta compartida (usada también por ControlPanel) ----------
    public static final Color COLOR_ACCENT = new Color(0x6C5CE7);
    public static final Color COLOR_ACCENT_DARK = new Color(0x5645C4);
    public static final Color COLOR_DANGER = new Color(0xFF5C7C);
    public static final Color COLOR_Z = new Color(0x00B8A9);
    public static final Color COLOR_ALPHA = new Color(0xFFA726);
    public static final Color COLOR_BORDER = new Color(0xE3E3EF);
    public static final Color COLOR_TEXT_MUTED = new Color(0x8A8AA3);
    public static final Color COLOR_ROW_ALT = new Color(0xF7F7FC);
    public static final Color COLOR_ROW_HOVER = new Color(0xF1EEFF);
    public static final Color COLOR_SELECTED = new Color(0xE7E1FF);

    private static final int ALTO_FILA = 58;
    private static final int MINIATURA_PX = 38;
    private static final ImageIcon PLACEHOLDER_ICON = crearIconoPlaceholder();

    private final LayerManager layerManager;
    private final CapaTableModel tableModel;
    private final JTable table;
    private final JLabel lblBadgeContador;
    private final CardLayout cardLayout;
    private final JPanel centro;

    private final Map<String, ImageIcon> cacheMiniaturas = new HashMap<>();
    private final List<Consumer<ImageLayer>> selectionListeners = new ArrayList<>();

    public LayerListPanel(LayerManager layerManager) {
        this.layerManager = layerManager;
        this.tableModel = new CapaTableModel();

        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(4, 4, 4, 4));

        lblBadgeContador = crearPill("0 capas", COLOR_ACCENT);
        add(construirCabecera(), BorderLayout.NORTH);

        table = construirTabla();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(420, 320));

        JPanel estadoVacio = construirEstadoVacio();

        cardLayout = new CardLayout();
        centro = new JPanel(cardLayout);
        centro.setOpaque(false);
        centro.add(scroll, "TABLA");
        centro.add(estadoVacio, "VACIO");
        add(centro, BorderLayout.CENTER);

        // Sincronización automática con el modelo de datos compartido.
        layerManager.addLayerChangeListener(capas -> SwingUtilities.invokeLater(this::refrescar));
        refrescar();
    }

    // ------------------------------------------------------------------
    // Construcción de UI
    // ------------------------------------------------------------------

    private JPanel construirCabecera() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titulo = new JLabel("\uD83D\uDDC2\uFE0F  Capas Cargadas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setForeground(new Color(0x2B2B3D));

        JLabel subtitulo = new JLabel("Selecciona una fila para editarla en el panel de control");
        subtitulo.setFont(subtitulo.getFont().deriveFont(Font.PLAIN, 11.5f));
        subtitulo.setForeground(COLOR_TEXT_MUTED);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);

        header.add(textos, BorderLayout.WEST);
        header.add(lblBadgeContador, BorderLayout.EAST);
        return header;
    }

    private JPanel construirEstadoVacio() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1, true));

        JLabel icono = new JLabel("\uD83D\uDDBC\uFE0F", SwingConstants.CENTER);
        icono.setFont(icono.getFont().deriveFont(40f));

        JLabel texto = new JLabel("Aún no hay capas cargadas", SwingConstants.CENTER);
        texto.setFont(texto.getFont().deriveFont(Font.BOLD, 13f));
        texto.setForeground(new Color(0x555566));

        JLabel ayuda = new JLabel("Usa el botón \"Cargar Imagen\" del panel de control", SwingConstants.CENTER);
        ayuda.setFont(ayuda.getFont().deriveFont(Font.PLAIN, 11f));
        ayuda.setForeground(COLOR_TEXT_MUTED);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        icono.setAlignmentX(CENTER_ALIGNMENT);
        texto.setAlignmentX(CENTER_ALIGNMENT);
        ayuda.setAlignmentX(CENTER_ALIGNMENT);
        contenido.add(icono);
        contenido.add(Box.createVerticalStrut(8));
        contenido.add(texto);
        contenido.add(Box.createVerticalStrut(2));
        contenido.add(ayuda);

        panel.add(contenido);
        return panel;
    }

    private JTable construirTabla() {
        JTable t = new JTable(tableModel);
        t.setRowHeight(ALTO_FILA);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(COLOR_SELECTED);
        t.setSelectionForeground(new Color(0x2B2B3D));
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.putClientProperty("hoverRow", -1);
        t.setFont(t.getFont().deriveFont(12f));

        JTableHeader cabecera = t.getTableHeader();
        cabecera.setFont(cabecera.getFont().deriveFont(Font.BOLD, 11f));
        cabecera.setForeground(COLOR_TEXT_MUTED);
        cabecera.setBackground(Color.WHITE);
        cabecera.setPreferredSize(new Dimension(cabecera.getWidth(), 30));

        // Hover interactivo: resalta la fila bajo el cursor.
        t.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = t.rowAtPoint(e.getPoint());
                Object actual = t.getClientProperty("hoverRow");
                if (!Integer.valueOf(fila).equals(actual)) {
                    t.putClientProperty("hoverRow", fila);
                    t.repaint();
                }
            }
        });
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                t.putClientProperty("hoverRow", -1);
                t.repaint();
            }
        });

        // Selección de fila -> notifica capa activa (Tarea 3.7 / 3.9).
        t.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int fila = t.getSelectedRow();
            ImageLayer capa = (fila >= 0) ? tableModel.getCapaAt(fila) : null;
            fireSeleccion(capa);
        });

        TableColumn colMiniatura = t.getColumnModel().getColumn(0);
        colMiniatura.setPreferredWidth(56);
        colMiniatura.setMaxWidth(56);
        colMiniatura.setCellRenderer(new MiniaturaRenderer());

        TableColumn colNombre = t.getColumnModel().getColumn(1);
        colNombre.setPreferredWidth(160);
        colNombre.setCellRenderer(new NombreRenderer());

        TableColumn colZ = t.getColumnModel().getColumn(2);
        colZ.setPreferredWidth(90);
        colZ.setCellRenderer(new ZRenderer());

        TableColumn colAlpha = t.getColumnModel().getColumn(3);
        colAlpha.setPreferredWidth(100);
        colAlpha.setCellRenderer(new AlphaRenderer());

        TableColumn colColor = t.getColumnModel().getColumn(4);
        colColor.setPreferredWidth(110);
        colColor.setCellRenderer(new ColorRenderer());

        return t;
    }

    private static JLabel crearPill(String texto, Color color) {
        JLabel pill = new JLabel(texto, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setBackground(color);
        pill.setForeground(Color.WHITE);
        pill.setFont(pill.getFont().deriveFont(Font.BOLD, 11f));
        pill.setBorder(new EmptyBorder(4, 12, 4, 12));
        return pill;
    }

    // ------------------------------------------------------------------
    // API pública
    // ------------------------------------------------------------------

    /** Registra un observador que será notificado cuando cambie la fila seleccionada. */
    public void addCapaSelectionListener(Consumer<ImageLayer> listener) {
        if (listener != null) selectionListeners.add(listener);
    }

    /** @return la capa actualmente seleccionada en la tabla, o {@code null} si ninguna lo está. */
    public ImageLayer getCapaSeleccionada() {
        int fila = table.getSelectedRow();
        return fila >= 0 ? tableModel.getCapaAt(fila) : null;
    }

    /** Selecciona programáticamente la capa con el id indicado (por ejemplo, tras cargarla). */
    public void seleccionarCapa(String id) {
        int fila = tableModel.indexOfId(id);
        if (fila >= 0) {
            table.setRowSelectionInterval(fila, fila);
            table.scrollRectToVisible(table.getCellRect(fila, 0, true));
        }
    }

    /** Limpia la selección actual de la tabla. */
    public void limpiarSeleccion() {
        table.clearSelection();
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private void fireSeleccion(ImageLayer capa) {
        for (Consumer<ImageLayer> l : selectionListeners) {
            l.accept(capa);
        }
    }

    private void refrescar() {
        List<ImageLayer> capas = layerManager.getCapas();
        String idPrevio = getCapaSeleccionada() != null ? getCapaSeleccionada().getId() : null;

        tableModel.setCapas(capas);
        lblBadgeContador.setText(capas.size() + (capas.size() == 1 ? " capa" : " capas"));

        cardLayout.show(centro, capas.isEmpty() ? "VACIO" : "TABLA");

        if (idPrevio != null) {
            seleccionarCapa(idPrevio);
        }
    }

    private Color colorDeFondo(JTable t, int row, boolean isSelected) {
        if (isSelected) return COLOR_SELECTED;
        Object hover = t.getClientProperty("hoverRow");
        if (hover instanceof Integer && (Integer) hover == row) return COLOR_ROW_HOVER;
        return (row % 2 == 0) ? Color.WHITE : COLOR_ROW_ALT;
    }

    private ImageIcon obtenerMiniatura(ImageLayer capa) {
        if (capa == null) return PLACEHOLDER_ICON;
        ImageIcon cacheada = cacheMiniaturas.get(capa.getId());
        if (cacheada != null) return cacheada;

        cacheMiniaturas.put(capa.getId(), PLACEHOLDER_ICON);
        cargarMiniaturaAsync(capa);
        return PLACEHOLDER_ICON;
    }

    /** Carga la miniatura en segundo plano para no congelar la interfaz (uso de SwingWorker). */
    private void cargarMiniaturaAsync(ImageLayer capa) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    BufferedImage original = ImageUtils.cargarImagen(capa.getRuta());
                    BufferedImage mini = ImageUtils.generarThumbnail(original);
                    Image escalada = mini.getScaledInstance(MINIATURA_PX, MINIATURA_PX, Image.SCALE_SMOOTH);
                    return new ImageIcon(escalada);
                } catch (IOException ex) {
                    return PLACEHOLDER_ICON;
                }
            }

            @Override
            protected void done() {
                try {
                    cacheMiniaturas.put(capa.getId(), get());
                    table.repaint();
                } catch (Exception ignored) {
                    // Si falla la carga, se conserva el placeholder ya asignado.
                }
            }
        }.execute();
    }

    private static ImageIcon crearIconoPlaceholder() {
        BufferedImage img = new BufferedImage(MINIATURA_PX, MINIATURA_PX, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0xEDEDF6));
        g2.fillRoundRect(0, 0, MINIATURA_PX, MINIATURA_PX, 10, 10);
        g2.setColor(new Color(0xB9B9D0));
        Font f = new Font("SansSerif", Font.PLAIN, 16);
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        String s = "\uD83D\uDDBC";
        int x = (MINIATURA_PX - fm.stringWidth(s)) / 2;
        int y = (MINIATURA_PX + fm.getAscent()) / 2 - 4;
        g2.drawString(s, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }

    // ------------------------------------------------------------------
    // Modelo de tabla
    // ------------------------------------------------------------------

    private static class CapaTableModel extends AbstractTableModel {
        private static final String[] COLUMNAS = {"", "Nombre", "Z", "Alpha", "Color"};
        private List<ImageLayer> capas = new ArrayList<>();

        void setCapas(List<ImageLayer> nuevas) {
            this.capas = new ArrayList<>(nuevas);
            fireTableDataChanged();
        }

        ImageLayer getCapaAt(int row) {
            if (row < 0 || row >= capas.size()) return null;
            return capas.get(row);
        }

        int indexOfId(String id) {
            for (int i = 0; i < capas.size(); i++) {
                if (capas.get(i).getId().equals(id)) return i;
            }
            return -1;
        }

        @Override
        public int getRowCount() {
            return capas.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNAS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNAS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return capas.get(rowIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Renderers personalizados
    // ------------------------------------------------------------------

    private class MiniaturaRenderer extends JLabel implements TableCellRenderer {
        MiniaturaRenderer() {
            setHorizontalAlignment(CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            ImageLayer capa = (ImageLayer) value;
            setBackground(colorDeFondo(t, row, isSelected));
            setIcon(obtenerMiniatura(capa));
            return this;
        }
    }

    private class NombreRenderer extends JLabel implements TableCellRenderer {
        NombreRenderer() {
            setOpaque(true);
            setBorder(new EmptyBorder(0, 6, 0, 6));
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            ImageLayer capa = (ImageLayer) value;
            setBackground(colorDeFondo(t, row, isSelected));
            String nombre = escapeHtml(capa.getNombre());
            String ruta = escapeHtml(rutaCorta(capa.getRuta()));
            setText("<html><b>" + nombre + "</b><br>"
                    + "<span style='color:#9A9AB0;font-size:9.5px;'>" + ruta + "</span></html>");
            return this;
        }

        private String rutaCorta(String ruta) {
            if (ruta == null) return "";
            return ruta.length() > 34 ? "…" + ruta.substring(ruta.length() - 33) : ruta;
        }
    }

    private class ZRenderer extends JPanel implements TableCellRenderer {
        private float valor;
        private Color fondo;

        ZRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            ImageLayer capa = (ImageLayer) value;
            valor = capa.getPosZ();
            fondo = colorDeFondo(t, row, isSelected);
            setToolTipText(String.format(Locale.US, "Profundidad Z: %.1f", valor));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRect(0, 0, getWidth(), getHeight());

            String texto = String.format(Locale.US, "%.1f", valor);
            g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g2.setColor(COLOR_Z.darker());
            g2.drawString(texto, 10, 20);

            int margen = 10;
            int pistaY = 34;
            int pistaAlto = 6;
            int pistaAncho = getWidth() - margen * 2;
            g2.setColor(new Color(0xE7E7F3));
            g2.fillRoundRect(margen, pistaY, pistaAncho, pistaAlto, pistaAlto, pistaAlto);

            g2.setColor(new Color(0xCFCFE4));
            int centroX = margen + pistaAncho / 2;
            g2.drawLine(centroX, pistaY - 2, centroX, pistaY + pistaAlto + 2);

            float fraccion = (valor + 10f) / 20f; // rango -10..10
            fraccion = Math.max(0f, Math.min(1f, fraccion));
            int marcaX = margen + Math.round(pistaAncho * fraccion);
            g2.setColor(COLOR_Z);
            g2.fillOval(marcaX - 4, pistaY - 1, pistaAlto + 2, pistaAlto + 2);
            g2.dispose();
        }
    }

    private class AlphaRenderer extends JPanel implements TableCellRenderer {
        private float valor;
        private Color fondo;

        AlphaRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            ImageLayer capa = (ImageLayer) value;
            valor = capa.getAlpha();
            fondo = colorDeFondo(t, row, isSelected);
            setToolTipText(Math.round(valor * 100) + "% de opacidad");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRect(0, 0, getWidth(), getHeight());

            String texto = Math.round(valor * 100) + "%";
            g2.setFont(getFont().deriveFont(Font.BOLD, 11f));
            g2.setColor(COLOR_ALPHA.darker());
            g2.drawString(texto, 10, 20);

            int margen = 10;
            int pistaY = 34;
            int pistaAlto = 8;
            int pistaAncho = getWidth() - margen * 2;
            g2.setColor(new Color(0xE7E7F3));
            g2.fillRoundRect(margen, pistaY, pistaAncho, pistaAlto, pistaAlto, pistaAlto);

            int relleno = Math.round(pistaAncho * valor);
            g2.setColor(COLOR_ALPHA);
            g2.fillRoundRect(margen, pistaY, Math.max(relleno, pistaAlto), pistaAlto, pistaAlto, pistaAlto);
            g2.dispose();
        }
    }

    private class ColorRenderer extends JPanel implements TableCellRenderer {
        private Color colorCapa;
        private Color fondo;

        ColorRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            ImageLayer capa = (ImageLayer) value;
            colorCapa = capa.getColorRGB() != null ? capa.getColorRGB() : Color.WHITE;
            fondo = colorDeFondo(t, row, isSelected);
            setToolTipText(String.format("#%02X%02X%02X", colorCapa.getRed(), colorCapa.getGreen(), colorCapa.getBlue()));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int swatchSize = 22;
            int y = (getHeight() - swatchSize) / 2;
            g2.setColor(colorCapa);
            g2.fillRoundRect(12, y, swatchSize, swatchSize, 6, 6);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawRoundRect(12, y, swatchSize, swatchSize, 6, 6);

            String hex = String.format("#%02X%02X%02X", colorCapa.getRed(), colorCapa.getGreen(), colorCapa.getBlue());
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            g2.setColor(new Color(0x555566));
            g2.drawString(hex, 12 + swatchSize + 8, getHeight() / 2 + 4);
            g2.dispose();
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
