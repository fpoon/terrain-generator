package com.fpoon.tgen;

import com.badlogic.gdx.math.Vector2;

import java.util.Random;

public class TerrainGenerator {
    int size;
    float[][] vertices;
    private float roughness;
    private Random rand;
    float min = Float.MAX_VALUE;
    float max = Float.MIN_VALUE;
    float water = 0;
    float waterLevel = 0.0f;

    public TerrainGenerator(int s, float roughness, int seed, float waterLevel) {
        changeSize(s); // Ustaw rozmiar terenu
        this.roughness = roughness;
        rand = new Random(seed);
        this.waterLevel = waterLevel;
    }

    // Ustaw środek
    private float centerHeight(Vector2 v1, Vector2 v2, Vector2 v3, Vector2 v4, float r) {
        float sum = 0.0f;
        float a = 0f;
        Vector2[] vs = {v1, v2, v3, v4};
        for (Vector2 v : vs) {
            try {
                sum += vertices[(int) v.x][(int) v.y];
                a += 1;
            } catch (Exception e) {
//Nic wierzchołek nie istnieje i można go zignorować
            }
        }
        float bar = sum / a + r; // Średnia arytmetyczna + losowe przesunięcie.
        minMaxUpd(bar); // Uaktualnij wartości minimalne i maxymalne
        return bar;
    }

    private void minMaxUpd(float bar) { // Uaktualnij wartości minimalne i maxymalne
        if (bar > max)
            max = bar;
        if (bar < min)
            min = bar;
    }

    public void diamond(int x, int y, int size, float offset) {
        Vector2 v1 = new Vector2(x, y - size);
// góra
        Vector2 v2 = new Vector2(x + size, y);
// prawo
        Vector2 v3 = new Vector2(x, y + size);
// dół
        Vector2 v4 = new Vector2(x - size, y);
// lewo
        vertices[x][y] = centerHeight(v1, v2, v3, v4, offset); // Ustaw wartość środka
    }

    public void square(int x, int y, int size, float offset) {
        Vector2 v1 = new Vector2(x - size, y - size);
// lewy górny
        Vector2 v2 = new Vector2(x + size, y - size);
// prawy górny
        Vector2 v3 = new Vector2(x - size, y + size);
// dolny lewy
        Vector2 v4 = new Vector2(x + size, y + size);
// dolny prawy
        vertices[x][y] = centerHeight(v1, v2, v3, v4, offset); // Ustaw wartość środka
    }

    public void generate() { // Metoda do generowania terenu
        float scale = roughness * size; // Ustaw skalę
// Ustaw wierzchołki przed rozpoczęciem właściwego algorytmu
        minMaxUpd(vertices[0][0]
                = rand.nextFloat() * scale * 2 - scale);
        minMaxUpd(vertices[size][0]
                = rand.nextFloat() * scale * 2 - scale);
        minMaxUpd(vertices[0][size]
                = rand.nextFloat() * scale * 2 - scale);
        minMaxUpd(vertices[size][size] = rand.nextFloat() * scale * 2 - scale);
        diamondSquare(size); // Przejdź do właściwego algorytmu
        water = min + ((max - min) * waterLevel); // Poziom wody w unitach. Używane przez budowniczego.
    }

    public void diamondSquare(int size) { // Algorytmi Diamond Square
        int x, y, half = size / 2; // Połowa długośći aktualnie obrabianego boku
        float scale = roughness * size; // UStaw skalę
        if (half < 1) return; // Jeśli bok jest mniejszy od 2 - algorytm zakończył bieg.
// Krok Kwadratowy
        for (y = half; y < this.size; y += size) { // Wybieraj środki kwadratów
            for (x = half; x < this.size; x += size) {
                square(x, y, half, rand.nextFloat() * scale * 2 - scale); // Ustaw kwadrat
            }
        }
// Krok diamentowy
        for (y = 0; y <= this.size; y += half) { // Wybieraj środki diamentów
            for (x = (y + half) % size; x <= this.size; x += size) {
                diamond(x, y, half, rand.nextFloat() * scale * 2 - scale); // Ustaw diament
            }
        }
        diamondSquare(size / 2); // Kolejna iteracja. Rozpatrywane boki są 2x mniejsze
    }

    public void changeSize(int w) {
        size = w; // Ustaw rozmiar
        resetVertices(); // Utwórz tablicę z wierzchołkami
    }

    private void resetVertices() { //DiamondSquare obsługuje tylko obszary o boku 2^n kwadratów (2^n+1 wierzchołków)
        vertices = new float[size + 1][size + 1]; // Nowa tablica z wierzchołkami
    }
}