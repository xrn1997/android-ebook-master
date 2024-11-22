package com.ebook.common.event


interface KeyCode {
    interface Main

    interface Login {
        companion object {
            //存储是否登录
            const val SP_IS_LOGIN: String = "sp_is_login"

            //存储用户名，密码，昵称，头像，id
            const val SP_USERNAME: String = "sp_username"
            const val SP_PASSWORD: String = "sp_password"
            const val SP_IMAGE: String = "sp_image"
            const val SP_NICKNAME: String = "sp_nickname"
            const val SP_USER_ID: String = "sp_user_id"

            const val PATH: String = "path"
            const val BASE_PATH: String = "/ebook/user/"

            //登录
            const val LOGIN_PATH: String = BASE_PATH + "login"

            //注册
            const val REGISTER_PATH: String = BASE_PATH + "register"

            //拦截登录测试
            const val TEST_PATH: String = BASE_PATH + "test"

            //修改密码
            const val MODIFY_PATH: String = BASE_PATH + "modify"
        }
    }

    interface Book {
        companion object {
            const val BASE_PATH: String = "/ebook/book/"
            const val COMMENT_PATH: String = BASE_PATH + "comment"
        }
    }

    interface Find

    interface Me {
        companion object {
            const val BASE_PATH: String = "/ebook/me/"
            const val SETTING_PATH: String = BASE_PATH + "setting"
            const val MODIFY_PATH: String = BASE_PATH + "modify"
            const val COMMENT_PATH: String = BASE_PATH + "comment"
        }
    }
}
