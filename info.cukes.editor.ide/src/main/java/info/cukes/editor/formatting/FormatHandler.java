package info.cukes.editor.formatting;

import info.cukes.editor.FeatureEditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class FormatHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        FeatureEditor editor = (FeatureEditor) HandlerUtil.getActiveEditor(event);
        editor.format();

        return null;
    }
}
