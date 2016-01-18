package sypan.draughts.client.effect;

import sypan.draughts.client.Client;
import sypan.draughts.client.manager.SoundManager.SoundType;
import sypan.draughts.game.player.Side;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 * Plays when a piece is removed from the board.<p>
 *
 * It is based on a class named {@code TestExplosionEffect}, which can be found
 * in JMonkeyEngine's Google code repository (URL in the <b>See Also</b> section).<p>
 *
 * @see
 * <a href="https://jmonkeyengine.googlecode.com/svn/trunk/engine/src/test/jme3test/effect/TestExplosionEffect.java">
 * https://jmonkeyengine.googlecode.com/svn/trunk/engine/src/test/jme3test/effect/TestExplosionEffect.java</a>
 *
 * @author Carl Linley
 * @author JMonkeyEngine 3
 **/
public class ExplosionEffect extends AbstractEffect {

    private enum ExplosionStage {
        PRIMARY_EMISSION, SECONDARY_EMISSION, DECOMPOSITION, COMPLETE
    };

    private final ColorRGBA debrisColour;

    private ExplosionStage currentStage = ExplosionStage.PRIMARY_EMISSION;

    private ParticleEmitter debrisEmitter, flameEmitter, flashEmitter, roundSparkEmitter,
                            shockwaveEmitter, smokeTrailEmitter, sparkEmitter;

    private PointLight explosionLight;
    private float lightRadius;

    public ExplosionEffect(Client c, boolean largeExplosion, Side pieceSide) {
        setClient(c);
        debrisColour = (pieceSide == Side.BLACK ? ColorRGBA.DarkGray : ColorRGBA.LightGray);

        createFlame();
        createFlash();
        createSpark();
        createRoundSpark();
        createSmokeTrail();
        createDebris();
        createShockwave();

        setLocalScale(largeExplosion ? 1f : 0.25f);

        explosionLight = new PointLight();
        explosionLight.setRadius((lightRadius = (largeExplosion ? 50f : 25f)));
        explosionLight.setColor(ColorRGBA.Orange);
        c.getRootNode().addLight(explosionLight);

        getClient().getSoundManager().playSound((largeExplosion ? SoundType.KING_EXPLOSION : SoundType.MAN_EXPLOSION), getLocalTranslation());
    }

    @Override
    public void update(Client draughts, float timePerFrame) {
        incremementTime(timePerFrame);

        switch (currentStage) {
            case PRIMARY_EMISSION:
                flashEmitter.emitAllParticles();
                sparkEmitter.emitAllParticles();
                smokeTrailEmitter.emitAllParticles();
                debrisEmitter.emitAllParticles();
                shockwaveEmitter.emitAllParticles();

                currentStage = ExplosionStage.SECONDARY_EMISSION;
            break;

            case SECONDARY_EMISSION:
                if (getTime() > 0.05f) {
                    flameEmitter.emitAllParticles();
                    roundSparkEmitter.emitAllParticles();

                    currentStage = ExplosionStage.DECOMPOSITION;
                }
            break;

            case DECOMPOSITION:
                if (getTime() > 4f) {
                    currentStage = ExplosionStage.COMPLETE;
                }
            break;

            case COMPLETE:
                destroy();
                markComplete();
            break;
        }

        if (currentStage != ExplosionStage.COMPLETE) {
            updateLight();
        }
    }

    private void updateLight() {
        if (explosionLight == null) {
            return;
        }

        if (lightRadius != -1 && (lightRadius -= getTime()) > 0) {
            if (explosionLight != null) {// Null check required, sometimes explosionLight is prematurely null.
                explosionLight.setRadius(lightRadius);
            }
        } else {
            getClient().getRootNode().removeLight(explosionLight);
            explosionLight = null;
        }
    }

    @Override
    public void destroy() {
        flashEmitter.killAllParticles();
        sparkEmitter.killAllParticles();
        smokeTrailEmitter.killAllParticles();
        debrisEmitter.killAllParticles();
        flameEmitter.killAllParticles();
        roundSparkEmitter.killAllParticles();
        shockwaveEmitter.killAllParticles();
    }

