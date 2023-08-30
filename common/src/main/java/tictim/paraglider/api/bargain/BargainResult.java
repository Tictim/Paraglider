package tictim.paraglider.api.bargain;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

/**
 * Result of {@link Bargain#bargain(Player, boolean)}.
 */
public sealed abstract class BargainResult{
	/**
	 * Make a successful bargain result.
	 *
	 * @return Bargain result instance
	 */
	@NotNull public static BargainResult success(){
		return Success.instance;
	}
	/**
	 * Make a failed bargain result with specified reasons.
	 *
	 * @param reasons Array of reasons for failure
	 * @return Bargain result instance
	 */
	@NotNull public static BargainResult fail(@NotNull String @NotNull ... reasons){
		return new Fail(Set.of(reasons));
	}
	/**
	 * Make a failed bargain result with specified reasons.
	 *
	 * @param reasons Set of reasons for failure
	 * @return Bargain result instance
	 */
	@NotNull public static BargainResult fail(@NotNull Set<@NotNull String> reasons){
		return new Fail(Set.copyOf(reasons));
	}

	/**
	 * @return Whether this result is success or not
	 */
	public abstract boolean isSuccess();

	/**
	 * @return Failed reason for this bargain result. If the result is success (that is, the value of
	 * {@link #isSuccess()} is true) then the set of reasons will be empty.
	 * @see ParagliderFailReasons
	 */
	@NotNull @Unmodifiable public abstract Set<@NotNull String> failReasons();

	private static final class Success extends BargainResult{
		private static final Success instance = new Success();

		@Override public boolean isSuccess(){
			return true;
		}

		@Override @NotNull @Unmodifiable public Set<@NotNull String> failReasons(){
			return Set.of();
		}

		@Override public String toString(){
			return "Success";
		}
	}

	private static final class Fail extends BargainResult{
		private final @NotNull @Unmodifiable Set<@NotNull String> failReasons;

		private Fail(@NotNull @Unmodifiable Set<@NotNull String> failReasons){
			this.failReasons = failReasons;
		}

		@Override public boolean isSuccess(){
			return false;
		}

		@Override @NotNull @Unmodifiable public Set<@NotNull String> failReasons(){
			return failReasons;
		}

		@Override
		public boolean equals(Object obj){
			if(obj==this) return true;
			if(!(obj instanceof Fail fail)) return false;
			return Objects.equals(this.failReasons, fail.failReasons);
		}

		@Override
		public int hashCode(){
			return Objects.hash(failReasons);
		}

		@Override
		public String toString(){
			return "Fail("+String.join(", ", failReasons)+')';
		}
	}

}
