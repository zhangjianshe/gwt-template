package cn.mapway.gwt_template.shared;

/**
 * 系统常量
 */
public class AppConstant {
    public static final String DEFAULT_SERVER_PATH = "/app";
    public static final String API_TOKEN = "app-token";
    public static final String AUTH_COOKIE_NAME = "auth-token";
    public static final String KEY_LOGIN_USER = "login_user";
    public static final String KEY_DB_VERSION = "db_version";
    public static final String KEY_CLOUDFLARE_TOKEN = "cloudflare_token";
    public static final String SYS_CODE = "SYS_SOFTWARE";
    public static final String DEFAULT_UPLOAD_LOCATION = "/api/v1/project/upload";
    public static final String TOPIC_GIT_IMPORT = "topic_git_import";
    public static final String URL_WEBSOCKET = "/ws/git/USER_ID";
    public static final String MESSAGE_PHASE_IMPORT = "IMPORT";
    public static final String MESSAGE_TYPE_START = "START";
    public static final String MESSAGE_TYPE_PROGRESS = "PROGRESS";
    public static final String MESSAGE_TYPE_END = "END";
    public static final String MESSAGE_TYPE_SUCCESS = "SUCCESS";
    public static final String MESSAGE_TYPE_ERROR = "ERROR";
    public static final String TOPIC_MAILBOX_MESSAGE = "topic_mailbox_message";
    public static final String USER_BROADCAST_NAME = "公共信箱";
    public static final String ROLE_SYS_PROJECT_MANAGER = "ROLE_SYS_PROJECT_MANAGER"; //项目管理角色
    public static final String ROLE_SYS_MESSAGE_MANAGER = "ROLE_SYS_MESSAGE_MANAGER"; //系统消息管理角色
    public static final String ROLE_SYS_LDAP_MANAGER = "ROLE_SYS_LDAP_MANAGER"; //系统LDAP管理角色
    public static final String USER_IS_PUBLIC_ACCOUNT = "USER_IS_PUBLIC_ACCOUNT"; //用户是一个公共账户
    public static final String TEMP_WORKSPACE_FOLDER_ID = "TEMP_FOLDER";
    public static final String UPLOAD_PREFIX_PROJECT_RESOURCE = "project_resource:";
    public static final String UPLOAD_PREFIX_TASK_ATTACHMENT = "task_attachment:";
    public static final String UPLOAD_PREFIX_ISSUE_ATTACHMENT = "issue_attachment:";
    public static final String CANGLING_MIME_TYPE = "application/x-cangling-preview";
    public static final String CANGLING_MIME_FRAME = "application/x-cangling-frame";
    public static final Integer ERROR_CODE_UNAUTHORITY = 50300;
}
