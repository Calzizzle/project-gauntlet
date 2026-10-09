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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.viewport.FitViewport;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    public static Sprite heroSprite;
    private Texture image;
    Texture bobTexture;
    Texture backgroundTexture;
    public static Texture skeleboneTexture;
    public static Texture spiderTexture;
    public static Texture bulletTexture;
    Sound hurtSound;
    Sound bulletSound;
    Sound failSound;
    Sound scoreSound;
    Music music;
    boolean hasPlayedFail = false;
    SpriteBatch spriteBatch;
    public static FitViewport viewport;
    Vector2 touchPos;
    Rectangle heroRectangle;
    Rectangle spiderRectangle;
    Rectangle skeleboneRectangle;
    Rectangle bulletRectangle;
    int health = 100;
    public static int score = 0;
    int numEnemies = 0;
    float enemyTimer = 0;
    final float damageCooldown = 1f;
    float damageCooldownTime = 0f;
    final float bulletCooldown = .25f;
    float bulletcooldownTime = 0f;
    boolean Invincible = false;
    BitmapFont font;
    float skeleboneSpeed = 20f;
    float spiderSpeed = 20f;
    float bulletSpeed = 100f;
    public static Array<Sprite> skeleboneSpriteList;
    public static Array<Sprite> spiderSpriteList;
    public static Array<Sprite> bulletSpriteList;
    public static Array<Boolean> skeleboneAliveList;
    public static Array<Boolean> spiderAliveList;
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
        hurtSound = Gdx.audio.newSound(Gdx.files.internal("Hurt.mp3"));
        failSound = Gdx.audio.newSound(Gdx.files.internal("Fail.mp3"));
        scoreSound = Gdx.audio.newSound(Gdx.files.internal("Score.mp3"));
        bulletSound = Gdx.audio.newSound(Gdx.files.internal("Bullet.mp3"));
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
        heroSprite.setX(MathUtils.clamp(heroSprite.getX(), 0, worldWidth - heroWidth));
        heroSprite.setY(MathUtils.clamp(heroSprite.getY(), 0, worldHeight - heroHeight));
        heroRectangle.set(heroSprite.getX(), heroSprite.getY(), heroWidth, heroHeight);
        enemyTimer += delta;
        if (bulletcooldownTime > 0) {
            bulletcooldownTime -= delta;
        }
        if (Gdx.input.isTouched()) {
            if (bulletcooldownTime <= 0) {
                createBullet();
                bulletcooldownTime = bulletCooldown;
                bulletSound.play(.25f);
            }
        }
        if (enemyTimer > 1f && numEnemies < 10) {
            enemyTimer = 0;
            createSkelebone();
            createSpider();
            numEnemies += 2;
        }
        for (int i = 0; i < bulletX.size; i++) {
            float currentX = bulletX.get(i) + bulletDX.get(i) * delta;
            float currentY = bulletY.get(i) + bulletDY.get(i) * delta;
            bulletX.set(i, currentX);
            bulletY.set(i, currentY);
            if (i < bulletSpriteList.size) {
                bulletSpriteList.get(i).setPosition(currentX, currentY);
            }
        }
        for (int i = skeleboneSpriteList.size - 1; i >= 0; i--) {
            Sprite skeleboneSprite = skeleboneSpriteList.get(i);
            float skeleboneWidth = skeleboneSprite.getWidth();
            float skeleboneHeight = skeleboneSprite.getHeight();
            Vector2 skelebonePos = new Vector2(skeleboneSprite.getX(), skeleboneSprite.getY());
            Vector2 heroPosVec = new Vector2(heroSprite.getX(), heroSprite.getY());
            Vector2 direction = new Vector2(heroPosVec).sub(skelebonePos);
            float distance = direction.len();
            float movementDistance = skeleboneSpeed * delta;
            if (movementDistance >= distance) {
                skeleboneSprite.setPosition(heroPosVec.x, heroPosVec.y);
            } else {
                direction.nor().scl(movementDistance);
                skeleboneSprite.translate(direction.x, direction.y);
            }
            skeleboneRectangle.set(skeleboneSprite.getX(), skeleboneSprite.getY(), skeleboneWidth, skeleboneHeight);
            if (skeleboneRectangle.overlaps(heroRectangle)) {
                if (Invincible) {
                    damageCooldownTime -= delta;
                    if (damageCooldownTime <= 0) {
                        Invincible = false;
                        damageCooldownTime = 0f;
                    }
                } else {
                    takeDamage(5);
                }
            }
            boolean skeleboneDied = false;
            for (int b = bulletSpriteList.size - 1; b >= 0; b--) {
                Sprite bulletSprite = bulletSpriteList.get(b);
                bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(), bulletSprite.getWidth(), bulletSprite.getHeight());
                if (bulletRectangle.overlaps(skeleboneRectangle)) {
                    bulletSpriteList.removeIndex(b);
                    if (b < bulletX.size) bulletX.removeIndex(b);
                    if (b < bulletY.size) bulletY.removeIndex(b);
                    if (b < bulletDX.size) bulletDX.removeIndex(b);
                    if (b < bulletDY.size) bulletDY.removeIndex(b);
                    skeleboneDied = true;
                    break;
                }
            }
            if (skeleboneDied) {
                skeleboneSpriteList.removeIndex(i);
                skeleboneAliveList.removeIndex(i);
                numEnemies--;
                score += 100;
                scoreSound.play();
            }
        }
        for (int i = spiderSpriteList.size - 1; i >= 0; i--) {
            Sprite spiderSprite = spiderSpriteList.get(i);
            float spiderWidth = spiderSprite.getWidth();
            float spiderHeight = spiderSprite.getHeight();
            Vector2 spiderPos = new Vector2(spiderSprite.getX(), spiderSprite.getY());
            Vector2 heroPosVec = new Vector2(heroSprite.getX(), heroSprite.getY());
            Vector2 direction = new Vector2(heroPosVec).sub(spiderPos);
            float distance = direction.len();
            float movementDistance = spiderSpeed * delta;
            if (movementDistance >= distance) {
                spiderSprite.setPosition(heroPosVec.x, heroPosVec.y);
            } else {
                direction.nor().scl(movementDistance);
                spiderSprite.translate(direction.x, direction.y);
            }
            spiderRectangle.set(spiderSprite.getX(), spiderSprite.getY(), spiderWidth, spiderHeight);
            if (spiderRectangle.overlaps(heroRectangle)) {
                if (Invincible) {
                    damageCooldownTime -= delta;
                    if (damageCooldownTime <= 0) {
                        Invincible = false;
                        damageCooldownTime = 0f;
                    }
                } else {
                    takeDamage(5);
                }
            }
            boolean spiderDied = false;
            for (int b = bulletSpriteList.size - 1; b >= 0; b--) {
                Sprite bulletSprite = bulletSpriteList.get(b);
                bulletRectangle.set(bulletSprite.getX(), bulletSprite.getY(), bulletSprite.getWidth(), bulletSprite.getHeight());
                if (bulletRectangle.overlaps(spiderRectangle)) {
                    bulletSpriteList.removeIndex(b);
                    if (b < bulletX.size) bulletX.removeIndex(b);
                    if (b < bulletY.size) bulletY.removeIndex(b);
                    if (b < bulletDX.size) bulletDX.removeIndex(b);
                    if (b < bulletDY.size) bulletDY.removeIndex(b);
                    spiderDied = true;
                    break;
                }
            }
            if (spiderDied) {
                spiderSpriteList.removeIndex(i);
                spiderAliveList.removeIndex(i);
                numEnemies--;
                score += 100;
                scoreSound.play();
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
        font.draw(spriteBatch, "Score = " + String.valueOf(score), 50, 110);
        if (health > 0) {
            heroSprite.draw(spriteBatch);
            for (Sprite bulletSprite : bulletSpriteList) {
                bulletSprite.draw(spriteBatch);
            }
            font.draw(spriteBatch, "Health= " + String.valueOf(health), 1, 110);
        }
        if (health <= 0) {
            skeleboneSpriteList.clear();
            spiderSpriteList.clear();
            bulletSpriteList.clear();
            skeleboneAliveList.clear();
            spiderAliveList.clear();
            bulletX.clear();
            bulletY.clear();
            bulletDX.clear();
            bulletDY.clear();
            numEnemies = 0;
            music.stop();
            if(!hasPlayedFail){
                failSound.play();
                hasPlayedFail = true;
            }
            font.draw(spriteBatch, "You Died, Space to Restart", 1, 61);
            font.draw(spriteBatch, "Score= " + String.valueOf(score), 1, 51);
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                health = 100;
                score = 0;
                music.setLooping(true);
                music.setVolume(.5f);
                music.play();
                hasPlayedFail = false;
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
    public void createBullet() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.getCamera().unproject(mousePos); // Now mousePos matches your 123x123 world size
        float startX = heroSprite.getX() + 6;
        float startY = heroSprite.getY() + 6;
        float directionX = mousePos.x - startX;
        float directionY = mousePos.y - startY;
        float distance = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (distance == 0) distance = 1f;
        float normalizedX = directionX / distance;
        float normalizedY = directionY / distance;
        Sprite newBulletSprite = new Sprite(bulletTexture);
        newBulletSprite.setPosition(startX, startY);
        newBulletSprite.setSize(4, 4); // Match your game's small viewport scale
        bulletSpriteList.add(newBulletSprite);
        bulletX.add(startX);
        bulletY.add(startY);
        bulletDX.add(normalizedX * bulletSpeed);
        bulletDY.add(normalizedY * bulletSpeed);
    }

    private void takeDamage(int x){
        if (Invincible)return;
        health -= x;
        Invincible = true;
        hurtSound.play();
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


