package tictim.paraglider.dialog.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.dialog.Scenario;
import tictim.paraglider.dialog.data.ChoiceData;
import tictim.paraglider.dialog.data.Diff;
import tictim.paraglider.dialog.data.DiffFile;
import tictim.paraglider.dialog.data.EndData;
import tictim.paraglider.dialog.data.ErrorData;
import tictim.paraglider.dialog.data.GoToData;
import tictim.paraglider.dialog.data.IfActionData;
import tictim.paraglider.dialog.data.ScenarioData;
import tictim.paraglider.dialog.data.ScriptData;
import tictim.paraglider.dialog.data.TextData;
import tictim.paraglider.dialog.script.Choice;
import tictim.paraglider.dialog.script.Script;
import tictim.paraglider.dialog.script.Text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class DialogJson{
	private DialogJson(){}

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(ScenarioData.class, (JsonDeserializer<ScenarioData>)(json, typeOfT, context) -> {
				JsonObject obj = json.getAsJsonObject();
				return parseScenario(obj.get("scenario"));
			})
			.registerTypeAdapter(DiffFile.class, (JsonDeserializer<DiffFile>)(json, typeOfT, context) -> {
				JsonObject obj = json.getAsJsonObject();
				DiffFile diffFile = new DiffFile();
				for(JsonElement diff : obj.get("diff").getAsJsonArray()){
					diffFile.diff.add(parseDiff(diff.getAsJsonObject()));
				}
				return diffFile;
			})
			.create();

	private static ScenarioData parseScenario(JsonElement element){
		ScenarioData d = new ScenarioData();
		parseScriptData(element, d.scenario);
		return d;
	}

	private static void parseScriptData(JsonElement element, List<ScriptData> list){
		if(element.isJsonPrimitive()){
			list.add(new TextData(element.getAsString()));
		}else if(element.isJsonArray()){
			for(JsonElement e : element.getAsJsonArray()) parseScriptData(e, list);
		}else if(element.isJsonObject()){
			JsonObject obj = element.getAsJsonObject();
			ScriptData parsed = null;

			if(obj.has("dialog")){
				TextData textData = new TextData(obj.get("dialog").getAsString());
				parsed = textData;
				JsonElement choices = obj.get("choices");
				if(choices!=null){
					textData.choices = parseChoiceData(choices.getAsJsonArray());
				}
			}
			if(obj.has("go")){
				if(parsed!=null) throw new IllegalStateException("Ambiguous script type");
				parsed = new GoToData(obj.get("go").getAsString());
			}
			if(obj.has("do")){
				if(parsed!=null) throw new IllegalStateException("Ambiguous script type");
				IfActionData ifAction = new IfActionData(obj.get("do").getAsString());
				parsed = ifAction;
				JsonElement e = obj.get("if");
				if(e!=null){
					ifAction.ifScenario = parseScenario(e);
				}
				e = obj.get("else");
				if(e!=null){
					ifAction.elseScenario = parseScenario(e);
				}
			}
			if(obj.has("error")){
				if(parsed!=null) throw new IllegalStateException("Ambiguous script type");
				parsed = new ErrorData(obj.get("error").getAsString());
			}
			if(obj.has("end")){
				if(parsed!=null) throw new IllegalStateException("Ambiguous script type");
				parsed = new EndData();
			}
			if(obj.has("scenario")){
				if(parsed!=null) throw new IllegalStateException("Ambiguous script type");
				parsed = parseScenario(obj.get("scenario"));
			}

			if(parsed==null) throw new IllegalStateException("Cannot specify script type");

			JsonElement label = obj.get("label");
			if(label!=null){
				parsed.label = label.getAsString();
			}
			list.add(parsed);
		}else throw new IllegalArgumentException("Invalid Json element type");
	}

	private static ChoiceData parseChoiceData(JsonArray array){
		ChoiceData choiceData = new ChoiceData();

		for(JsonElement e : array){
			JsonObject o = e.getAsJsonObject();

			String text = o.get("text").getAsString();
			JsonElement scenario = o.get("scenario");
			JsonElement then = o.get("then");

			choiceData.cases.add(new ChoiceData.Case(
					text,
					scenario==null ? new ScenarioData() : parseScenario(scenario),
					then==null ? null : then.getAsString()));
		}

		return choiceData;
	}

	private static Diff parseDiff(JsonObject obj){
		JsonElement actionJson = obj.get("before");
		Diff.Action action = null;
		String target = null;

		if(actionJson!=null){
			action = Diff.Action.BEFORE;
			target = actionJson.getAsString();
		}
		actionJson = obj.get("after");
		if(actionJson!=null){
			if(action!=null) throw new IllegalStateException("Ambiguous diff action");
			action = Diff.Action.AFTER;
			target = actionJson.getAsString();
		}
		actionJson = obj.get("replace");
		if(actionJson!=null){
			if(action!=null) throw new IllegalStateException("Ambiguous diff action");
			action = Diff.Action.REPLACE;
			target = actionJson.getAsString();
		}

		if(action==null) throw new IllegalStateException("No diff action specified");

		return new Diff(action, target, parseScenario(obj.get("insert")));
	}

	public static final class Client{
		private Client(){}

		public static Scenario readFromResource(ResourceLocation resource){
			try{
				IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

				ScenarioData scenarioData = read(
						resourceManager.getResource(
								new ResourceLocation(
										resource.getNamespace(),
										"dialog/"+resource.getPath()+".json")),
						ScenarioData.class);

				List<DiffFile> diffFiles = new ArrayList<>();
				String locale = Minecraft.getInstance().gameSettings.language;
				ResourceLocation diffFileLocation = new ResourceLocation(
						resource.getNamespace(),
						"dialog/"+locale+"/"+resource.getPath()+".diff.json");
				if(resourceManager.hasResource(diffFileLocation)){
					for(IResource r : resourceManager.getAllResources(diffFileLocation)){
						diffFiles.add(read(r, DiffFile.class));
					}
				}

				Scenario scenario = new ScenarioParser(scenarioData, diffFiles.isEmpty() ? null : DiffFile.combine(diffFiles)).parse();

				if(ModCfg.debugDialogLoading()){
					if(diffFiles.isEmpty()){
						ParagliderMod.LOGGER.debug("Read scenario from {} :: {} scripts, no patches (locale: \"{}\")", resource, scenario.size(), locale);
					}else{
						ParagliderMod.LOGGER.debug("Read scenario from {} :: {} scripts, {} patches applied (locale: \"{}\")", resource, scenario.size(), diffFiles.size(), locale);
					}
					for(int i = 0; i<scenario.size(); i++){
						Script script = scenario.getScript(i);
						ParagliderMod.LOGGER.debug(String.format(" %3d   %s", i, script));
						if(script instanceof Choice){
							List<Choice.Case> cases = ((Choice)script).getCases();
							for(int j = 0; j<cases.size(); j++){
								Choice.Case c = cases.get(j);
								ParagliderMod.LOGGER.debug("       "+(j+1)+": "+Text.textComponentToString(c.getText()));
								ParagliderMod.LOGGER.debug("          => "+c.getThen());
							}
						}
					}
				}
				return scenario;
			}catch(IOException e){
				throw new RuntimeException(e);
			}
		}

		private static <T> T read(IResource resource, Class<T> clazz){
			return GSON.fromJson(
					new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8),
					clazz);
		}
	}
}
