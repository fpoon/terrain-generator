package com.fpoon.tgen.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.fpoon.tgen.Log;
import com.fpoon.tgen.TGen;
import com.fpoon.tgen.TerrainGenerator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class MainForm {
    private JPanel mainPanel;
    private JPanel ctrlPanel;
    public JPanel displayPanel;
    private JTextField seedValue;
    private JButton randSeedButton;
    private JComboBox sizeComboBox;
    private JSlider roughnessSlider;
    private JLabel roughnessValLabel;
    private JSlider waterLevelSlider;
    private JButton genButton;
    private JTextArea logArea;
    private JLabel waterLevelValLabel;
    private float roughness = 0.2f;
    private float waterLevel = 0.5f;
    private int seed = 0;
    private int size = 64;
    private static Log log;
    private TerrainGenerator tg;
    private static TGen tgen;
    public static Random rand;
    static JFrame frame;
    static LwjglCanvas canvas;

    public static void main(String[] args) {
//Ustawianie Look'n'Feel swinga
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            System.out.println("Ustawiono LNF Nimbus");
        } catch (Exception ex) {
            System.out.println("Nie można ustawić LNF");
        }
        rand = new Random(); // Inicjalizacja generatora liczb pseudolosowych
        SwingUtilities.invokeLater(new Runnable() { // Pozwól na uruchomienie wątku z dostępem do kontekstu OpenGL
            @Override
            public void run() {
                frame = new JFrame("Terrain Generator - Mariusz Rebandel"); // Utwórz główne okno
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Przy zamknięciu okna nie rób nic
                frame.addWindowListener(new WindowAdapter() { // Dodawanie niestandardowej akcji przy zamykaniu okna
                    public void windowClosing(WindowEvent evt) {
                        try {
                            tgen.dispose(); // Pozbądź się z pamięci danych zarządzanych przez LibGDX i OpenGL
                        } catch (Exception e) {
// Jeśli pojawi się błąd - nie rób nic. Dane zostały usunięte w innym miejscu programu.
                        }
                        System.exit(0); // Zakończ program
                    }
                });
                MainForm mf = new MainForm(); // Utwórz nowy panel
                frame.setContentPane(mf.mainPanel); // Ustaw zawartośc okna
                JPanel displayPanel = mf.displayPanel;
                tgen = new TGen(); // Utwórz nowy podprogram LibGDX
// Konfiguracja okna OpenGL
                LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.disableAudio = true; // Wyłącz dźwięk
                canvas = new LwjglCanvas(tgen, config); // Pobierz panel do wyświetlania
                displayPanel.add(canvas.getCanvas(), BorderLayout.CENTER); // Dodaj panel do okna
                frame.pack(); // Utwórz okno
                frame.setVisible(true); // Pokaż
            }
        });
    }

    public void generate() { // Rozpocznij generację terenu
        try {
            seed = Integer.valueOf(seedValue.getText()); // Pobierz ziarno z paska
        } catch (Exception ex) { // Każdy wyjątek powoduje wyświetlenie komunikatu
            log.print("Ziarno nieprawidłowe");
            seedValue.setText("" + seed);
            return; // I przerwanie procedury
        }
//Wypisz wiadomość
        log.print("Rozpoczęto generację terenu.\nZiarno=" + seed +
                "\nRozmiar=" + sizeComboBox.getSelectedItem() +
                "\nSzorstkość=" + roughnessValLabel.getText() +
                "\nPoziom wody=" + waterLevelValLabel.getText());
        new Thread(new Runnable() {
            @Override
            public void run() {
                tgen.setLog(log); // Ustaw log dla obiektu budującego mesh
                tg = new TerrainGenerator(size, roughness, seed, waterLevel); // Utwórz nowy generator
                tg.generate(); // Wygeneruj
                tgen.build(tg); // Zbuduj mesh na podstawie terenu
            }
        }).start(); // Rozpocznij wątek
    }

    public MainForm() {
        log = new Log(logArea); // Utwórz nowy log
        randSeed(); // Ustaw nowe ziarno
        sizeComboBox.setSelectedIndex(4); // Wybierz domyślną opcję w comoboxie
        roughnessSlider.addChangeListener(new ChangeListener() { // Co się dzieje przy suwaku od szorstkości
            @Override
            public void stateChanged(ChangeEvent e) {
                float v = roughnessSlider.getValue(); // Pobierz wartość
                roughnessValLabel.setText("" + v + "%"); // Wyświetl wartosć
                roughness = v * 0.01f; // Znormalizuj wartość
            }
        });
        waterLevelSlider.addChangeListener(new ChangeListener() { // Co się dzieje przy suwaku od wody
            @Override
            public void stateChanged(ChangeEvent e) {
                float v = waterLevelSlider.getValue(); // Pobierz wartość
                if (v == 0)
                    waterLevelValLabel.setText("Wyłączono"); // Wartość 0 wyłącza wodę
                else
                    waterLevelValLabel.setText("" + v + "%"); // Wyśiwetl wartość
                waterLevel = v * 0.01f; // Znormalizuj wartość
            }
        });
        randSeedButton.addActionListener(new ActionListener() { // Losowanie nowego ziarna
            @Override
            public void actionPerformed(ActionEvent e) {
                randSeed();
            }
        });
        seedValue.addKeyListener(new KeyAdapter() { // Wciśnięcie entera powoduje zapisanie wprowadzonego ziarna
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        seed = Integer.valueOf(seedValue.getText());
                    } catch (Exception ex) { // Błąd
                        log.print("Ziarno nieprawidłowe");
                        seedValue.setText("" + seed);
                    }
                }
            }
        });
        sizeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
// Teren generowany przez alg. DiamondSquare może mieć rozmary tylko 2^n x 2^n
                size = (int) Math.pow(2.0, sizeComboBox.getSelectedIndex() + 2.0);
            }
        });
        genButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generate();
            } // Wygeneruj nowy teren
        });
    }

    private void randSeed() { // Wylosuj nowe ziarno
        seed = rand.nextInt();
        seedValue.setText(String.valueOf(seed)); // Wyświetl nowe ziarno
    }

    private void createUIComponents() {
/**/
    }
}