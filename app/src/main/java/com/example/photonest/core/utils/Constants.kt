package com.example.photonest.core.utils

object Constants {

    // Firebase Collection Names
    const val USERS_COLLECTION = "users"
    const val POSTS_COLLECTION = "posts"
    const val CATEGORIES_COLLECTION = "categories"
    const val COMMENTS_COLLECTION = "comments"
    const val NOTIFICATIONS_COLLECTION = "notifications"
    const val FOLLOWS_COLLECTION = "follows"
    const val LIKES_COLLECTION = "likes"
    const val BOOKMARKS_COLLECTION = "bookmarks"
    const val REPORTS_COLLECTION = "reports"
    const val STORIES_COLLECTION = "stories"
    const val CHATS_COLLECTION = "chats"
    const val MESSAGES_COLLECTION = "messages"

    // Firebase Storage Paths
    const val PROFILE_IMAGES_PATH = "profile_images"
    const val POST_IMAGES_PATH = "post_images"
    const val STORY_IMAGES_PATH = "story_images"
    const val CHAT_IMAGES_PATH = "chat_images"
    const val TEMP_IMAGES_PATH = "temp_images"

    // Database
    const val DATABASE_NAME = "photonest_database"
    const val DATABASE_VERSION = 1

    // DataStore
    const val DATASTORE_NAME = "photonest_preferences"

    // Image Constraints
    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB in bytes
    const val MAX_IMAGE_WIDTH = 1920
    const val MAX_IMAGE_HEIGHT = 1920
    const val PROFILE_IMAGE_SIZE = 300
    const val POST_IMAGE_MAX_SIZE = 1080
    const val THUMBNAIL_SIZE = 150
    const val STORY_IMAGE_SIZE = 720

    // Pagination
    const val PAGE_SIZE = 20
    const val POSTS_PAGE_SIZE = 15
    const val USERS_PAGE_SIZE = 20
    const val COMMENTS_PAGE_SIZE = 30
    const val NOTIFICATIONS_PAGE_SIZE = 25

    // Validation
    const val MIN_USERNAME_LENGTH = 3
    const val MAX_USERNAME_LENGTH = 30
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PASSWORD_LENGTH = 128
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 50
    const val MAX_BIO_LENGTH = 150
    const val MAX_CAPTION_LENGTH = 2200
    const val MAX_COMMENT_LENGTH = 500
    const val MAX_LOCATION_LENGTH = 100
    const val MAX_WEBSITE_LENGTH = 100

    // Cache
    const val IMAGE_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    const val DISK_CACHE_SIZE = 100 * 1024 * 1024 // 100MB
    const val MEMORY_CACHE_PERCENTAGE = 0.25 // 25% of available memory

    // Network
    const val NETWORK_TIMEOUT = 30000L // 30 seconds
    const val CONNECT_TIMEOUT = 15000L // 15 seconds
    const val READ_TIMEOUT = 30000L // 30 seconds
    const val WRITE_TIMEOUT = 30000L // 30 seconds

    // Search
    const val SEARCH_DEBOUNCE_DELAY = 500L // 500ms
    const val MIN_SEARCH_QUERY_LENGTH = 2
    const val MAX_SEARCH_RESULTS = 50

    // Notification Types
    object NotificationTypes {
        const val LIKE = "like"
        const val COMMENT = "comment"
        const val FOLLOW = "follow"
        const val FOLLOW_REQUEST = "follow_request"
        const val MENTION = "mention"
        const val POST_UPLOAD = "post_upload"
        const val STORY_UPLOAD = "story_upload"
        const val MESSAGE = "message"
        const val SYSTEM = "system"
    }

    // SharedPreferences Keys
    object PreferenceKeys {
        const val IS_LOGGED_IN = "is_logged_in"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
        const val USER_USERNAME = "user_username"
        const val USER_PROFILE_PICTURE = "user_profile_picture"

        // App Settings
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE = "language"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        const val EMAIL_NOTIFICATIONS_ENABLED = "email_notifications_enabled"

        // Privacy Settings
        const val ACCOUNT_PRIVATE = "account_private"
        const val SHOW_ACTIVITY_STATUS = "show_activity_status"
        const val ALLOW_STORY_SHARING = "allow_story_sharing"
        const val ALLOW_POST_SHARING = "allow_post_sharing"

        // Cache Settings
        const val AUTO_DOWNLOAD_IMAGES = "auto_download_images"
        const val AUTO_PLAY_VIDEOS = "auto_play_videos"
        const val DATA_SAVER_MODE = "data_saver_mode"

        // Onboarding
        const val IS_FIRST_TIME_USER = "is_first_time_user"
        const val HAS_SEEN_ONBOARDING = "has_seen_onboarding"
        const val ONBOARDING_VERSION = "onboarding_version"

        // Security
        const val BIOMETRIC_ENABLED = "biometric_enabled"
        const val AUTO_LOCK_ENABLED = "auto_lock_enabled"
        const val AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        const val LAST_PASSWORD_CHANGE = "last_password_change"
    }

