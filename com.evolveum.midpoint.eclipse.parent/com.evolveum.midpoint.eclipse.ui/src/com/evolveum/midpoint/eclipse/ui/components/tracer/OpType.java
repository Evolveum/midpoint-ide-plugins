package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public enum OpType {
	
	REPOSITORY ("com.evolveum.midpoint.repo.api.RepositoryService.*"),
	REPOSITORY_CACHE ("xxxx"), 
	CLOCKWORK_RUN ("com.evolveum.midpoint.model.impl.lens.Clockwork.run"), 
	CLOCKWORK_CLICK ("com.evolveum.midpoint.model.impl.lens.Clockwork.*.click"), 
	PROJECTOR_PROJECT ("com.evolveum.midpoint.model.impl.lens.projector.Projector.project"), 
	PROJECTOR_COMPONENT (
			"com.evolveum.midpoint.model.impl.lens.projector.Projector.INITIAL.*",
			"com.evolveum.midpoint.model.impl.lens.projector.Projector.PRIMARY.*",
			"com.evolveum.midpoint.model.impl.lens.projector.Projector.SECONDARY.*"), 
	MAPPING_PREPARATION ("com.evolveum.midpoint.model.common.mapping.MappingImpl.prepare"), 
	MAPPING_EVALUATION ("com.evolveum.midpoint.model.common.mapping.MappingImpl.evaluate"), 
	SCRIPT_EXECUTION ("xxxx"), 
	CHANGE_EXECUTION ("com.evolveum.midpoint.model.impl.lens.ChangeExecutor.execute"), 
	CHANGE_EXECUTION_SUB (
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.execute.focus.*",
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.execute.projection.*"), 
	CHANGE_EXECUTION_SUB2 ("com.evolveum.midpoint.model.impl.lens.ChangeExecutor.executeDelta.*");

	private final List<String> patterns;
	private final List<Pattern> compiledPatterns;
	
	OpType(String... patterns) {
		this.patterns = Arrays.asList(patterns);
		this.compiledPatterns = new ArrayList<>();
		for (String pattern : patterns) {
			String regex = toRegex(pattern);
			System.out.println(pattern + " -> " + regex);
			compiledPatterns.add(Pattern.compile(regex));
		}
	}

	private String toRegex(String pattern) {
		return pattern.replace(".", "\\.").replace("*", ".*");
	}

	public static OpType determine(OperationResultType operation) {
		for (OpType type : OpType.values()) {
			if (type.matches(operation)) {
				return type;
			}
		}
		return null;
	}

	private boolean matches(OperationResultType operation) {
		for (Pattern pattern : compiledPatterns) {
			if (pattern.matcher(operation.getOperation()).matches()) {
				return true;
			}
		}
		return false;
	}
}
