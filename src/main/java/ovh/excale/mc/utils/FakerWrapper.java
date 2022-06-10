package ovh.excale.mc.utils;

import com.github.javafaker.Faker;
import ovh.excale.mc.UHC;
import ovh.excale.mc.uhc.configuration.ConfigKeys;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * A Wrapper Class for the {@link Faker} class, wich convert a dot-notated string into a faker method.<br><br>
 * Example: converts "esports.team" into faker method esports().team()
 */
public class FakerWrapper {

	private final static Faker faker = Faker.instance(new Locale(UHC.instance()
			.getConfig()
			.getString(ConfigKeys.FAKER_LOCALE, "en")));

	private final String fakerString;

	private final FakerEnum fakerEnum;

	/**
	 * The constructor of the wrapper class<br>
	 * Convert the given string into a faker method, if the string is invalid it throws an {@link IllegalArgumentException}
	 *
	 * @param fakerString The string to convert into the faker method
	 * @throws IllegalArgumentException Thrown when the string is invalid
	 */
	public FakerWrapper(String fakerString) throws IllegalArgumentException {
		this.fakerString = fakerString;

		fakerEnum = FakerEnum.get(this.fakerString);
		if(fakerEnum == null)
			throw new IllegalArgumentException();

	}

	/**
	 * A method which returns the string passed in the constructor
	 *
	 * @return The {@link String} passed in the constructor
	 */
	public String getFakerString() {
		return fakerString;
	}

	/**
	 * The Enumerator class which handles the conversion from string to faker method
	 */
	public enum FakerEnum {
		// TODO: create enum for all faker random methods
		ESPORTS_TEAM("esports.team", faker.esports()::team),
		ESPORTS_PLAYER("esports.player", faker.esports()::player),
		ESPORTS_LEAGUE("esports.league", faker.esports()::league),
		ESPORTS_EVENT("esports.event", faker.esports()::event);

		private final String name;
		private final Supplier<String> function;

		FakerEnum(String n, Supplier<String> f) {
			name = n;
			function = f;
		}

		/**
		 * The method which list all the {@link FakerEnum} entries and return the entry which name correspond to the given string
		 *
		 * @param name The {@link String} to search in the entries names
		 * @return The {@link FakerEnum} entry if the string matches, if not returns null
		 */
		public static FakerEnum get(String name) {
			for(FakerEnum value : FakerEnum.values()) {
				if(value.name.equals(name))
					return value;
			}
			return null;
		}

	}

	/**
	 * The method which invokes the faker method and returns the random {@link String}
	 *
	 * @return The random {@link String} which the {@link Faker} method returns
	 */
	public String getString() {
		return fakerEnum.function.get();
	}

}
