package tictim.paraglider.client.dialog;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import tictim.paraglider.dialog.DialogContainer;
import tictim.paraglider.dialog.IntTracker;
import tictim.paraglider.utils.ParagliderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bunch of overcomplicated stupid text thing used only in single class.
 */
public final class TextUnit{
	private static final String NAMESPACE_REGEX = "(?:[a-z0-9_.-]+:)?[a-z0-9_.-]+";

	private static final Pattern INT_PATTERN = Pattern.compile("^\\{("+IntTracker.NAME_REGEX+")}");
	private static final Pattern ITEM_PATTERN = Pattern.compile("^\\{item:("+NAMESPACE_REGEX+")}");

	public static TextUnit[] parse(String text, DialogContainer container){
		List<TextUnit> textUnits = new ArrayList<>();

		int lineStart = 0;
		List<String> strings = new ArrayList<>();
		boolean incomplete = false;
		for(int i = 0; i<text.length(); i++){
			switch(text.charAt(i)){
				case '\n':
					append(text, strings, lineStart, i, incomplete);
					incomplete = false;
					lineStart = i+1;
					break;
				case '|':
					append(text, strings, lineStart, i, incomplete);
					incomplete = true;
					textUnits.add(new TextUnit(strings));
					lineStart = i+1;
					break;
				case '{':
					Matcher matcher = INT_PATTERN.matcher(text);
					matcher.region(i, text.length());

					boolean isItem;

					if(!matcher.find()){
						matcher.usePattern(ITEM_PATTERN).reset(text).region(i, text.length());
						if(!matcher.find()) break;
						isItem = true;
					}else isItem = false;

					append(text, strings, lineStart, i, incomplete);
					incomplete = true;
					lineStart = matcher.end();
					i = lineStart-1;

					String group = matcher.group(1);

					if(isItem){
						Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(group));
						if(item!=null&&item!=Items.AIR){
							int count = ParagliderUtils.count(container.getPlayerInventory(), item);
							append(Integer.toString(count), strings, true);
						}else{
							append("##ERROR: No Item with ID of "+group+"##", strings, true);
						}
					}else{
						IntTracker intTracker = container.getDialog().getIntTracker(group);
						if(intTracker!=null){
							int trackedInt = container.getTrackedInt(intTracker.getIndex());
							append(Integer.toString(trackedInt), strings, true);
						}else{
							append("##ERROR: No IntTracker with ID of "+group+"##", strings, true);
						}
					}

					break;
			}
		}

		if(incomplete){
			int index = strings.size()-1;
			strings.set(index, strings.get(index)+text.substring(lineStart));
		}else strings.add(text.substring(lineStart));
		textUnits.add(new TextUnit(strings));

		return textUnits.toArray(new TextUnit[0]);
	}

	private static void append(String text, List<String> strings, int lineStart, int end, boolean incomplete){
		append(text.substring(lineStart, end), strings, incomplete);
	}
	private static void append(String text, List<String> strings, boolean incomplete){
		if(incomplete){
			int lastIndex = strings.size()-1;
			strings.set(lastIndex, strings.get(lastIndex)+text);
		}else{
			strings.add(text);
		}
	}

	/**
	 * Dialog text divided by newlines.
	 */
	private final String[] text;

	public TextUnit(List<String> text){
		this(text.toArray(new String[0]));
	}
	private TextUnit(String[] text){
		this.text = Objects.requireNonNull(text);
	}

	public int getWidth(int i, FontRenderer font){
		return i<0||i>=text.length ? 0 : font.getStringWidth(text[i]);
	}

	public String getText(int i){
		return text[i];
	}
	public int getLines(){
		return text.length;
	}

	public int getMaxWidth(FontRenderer font){
		int maxWidth = 0;
		for(int i = 0; i<text.length; i++){
			int width = getWidth(i, font);
			if(maxWidth<width) maxWidth = width;
		}
		return maxWidth;
	}
}
