package com.calzizzle.gauntlet;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    Texture bobTexture;
    Texture spiderTexture;
    Texture skeleboneTexture;
    Texture backgroundTexture;
    Texture bulletTexture;
    Sound hurtSound;
    Sound bulletSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite heroSprite;
    Vector2 touchPos;
    Vector2 enemyPos;
    Vector2 enemyTarget;
    Vector2 enemyVelocity;
    Sprite spiderSprite;
    Sprite skeleboneSprite;

    float enemyTimer;
    Rectangle heroRectangle;
    Rectangle enemyRectangle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        backgroundTexture = new Texture("backgroundHeroBob.png");
        bobTexture = new Texture("HeroBob.png");
        spiderTexture = new Texture("spider.png");
        skeleboneTexture = new Texture("Skelebone.png");
        bulletTexture = new Texture("bullet.png");
        music = Gdx.audio.newMusic(Gdx.files.internal("AdventureofBob.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(123, 123);
        heroSprite = new Sprite(bobTexture);
        heroSprite.setPosition(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
        heroSprite.setSize(12, 16);
        spiderSprite = new Sprite(spiderTexture);
        skeleboneSprite = new Sprite(skeleboneTexture);
        touchPos = new Vector2();
        createSkelebone();
        createSpider();
        heroRectangle = new Rectangle();
        enemyRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        enemyPos = new Vector2();
        enemyTarget = new Vector2();
        enemyVelocity = new Vector2();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 140, 210);
        batch.end();
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 50f;
        float delta = Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            heroSprite.translateY(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            heroSprite.translateY(-speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            heroSprite.translateX(speed * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            heroSprite.translateX(-speed * delta);
        }
    }

    private void logic() {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float heroWidth = heroSprite.getWidth();
        float heroHeight = heroSprite.getHeight();
        heroSprite.setX(MathUtils.clamp(heroSprite.getX(), 0, worldWidth - heroWidth));
        float delta = Gdx.graphics.getDeltaTime();
        heroRectangle.set(heroSprite.getX(), heroSprite.getY(), heroWidth, heroHeight);
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        heroSprite.draw(spriteBatch);
        spiderSprite.draw(spriteBatch);
        skeleboneSprite.draw(spriteBatch);

        spriteBatch.end();
    }

    private void createSkelebone() {
        float enemyWidth = 16;
        float enemyHeight = 16;
        float speed = 50f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float delta = Gdx.graphics.getDeltaTime();
        enemyPos.set(MathUtils.random(), MathUtils.random());
        enemyTarget.set(heroSprite.getX(), heroSprite.getY());
        enemyVelocity.set(enemyTarget).sub(enemyPos.x, enemyPos.y);
        if(enemyVelocity.len() > speed * delta){
            enemyVelocity.nor().scl(speed * delta);
            enemyPos.add(enemyVelocity);
        }else {
            enemyPos.set(enemyTarget);
        }
        skeleboneSprite.setPosition(enemyPos.x, enemyPos.y);
        spriteBatch.begin();
        skeleboneSprite.draw(spriteBatch);
        spriteBatch.end();
    }

    private void createSpider() {
        float enemyWidth = 16;
        float enemyHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        Sprite enemySprite = new Sprite(spiderTexture);
        enemySprite.setSize(enemyWidth, enemyHeight);
        float maxX = worldWidth - enemyWidth;
        float maxY =worldHeight - enemyWidth;
        float randX = MathUtils.random(0, maxX);
        float randY = MathUtils.random(0, maxY);
        enemySprite.setPosition(randX, randY);
    }
    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }

}


