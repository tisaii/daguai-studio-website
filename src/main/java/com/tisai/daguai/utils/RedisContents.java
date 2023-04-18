package com.tisai.daguai.utils;

public class RedisContents {

    public static final String LUA_UPDATE = "update";
    public static final String LUA_DELETE = "delete";
    public static final String LUA_INSERT = "insert";

    public static final String BLOG_ID_BUSINESS = "blog:";

    public static final String LOGIN_ADMIN_TOKEN_KEY = "token:admin:";
    public static final String VALIDATE_ADMIN_RESET = "validate:admin:reset:";
    public static final Long CACHE_AVALANCHE_RANDOM_LIMIT = 5L;
    public static final Long CACHE_BLANK_TTL = 120L;
    public static final String CACHE_BLOG_PREFIX = "cache:blog:";
    public static final String CACHE_BLOG_BASIC_INFO_SUFFIX = ":info:";
    public static final Long CACHE_BLOG_BASIC_INFO_TTL = 30L;
    public static final Long CACHE_BLOG_WITH_PRODUCT_TTL = 30L;
    public static final Long CACHE_BLOG_TTL = 30L;
    public static final String CACHE_BLOG_WITH_PRODUCT_SUFFIX = ":product:";
    public static final String CACHE_CATEGORY_PREFIX = "cache:category:";
    public static final String CACHE_CATEGORY_INFO_SUFFIX = ":info:";
    public static final Long CACHE_CATEGORY_TTL = 30L;
    public static final String CACHE_PRODUCT_PREFIX = "cache:product:";
    public static final String CACHE_PRODUCT_INFO_SUFFIX = ":info:";
    public static final Long CACHE_PRODUCT_TTL = 30L;
    public static final String CACHE_FEEDBACK_PREFIX = "cache:feedback:";
    public static final String CACHE_FEEDBACK_INFO_SUFFIX = ":info:";
    public static final Long CACHE_FEEDBACK_TTL = 30L;


    public static final Long CACHE_PAGE_DEFAULT_TTL = 30L;
    public static final String BLANK_STRING = "";
    public static final String CACHE_PAGE_BLOGS_PREFIX = "cache:page:blogs:";
    public static final String CACHE_PAGE_CATEGORIES_PREFIX = "cache:page:categories:";
    public static final String CACHE_PAGE_PRODUCTS_PREFIX = "cache:page:products:";
    public static final String CACHE_PAGE_ZSET_FEEDBACKS_PREFIX = "cache:page:zset:feedbacks:";
    public static final String CT_SUFFIX = "ct:";
    public static final String UT_SUFFIX = "ut:";
    public static final String CACHE_ALL_BLOG_IDS = "ids:";
    public static final String CACHE_ALL_CATEGOTY_IDS = "ids:";
    public static final String CACHE_ALL_PRODUCT_IDS = "ids:";
    public static final String CACHE_ALL_FEEDBACK_IDS = "ids:";
}
