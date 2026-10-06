package com.calzizzle.gauntlet;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    Sprite bulletSprite;

    float enemyTimer;
    Rectangle heroRectangle;
    Rectangle spiderRectangle;
    Rectangle skeleboneRectangle;
    int health = 100;
    final float damageCooldown = 1f;
    float cooldownTime = 0f;
    boolean Invincible = false;
    BitmapFont font;
    int bulletCount = 0;
    boolean bulletExist = false;

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
        heroSprite.setPosition(viewport.getWorldWidth() / 2 - 8, viewport.getWorldHeight() / 2 - 8);
        heroSprite.setSize(16, 16);
        spiderSprite = new Sprite(spiderTexture);
        skeleboneSprite = new Sprite(skeleboneTexture);
        touchPos = new Vector2();
        heroRectangle = new Rectangle();
        spiderRectangle = new Rectangle();
        skeleboneRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        enemyPos = new Vector2();
        enemyTarget = new Vector2();
        enemyVelocity = new Vector2();
        font = new BitmapFont();
        font.getData().setScale(0.5f);
        bulletSprite = new Sprite(bulletTexture);
        skeleboneRandomizer();
        spiderRandomizer();
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
        float delta = Gdx.graphics.getDeltaTime();
        heroSprite.setX(MathUtils.clamp(heroSprite.getX(), 0, worldWidth - heroWidth));
        heroRectangle.set(heroSprite.getX(), heroSprite.getY(), heroWidth, heroHeight);
        skeleboneRectangle.set(skeleboneSprite.getX(), skeleboneSprite.getY(), 16, 16);
        spiderRectangle.set(spiderSprite.getX(), spiderSprite.getY(), 16, 16);
        if(skeleboneRectangle.overlaps(heroRectangle) || spiderRectangle.overlaps(heroRectangle)){
            if(Invincible){
                cooldownTime -= delta;
                if(cooldownTime <= 0){
                    Invincible = false;
                    cooldownTime = 0f;
                }
            }else {
                takeDamage(5);
            }
        }
    }

    private void draw() {
        String healthText = String.valueOf(health);
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        if(health > 0) {
            heroSprite.draw(spriteBatch);
        }
        if(Gdx.input.isTouched()){
            bulletSprite.draw(spriteBatch);
            bulletCount ++;
            bulletExist = true;
            if(bulletExist){
                bulletSprite.draw(spriteBatch);
            }
        }
        skeleboneSprite.draw(spriteBatch);
        spiderSprite.draw(spriteBatch);
        font.draw(spriteBatch, "Health: " + healthText, 1, 110);
        spriteBatch.end();

    }

    private void skeleboneRandomizer() {
        float enemyWidth = 16;
        float enemyHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float maxX = worldWidth - enemyWidth;
        float maxY =worldHeight - enemyHeight;
        float randX = MathUtils.random(0, maxX);
        float randY = MathUtils.random(0, maxY);
        skeleboneSprite.setPosition(randX, randY);
    }

    private void spiderRandomizer() {
        float enemyWidth = 16;
        float enemyHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float maxX = worldWidth - enemyWidth;
        float maxY =worldHeight - enemyHeight;
        float randX = MathUtils.random(0, maxX);
        float randY = MathUtils.random(0, maxY);
        spiderSprite.setPosition(randX, randY);
    }
    private void bulletInit(){
        bulletSprite.setPosition(heroSprite.getX(), heroSprite.getY());
    }
    private void takeDamage(int x){
        if (Invincible)return;
        health -= x;
        Invincible = true;
        cooldownTime = damageCooldown;
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