    // Theme Options
    object ThemeMode {
        const val SYSTEM = "system"
        const val LIGHT = "light"
        const val DARK = "dark"
    }

    // Language Options
    object Languages {
        const val ENGLISH = "en"
        const val SPANISH = "es"
        const val FRENCH = "fr"
        const val GERMAN = "de"
        const val ITALIAN = "it"
        const val PORTUGUESE = "pt"
        const val RUSSIAN = "ru"
        const val JAPANESE = "ja"
        const val KOREAN = "ko"
        const val CHINESE_SIMPLIFIED = "zh-CN"
        const val CHINESE_TRADITIONAL = "zh-TW"
        const val ARABIC = "ar"
        const val HINDI = "hi"
    }

    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "Network error. Please check your connection."
        const val UNKNOWN_ERROR = "An unknown error occurred. Please try again."
        const val SERVER_ERROR = "Server error. Please try again later."
        const val AUTH_ERROR = "Authentication error. Please sign in again."
        const val PERMISSION_DENIED = "Permission denied. Please check your permissions."
        const val FILE_NOT_FOUND = "File not found."
        const val INVALID_INPUT = "Invalid input. Please check your data."
        const val TIMEOUT_ERROR = "Request timeout. Please try again."
        const val NO_INTERNET = "No internet connection. Please check your network."
        const val WEAK_PASSWORD = "Password is too weak. Please choose a stronger password."
        const val EMAIL_ALREADY_EXISTS = "Email already exists. Please use a different email."
        const val USER_NOT_FOUND = "User not found."
        const val INVALID_EMAIL = "Invalid email address."
        const val INVALID_CREDENTIALS = "Invalid credentials. Please check your email and password."
    }

    // Success Messages
    object SuccessMessages {
        const val PROFILE_UPDATED = "Profile updated successfully!"
        const val POST_CREATED = "Post created successfully!"
        const val POST_UPDATED = "Post updated successfully!"
        const val POST_DELETED = "Post deleted successfully!"
        const val COMMENT_ADDED = "Comment added successfully!"
        const val COMMENT_DELETED = "Comment deleted successfully!"
        const val PASSWORD_CHANGED = "Password changed successfully!"
        const val EMAIL_SENT = "Email sent successfully!"
        const val SETTINGS_SAVED = "Settings saved successfully!"
        const val IMAGE_UPLOADED = "Image uploaded successfully!"
        const val FOLLOW_SUCCESS = "You are now following this user!"
        const val UNFOLLOW_SUCCESS = "You have unfollowed this user!"
    }

    // Regex Patterns
    object RegexPatterns {
        const val EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        const val USERNAME = "^[a-zA-Z0-9._-]{3,30}$"
        const val PHONE_NUMBER = "^\\+?[1-9]\\d{1,14}$"
        const val URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
        const val HASHTAG = "#[a-zA-Z0-9_]+"
        const val MENTION = "@[a-zA-Z0-9._-]+"
        const val STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    }

    // Date Formats
    object DateFormats {
        const val SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val DISPLAY_DATE_FORMAT = "MMM dd, yyyy"
        const val DISPLAY_TIME_FORMAT = "HH:mm"
        const val DISPLAY_DATE_TIME_FORMAT = "MMM dd, yyyy 'at' HH:mm"
        const val FILE_DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }

    // Intent Actions
    object IntentActions {
        const val OPEN_POST = "com.example.photonest.OPEN_POST"
        const val OPEN_PROFILE = "com.example.photonest.OPEN_PROFILE"
        const val OPEN_CHAT = "com.example.photonest.OPEN_CHAT"
        const val SHARE_POST = "com.example.photonest.SHARE_POST"
        const val SHARE_PROFILE = "com.example.photonest.SHARE_PROFILE"
    }

    // Deep Link Paths
    object DeepLinks {
        const val BASE_URL = "https://photonest.com"
        const val POST_PATH = "/post"
        const val PROFILE_PATH = "/profile"
        const val CHAT_PATH = "/chat"
        const val STORY_PATH = "/story"
        const val EXPLORE_PATH = "/explore"
    }

    // API Endpoints (if using REST API)
    object ApiEndpoints {
        const val BASE_URL = "https://api.photonest.com/v1/"
        const val AUTH_LOGIN = "auth/login"
        const val AUTH_REGISTER = "auth/register"
        const val AUTH_REFRESH = "auth/refresh"
        const val AUTH_LOGOUT = "auth/logout"
        const val USER_PROFILE = "user/profile"
        const val USER_POSTS = "user/{userId}/posts"
        const val POSTS = "posts"
        const val POST_LIKE = "posts/{postId}/like"
        const val POST_COMMENTS = "posts/{postId}/comments"
        const val SEARCH = "search"
        const val TRENDING = "trending"
        const val NOTIFICATIONS = "notifications"
        const val UPLOAD_IMAGE = "upload/image"
    }
}
