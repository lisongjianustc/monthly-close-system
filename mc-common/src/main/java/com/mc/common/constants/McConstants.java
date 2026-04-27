package com.mc.common.constants;

/**
 * 业务常量
 */
public class McConstants {

    private McConstants() {}

    // ========== 缓存 Key ==========

    /**
     * 用户Token Key
     */
    public static final String TOKEN_KEY_PREFIX = "mc:token:";

    /**
     * 用户信息缓存 Key
     */
    public static final String USER_INFO_KEY_PREFIX = "mc:user:";

    /**
     * 组织树缓存 Key
     */
    public static final String ORG_TREE_KEY = "mc:org:tree";

    /**
     * 规则执行缓存 Key
     */
    public static final String RULE_CACHE_KEY_PREFIX = "mc:rule:";

    // ========== 过期时间 ==========

    /**
     * Token 过期时间（天）
     */
    public static final int TOKEN_EXPIRE_DAYS = 7;

    /**
     * 缓存过期时间（分钟）
     */
    public static final int CACHE_EXPIRE_MINUTES = 30;

    // ========== 文件限制 ==========

    /**
     * 最大上传文件大小（MB）
     */
    public static final int MAX_FILE_SIZE_MB = 50;

    /**
     * 支持的文件类型
     */
    public static final String[] SUPPORT_FILE_TYPES = {".xlsx", ".xls", ".csv"};

    // ========== 分页 ==========

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    // =========.jwt ==========

    /**
     * JWT 签名密钥
     */
    public static final String JWT_SECRET = "mc-monthly-close-system-secret-key-2024";

    /**
     * JWT 签发者
     */
    public static final String JWT_ISSUER = "mc-monthly-close-system";

    // ========== 任务 SLA ==========

    /**
     * 默认 SLA 小时数
     */
    public static final int DEFAULT_SLA_HOURS = 48;

    // ========== 数据域 ==========

    /**
     * 数据域：仅本单位
     */
    public static final String DATA_SCOPE_SELF = "SELF";

    /**
     * 数据域：辖内单位树
     */
    public static final String DATA_SCOPE_SUBTREE = "SUBTREE";

    /**
     * 数据域：全局
     */
    public static final String DATA_SCOPE_ALL = "ALL";

    // =========.操作域 ==========

    /**
     * 操作域：查看
     */
    public static final String OPERATION_VIEW = "view";

    /**
     * 操作域：执行
     */
    public static final String OPERATION_EXECUTE = "execute";

    /**
     * 操作域：复核
     */
    public static final String OPERATION_REVIEW = "review";

    /**
     * 操作域：审核
     */
    public static final String OPERATION_APPROVE = "approve";

    /**
     * 操作域：配置
     */
    public static final String OPERATION_CONFIGURE = "configure";
}
