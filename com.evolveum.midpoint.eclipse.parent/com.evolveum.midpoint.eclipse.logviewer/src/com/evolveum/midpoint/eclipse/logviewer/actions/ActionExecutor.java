package com.evolveum.midpoint.eclipse.logviewer.actions;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

import com.evolveum.midpoint.eclipse.logviewer.config.ConfigurationParser;
import com.evolveum.midpoint.eclipse.logviewer.config.EditorConfiguration;
import com.evolveum.midpoint.eclipse.logviewer.config.FoldingInstruction;

public class ActionExecutor {

	public static void executeActions(ISourceViewer sourceViewer) {

		IDocument document = sourceViewer.getDocument();
		EditorConfiguration configuration = ConfigurationParser.getConfiguration(document);
		
		LineKiller.applyKillInstructions(document, configuration);
		
		if (sourceViewer instanceof ProjectionViewer) {		// always should be
			ProjectionViewer projectionViewer = (ProjectionViewer) sourceViewer;
			ProjectionAnnotationModel pam = projectionViewer.getProjectionAnnotationModel();
			
			document = sourceViewer.getDocument();
			List<FoldingInstruction> instructions = configuration.getInstructions(FoldingInstruction.class);
			FoldingExecutor.applyFoldingInstructions(instructions, document, pam);
		} else {
			System.out.println("Unknown sourceViewer: " + sourceViewer);
		}
	}


}
