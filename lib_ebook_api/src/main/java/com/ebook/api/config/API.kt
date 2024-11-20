package com.ebook.api.config


object API {
    //安卓模拟器访问localhost
    //    String URL_HOST_USER = "http://10.0.2.2:5000/userapi/";
    //    String URL_HOST_COMMENT = "http://10.0.2.2:5000/commentapi/";
    //本地局域网IP地址
    //    String URL_HOST_USER = "http://192.168.0.105:5000/userapi/";
    //    String URL_HOST_COMMENT = "http://192.168.0.105:5000/commentapi/";
    //腾讯云服务器(todo 已失效多年)
    const val URL_HOST_USER: String = "http://175.24.63.162:5000/userapi/"
    const val URL_HOST_COMMENT: String = "http://175.24.63.162:5000/commentapi/"
}
