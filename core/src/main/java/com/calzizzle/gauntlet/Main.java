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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    public static Sprite heroSprite;
    private Texture image;
    Texture bobTexture;
    Texture backgroundTexture;
    Sound hurtSound;
    Sound bulletSound;
    Music music;
    SpriteBatch spriteBatch;
    public static FitViewport viewport;
    Vector2 touchPos;
    float enemyTimer = 0;
    final float period = 2.0f;
    Rectangle heroRectangle;
    Rectangle spiderRectangle;
    Rectangle skeleboneRectangle;
    Rectangle bulletRectangle;
    int health = 100;
    final float damageCooldown = 2f;
    float damageCooldownTime = 0f;
    final float bulletCooldown = .25f;
    float bulletcooldownTime = 0f;
    boolean Invincible = false;
    BitmapFont font;
    int bulletCount = 0;
    boolean bulletExist = false;
    float skeleboneSpeed = 20f;
    float spiderSpeed = 20f;
    float bulletSpeed = 50f;
    float numEnemies = 0f;
    public static Texture skeleboneTexture;
    public static Texture spiderTexture;
    public static Texture bulletTexture;
    public static Array<Sprite> skeleboneSpriteList;
    public static Array<Sprite> spiderSpriteList;
    public static Array<Sprite> bulletSpriteList;
    public static Array<Boolean> skeleboneAliveList;
    public static Array<Boolean> spiderAliveList;
    public static Array<Vector2> bulletSpawns = new Array<>();
    public static Array<Vector2> bulletTargets = new Array<>();
    Array<Float> bulletX = new Array<Float>();
    Array<Float> bulletY = new Array<Float>();
    Array<Float> bulletDX = new Array<Float>();
    Array<Float> bulletDY = new Array<Float>();

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        backgroundTexture = new Texture("backgroundHeroBob.png");
        bobTexture = new Texture("HeroBob.png");
        skeleboneTexture  = new Texture("Skelebone.png");
        spiderTexture = new Texture("spider.png");
        bulletTexture = new Texture("bullet.png");
        music = Gdx.audio.newMusic(Gdx.files.internal("AdventureofBob.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(123, 123);
        heroSprite = new Sprite(bobTexture);
        heroSprite.setPosition(viewport.getWorldWidth() / 2 - 8, viewport.getWorldHeight() / 2 - 8);
        heroSprite.setSize(16, 16);
        touchPos = new Vector2();
        heroRectangle = new Rectangle();
        spiderRectangle = new Rectangle();
        skeleboneRectangle = new Rectangle();
        bulletRectangle = new Rectangle();
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
        font = new BitmapFont();
        font.getData().setScale(0.5f);
        skeleboneSpriteList = new Array<>();
        spiderSpriteList = new Array<>();
        bulletSpriteList = new Array<>();
        skeleboneAliveList = new Array<>();
        spiderAliveList = new Array<>();
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
        Vector2 heroPos = new Vector2(heroSprite.getX(), heroSprite.getY());
        heroSprite.setX(MathUtils.clamp(heroSprite.getX(), 0, worldWidth - heroWidth));
        heroRectangle.set(heroSprite.getX(), heroSprite.getY(), heroWidth, heroHeight);
        enemyTimer += delta;
        if(bulletcooldownTime > 0){
            bulletcooldownTime -= delta;
        }
        if(Gdx.input.isTouched()){
            if(bulletcooldownTime <=0){
                createBullet();
                bulletcooldownTime = bulletCooldown;
            }
        }
        if(enemyTimer > 3f && numEnemies < 10 ){
            enemyTimer = 0;
            createSkelebone();
            createSpider();
            numEnemies += 2;
        }
        for (int i = bulletSpriteList.size - 1; i >= 0; i--) {
            Sprite bulletSprite = bulletSpriteList.get(i);
            float bulletSpriteWidth = bulletSprite.getWidth();
            float bulletSpriteHeight = bulletSprite.getHeight();
            float t = 0.5f;
            Vector2 bulletPos = new Vector2(bulletSprite.getX(), bulletSprite.getY());
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            Vector2 direction = new Vector2(touchPos).sub(bulletPos);
            float distance = direction.len();
            float movementDistance = bulletSpeed * delta;
            if (movementDistance >= distance) {
                bulletSprite.setPosition(touchPos.x, touchPos.y);
            } else {
                direction.nor();
                direction.scl(movementDistance);
                bulletSprite.translate(direction.x, direction.y);
            }
            bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(), bulletSpriteWidth, bulletSpriteHeight);
        }
        for (int i = skeleboneSpriteList.size - 1; i >= 0; i--) {
            Sprite skeleboneSprite = skeleboneSpriteList.get(i);
            float skeleboneWidth = skeleboneSprite.getWidth();
            float skeleboneHeight = skeleboneSprite.getHeight();
            float t = 0.5f;
            Vector2 skelebonePos = new Vector2(skeleboneSprite.getX(), skeleboneSprite.getY());
            Vector2 heroPosVec = new Vector2(heroSprite.getX(), heroSprite.getY());
            Vector2 direction = new Vector2(heroPosVec).sub(skelebonePos);
            float distance = direction.len();
            float movementDistance = skeleboneSpeed * delta;
            if(movementDistance >= distance){
                skeleboneSprite.setPosition(heroPosVec.x, heroPosVec.y);
            }else {
                direction.nor();
                direction.scl(movementDistance);
                skeleboneSprite.translate(direction.x, direction.y);
            }
            skeleboneRectangle.set(skeleboneSprite.getX(), skeleboneSprite.getY(), skeleboneWidth, skeleboneHeight);
            if(skeleboneRectangle.overlaps(heroRectangle)){
                if(Invincible){
                    damageCooldownTime -= delta;
                    if(damageCooldownTime <= 0){
                        Invincible = false;
                        damageCooldownTime = 0f;
                    }
                }else {
                    takeDamage(5);
                }
            }
            if(bulletRectangle.overlaps(skeleboneRectangle)){
                skeleboneSpriteList.removeIndex(i);
                skeleboneAliveList.removeIndex(i);
                bulletSpriteList.removeIndex(i);
                numEnemies--;
                break;
            }
        }
        for (int i = spiderSpriteList.size - 1; i >= 0; i--) {
            Sprite spiderSprite = spiderSpriteList.get(i);
            float spiderWidth = spiderSprite.getWidth();
            float spiderHeight = spiderSprite.getHeight();
            float t = 0.5f;
            Vector2 spiderPos = new Vector2(spiderSprite.getX(), spiderSprite.getY());
            Vector2 heroPosVec = new Vector2(heroSprite.getX(), heroSprite.getY());
            Vector2 direction = new Vector2(heroPosVec).sub(spiderPos);
            float distance = direction.len();
            float movementDistance = spiderSpeed * delta;
            if(movementDistance >= distance){
                spiderSprite.setPosition(heroPosVec.x, heroPosVec.y);
            }else {
                direction.nor();
                direction.scl(movementDistance);
                spiderSprite.translate(direction.x, direction.y);
            }
            spiderRectangle.set(spiderSprite.getX(), spiderSprite.getY(), spiderWidth, spiderHeight);
            if(spiderRectangle.overlaps(heroRectangle)) {
                if (Invincible) {
                    damageCooldownTime -= delta;
                    if (damageCooldownTime <= 0) {
                        Invincible = false;
                        damageCooldownTime = 0f;
                    }
                } else {
                    takeDamage(5);
                }
                if(bulletRectangle.overlaps(spiderRectangle)){
                    spiderSpriteList.removeIndex(i);
                    spiderAliveList.removeIndex(i);
                    bulletSpriteList.removeIndex(i);
                    numEnemies--;
                    break;
                }
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        if (health >= 0) {
            heroSprite.draw(spriteBatch);
            for (Sprite bulletSprite : bulletSpriteList) {
                bulletSprite.draw(spriteBatch);
            }
            font.draw(spriteBatch, "Health: " + String.valueOf(health), 1, 110);
        }
        if (health <= 0) {
            font.draw(spriteBatch, "You Died, Space to Restart", 61, 61);
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                skeleboneAliveList.clear();
                skeleboneSpriteList.clear();
                spiderSpriteList.clear();
                spiderAliveList.clear();
                bulletSpriteList.clear();
                numEnemies = 0;
                health = 100;
                heroSprite.setPosition(worldWidth / 2 - 8, worldHeight / 2 - 8);
            }
        }
        for(int i = skeleboneSpriteList.size -1; i >= 0; i --){
            if(skeleboneAliveList.get(i).equals(true)){
                skeleboneSpriteList.get(i).draw(spriteBatch);
            }
        }
        for(int i = spiderSpriteList.size -1; i >= 0; i --){
            if(spiderAliveList.get(i).equals(true)){
                spiderSpriteList.get(i).draw(spriteBatch);
            }
        }
        spriteBatch.end();
    }
    private void createSkelebone() {
        float skeleboneWidth = 16;
        float skeleboneHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float randX = MathUtils.random(0f, worldWidth - skeleboneWidth);
        float randY = MathUtils.random(0f, worldHeight - skeleboneHeight);
        Sprite skeleboneSprite = new Sprite(skeleboneTexture);
        skeleboneSprite.setSize(skeleboneWidth, skeleboneHeight);
        skeleboneSprite.setPosition(randX, randY);
        skeleboneSpriteList.add(skeleboneSprite);
        skeleboneAliveList.add(true);
    }
    private void createSpider() {
        float spiderWidth = 16;
        float spiderHeight = 16;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float randX = MathUtils.random(0f, worldWidth - spiderWidth);
        float randY = MathUtils.random(0f, worldHeight - spiderHeight);
        Sprite spiderSprite = new Sprite(spiderTexture);
        spiderSprite.setSize(spiderWidth, spiderHeight);
        spiderSprite.setPosition(randX, randY);
        spiderSpriteList.add(spiderSprite);
        spiderAliveList.add(true);
    }
    private void createBullet(){
        float bulletWidth = 2;
        float bulletHeight = 2;
        Vector2 target = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        Vector2 direction = new Vector2(target.x, target.y);
        float distance = (float) Math.sqrt(direction.x * direction.x + direction.y * direction.y);
        if(distance == 0) {distance = 1;}
        Vector2 normalized = new Vector2(direction.x / distance, direction.y / distance);
        Vector2 heroPos = new Vector2(heroSprite.getX(), heroSprite.getY());
        bulletSpawns.add(heroPos);
        Vector2 bulletDirection = new Vector2(normalized.x * bulletSpeed, normalized.y * bulletSpeed);
        bulletTargets.add(bulletDirection);
        Sprite bulletSprite = new Sprite(bulletTexture);
        bulletSprite.setSize(bulletWidth, bulletHeight);
        bulletSprite.setPosition(heroSprite.getX(), heroSprite.getY());
        bulletSpriteList.add(bulletSprite);
    }
    private void takeDamage(int x){
        if (Invincible)return;
        health -= x;
        Invincible = true;
        damageCooldownTime = damageCooldown;
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


