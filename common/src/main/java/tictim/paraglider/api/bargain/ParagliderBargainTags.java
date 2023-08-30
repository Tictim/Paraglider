package tictim.paraglider.api.bargain;

/**
 * Default set of bargain tags used by Paragliders mod. It is highly encouraged for custom bargain implementations to
 * include support for these tags.
 *
 * @see Bargain#getBargainTags()
 */
public interface ParagliderBargainTags{
	/**
	 * Tag for bargains consuming items.
	 */
	String CONSUMES_ITEM = "consumes_item";
	/**
	 * Tag for bargains consuming Heart Containers.
	 */
	String CONSUMES_HEART_CONTAINER = "consumes_heart_container";
	/**
	 * Tag for bargains consuming Stamina Vessels.
	 */
	String CONSUMES_STAMINA_VESSEL = "consumes_stamina_vessel";
	/**
	 * Tag for bargains consuming Essences.
	 */
	String CONSUMES_ESSENCE = "consumes_essence";
	/**
	 * Tag for bargains giving items.
	 */
	String GIVES_ITEM = "gives_item";
	/**
	 * Tag for bargains giving Heart Containers.
	 */
	String GIVES_HEART_CONTAINER = "gives_heart_container";
	/**
	 * Tag for bargains giving Stamina Vessels.
	 */
	String GIVES_STAMINA_VESSEL = "gives_stamina_vessel";
	/**
	 * Tag for bargains giving Essences.
	 */
	String GIVES_ESSENCE = "gives_essence";
}
