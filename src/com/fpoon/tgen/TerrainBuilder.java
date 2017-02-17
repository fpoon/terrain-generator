package com.fpoon.tgen;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class TerrainBuilder {
    public static final int VERTEX_INFO_SIZE = 6;
    public static final float UNIT = 1.0f;
    Color sand = Color.valueOf("ffff66"); // Kolor piasku - (0) najniższy
    Color seaside = Color.OLIVE; // Kolor brzegu - (1)
    Color wood = Color.valueOf("266A2E"); // Kolor lasu - (2)
    Color hill = Color.DARK_GRAY; // Kolor gór - (3)
    Color summit = Color.LIGHT_GRAY; // Kolor szczytów - (4) najwyższy
    Color desk = Color.valueOf("663311"); // Kolor pudła
    Color darkDesk = new Color(Color.BLACK).lerp(desk, 0.5f); // Przy powierzchni będzie ciemniej
    ColorManager cm; // Zarządzanie kolorami.
    TerrainGenerator tg;
    Log log;
    Material baseColor = new Material(ColorAttribute.createDiffuse(Color.WHITE));
    int width, height;
    float[][] vertices;
    float water = 0;
    float waterLevel = 0.0f;
    float min, max;

    public TerrainBuilder() {
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public void print(String text) {
        if (log != null)
            log.print(text);
    }

    private void setTg(TerrainGenerator tg) {
        this.tg = tg;
        width = tg.size;
        height = tg.size;
        vertices = tg.vertices;
        water = tg.water;
        waterLevel = tg.waterLevel;
        min = tg.min;
        max = tg.max;
        cm = new ColorManager();
        if (waterLevel > 0) { // Jeśli woda jest włączona
            cm.insert(sand, 0.0f);
            cm.insert(seaside, 0.05f);
        } else // Jeśli nie - nie rób piasku
            cm.insert(seaside, 0.00f);
        cm.insert(wood, 0.2f);
        cm.insert(hill, 0.6f);
        cm.insert(summit, 1.0f);
    }

    public void buildMeshChunk(ModelBuilder builder, int p, int q, int id) {
        String name = "Terrain" + (id++);
        print("Tworzę mesh " + name);
//Rozpocznij tworzenie nowej siatki w obrębie modelu
        MeshPartBuilder meshbuild = builder.part(name, // Nazwa
                GL20.GL_TRIANGLES, // Sposób renderowania
//Jakie informacje niosą wierzchołki
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked,
//Kolor bazowy (biały)
                baseColor);
        for (int i = p; i < 64 + p && i < width; i++) {
            for (int j = q; j < 64 + q && j < height; j++) {
// Utworzenie pozycji wierzchołków
                Vector3 v1 = new Vector3(i * UNIT, vertices[i][j], j * UNIT);
                Vector3 v2 = new Vector3((i + 1) * UNIT, vertices[i + 1][j], j * UNIT);
                Vector3 v3 = new Vector3((i + 1) * UNIT, vertices[i + 1][j + 1], (j + 1) * UNIT);
                Vector3 v4 = new Vector3((i) * UNIT, vertices[i][j + 1], (j + 1) * UNIT);
                Vector3 n = calculateNormal(v1, v2, v3); // Obliczenie normalnej (prostopadłej do powierzchni)
// Utwórz nowe wierzchołki i dodaj informacje (pozycja, normalna, kolor)
                MeshPartBuilder.VertexInfo a = new MeshPartBuilder.VertexInfo().setPos(v1).setNor(n).setCol(getColor(v1.y));
                MeshPartBuilder.VertexInfo b = new MeshPartBuilder.VertexInfo().setPos(v2).setNor(n).setCol(getColor(v2.y));
                MeshPartBuilder.VertexInfo c = new MeshPartBuilder.VertexInfo().setPos(v3).setNor(n).setCol(getColor(v3.y));
                MeshPartBuilder.VertexInfo d;
                meshbuild.triangle(b, a, c); //Zbuduj trójkąt
                n = calculateNormal(v1, v2, v4);
                a = new MeshPartBuilder.VertexInfo().setPos(v1).setNor(n).setCol(getColor(v1.y));
                c = new MeshPartBuilder.VertexInfo().setPos(v3).setNor(n).setCol(getColor(v3.y));
                d = new MeshPartBuilder.VertexInfo().setPos(v4).setNor(n).setCol(getColor(v4.y));
                meshbuild.triangle(a, d, c);
            }
        }
    }

    public void buildQuad(Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4, MeshPartBuilder meshbuild) { // Zbuduj czworobok
        Vector3 n = calculateNormal(v1, v2, v3);
        MeshPartBuilder.VertexInfo a = new MeshPartBuilder.VertexInfo().setPos(v1).setNor(n).setCol(darkDesk);
        MeshPartBuilder.VertexInfo b = new MeshPartBuilder.VertexInfo().setPos(v2).setNor(n).setCol(darkDesk);
        MeshPartBuilder.VertexInfo c = new MeshPartBuilder.VertexInfo().setPos(v3).setNor(n).setCol(desk);
        MeshPartBuilder.VertexInfo d;
        meshbuild.triangle(a, b, c);
        n = calculateNormal(v1, v2, v4);
        a = new MeshPartBuilder.VertexInfo().setPos(v1).setNor(n).setCol(darkDesk);
        c = new MeshPartBuilder.VertexInfo().setPos(v3).setNor(n).setCol(desk);
        d = new MeshPartBuilder.VertexInfo().setPos(v4).setNor(n).setCol(desk);
        meshbuild.triangle(d, a, c);
    }

    public Model build(TerrainGenerator tg) { // Zbuduj model
        setTg(tg); // Ustaw generator
        ModelBuilder builder = new ModelBuilder(); // Nowy obiekt budujący model
        builder.begin(); // Rozpocznij budowę nowego modelu.
        int id = 0;
        for (int p = 0; p < width; p += 64) { // Podziel siatkę na kawałki 64x64 (24576 wierzchołków)
// pojedyncza siatka nie może mieć więcej niż 2^15 wierzchołków
            for (int q = 0; q < height; q += 64) {
                buildMeshChunk(builder, p, q, id++); // Zbuduj kawałek
            }
        }
// Zbuduj skrzynię. Brązową.
        buildBox(builder.part("Walls", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.ColorPacked, baseColor));
        print("Tworzę mesh Water");
// Materiał na wodę
        Material waterColor = new Material(ColorAttribute.createDiffuse(0, 0.5f, 0.5f, 0.7f));
// Ustaw obsługę przezroczystości przez OpenGl
        waterColor.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        MeshPartBuilder meshbuild = builder.part("Water", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal, waterColor);
        float eps = width / 1600f; // Wartość dzięki której woda nie prześwituje przez skrzynię (ma odrobinę mniejsze wymiary niż ona)
        Vector3 v1 = new Vector3(0 + eps, water, 0 + eps);
        Vector3 v2 = new Vector3(0 + eps, water, width - eps);
        Vector3 v3 = new Vector3(width - eps, water, 0 + eps);
        Vector3 v4 = new Vector3(width - eps, water, width - eps);
        Vector3 v5 = new Vector3(0 + eps, min, 0 + eps);
        Vector3 v6 = new Vector3(0 + eps, min, width - eps);
        Vector3 v7 = new Vector3(width - eps, min, 0 + eps);
        Vector3 v8 = new Vector3(width - eps, min, width - eps);
        meshbuild.box(v1, v2, v3, v4, v5, v6, v7, v8); // Utwórz prostopadłościan imitujący wodę.
        log.print("-------------------");
        return builder.end(); // Wypluj gotowy model terenu
    }

    private void buildBox(MeshPartBuilder meshbuild) { // Utwórz skrzynię
        print("Tworzę mesh Box");
        float min = this.min - width / 8; // Wysokośc dna skrzyni
        for (int i = 0; i < width; i++) {// Jedna krawędź
            Vector3 v1 = new Vector3(i * UNIT, vertices[i][0], 0);
            Vector3 v2 = new Vector3((i + 1) * UNIT, vertices[i + 1][0], 0);
            Vector3 v4 = new Vector3(i * UNIT, min, 0);
            Vector3 v3 = new Vector3((i + 1) * UNIT, min, 0);
            buildQuad(v1, v2, v3, v4, meshbuild);
        }
        for (int i = 0; i < width; i++) {//Druga krawędź
            Vector3 v1 = new Vector3(i * UNIT, vertices[i][width], width);
            Vector3 v2 = new Vector3((i + 1) * UNIT, vertices[i + 1][width], width);
            Vector3 v4 = new Vector3(i * UNIT, min, width);
            Vector3 v3 = new Vector3((i + 1) * UNIT, min, width);
            buildQuad(v2, v1, v4, v3, meshbuild);
        }
        for (int i = 0; i < width; i++) {//Trzecia krawędź
            Vector3 v1 = new Vector3(0, vertices[0][i], i * UNIT);
            Vector3 v2 = new Vector3(0, vertices[0][i + 1], (i + 1) * UNIT);
            Vector3 v4 = new Vector3(0, min, i * UNIT);
            Vector3 v3 = new Vector3(0, min, (i + 1) * UNIT);
            buildQuad(v2, v1, v4, v3, meshbuild);
        }
        for (int i = 0; i < width; i++) {//Czwarta krawędź
            Vector3 v1 = new Vector3(width, vertices[width][i], i * UNIT);
            Vector3 v2 = new Vector3(width, vertices[width][i + 1], (i + 1) * UNIT);
            Vector3 v4 = new Vector3(width, min, i * UNIT);
            Vector3 v3 = new Vector3(width, min, (i + 1) * UNIT);
            buildQuad(v1, v2, v3, v4, meshbuild);
        }
//Dno.
        Vector3 v1 = new Vector3(0, min, 0);
        Vector3 v2 = new Vector3(width, min, 0);
        Vector3 v3 = new Vector3(width, min, width);
        Vector3 v4 = new Vector3(0, min, width);
        buildQuad(v1, v2, v3, v4, meshbuild);
    }

    private Color getColor(float h) { // Pobierz kolor z managera
        if (h < water && waterLevel > 0.0f) // Poniżej poziomu wody daj piach
            return sand;
        h -= water; // Wywal wodę
        float x = h / (max - water); // Licz od poziomu wody
        return cm.get(x); // Pobierz gradient
    }

    private Vector3 calculateNormal(Vector3 p1, Vector3 p2, Vector3 p3) {
// u = p3 - p1
        float ux = p3.x - p1.x;
        float uy = p3.y - p1.y;
        float uz = p3.z - p1.z;
// v = p2 - p1
        float vx = p2.x - p1.x;
        float vy = p2.y - p1.y;
        float vz = p2.z - p1.z;
// n = cross(v, u)
        float nx = ((vy * uz) - (vz * uy));
        float ny = ((vz * ux) - (vx * uz));
        float nz = ((vx * uy) - (vy * ux));
// // normalize(n)
        float num2 = ((nx * nx) + (ny * ny)) + (nz * nz);
        float num = 1f / (float) Math.sqrt(num2);
        nx *= num;
        ny *= num;
        nz *= num;
        return new Vector3(nx, ny, nz);
    }
}