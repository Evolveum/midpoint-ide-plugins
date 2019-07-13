package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public enum OpType {
	
	CLOCKWORK_RUN ("Clockwork run", "com.evolveum.midpoint.model.impl.lens.Clockwork.run"), 
	CLOCKWORK_CLICK ("Clockwork click", "com.evolveum.midpoint.model.impl.lens.Clockwork.click"), 
	PROJECTOR_PROJECT ("Projector project", "com.evolveum.midpoint.model.impl.lens.projector.Projector.project"), 
	PROJECTOR_COMPONENT ("Projector component",
			"com.evolveum.midpoint.model.impl.lens.projector.Projector.*"),
	CLOCKWORK_METHOD ("Clockwork method",
			"com.evolveum.midpoint.model.impl.lens.Clockwork.*"),
	MAPPING_EVALUATION ("Mapping evaluation",
			"com.evolveum.midpoint.model.common.mapping.MappingImpl.evaluate"), 
	MAPPING_PREPARATION ("Mapping preparation",
			"com.evolveum.midpoint.model.common.mapping.MappingImpl.prepare"), 
	MAPPING_EVALUATION_PREPARED ("Prepared mapping evaluation",
			"com.evolveum.midpoint.model.common.mapping.MappingImpl.evaluatePrepared"), 
	SCRIPT_EXECUTION ("Script evaluation",
			"xxxx"), 
	CHANGE_EXECUTION ("Change execution",
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.execute"), 
	CHANGE_EXECUTION_SUB ("Change execution - focus/projection",
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.execute.focus.*",
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.execute.projection.*"), 
	CHANGE_EXECUTION_DELTA ("Change execution - delta",
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.executeDelta"),
	CHANGE_EXECUTION_OTHER ("Change execution - other",
			"com.evolveum.midpoint.model.impl.lens.ChangeExecutor.*"),
	FOCUS_LOAD ("Focus load",
			result -> isLoadedFromRepository(result),
			"com.evolveum.midpoint.model.impl.lens.projector.ContextLoader.determineFocusContext"),
	FOCUS_LOAD_CHECK ("Focus load check",
			"com.evolveum.midpoint.model.impl.lens.projector.ContextLoader.determineFocusContext"),
	SHADOW_LOAD ("Shadow load",
			"com.evolveum.midpoint.model.impl.lens.projector.ContextLoader.loadProjection"),
	MODEL_OTHER ("Model - other",
			"com.evolveum.midpoint.model.*"),
	PROVISIONING_API ("Provisioning (API)", "com.evolveum.midpoint.provisioning.api.*"),
	PROVISIONING_INTERNAL ("Provisioning (internal)", "com.evolveum.midpoint.provisioning.impl.*"),
	REPOSITORY ("Repository", "com.evolveum.midpoint.repo.api.RepositoryService.*"),
	REPOSITORY_CACHE ("Repository cache", "com.evolveum.midpoint.repo.cache.RepositoryCache.*"), 
	OTHER ("Other", 
			"*");

	private static final String LOADED_FROM_REPOSITORY = "Loaded from repository";			// TODO
	
	private final String label;
	private final Function<OperationResultType, Boolean> predicate;
	@SuppressWarnings("unused")
	private final List<String> patterns;
	private final List<Pattern> compiledPatterns;

	OpType(String label, Function<OperationResultType, Boolean> predicate, String... patterns) {
		this.label = label;
		this.predicate = predicate;
		this.patterns = Arrays.asList(patterns);
		this.compiledPatterns = new ArrayList<>();
		for (String pattern : patterns) {
			String regex = toRegex(pattern);
			System.out.println(pattern + " -> " + regex);
			compiledPatterns.add(Pattern.compile(regex));
		}
	}
	
	private static boolean isLoadedFromRepository(OperationResultType result) {
		return LOADED_FROM_REPOSITORY.equals(OpNode.getResultComment(result));
	}

	OpType(String label, String... patterns) {
		this(label, null, patterns);
	}
	
	public String getFormattedName(OpNode node) {
		OperationResultType opResult = node.getResult();
		String operation = opResult.getOperation();
		String last = getLast(operation);
		String qualifiers = String.join("; ", opResult.getQualifier());
		String commaQualifiers = qualifiers.isEmpty() ? "" : " - " + qualifiers;
		switch (this) {
		case CLOCKWORK_RUN: return "Clockwork run";
		case CLOCKWORK_CLICK: return "Clockwork click";
		case PROJECTOR_PROJECT: return "Projector";
		case PROJECTOR_COMPONENT: return "Projector " + last;
		case CLOCKWORK_METHOD: return "Clockwork " + last;
		
		case MAPPING_EVALUATION: return "Mapping evaluation - " + getContext(opResult, "context");
		case MAPPING_PREPARATION: return "Mapping preparation";
		case MAPPING_EVALUATION_PREPARED: return "Prepared mapping evaluation";

		// TODO script
		case CHANGE_EXECUTION: return "Change execution";
		case CHANGE_EXECUTION_SUB: 
			String s = "Change execution for ";
			if (operation.contains(".focus.")) {
				s += "focus";
			} else {
				s += "projection";
			}
			s += " (" + last + ")";
			return s;
		case CHANGE_EXECUTION_DELTA: return "Delta execution";
		case CHANGE_EXECUTION_OTHER: return "Change execution - " + last;
		case REPOSITORY: return "Repository " + last + commaQualifiers;
		case REPOSITORY_CACHE: return "Cache " + last + commaQualifiers;
		case PROVISIONING_API: return "Provisioning " + last + commaQualifiers;
		case PROVISIONING_INTERNAL: return getLastTwo(operation) + commaQualifiers;
		case FOCUS_LOAD: return "Focus load";
		case FOCUS_LOAD_CHECK: return "Focus load check (" + node.getResultComment() + ")";
		case SHADOW_LOAD: return "Shadow load";
		case MODEL_OTHER:
		case OTHER:
			return getLastTwo(operation) + commaQualifiers;
		}
		return opResult.getOperation() + (qualifiers.isEmpty() ? "" : " (" + qualifiers + ")");
	}
	
	private String getLastTwo(String operation) {
		int i = StringUtils.lastOrdinalIndexOf(operation, ".", 2);
		if (i < 0) {
			return operation;
		} else {
			return operation.substring(i+1);
		}
	}

	private String getContext(OperationResultType opResult, String name) {
		if (opResult.getContext() != null) {
			for (EntryType e : opResult.getContext().getEntry()) {
				if (name.equals(e.getKey())) {
					return TraceDetailsView.dump(e.getEntryValue());
				}
			}
		}
		return "";
	}

	private String getLast(String operation) {
		return StringUtils.substringAfterLast(operation, ".");
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
				if (predicate == null || predicate.apply(operation)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getLabel() {
		return label;
	}
}
