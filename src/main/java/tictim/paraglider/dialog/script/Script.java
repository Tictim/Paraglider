package tictim.paraglider.dialog.script;

/**
 * Abstraction of simple dialogs, dialogs with choices, and server syncable executable codes.
 * Some control statements are also included for extra funniness. Yay for bloated and overcomplicated design choice!
 */
public interface Script{
	void visit(ScriptVisitor visitor);
}