    /**
     * When this node is moved, the light needs moving too.
     *
     * @param newLocation - the local to move to.
     **/
    @Override
    public void setLocalTranslation(Vector3f newLocation) {
        super.setLocalTranslation(newLocation);
        explosionLight.setPosition(newLocation);
    }

    /**
     * Creates the flame part of the effect.
     **/
    private void createFlame() {
        flameEmitter = new ParticleEmitter("Flame", Type.Point, 32);
        flameEmitter.setSelectRandomImage(true);
        flameEmitter.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, 1));
        flameEmitter.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flameEmitter.setStartSize(1.3f);
        flameEmitter.setEndSize(2f);
        flameEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flameEmitter.setParticlesPerSec(0);
        flameEmitter.setGravity(0, -5, 0);
        flameEmitter.setLowLife(.4f);
        flameEmitter.setHighLife(.5f);
        flameEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
        flameEmitter.getParticleInfluencer().setVelocityVariation(1f);
        flameEmitter.setImagesX(2);
        flameEmitter.setImagesY(2);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/FLAME.png"));
        emitterMaterial.setBoolean("PointSprite", true);

        flameEmitter.setMaterial(emitterMaterial);
        attachChild(flameEmitter);
    }
    
    /**
     * Creates the flash part of the effect.
     **/
    private void createFlash() {
        flashEmitter = new ParticleEmitter("Flash", Type.Point, 24);
        flashEmitter.setSelectRandomImage(true);
        flashEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1));
        flashEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        flashEmitter.setStartSize(.1f);
        flashEmitter.setEndSize(3.0f);
        flashEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        flashEmitter.setParticlesPerSec(0);
        flashEmitter.setGravity(0, 0, 0);
        flashEmitter.setLowLife(.2f);
        flashEmitter.setHighLife(.2f);
        flashEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 5f, 0));
        flashEmitter.getParticleInfluencer().setVelocityVariation(1);
        flashEmitter.setImagesX(2);
        flashEmitter.setImagesY(2);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/FLASH.png"));
        emitterMaterial.setBoolean("PointSprite", true);

        flashEmitter.setMaterial(emitterMaterial);
        attachChild(flashEmitter);
    }

    /**
     * Creates the spark part of the effect.
     **/
    private void createRoundSpark() {
        roundSparkEmitter = new ParticleEmitter("RoundSpark", Type.Point, 20);
        roundSparkEmitter.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, 1));
        roundSparkEmitter.setEndColor(new ColorRGBA(0, 0, 0, 0.5f));
        roundSparkEmitter.setStartSize(1.2f);
        roundSparkEmitter.setEndSize(1.8f);
        roundSparkEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        roundSparkEmitter.setParticlesPerSec(0);
        roundSparkEmitter.setGravity(0, -.5f, 0);
        roundSparkEmitter.setLowLife(1.8f);
        roundSparkEmitter.setHighLife(2f);
        roundSparkEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3, 0));
        roundSparkEmitter.getParticleInfluencer().setVelocityVariation(.5f);
        roundSparkEmitter.setImagesX(1);
        roundSparkEmitter.setImagesY(1);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/ROUND_SPARK.png"));
        emitterMaterial.setBoolean("PointSprite", true);

        roundSparkEmitter.setMaterial(emitterMaterial);
        attachChild(roundSparkEmitter);
    }

    /**
     * Creates the jumping sparks of the effect.
     **/
    private void createSpark() {
        sparkEmitter = new ParticleEmitter("Spark", Type.Triangle, 30);
        sparkEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
        sparkEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        sparkEmitter.setStartSize(.5f);
        sparkEmitter.setEndSize(.5f);
        sparkEmitter.setFacingVelocity(true);
        sparkEmitter.setParticlesPerSec(0);
        sparkEmitter.setGravity(0, 5, 0);
        sparkEmitter.setLowLife(1.1f);
        sparkEmitter.setHighLife(1.5f);
        sparkEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 20, 0));
        sparkEmitter.getParticleInfluencer().setVelocityVariation(1);
        sparkEmitter.setImagesX(1);
        sparkEmitter.setImagesY(1);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/SPARK.png"));
        sparkEmitter.setMaterial(emitterMaterial);

        attachChild(sparkEmitter);
    }
    
    /**
     * Creates the smoke part of the effect.
     **/
    private void createSmokeTrail() {
        smokeTrailEmitter = new ParticleEmitter("SmokeTrail", Type.Triangle, 22);
        smokeTrailEmitter.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, 1f));
        smokeTrailEmitter.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        smokeTrailEmitter.setStartSize(.2f);
        smokeTrailEmitter.setEndSize(1f);

        smokeTrailEmitter.setFacingVelocity(true);
        smokeTrailEmitter.setParticlesPerSec(0);
        smokeTrailEmitter.setGravity(0, 1, 0);
        smokeTrailEmitter.setLowLife(.4f);
        smokeTrailEmitter.setHighLife(.5f);
        smokeTrailEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 12, 0));
        smokeTrailEmitter.getParticleInfluencer().setVelocityVariation(1);
        smokeTrailEmitter.setImagesX(1);
        smokeTrailEmitter.setImagesY(3);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/SMOKE_TRAIL.png"));
        smokeTrailEmitter.setMaterial(emitterMaterial);

        attachChild(smokeTrailEmitter);
    }
    
    /**
     * Creates the debris part of the effect.
     **/
    private void createDebris() {
        debrisEmitter = new ParticleEmitter("Debris", Type.Triangle, 15);
        debrisEmitter.setSelectRandomImage(true);
        debrisEmitter.setRandomAngle(true);
        debrisEmitter.setRotateSpeed(FastMath.TWO_PI * 4);
        debrisEmitter.setStartColor(debrisColour);
        debrisEmitter.setEndColor(debrisColour.mult(new ColorRGBA(1, 1, 1, 0)));
        debrisEmitter.setStartSize(.2f);
        debrisEmitter.setEndSize(.2f);
        debrisEmitter.setParticlesPerSec(0);
        debrisEmitter.setGravity(0, 12f, 0);
        debrisEmitter.setLowLife(1.4f);
        debrisEmitter.setHighLife(1.5f);
        debrisEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 15, 0));
        debrisEmitter.getParticleInfluencer().setVelocityVariation(.60f);
        debrisEmitter.setImagesX(3);
        debrisEmitter.setImagesY(3);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/DEBRIS.png"));
        debrisEmitter.setMaterial(emitterMaterial);

        attachChild(debrisEmitter);
    }

    /**
     * Creates the shockwave part of the effect.
     **/
    private void createShockwave() {
        shockwaveEmitter = new ParticleEmitter("Shockwave", Type.Triangle, 1);
        shockwaveEmitter.setFaceNormal(Vector3f.UNIT_Y);
        shockwaveEmitter.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0.8f));
        shockwaveEmitter.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));

        shockwaveEmitter.setStartSize(0f);
        shockwaveEmitter.setEndSize(7f);

        shockwaveEmitter.setParticlesPerSec(0);
        shockwaveEmitter.setGravity(0, 0, 0);
        shockwaveEmitter.setLowLife(0.5f);
        shockwaveEmitter.setHighLife(0.5f);
        shockwaveEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        shockwaveEmitter.getParticleInfluencer().setVelocityVariation(0f);
        shockwaveEmitter.setImagesX(1);
        shockwaveEmitter.setImagesY(1);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/SHOCKWAVE.png"));
        shockwaveEmitter.setMaterial(emitterMaterial);

        attachChild(shockwaveEmitter);
    }

    /**
     * @return the effect's point light.
     **/
    public PointLight getLight() {
        return explosionLight;
    }
}
