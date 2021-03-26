package tictim.paraglider.dialog.parser;

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
import tictim.paraglider.dialog.data.ScriptDataVisitor;
import tictim.paraglider.dialog.data.TextData;
import tictim.paraglider.dialog.script.Choice;
import tictim.paraglider.dialog.script.ChoiceWithLabel;
import tictim.paraglider.dialog.script.End;
import tictim.paraglider.dialog.script.Error;
import tictim.paraglider.dialog.script.GoTo;
import tictim.paraglider.dialog.script.GoToWithLabel;
import tictim.paraglider.dialog.script.IfAction;
import tictim.paraglider.dialog.script.IfActionWithLabel;
import tictim.paraglider.dialog.script.Script;
import tictim.paraglider.dialog.script.ScriptVisitor;
import tictim.paraglider.dialog.script.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ScenarioParser implements ScriptDataVisitor{
	private final ScenarioData scenarioData;
	@Nullable private final DiffFile diffFile;

	private final List<Script> scripts = new ArrayList<>();
	private final Map<String, NamedLabel> namedLabels = new HashMap<>();

	public ScenarioParser(ScenarioData scenarioData, @Nullable DiffFile diffFile){
		this.scenarioData = scenarioData;
		this.diffFile = diffFile;
	}

	public Scenario parse(){
		if(!scripts.isEmpty()) throw new IllegalStateException("Woah hol' up, did you read just parsed same ScenarioData twice?");

		consume(scenarioData);

		scripts.add(End.INSTANCE);

		StringBuilder missingLabelError = null;
		for(NamedLabel label : namedLabels.values()){
			if(!label.hasIndex()){
				if(missingLabelError==null)
					missingLabelError = new StringBuilder("One or more referenced labels are not defined:");
				missingLabelError.append("\n  ").append(label);
			}
		}

		if(missingLabelError!=null){
			throw new IllegalStateException(missingLabelError.toString());
		}

		// Get rid of all labels
		List<Script> scripts2 = new ArrayList<>();
		for(Script script : scripts){
			script.visit(new ScriptVisitor(){
				@Override public void visitText(Text text){
					scripts2.add(text);
				}
				@Override public void visitChoice(Choice choice){
					scripts2.add(choice);
				}
				@Override public void visitGoTo(GoTo goTo){
					scripts2.add(goTo);
				}
				@Override public void visitIfAction(IfAction ifAction){
					scripts2.add(ifAction);
				}
				@Override public void visitEnd(){
					scripts2.add(End.INSTANCE);
				}
				@Override public void visitError(Error error){
					scripts2.add(error);
				}
			});
		}

		return new Scenario(scripts2.toArray(new Script[0]));
	}

	private void consume(ScriptData scriptData){
		if(scriptData.label!=null){
			if(diffFile!=null){ // TODO Test diffs...properly?
				for(Diff d : diffFile.diff){
					if(!Objects.equals(d.label, scriptData.label)) continue;
					if(d.action==Diff.Action.BEFORE){
						consume(d.insert);
					}
				}

				boolean replaced = false;

				for(Diff d : diffFile.diff){
					if(!Objects.equals(d.label, scriptData.label)) continue;
					if(d.action==Diff.Action.REPLACE){
						if(replaced) throw new IllegalStateException("Two REPLACEs targeting same script, drop one of it");
						replaced = true;
						consume(d.insert);
					}
				}

				if(replaced){
					// Just apply all AFTERs and go
					for(Diff d : diffFile.diff){
						if(!Objects.equals(d.label, scriptData.label)) continue;
						if(d.action==Diff.Action.AFTER){
							consume(d.insert);
						}
					}

					return;
				}
			}

			getLabel(scriptData.label).setIndex(scripts.size());
		}

		scriptData.accept(this);

		if(scriptData.label!=null){
			if(diffFile!=null){
				for(Diff d : diffFile.diff){
					if(!Objects.equals(d.label, scriptData.label)) continue;
					if(d.action==Diff.Action.AFTER){
						consume(d.insert);
					}
				}
			}
		}
	}

	private NamedLabel getLabel(String name){
		NamedLabel label = namedLabels.get(name);
		if(label==null){
			label = new NamedLabel(name);
			namedLabels.put(name, label);
		}
		return label;
	}

	@Override public void visitText(TextData textData){
		if(textData.choices!=null){
			ChoiceWithLabel s = new ChoiceWithLabel(textData.convertDialog());
			SimpleLabel start = new SimpleLabel(scripts.size());
			SimpleLabel end = new SimpleLabel(-1);

			scripts.add(s);

			List<ChoiceData.Case> cases = textData.choices.cases;
			for(int i = 0; i<cases.size(); i++){
				ChoiceData.Case c = cases.get(i);
				ChoiceData.ScriptEnding scriptEnding = c.parseThen();
				Label label = new SimpleLabel(scripts.size());
				consume(c.scenario);
				switch(scriptEnding){
					case ERROR:
						scripts.add(new Error("Unterminated choice"));
						break;
					case END:
						scripts.add(End.INSTANCE);
						break;
					case LOOP:
						scripts.add(new GoToWithLabel(start));
						break;
					case CONTINUE:
						if(i!=cases.size()-1) scripts.add(new GoToWithLabel(end));
						break;
					default:
						throw new IllegalStateException("Unreachable");
				}
				s.addCase(c.convertText(), label);
			}
			end.setIndex(scripts.size());
		}else{
			scripts.add(new Text(textData.convertDialog()));
		}
	}
	@Override public void visitIfAction(IfActionData ifActionData){
		IfActionWithLabel a = new IfActionWithLabel(ifActionData.doAction);
		scripts.add(a);

		if(ifActionData.elseScenario!=null){
			SimpleLabel end = new SimpleLabel(-1);
			if(ifActionData.ifScenario!=null){
				consume(ifActionData.ifScenario);
			}
			scripts.add(new GoToWithLabel(end));
			a.setElseThen(new SimpleLabel(scripts.size()));
			consume(ifActionData.elseScenario);
			end.setIndex(scripts.size());
		}else if(ifActionData.ifScenario!=null){
			consume(ifActionData.ifScenario);
			a.setElseThen(new SimpleLabel(scripts.size()));
		}
	}
	@Override public void visitEnd(EndData endData){
		scripts.add(End.INSTANCE);
	}
	@Override public void visitError(ErrorData errorData){
		scripts.add(new Error(errorData.error));
	}
	@Override public void visitGoTo(GoToData goToData){
		scripts.add(new GoToWithLabel(getLabel(goToData.go)));
	}
	@Override public void visitScenario(ScenarioData scenarioData){
		for(ScriptData scriptData : scenarioData.scenario) consume(scriptData);
	}
}
