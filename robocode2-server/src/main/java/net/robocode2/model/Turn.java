package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// FIXME: Missing events: Bullet hit bot, bullet hit bullet, bot hit bot + ramming, bot hit wall
public final class Turn {

	private final int turnNumber;
	private final Set<Bot> bots;
	private final Set<Bullet> bullets;

	public Turn(int turnNumber, Set<Bot> bots, Set<Bullet> bullets) {
		this.turnNumber = turnNumber;
		this.bots = new HashSet<>(bots);
		this.bullets = new HashSet<>(bullets);
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public Set<Bot> getBots() {
		return Collections.unmodifiableSet(bots);
	}

	public Bot getBot(int botId) {
		return bots.stream().filter(b -> b.getId() == botId).findAny().get();
	}

	public Set<Bullet> getBullets() {
		return Collections.unmodifiableSet(bullets);
	}

	public Set<Bullet> getBullets(int botId) {
		return bullets.stream().filter(b -> b.getBotId() == botId).collect(Collectors.toSet());
	}

	public static final class TurnBuilder {
		private int turnNumber;
		private Set<Bot> bots = new HashSet<>();
		private Set<Bullet> bullets = new HashSet<>();

		public Turn build() {
			return new Turn(turnNumber, bots, bullets);
		}

		public TurnBuilder setTurnNumber(int turnNumber) {
			this.turnNumber = turnNumber;
			return this;
		}

		public TurnBuilder setBots(Set<Bot> bots) {
			this.bots = new HashSet<>(bots);
			return this;
		}

		public TurnBuilder setBullets(Set<Bullet> bullets) {
			this.bullets = new HashSet<>(bullets);
			return this;
		}

		public TurnBuilder addBullet(Bullet bullet) {
			bullets.add(bullet);
			return this;
		}
	}
}