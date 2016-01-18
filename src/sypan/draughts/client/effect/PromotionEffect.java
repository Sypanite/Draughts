package sypan.draughts.client.effect;

import sypan.draughts.client.Client;
import sypan.draughts.game.piece.Piece;

import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 * Plays when a man is promoted to a king.
 *
 * @author Carl Linley
 **/
public class PromotionEffect extends AbstractEffect {

    private enum EffectStage {
        INITIALISATION, REGENERATION, DESTRUCTION
    };

    private final ColorRGBA coreColour, startColour, endColour;
    private final PointLight effectLight;
    private final Piece toPromote;

    private ParticleEmitter ringEmitter;
    
    private EffectStage currentStage;
    private float lightRadius;

    public PromotionEffect(Client c, Piece toPromote) {
        setClient(c);
        this.toPromote = toPromote;
        currentStage = EffectStage.INITIALISATION;

        coreColour = ColorRGBA.randomColor();
        startColour = coreColour.mult(new ColorRGBA(1f, 1f, 1f, 0.1f));
        endColour = coreColour.mult(new ColorRGBA(1f, 1f, 1f, 0f));

        effectLight = new PointLight();
        effectLight.setColor(coreColour);
        c.getRootNode().addLight(effectLight);

        initRingEmitter();
    }

    private void initRingEmitter() {
        ringEmitter = new ParticleEmitter("RING_EMITTER", Type.Triangle, 500);
        ringEmitter.setFaceNormal(Vector3f.UNIT_Y);
        ringEmitter.setStartColor(startColour);
        ringEmitter.setEndColor(endColour);

        ringEmitter.setStartSize(1.25f);
        ringEmitter.setEndSize(1.25f);

        ringEmitter.setParticlesPerSec(500);
        ringEmitter.getParticleInfluencer().setVelocityVariation(0.1f);
        ringEmitter.setGravity(0, 0, 0);

        ringEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 10f, 0));

        ringEmitter.setImagesX(1);
        ringEmitter.setImagesY(1);

        Material emitterMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        emitterMaterial.setTexture("Texture", getAssetManager().loadTexture("textures/effect/SHOCKWAVE.png"));
        ringEmitter.setMaterial(emitterMaterial);

        attachChild(ringEmitter);
    }

    @Override
    public void update(Client c, float timePerFrame) {
        incremementTime((timePerFrame * 60));

        if (currentStage.ordinal() < 2) {
            lightRadius += (timePerFrame * 20);

            if (lightRadius < 50) {
                effectLight.setRadius(lightRadius);
            }
            else {
                lightRadius = 50;
                effectLight.setRadius(lightRadius);
            }
        }
        else if (lightRadius != -1) {
            lightRadius -= (timePerFrame * 20);

            if (lightRadius > 0) {
                effectLight.setRadius(lightRadius);
            }
            else {
                effectLight.setRadius(0.1f);
            }
        }

        switch (currentStage) {
            case INITIALISATION:
                currentStage = EffectStage.REGENERATION;
            break;

            case REGENERATION://Change the piece's type, its model, and play the fanfare sound.
                if (getTime() > 60f) {
                    getClient().getCurrentGame().applyPromotion(toPromote);

                    ringEmitter.setParticlesPerSec(0);
                    currentStage = EffectStage.DESTRUCTION;
                }
            break;

            case DESTRUCTION:
                if (getTime() > 175f) {
                    markComplete();
                }
            break;
        }
    }

    @Override
    public void destroy() {
        ringEmitter.killAllParticles();
    }

    /**
     * When this node is moved, the light needs moving too.
     * @param newLocation - the node's new local translation.
     **/
    @Override
    public void setLocalTranslation(Vector3f newLocation) {
        super.setLocalTranslation(newLocation);
        effectLight.setPosition(newLocation.add(0, 3, 0));
    }
}
