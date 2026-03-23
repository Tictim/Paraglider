package tictim.paraglider.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.api.ParagliderAPI;
import tictim.paraglider.wind.WindLogic;

public class WindParticleProvider implements ParticleProvider<SimpleParticleType> {
	public static final Identifier PARTICLE_TYPE_ID = ParagliderAPI.id("wind");
	public static final SimpleParticleType PARTICLE_TYPE = new SimpleParticleType(false);

	private final SpriteSet sprites;

	public WindParticleProvider(SpriteSet sprites) {
		this.sprites = sprites;
	}

	@Override public @Nullable Particle createParticle(
			@NotNull SimpleParticleType particleType, @NotNull ClientLevel level,
			double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed,
			@NotNull RandomSource random) {
		return new WindParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
	}

	public static class WindParticle extends SimpleAnimatedParticle {
		private boolean outsideWind;

		public WindParticle(
				ClientLevel level, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites
		) {
			super(level, x, y, z, sprites, 0.5F);
			this.xd = xSpeed;
			this.yd = ySpeed;
			this.zd = zSpeed;
			this.friction = 0.9f;
			this.quadSize *= 0.75F;
			this.lifetime = 12 + this.random.nextInt(12);
			this.setSpriteFromAge(sprites);
		}

		@Override public void tick() {
			if (!this.outsideWind) {
				double windAbove = WindLogic.getWindAbove(this.level, getBoundingBox());
				if (windAbove > 1) {
					this.lifetime++;
					this.yd = 0.5;
				} else {
					this.outsideWind = true;
					this.friction = 0.8f;
				}
			}

			super.tick();

			if (!this.outsideWind && this.y == this.yo) this.outsideWind = true;
		}

		@Override public void move(double x, double y, double z) {
			if (this.outsideWind) super.move(x, y, z);
			else {
				setBoundingBox(getBoundingBox().move(x, y, z));
				setLocationFromBoundingbox();
			}
		}
	}
}
