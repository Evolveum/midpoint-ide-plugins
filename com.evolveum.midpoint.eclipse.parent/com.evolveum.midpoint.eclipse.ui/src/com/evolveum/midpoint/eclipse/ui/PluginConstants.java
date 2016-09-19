package com.evolveum.midpoint.eclipse.ui;

public class PluginConstants {
	
	public static final String CMD_TEST_CONNECTION = "com.evolveum.midpoint.eclipse.ui.command.testConnection";
	public static final String CMD_TEST_CONNECTIONS = "com.evolveum.midpoint.eclipse.ui.command.testConnections";

	public static final String CMD_SHOW_CONSOLE = "com.evolveum.midpoint.eclipse.ui.command.showConsole";
	public static final String CMD_EDIT_PREFERENCES = "com.evolveum.midpoint.eclipse.ui.command.editPreferences";
	public static final String CMD_EDIT_ACTIONS_PREFERENCES = "com.evolveum.midpoint.eclipse.ui.command.editActionsPreferences";
	public static final String PARAM_WITH_ACTION = "com.evolveum.midpoint.eclipse.ui.commandParameter.withAction";
	public static final String PARAM_RAW = "com.evolveum.midpoint.eclipse.ui.commandParameter.raw";
	public static final String PARAM_ALSO_LOCALLY = "com.evolveum.midpoint.eclipse.ui.commandParameter.alsoLocally";
	public static final String PARAM_UPLOAD_FIRST = "com.evolveum.midpoint.eclipse.ui.commandParameter.uploadFirst";
	public static final String PARAM_ACTION_NUMBER = "com.evolveum.midpoint.eclipse.ui.commandParameter.actionNumber";
	public static final String PARAM_SERVER_NUMBER = "com.evolveum.midpoint.eclipse.ui.commandParameter.serverNumber";
	public static final String PARAM_STOP_ON_ERROR = "com.evolveum.midpoint.eclipse.ui.commandParameter.stopOnError";
	
	public static final String CMD_NOOP = "com.evolveum.midpoint.eclipse.ui.command.noop";
	public static final String CMD_UPLOAD_OR_EXECUTE = "com.evolveum.midpoint.eclipse.ui.command.uploadOrExecute";
	public static final String CMD_EXECUTE_ACTION = "com.evolveum.midpoint.eclipse.ui.command.executeAction";
	public static final String CMD_COMPUTE_DIFFERENCE = "com.evolveum.midpoint.eclipse.ui.command.computeDifferences";
	public static final String CMD_RELOAD_FROM_SERVER = "com.evolveum.midpoint.eclipse.ui.command.reloadFromServer";
	public static final String CMD_SET_AS_ACTION = "com.evolveum.midpoint.eclipse.ui.command.setAsAction";
	public static final String CMD_BROWSE = "com.evolveum.midpoint.eclipse.ui.command.browse";

	public static final String CMD_SET_LOG_LEVEL = "com.evolveum.midpoint.eclipse.ui.command.setLogLevel";
	public static final String PARAM_COMPONENT = "com.evolveum.midpoint.eclipse.ui.commandParameter.component";
	public static final String PARAM_LEVEL = "com.evolveum.midpoint.eclipse.ui.commandParameter.level";
	
	public static final String VALUE_MODEL = "model";
	public static final String VALUE_PROVISIONING = "provisioning";
	public static final String VALUE_REPOSITORY = "repository";
	public static final String VALUE_GUI = "gui";
	public static final String VALUE_ALL = "all";
	
	public static final String VALUE_INFO = "info";
	public static final String VALUE_WARN = "warn";
	public static final String VALUE_DEBUG = "debug";
	public static final String VALUE_TRACE = "trace";
	
	// model-specific ones
	public static final String VALUE_CLOCKWORK_SUMMARY = "clockworkSummary";
	public static final String VALUE_PROJECTOR_SUMMARY = "projectorSummary";
	public static final String VALUE_MAPPING_TRACE = "mappingTrace";
	public static final String VALUE_EXPRESSION_TRACE = "expressionTrace";
	public static final String VALUE_PROJECTOR_TRACE = "projectorTrace";
	public static final String VALUE_LENS_TRACE = "lensTrace";

	public static final String CMD_MARK_CURRENT_LOG_POSITION = "com.evolveum.midpoint.eclipse.ui.command.markCurrentLogPosition";
	public static final String CMD_SHOW_LOG_IN_CONSOLE = "com.evolveum.midpoint.eclipse.ui.command.showLogInConsole";
	public static final String CMD_SHOW_LOG_IN_EDITOR = "com.evolveum.midpoint.eclipse.ui.command.showLogInEditor";
	public static final String PARAM_FROM = "com.evolveum.midpoint.eclipse.ui.commandParameter.from";
	public static final String VALUE_START = "start";
	public static final String VALUE_BACK_N = "backN";
	public static final String VALUE_MARK = "mark";
	public static final String VALUE_NOW = "now";
	public static final String VALUE_CONSOLE = "console";
	public static final String CMD_CLEAR_SERVER_LOG = "com.evolveum.midpoint.eclipse.ui.command.clearServerLog";
	
	public static final String CMD_SELECT_SERVER = "com.evolveum.midpoint.eclipse.ui.command.selectServer";
	public static final String CMD_GENERATE_OID = "com.evolveum.midpoint.eclipse.ui.command.generateOid";

//	public static final String CMD_ENABLE_ON_SERVER = "com.evolveum.midpoint.eclipse.ui.command.enableOnServer";
//	public static final String CMD_DISABLE_ON_SERVER = "com.evolveum.midpoint.eclipse.ui.command.disableOnServer";
	public static final String CMD_RECOMPUTE_ON_SERVER = "com.evolveum.midpoint.eclipse.ui.command.recomputeOnServer";
	public static final String CMD_DELETE_ON_SERVER = "com.evolveum.midpoint.eclipse.ui.command.deleteOnServer";
	public static final String CMD_TEST_RESOURCE = "com.evolveum.midpoint.eclipse.ui.command.testResourceOnServer";

	public static final String SERVER_LOG = "Server log";
	public static final String DATA_OUTPUT = "Data output";
	public static final String CONSOLE_OUTPUT = "Console output";
	public static final String OP_RESULT = "Operation result";
}
