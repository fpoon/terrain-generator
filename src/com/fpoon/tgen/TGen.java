package com.fpoon.tgen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

public class TGen extends ApplicationAdapter { // Aplikacja LibGDX.
    Camera cam; // Domyślna kamera
    CameraInputController camCtrl; // Sterowanie kamerą
    Model model; // Model terenu
    ModelInstance instance; // Instancja modelu
    ModelBatch batch; // Obiekt służacy do dostarczania danych do wyświetlania
    Environment environment; // Środowisko (Światła)
    TerrainGenerator tg = null; // Generator terenu
    TerrainBuilder tb; // Obiekt budujący teren
    Log log; // Log.
    int size = 1;

    @Override
    public void create() {
        batch = new ModelBatch(); // Nowy batch
        environment = new Environment(); // Nowe środowisko
// Światło rozproszone
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
// Światło kierunkowe
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.5f, 0.8f, 0.2f));
// Nowy budowniczy
        tb = new TerrainBuilder();
        reset(); // Ustaw wszystko.
    }

    public void setLog(Log log) {
        this.log = log;
        tb.setLog(log);
    }

    public void setModel(Model model, int size) {
        if (this.model != null)
            this.model.dispose();
        this.model = model;
        this.instance = new ModelInstance(model); // Utwórz nową instancję obiektu
        reset(size);
    }

    public void build(TerrainGenerator tg) {
        class BuildRunner implements Runnable {
            TerrainGenerator tg;

            public BuildRunner(TerrainGenerator tg) {
                this.tg = tg;
            }

            @Override
            public void run() {
                setModel(tb.build(tg), tg.size);
            }
        }
        Gdx.app.postRunnable(new BuildRunner(tg)); // Uruchom w wątku z dostępem do konstekstu OpenGL
    }

    public void reset() {
        reset(1);
    }

    public void reset(int size) {
        this.size = size; // Rozmiar obiektu (w unitach)
// Nowa kamera perspektywiczna o kącie widzenia 67 i renderująca obraz o rozmiarach okna)
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(size / 2, size, size / 2); // Ustaw kamerę nad obiektem
        cam.lookAt(size / 2, 0, size / 2); // Patrz na środek obiektu
        cam.near = 1f; // Nierenderuj obiektów będących bliżej niż 1 unit
        cam.far = 4096f; // Nierenderuj obiektów będących dalej niż 4096 unitów
        cam.update(); // Odśwież kamerę
        camCtrl = new CameraInputController(cam); // Dodaj sterowanie kamerą
        Gdx.input.setInputProcessor(camCtrl); // Przekaż wejście do kontrolera kamery
    }

    @Override
    public void render() { // Procedura renderująca
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Ustaw rozmiary do renderowania
        Gdx.gl.glClearColor(0.5f, 0.75f, 0.9f, 1.0f); // Ustaw kolor czyszczenia
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); // Wyczyść okno renderowania
        batch.begin(cam); // Rozpocznij kolejkę renderowania
        if (instance != null) // Jeśli jest model
            batch.render(instance, environment); // Wyrenderuj model zgodnie z podanym oświetleniem
        batch.end(); // Przekaż do wyświetlenia
    }

    @Override
    public void dispose() {
        if (batch != null)
            batch.dispose(); // Pozbądź się kolejki
        if (model != null)
            model.dispose(); // Pozbądź się modelu
    }

    @Override
    public void resize(int w, int h) { // Zwiększ pole renderowania kamery.
        PerspectiveCamera cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(this.cam.position);
        cam.lookAt(size / 2, 0, size / 2);
        cam.near = 1f;
        cam.far = 4096f;
        cam.update();
        this.cam = cam;
        camCtrl = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camCtrl);
    }
}