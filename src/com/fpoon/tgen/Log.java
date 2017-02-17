package com.fpoon.tgen;

import javax.swing.*;

public class Log {
    JTextArea jta;

    public Log(JTextArea jta) {
        this.jta = jta; //Rejestrowanie JTextArea używanego do wpisywania
    }

    public void print(String msg) {
        System.out.println(msg); //Wypisywanie na stdout
        jta.append(msg + "\n"); //Wypisywanie do JTextArea
        JScrollPane pane = (JScrollPane) (jta.getParent().getParent()); //Referencja na panel z przewijaniem
        JScrollBar bar = pane.getVerticalScrollBar(); // Referencja do pionowego suwaka
        bar.setValue(bar.getMaximum()); // Przewiń suwak na sam dół
    }
}